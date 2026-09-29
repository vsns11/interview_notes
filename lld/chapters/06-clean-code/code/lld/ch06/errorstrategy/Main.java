package lld.ch06.errorstrategy;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * An end-to-end error-handling STRATEGY for a service:
 *  1. one exception hierarchy with error codes (+ "retryable" flag)
 *  2. translate low-level errors at the infrastructure boundary
 *  3. retry only transient errors, with backoff and a limit
 *  4. use a Result type for EXPECTED failures (e.g. validating many rows)
 *  5. one global handler maps exceptions to responses and logs ONCE, hiding internals
 */
@SuppressWarnings("serial") // the nested exception classes are never serialized
public class Main {

    // ---------- 1. exception hierarchy ----------
    enum ErrorCode {
        VALIDATION_FAILED(400), NOT_FOUND(404), CONFLICT(409), DEPENDENCY_UNAVAILABLE(503);
        final int httpStatus;
        ErrorCode(int s) { httpStatus = s; }
    }
    static class AppException extends RuntimeException {
        final ErrorCode code;
        final boolean retryable;
        AppException(ErrorCode code, String message, boolean retryable, Throwable cause) {
            super(message, cause); this.code = code; this.retryable = retryable;
        }
    }
    static final class ValidationException extends AppException {
        ValidationException(String m) { super(ErrorCode.VALIDATION_FAILED, m, false, null); }
    }
    static final class NotFoundException extends AppException {
        NotFoundException(String m) { super(ErrorCode.NOT_FOUND, m, false, null); }
    }
    static final class TransientException extends AppException {
        TransientException(String m, Throwable cause) { super(ErrorCode.DEPENDENCY_UNAVAILABLE, m, true, cause); }
    }

    // ---------- 2. translate at the infrastructure boundary ----------
    static final class InventoryClient {
        private int callsUntilHealthy;
        InventoryClient(int failFirst) { this.callsUntilHealthy = failFirst; }
        int stockOf(String sku) {
            try {
                return rawHttpCall(sku);
            } catch (SocketTimeoutException e) {
                throw new TransientException("inventory service timed out", e);   // low-level -> domain meaning
            }
        }
        private int rawHttpCall(String sku) throws SocketTimeoutException {
            if (callsUntilHealthy-- > 0) throw new SocketTimeoutException("read timed out after 2000ms");
            if (sku.startsWith("X")) throw new NotFoundException("sku " + sku + " not found");
            return 7;
        }
    }

    // ---------- 3. retry transient errors only ----------
    static <T> T withRetry(Supplier<T> action, int maxAttempts, long initialBackoffMs) {
        long backoff = initialBackoffMs;
        for (int attempt = 1; ; attempt++) {
            try {
                return action.get();
            } catch (AppException e) {
                if (!e.retryable || attempt == maxAttempts) throw e;
                System.out.println("    attempt " + attempt + " failed (" + e.getMessage() + "), retrying in " + backoff + "ms");
                sleep(backoff);
                backoff *= 2;                                   // exponential backoff (add jitter in production)
            }
        }
    }
    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
    }

    // ---------- 4. Result type for expected, per-item failures ----------
    sealed interface Result<T> {
        record Ok<T>(T value) implements Result<T> {}
        record Err<T>(String error) implements Result<T> {}
    }
    record Row(int line, String sku, int qty) {}
    static Result<Row> parseRow(int line, String csv) {
        String[] f = csv.split(",");
        if (f.length != 2) return new Result.Err<>("line " + line + ": expected 2 fields");
        try {
            int qty = Integer.parseInt(f[1].trim());
            if (qty <= 0) return new Result.Err<>("line " + line + ": qty must be positive");
            return new Result.Ok<>(new Row(line, f[0].trim(), qty));
        } catch (NumberFormatException e) {
            return new Result.Err<>("line " + line + ": qty '" + f[1].trim() + "' is not a number");
        }
    }

    // ---------- 5. global handler at the edge ----------
    record Response(int status, String body) {}
    static <T> Response handle(String requestId, Supplier<T> endpoint, Function<T, String> render) {
        try {
            return new Response(200, render.apply(endpoint.get()));
        } catch (AppException e) {
            System.out.println("    [log once] " + requestId + " " + e.code + ": " + e.getMessage());
            return new Response(e.code.httpStatus, "{\"error\":\"" + e.code + "\",\"message\":\"" + e.getMessage() + "\"}");
        } catch (RuntimeException e) {
            System.out.println("    [log once] " + requestId + " UNEXPECTED " + e);   // full details only in logs
            return new Response(500, "{\"error\":\"INTERNAL\",\"requestId\":\"" + requestId + "\"}");
        }
    }

    public static void main(String[] args) {
        System.out.println("Retry a transient failure, then succeed:");
        var flaky = new InventoryClient(2);
        System.out.println("  " + handle("req-1", () -> withRetry(() -> flaky.stockOf("TEA"), 3, 10), n -> "{\"stock\":" + n + "}"));

        System.out.println("Do NOT retry a permanent failure:");
        var healthy = new InventoryClient(0);
        System.out.println("  " + handle("req-2", () -> withRetry(() -> healthy.stockOf("X-404"), 3, 10), n -> "{\"stock\":" + n + "}"));

        System.out.println("Give up after max attempts:");
        var down = new InventoryClient(99);
        System.out.println("  " + handle("req-3", () -> withRetry(() -> down.stockOf("TEA"), 3, 10), n -> "{\"stock\":" + n + "}"));

        System.out.println("Unexpected bug -> generic 500, no internals leaked:");
        System.out.println("  " + handle("req-4", () -> List.<String>of().get(0), s -> s));

        System.out.println("Bulk import: collect ALL row errors instead of stopping at the first:");
        List<String> csv = List.of("TEA,2", "CUP", "PEN,abc", "INK,-1", "MUG,1");
        List<Row> ok = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        for (int i = 0; i < csv.size(); i++) {
            switch (parseRow(i + 1, csv.get(i))) {
                case Result.Ok<Row> r -> ok.add(r.value());
                case Result.Err<Row> e -> errors.add(e.error());
            }
        }
        System.out.println("  imported " + ok);
        errors.forEach(e -> System.out.println("  rejected " + e));
        if (!errors.isEmpty())
            System.out.println("  " + handle("req-5", () -> { throw new ValidationException(errors.size() + " invalid rows"); }, x -> "" + x));
    }
}
