package lld.ch04.coupling.decoupled;

/** What Student needs, described as a contract rather than a working class. */
public interface StudentRepository {
    void save(String studentId);
}
