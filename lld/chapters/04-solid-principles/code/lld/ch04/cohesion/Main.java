package lld.ch04.cohesion;

import lld.ch04.cohesion.after.AuditFile;
import lld.ch04.cohesion.after.UserManager;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Before: one class, two unrelated jobs ---");
        var manager = new lld.ch04.cohesion.before.UserManager();
        manager.addUser("ada");
        manager.writeToFile("created ada");
        manager.deleteUser("ada");
        manager.readFromFile();

        System.out.println("--- After: two classes, one job each ---");
        var users = new UserManager();
        var audit = new AuditFile();
        users.addUser("ada");
        audit.writeLine("created ada");
        users.deleteUser("ada");
        audit.readAll();
    }
}
