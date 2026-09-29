package ch08.stacktrace;

public class StackTraces {

    static class ServiceException extends RuntimeException {
        ServiceException(String msg, Throwable cause) { super(msg, cause); }
    }

    static int parsePort(String text) {
        return Integer.parseInt(text);
    }

    static int loadPort(String text) {
        try {
            return parsePort(text);
        } catch (NumberFormatException e) {
            throw new ServiceException("invalid port setting", e);
        }
    }

    public static void main(String[] args) {
        try {
            loadPort("80a");
        } catch (ServiceException e) {
            e.printStackTrace(System.out);        // normally goes to System.err or, better, a logger
            StackTraceElement top = e.getStackTrace()[0];
            System.out.println("top frame: " + top.getClassName() + "." + top.getMethodName() + " line " + top.getLineNumber());
        }

        // StackOverflowError: infinite recursion exhausts the thread's stack
        try {
            recurse(0);
        } catch (StackOverflowError e) {
            System.out.println("caught StackOverflowError (an Error: normally you do NOT catch these)");
        }
    }

    static int recurse(int n) { return recurse(n + 1) + 1; }
}
