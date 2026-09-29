package ch05.access.client;

import ch05.access.bank.Teller;
import ch05.access.bank.Vault;

public class AccessDemo {

    static class BranchVault extends Vault {  // a subclass in ANOTHER package
        int floorsPlusOne() { return floors + 1; }   // protected: visible through inheritance
    }

    public static void main(String[] args) {
        Vault v = new Vault();
        System.out.println("public name:        " + v.name);
        // v.floors  -> compile error: protected, and we are not accessing it through a subclass
        // v.guards  -> compile error: package-private, different package
        // v.code    -> compile error: private
        System.out.println("open(\"1234\"):       " + v.open("1234"));
        System.out.println("Teller (same pkg):  " + Teller.inspect(v));
        System.out.println("subclass protected: " + new BranchVault().floorsPlusOne());
    }
}
