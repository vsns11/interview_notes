package lld.ch04.coupling.extracted;

public class MySqlDatabase {
    public void saveStudent(String studentId) {
        System.out.println("Connecting to MySQL...");
        System.out.println("Saving student with ID: " + studentId);
    }
}
