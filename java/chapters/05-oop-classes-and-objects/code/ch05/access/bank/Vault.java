package ch05.access.bank;

public class Vault {
    public    String name = "Main vault";   // everyone
    protected int    floors = 3;            // same package + subclasses (anywhere)
              int    guards = 4;            // package-private (no keyword): same package only
    private   String code = "0000";         // this class only

    public boolean open(String attempt) {   // the public method may use the private field
        return code.equals(attempt);
    }
}
