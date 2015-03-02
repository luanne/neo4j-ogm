/*
 * Copyright (c) 2014-2015 "GraphAware"
 *
 * GraphAware Ltd
 *
 * This file is part of Neo4j-OGM.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package org.neo4j.ogm.session;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.neo4j.ogm.cypher.compiler.CypherContext;
import org.neo4j.ogm.cypher.query.GraphModelQuery;
import org.neo4j.ogm.cypher.query.RowModelQuery;
import org.neo4j.ogm.cypher.statement.ParameterisedStatement;
import org.neo4j.ogm.entityaccess.FieldWriter;
import org.neo4j.ogm.mapper.EntityGraphMapper;
import org.neo4j.ogm.mapper.MappingContext;
import org.neo4j.ogm.metadata.MetaData;
import org.neo4j.ogm.metadata.info.ClassInfo;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.model.Property;
import org.neo4j.ogm.session.request.DefaultRequest;
import org.neo4j.ogm.session.request.Neo4jRequest;
import org.neo4j.ogm.session.request.RequestHandler;
import org.neo4j.ogm.session.request.SessionRequestHandler;
import org.neo4j.ogm.session.request.strategy.AggregateStatements;
import org.neo4j.ogm.session.request.strategy.DeleteStatements;
import org.neo4j.ogm.session.request.strategy.VariableDepthQuery;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.session.response.ResponseHandler;
import org.neo4j.ogm.session.response.SessionResponseHandler;
import org.neo4j.ogm.session.result.RowModel;
import org.neo4j.ogm.session.transaction.SimpleTransaction;
import org.neo4j.ogm.session.transaction.Transaction;
import org.neo4j.ogm.session.transaction.TransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Neo4jSession implements Session {

    private final Logger logger = LoggerFactory.getLogger(Neo4jSession.class);

    private final MetaData metaData;
    private final MappingContext mappingContext;
    private final ObjectMapper mapper;
    private final String autoCommitUrl;
    private final TransactionManager txManager;

    private Neo4jRequest<String> request;

    private static final Pattern WRITE_CYPHER_KEYWORDS = Pattern.compile("\\b(CREATE|MERGE|SET|DELETE|REMOVE)\\b");

    public Neo4jSession(MetaData metaData, String url, CloseableHttpClient client, ObjectMapper mapper) {
        this.metaData = metaData;
        this.mapper = mapper;
        this.mappingContext = new MappingContext(metaData);
        this.txManager = new TransactionManager(client, url);
        this.autoCommitUrl = autoCommit(url);
        this.request = new DefaultRequest(client);
    }

    public void setRequest(Neo4jRequest<String> neo4jRequest) {
        this.request=neo4jRequest;
    }

    private RequestHandler getRequestHandler() {
        return new SessionRequestHandler(mapper, request);
    }

    private ResponseHandler getResponseHandler() {
        return new SessionResponseHandler(metaData, mappingContext);
    }

    @Override
    public <T> T load(Class<T> type, Long id) {
        return load(type, id, 1);
    }

    @Override
    public <T> T load(Class<T> type, Long id, int depth) {
        String url = getOrCreateTransaction().url();
        GraphModelQuery qry = new VariableDepthQuery().findOne(id, depth);
        try (Neo4jResponse<GraphModel> response = getRequestHandler().execute(qry, url)) {
            return getResponseHandler().loadById(type, response, id);
        }
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids) {
        return loadAll(type, ids, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, Collection<Long> ids, int depth) {
        String url = getOrCreateTransaction().url();
        GraphModelQuery qry = new VariableDepthQuery().findAll(ids, depth);
        try (Neo4jResponse<GraphModel> response = getRequestHandler().execute(qry, url)) {
            return getResponseHandler().loadAll(type, response);
        }
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type) {
        return loadAll(type, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Class<T> type, int depth) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        String url = getOrCreateTransaction().url();
        GraphModelQuery qry = new VariableDepthQuery().findByLabel(classInfo.label(), depth);
        try (Neo4jResponse<GraphModel> response = getRequestHandler().execute(qry, url)) {
            return getResponseHandler().loadAll(type, response);
        }
    }

    @Override
    public <T> Collection<T> loadAll(Collection<T> objects) {
        return loadAll(objects, 1);
    }

    @Override
    public <T> Collection<T> loadAll(Collection<T> objects, int depth) {

        if (objects == null || objects.isEmpty()) {
            return objects;
        }

        Set<Long> ids = new HashSet<>();
        Class type = objects.iterator().next().getClass();
        ClassInfo classInfo = metaData.classInfo(type.getName());
        Field identityField = classInfo.getField(classInfo.identityField());
        for (Object o: objects) {
            ids.add((Long) FieldWriter.read(identityField, o));
        }
        return loadAll(type, ids, depth);
    }

    @Override
    public <T> Collection<T> loadByProperty(Class<T> type, Property<String, Object> property) {
        return loadByProperty(type, property, 1);
    }

    @Override
    public <T> Collection<T> loadByProperty(Class<T> type, Property<String, Object> property, int depth) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        String url = getOrCreateTransaction().url();
        GraphModelQuery qry = new VariableDepthQuery().findByProperty(classInfo.label(), property, depth);
        try (Neo4jResponse<GraphModel> response = getRequestHandler().execute(qry, url)) {
            return getResponseHandler().loadByProperty(type, response, property);
        }
    }

    @Override
    public Transaction beginTransaction() {

        logger.info("beginTransaction() being called on thread: " + Thread.currentThread().getId());
        logger.info("Neo4jSession identity: " + this);

        Transaction tx = txManager.openTransaction(mappingContext);

        logger.info("Obtained new transaction: " + tx.url() + ", tx id: " + tx);
        return tx;
    }


    @Override
    public <T> T queryForObject(Class<T> type, String cypher, Map<String, ?> parameters) {
        Iterable<T> results = query(type, cypher, parameters);

        int resultSize = Utils.size(results);

        if (resultSize < 1 ) {
            return null;
        }

        if (resultSize > 1) {
            throw new RuntimeException("Result not of expected size. Expected 1 row but found " + resultSize);
        }

        return results.iterator().next();
    }

    @Override
    public Iterable<Map<String, Object>> query(String cypher, Map<String, ?> parameters) {
        if (StringUtils.isEmpty(cypher)) {
            throw new RuntimeException("Supplied cypher statement must not be null or empty.");
        }

        if (parameters == null) {
            throw new RuntimeException("Supplied Parameters cannot be null.");
        }

        assertReadOnly(cypher);

        String url = getOrCreateTransaction().url();
        RowModelQuery qry = new RowModelQuery(cypher, parameters);
        try (Neo4jResponse<RowModel> response = getRequestHandler().execute(qry, url)) {

            String[] variables = response.columns();

            List<Map<String, Object>> result = new ArrayList<>();
            RowModel rowModel;

            while ((rowModel = response.next()) != null) {
                Object[] results = rowModel.getValues();
                Map<String, Object> element = new HashMap<>();
                for (int i = 0; i < variables.length; i++) {
                    element.put(variables[i], results[i]);
                }
                result.add(element);
            }

            return result;
        }
    }

    @Override
    public <T> Iterable<T> query(Class<T> type, String cypher, Map<String, ?> parameters) {
        if (type == null || type.equals(Void.class)) {
            throw new RuntimeException("Supplied type must not be nul or void.");
        }

        if (StringUtils.isEmpty(cypher)) {
            throw new RuntimeException("Supplied cypher statement must not be null or empty.");
        }

        if (parameters == null) {
            throw new RuntimeException("Supplied Parameters cannot be null.");
        }

        assertReadOnly(cypher);

        String url = getOrCreateTransaction().url();

        if (metaData.classInfo(type.getSimpleName()) != null) {
            GraphModelQuery qry = new GraphModelQuery(cypher, parameters);
            try (Neo4jResponse<GraphModel> response = getRequestHandler().execute(qry, url)) {
                return getResponseHandler().loadAll(type, response);
            }
        }
        else {
            RowModelQuery qry = new RowModelQuery(cypher, parameters);
            try (Neo4jResponse<RowModel> response = getRequestHandler().execute(qry, url)) {

                String[] variables = response.columns();

                if (variables.length > 1) {
                    throw new RuntimeException("Scalar response queries must only return one column. Make sure your cypher query only returns one item.");
                }

                Collection<T> result = new ArrayList<>();
                RowModel rowModel;
                while ((rowModel = response.next()) != null) {
                    Object[] results = rowModel.getValues();
                    for (int i = 0; i < variables.length; i++) {
                        result.add((T)results[i]);
                    }
                }

                return result;
            }
        }
    }

    private void assertReadOnly(String cypher) {
        Matcher matcher = WRITE_CYPHER_KEYWORDS.matcher(cypher.toUpperCase());

        if (matcher.find()) {
            throw new RuntimeException("query() only allows read only cypher. To make modifications use execute()");
        }
    }

    @Override
    public void execute(String cypher, Map<String, Object> parameters) {
        if (StringUtils.isEmpty(cypher)) {
            throw new RuntimeException("Supplied cypher statement must not be null or empty.");
        }

        if (parameters == null) {
            throw new RuntimeException("Supplied Parameters cannot be null.");
        }

        String url  = getOrCreateTransaction().url();
        // NOTE: No need to check if domain objects are parameters and flatten them to json as this is done
        // for us using the existing execute() method.
        RowModelQuery qry = new RowModelQuery(cypher, parameters);
        getRequestHandler().execute(qry, url).close();
    }


    @Override
    public void execute(String statement) {
        ParameterisedStatement parameterisedStatement = new ParameterisedStatement(statement, Utils.map());
        String url = getOrCreateTransaction().url();
        getRequestHandler().execute(parameterisedStatement, url).close();
    }

    @Override
    public void purgeDatabase() {
        String url = getOrCreateTransaction().url();
        getRequestHandler().execute(new DeleteStatements().purge(), url).close();
        mappingContext.clear();
    }

    @Override
    public void clear() {
        mappingContext.clear();
    }

    @Override
    public <T> void save(T object) {
        if (object.getClass().isArray() || Iterable.class.isAssignableFrom(object.getClass())) {
            saveAll(object, -1);
        } else {
            save(object, -1); // default : full tree of changed objects
        }
    }

    private <T> void saveAll(T object, int depth) {
        List<T> list;
        if (object.getClass().isArray()) {
            list = Arrays.asList(object);
        } else {
            list = (List<T>) object;
        }
        for (T element : list) {
            save(element, depth);
        }
    }

    private <T> void deleteAll(T object) {
        List<T> list;
        if (object.getClass().isArray()) {
            list = Arrays.asList(object);
        } else {
            list = (List<T>) object;
        }
        for (T element : list) {
            delete(element);
        }
    }

    @Override
    public <T> void save(T object, int depth) {
        if (object.getClass().isArray() || Iterable.class.isAssignableFrom(object.getClass())) {
            saveAll(object, depth);
        } else {
            ClassInfo classInfo = metaData.classInfo(object);
            if (classInfo != null) {
                Transaction tx = getOrCreateTransaction();
                CypherContext context = new EntityGraphMapper(metaData, mappingContext).map(object, depth);
                try (Neo4jResponse<String> response = getRequestHandler().execute(context.getStatements(), tx.url())) {
                    getResponseHandler().updateObjects(context, response, mapper);
                    Field identityField = classInfo.getField(classInfo.identityField());
                    Long id = (Long) FieldWriter.read(identityField, object);
                    // ensure the mapping context has the latest version of this object
                    //mappingContext.replace(object, id);
                    tx.append(context);
                }
            } else {
                logger.info(object.getClass().getName() + " is not an instance of a persistable class");
            }
        }
    }

    @Override
    public <T> void delete(T object) {
        if (object.getClass().isArray() || Iterable.class.isAssignableFrom(object.getClass())) {
            deleteAll(object);
        } else {
            ClassInfo classInfo = metaData.classInfo(object);
            if (classInfo != null) {
                Field identityField = classInfo.getField(classInfo.identityField());
                Long identity = (Long) FieldWriter.read(identityField, object);
                if (identity != null) {
                    String url = getOrCreateTransaction().url();
                    ParameterisedStatement request = new DeleteStatements().delete(identity);
                    try (Neo4jResponse<String> response = getRequestHandler().execute(request, url)) {
                        mappingContext.clear(object);
                    }
                }
            } else {
                logger.info(object.getClass().getName() + " is not an instance of a persistable class");
            }
        }
    }

    @Override
    public <T> void deleteAll(Class<T> type) {
        ClassInfo classInfo = metaData.classInfo(type.getName());
        if (classInfo != null) {
            String url = getOrCreateTransaction().url();
            ParameterisedStatement request = new DeleteStatements().deleteByLabel(classInfo.label());
            try (Neo4jResponse<String> response = getRequestHandler().execute(request, url)) {
                mappingContext.clear(type);
            }
        } else {
            logger.info(type.getName() + " is not a persistable class");
        }
    }

    @Override
    public long countEntitiesOfType(Class<?> entity) {
        ClassInfo classInfo = metaData.classInfo(entity.getName());
        if (classInfo == null) {
            return 0;
        }

        RowModelQuery countStatement = new AggregateStatements().countNodesLabelledWith(classInfo.labels());
        String url  = getOrCreateTransaction().url();
        try (Neo4jResponse<RowModel> response = getRequestHandler().execute(countStatement, url)) {
            RowModel queryResult = response.next();
            return queryResult == null ? 0 : ((Number) queryResult.getValues()[0]).longValue();
        }
    }

    private static String autoCommit(String url) {
        if (url == null) return url;
        if (!url.endsWith("/")) url = url + "/";
        return url + "db/data/transaction/commit";
    }

    private Transaction getOrCreateTransaction() {

        logger.info("--------- new request ----------");
        logger.info("getOrCreateTransaction() being called on thread: " + Thread.currentThread().getId());
        logger.info("Session identity: " + this);

        Transaction tx = txManager.getCurrentTransaction();
        if (tx == null
                || tx.status().equals(Transaction.Status.CLOSED)
                || tx.status().equals(Transaction.Status.COMMITTED)
                || tx.status().equals(Transaction.Status.ROLLEDBACK)) {
            logger.info("There is no existing transaction, creating a transient one");
            return new SimpleTransaction(mappingContext, autoCommitUrl);
        }

        logger.info("Current transaction: " + tx.url() + ", tx id: " + tx);
        return tx;

    }

}
