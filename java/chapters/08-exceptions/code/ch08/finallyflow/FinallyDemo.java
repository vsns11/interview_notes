package ch08.finallyflow;

public class FinallyDemo {

    static String flow(boolean fail) {
        StringBuilder log = new StringBuilder();
        try {
            log.append("try ");
            if (fail) throw new IllegalStateException("boom");
            log.append("try-end ");
        } catch (IllegalStateException e) {
            log.append("catch ");
        } finally {
            log.append("finally");
        }
        return log.toString();
    }

    static int returnInTry() {
        int x = 1;
        try {
            return x;             // the value 1 is saved here...
        } finally {
            x = 99;               // ...so changing x in finally does not change the returned value
            System.out.println("finally ran before the method returned");
        }
    }

    @SuppressWarnings("finally")
    static int finallyOverrides() {
        try {
            throw new RuntimeException("lost!");
        } finally {
            return -1;            // BAD: discards the exception and any return value from try
        }
    }

    public static void main(String[] args) {
        System.out.println("no failure: " + flow(false));
        System.out.println("failure:    " + flow(true));
        System.out.println("returnInTry() = " + returnInTry());
        System.out.println("finallyOverrides() = " + finallyOverrides() + " (the RuntimeException vanished)");

        try {
            try {
                throw new IllegalArgumentException("inner");
            } finally {
                System.out.println("inner finally runs even though nobody here catches");
            }
        } catch (IllegalArgumentException e) {
            System.out.println("outer caught: " + e.getMessage());
        }
    }
}
