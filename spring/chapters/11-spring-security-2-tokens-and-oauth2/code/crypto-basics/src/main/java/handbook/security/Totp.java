package handbook.security;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * One-time passwords as authenticator apps compute them.
 * HOTP (RFC 4226): HMAC(secret, counter) -> dynamic truncation -> N decimal digits.
 * TOTP (RFC 6238): HOTP with counter = floor((unixTime - T0) / step), step = 30 s by default.
 */
public final class Totp {

    private final byte[] secret;
    private final String hmacAlgorithm;   // "HmacSHA1" (what almost every app uses), "HmacSHA256", "HmacSHA512"
    private final int digits;             // 6 in apps, 8 in the RFC test vectors
    private final long stepSeconds;

    public Totp(byte[] secret, String hmacAlgorithm, int digits, long stepSeconds) {
        this.secret = secret.clone();
        this.hmacAlgorithm = hmacAlgorithm;
        this.digits = digits;
        this.stepSeconds = stepSeconds;
    }

    /** RFC 4226 section 5.3. */
    public String hotp(long counter) {
        try {
            Mac mac = Mac.getInstance(hmacAlgorithm);
            mac.init(new SecretKeySpec(secret, hmacAlgorithm));
            byte[] h = mac.doFinal(ByteBuffer.allocate(8).putLong(counter).array());   // counter as 8-byte big-endian
            int offset = h[h.length - 1] & 0x0f;                                      // dynamic truncation
            int binary = ((h[offset] & 0x7f) << 24) | ((h[offset + 1] & 0xff) << 16)
                       | ((h[offset + 2] & 0xff) << 8) | (h[offset + 3] & 0xff);
            int otp = binary % (int) Math.pow(10, digits);
            return String.format("%0" + digits + "d", otp);                          // keep leading zeros
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public long counterAt(long unixSeconds) { return Math.floorDiv(unixSeconds, stepSeconds); }

    public String at(long unixSeconds) { return hotp(counterAt(unixSeconds)); }

    public String now() { return at(Instant.now().getEpochSecond()); }

    /** Server-side check: accept the current step and +-window steps to absorb clock drift. */
    public boolean verify(String code, long unixSeconds, int window) {
        long c = counterAt(unixSeconds);
        for (long i = -window; i <= window; i++) {
            if (constantTimeEquals(hotp(c + i), code)) return true;
        }
        return false;
    }

    private static boolean constantTimeEquals(String a, String b) {
        return java.security.MessageDigest.isEqual(a.getBytes(), b.getBytes());
    }
}
