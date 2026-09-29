package lld.ch03.association;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/** One side of a bi-directional many-to-many association (Student * -- * Course). */
public final class Student {
    private final String name;
    private final Set<Course> courses = new LinkedHashSet<>();

    public Student(String name) { this.name = name; }

    public String name() { return name; }
    public Set<Course> courses() { return Collections.unmodifiableSet(courses); }

    /** Package-private: only Course.enroll/drop may touch this side, so both ends stay in sync. */
    void linkCourse(Course c)   { courses.add(c); }
    void unlinkCourse(Course c) { courses.remove(c); }

    @Override public String toString() { return name; }
}
