package handbook.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

/** RFC 6238 Appendix B test vectors (8 digits, 30-second step) for all three HMAC variants. */
class TotpTest {

    static final byte[] SEED20 = "12345678901234567890".getBytes(StandardCharsets.US_ASCII);
    static final byte[] SEED32 = "12345678901234567890123456789012".getBytes(StandardCharsets.US_ASCII);
    static final byte[] SEED64 = "1234567890123456789012345678901234567890123456789012345678901234".getBytes(StandardCharsets.US_ASCII);

    @ParameterizedTest(name = "t={0}")
    @CsvSource({
        "59,          94287082, 46119246, 90693936",
        "1111111109,  07081804, 68084774, 25091201",
        "1111111111,  14050471, 67062674, 99943326",
        "1234567890,  89005924, 91819424, 93441116",
        "2000000000,  69279037, 90698825, 38618901",
        "20000000000, 65353130, 77737706, 47863826"
    })
    void rfc6238Vectors(long t, String sha1, String sha256, String sha512) {
        assertThat(new Totp(SEED20, "HmacSHA1", 8, 30).at(t)).isEqualTo(sha1);
        assertThat(new Totp(SEED32, "HmacSHA256", 8, 30).at(t)).isEqualTo(sha256);
        assertThat(new Totp(SEED64, "HmacSHA512", 8, 30).at(t)).isEqualTo(sha512);
    }

    @Test
    void rfc4226HotpVectors() {   // RFC 4226 Appendix D: 6 digits, counters 0..9
        Totp h = new Totp(SEED20, "HmacSHA1", 6, 30);
        String[] expected = {"755224", "287082", "359152", "969429", "338314", "254676", "287922", "162583", "399871", "520489"};
        for (int c = 0; c < expected.length; c++) assertThat(h.hotp(c)).isEqualTo(expected[c]);
    }

    @Test
    void verifyAcceptsOneStepOfDriftOnly() {
        Totp app = new Totp(SEED20, "HmacSHA1", 6, 30);
        long t = 1_700_000_000L;
        String code = app.at(t);
        assertThat(app.verify(code, t + 30, 1)).isTrue();    // next step: still accepted
        assertThat(app.verify(code, t + 90, 1)).isFalse();   // three steps later: rejected
    }
}
