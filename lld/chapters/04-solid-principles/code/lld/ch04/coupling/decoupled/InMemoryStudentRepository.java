package lld.ch04.coupling.decoupled;

import java.util.ArrayList;
import java.util.List;

/** A stand-in that a unit test can use -- no database server needed. */
public class InMemoryStudentRepository implements StudentRepository {
    private final List<String> saved = new ArrayList<>();

    @Override
    public void save(String studentId) {
        saved.add(studentId);
        System.out.println("Keeping student " + studentId + " in memory for a test");
    }

    public List<String> saved() {
        return List.copyOf(saved);
    }
}
