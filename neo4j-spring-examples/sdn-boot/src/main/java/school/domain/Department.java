package school.domain;

import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

public class Department extends Entity {

    private String name;

    @Relationship(type = "DEPARTMENT_MEMBER")
    private Set<Teacher> teachers;

//    @Relationship(type = "DEPARTMENT_HEAD")
//    private Teacher departmentHead;

    @Relationship(type = "CURRICULUM")
    private Set<Subject> subjects;

    public Department() {
        this.teachers = new HashSet<>();
        this.subjects = new HashSet<>();
    }

    public Department(String name) {
        this();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Relationship(type = "DEPARTMENT_MEMBER")
    public Set<Teacher> getTeachers() {
        return teachers;
    }

    @Relationship(type = "DEPARTMENT_MEMBER")
    public void setTeachers(Set<Teacher> teachers) {
        this.teachers = teachers;
    }

    @Relationship(type = "CURRICULUM")
    public Set<Subject> getSubjects() {
        return subjects;
    }

    @Relationship(type = "CURRICULUM")
    public void setSubjects(Set<Subject> subjects) {
        this.subjects = subjects;
    }

    @Override
    public String toString() {
        return "Department{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", teachers=" + teachers.size() +
                ", subjects=" + subjects.size() +
                '}';
    }
}
