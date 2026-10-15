package ch22.tokenbucket;

/** A rate limiter that hands out tokens; one token = permission for one request. */
public interface TokenBucket {
    /** Blocks until a token is available, then takes it. */
    void getToken() throws InterruptedException;

    /** Takes a token if one is available right now; never blocks. */
    boolean tryGetToken();
}
