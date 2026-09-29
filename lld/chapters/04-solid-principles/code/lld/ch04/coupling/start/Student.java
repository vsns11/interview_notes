package lld.ch04.coupling.start;

/** Starting point: ONE class, two jobs. A cohesion problem -- nothing to be coupled to yet. */
public class Student {
    private final String studentId;

    public Student(String studentId) {
        this.studentId = studentId;
    }

    public String studentId() {
        return studentId;
    }

    // Student holds the data AND knows how to store it
    public void save() {
        System.out.println("Connecting to MySQL...");
        System.out.println("Saving student with ID: " + studentId);
    }
}
