package lld.ch10.chain;

import java.util.HashMap;
import java.util.Map;

/** Stage 2: at most N requests per user (keyed by user because auth ran first). */
final class RateLimitHandler extends Handler {
    private final int limitPerUser;
    private final Map<String, Integer> seen = new HashMap<>();

    RateLimitHandler(int limitPerUser) { this.limitPerUser = limitPerUser; }

    @Override
    public boolean handle(Request r) {
        int count = seen.merge(r.user(), 1, Integer::sum);
        if (count > limitPerUser) {
            System.out.println("  [ratelimit] rejected: " + r.user() + " made " + count + " requests, limit " + limitPerUser);
            return false;
        }
        System.out.println("  [ratelimit] ok (" + count + "/" + limitPerUser + ")");
        return passOn(r);
    }
}
