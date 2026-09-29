package lld.ch09.decorator;

/** CONCRETE DECORATOR 1 */
final class LoggingRepository extends RepositoryDecorator {
    LoggingRepository(UserRepository inner) { super(inner); }

    @Override
    public String findName(int id) {
        System.out.println("    [log]   findName(" + id + ")");
        return super.findName(id);
    }
}
