package handbook.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;

class CryptoBasicsTest {

    final byte[] msg = "pay bob 100".getBytes(StandardCharsets.UTF_8);

    @Test
    void encodingIsReversibleWithoutAKey() {
        assertThat(Base64.getDecoder().decode(Base64.getEncoder().encode(msg))).isEqualTo(msg);
    }

    @Test
    void hashIsDeterministicAndFixedSize() throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] a = sha.digest(msg), b = sha.digest(msg), c = sha.digest("pay bob 900".getBytes(StandardCharsets.UTF_8));
        assertThat(a).hasSize(32).isEqualTo(b);
        assertThat(Arrays.equals(a, c)).isFalse();
    }

    @Test
    void aesGcmRoundTripsAndDetectsTampering() throws Exception {
        SecretKey k = KeyGenerator.getInstance("AES").generateKey();
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        byte[] ct = CryptoBasicsDemo.aesGcm(Cipher.ENCRYPT_MODE, k, iv, msg);
        assertThat(CryptoBasicsDemo.aesGcm(Cipher.DECRYPT_MODE, k, iv, ct)).isEqualTo(msg);
        ct[3] ^= 1;
        assertThatThrownBy(() -> CryptoBasicsDemo.aesGcm(Cipher.DECRYPT_MODE, k, iv, ct)).isInstanceOf(AEADBadTagException.class);
    }

    @Test
    void signatureVerifiesOnlyTheSignedBytesWithTheMatchingKey() throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance("Ed25519");
        KeyPair alice = g.generateKeyPair(), mallory = g.generateKeyPair();
        Signature s = Signature.getInstance("Ed25519");
        s.initSign(alice.getPrivate());
        s.update(msg);
        byte[] sig = s.sign();
        assertThat(CryptoBasicsDemo.verify(alice.getPublic(), msg, sig)).isTrue();
        assertThat(CryptoBasicsDemo.verify(alice.getPublic(), "pay bob 900".getBytes(StandardCharsets.UTF_8), sig)).isFalse();
        assertThat(CryptoBasicsDemo.verify(mallory.getPublic(), msg, sig)).isFalse();
    }

    @Test
    void x25519BothSidesDeriveTheSameSecret() throws Exception {
        KeyPairGenerator x = KeyPairGenerator.getInstance("XDH");
        x.initialize(new NamedParameterSpec("X25519"));
        KeyPair a = x.generateKeyPair(), b = x.generateKeyPair();
        assertThat(CryptoBasicsDemo.agree(a.getPrivate(), b.getPublic()))
                .hasSize(32).isEqualTo(CryptoBasicsDemo.agree(b.getPrivate(), a.getPublic()));
    }
}
