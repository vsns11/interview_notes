package lld.ch04.coupling;

import lld.ch04.coupling.decoupled.InMemoryStudentRepository;
import lld.ch04.coupling.decoupled.MySqlStudentRepository;
import lld.ch04.coupling.decoupled.Student;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Start: one class holds data and SQL ---");
        new lld.ch04.coupling.start.Student("S-1024").save();

        System.out.println("--- Step 1: extracted, but tightly coupled to MySqlDatabase ---");
        new lld.ch04.coupling.extracted.Student("S-1024").save();

        System.out.println("--- Step 2: depends on the StudentRepository interface ---");
        new Student("S-1024", new MySqlStudentRepository()).save();
        var fake = new InMemoryStudentRepository();
        new Student("S-2048", fake).save();
        System.out.println("test double recorded: " + fake.saved());
    }
}
