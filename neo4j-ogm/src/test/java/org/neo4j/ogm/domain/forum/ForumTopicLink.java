package org.neo4j.ogm.domain.forum;

import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

/**
 * Completely pointless class that does nothing but exercise {@link RelationshipEntity}.
 */
@RelationshipEntity(type = "HAS_TOPIC")
public class ForumTopicLink {

    @StartNode
    private Forum forum;
    @EndNode
    private Topic topic;

    public Forum getForum() {
        return forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

}