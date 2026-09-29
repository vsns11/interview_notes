package ch13.serial;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/** Java serialization: how it works, what transient does, and how to put a filter in front of it. */
public class SerializationDemo {

    static class Account implements Serializable {
        @Serial private static final long serialVersionUID = 1L;   // version stamp you control
        private final String owner;
        private final long balanceCents;
        private transient String sessionToken;                      // skipped by serialization

        Account(String owner, long balanceCents, String token) {
            this.owner = owner; this.balanceCents = balanceCents; this.sessionToken = token;
            System.out.println("  constructor ran for " + owner);
        }
        @Override public String toString() {
            return "Account[" + owner + ", " + balanceCents + ", token=" + sessionToken + "]";
        }
    }

    record Point(int x, int y) implements Serializable {
        Point {                                                     // records deserialize THROUGH this
            if (x < 0 || y < 0) throw new IllegalArgumentException("negative");
            System.out.println("  canonical constructor ran for (" + x + "," + y + ")");
        }
    }

    static byte[] serialize(Object o) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bos)) { out.writeObject(o); }
        return bos.toByteArray();
    }

    static Object deserialize(byte[] bytes, ObjectInputFilter filter) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            if (filter != null) in.setObjectInputFilter(filter);
            return in.readObject();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("create:");
        Account acc = new Account("ana", 12_50, "secret-123");
        byte[] bytes = serialize(acc);
        System.out.println("serialized to " + bytes.length + " bytes");
        System.out.println("deserialize (note: NO constructor call for a normal class):");
        Account copy = (Account) deserialize(bytes, null);
        System.out.println("  " + copy + "  same object? " + (copy == acc));

        System.out.println("record - create:");
        byte[] pb = serialize(new Point(1, 2));
        System.out.println("record - deserialize (the canonical constructor runs again):");
        Point p = (Point) deserialize(pb, null);
        System.out.println("  " + p);

        // Allow-list filter: only our two classes (and nothing else) may be created
        ObjectInputFilter allow = ObjectInputFilter.Config.createFilter(
                "ch13.serial.SerializationDemo$Account;ch13.serial.SerializationDemo$Point;!*");
        System.out.println("filtered, allowed class: " + deserialize(pb, allow));
        try {
            deserialize(serialize(new ArrayList<>(List.of(1, 2))), allow);
        } catch (InvalidClassException e) {
            System.out.println("filtered, ArrayList -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}
