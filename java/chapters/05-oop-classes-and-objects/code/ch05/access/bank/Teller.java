package ch05.access.bank;

public class Teller {                        // same package as Vault
    public static String inspect(Vault v) {
        // v.code would NOT compile here: private is class-only
        return v.name + ", floors=" + v.floors + ", guards=" + v.guards;
    }
}
