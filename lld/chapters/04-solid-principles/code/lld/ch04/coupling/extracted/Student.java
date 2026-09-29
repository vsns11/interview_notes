package lld.ch04.coupling.extracted;

/** Step 1: cohesion fixed, but Student now names one concrete class -> TIGHT coupling. */
public class Student {
    private final String studentId;
    private final MySqlDatabase database = new MySqlDatabase(); // hard-wired

    public Student(String studentId) {
        this.studentId = studentId;
    }

    public void save() {
        database.saveStudent(studentId);
    }
}
