package handbook.security;

import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encoding vs hashing vs encryption vs signatures, a TLS-style hybrid key exchange, and TOTP codes,
 * all with the JDK alone. Run: mvn -q verify && java -cp target/classes handbook.security.CryptoBasicsDemo
 */
public class CryptoBasicsDemo {

    static final HexFormat HEX = HexFormat.of();

    public static void main(String[] args) throws Exception {
        byte[] msg = "pay bob 100".getBytes(StandardCharsets.UTF_8);

        // 1. Encoding: a reversible change of representation. No key, no secrecy.
        String b64 = Base64.getEncoder().encodeToString(msg);
        System.out.println("encode  Base64      : " + b64 + "  -> decodes back to: "
                + new String(Base64.getDecoder().decode(b64), StandardCharsets.UTF_8));

        // 2. Hashing: one-way, fixed size, same input -> same output; one changed byte changes everything.
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        String h1 = HEX.formatHex(sha.digest(msg));
        String h2 = HEX.formatHex(sha.digest("pay bob 900".getBytes(StandardCharsets.UTF_8)));
        System.out.println("hash    SHA-256     : " + h1.substring(0, 16) + "...  ('pay bob 900' -> " + h2.substring(0, 16) + "...)");

        // 3. Symmetric encryption: one shared key encrypts and decrypts. AES-GCM also detects tampering.
        SecretKey aesKey = KeyGenerator.getInstance("AES").generateKey();               // 256-bit by default on JDK 25
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);                                                  // never reuse an IV with the same key
        byte[] ct = aesGcm(Cipher.ENCRYPT_MODE, aesKey, iv, msg);
        System.out.println("encrypt AES-256-GCM : " + ct.length + " bytes (11 plaintext + 16 auth tag) -> decrypts to: "
                + new String(aesGcm(Cipher.DECRYPT_MODE, aesKey, iv, ct), StandardCharsets.UTF_8));
        ct[0] ^= 1;                                                                         // flip one bit in transit
        try {
            aesGcm(Cipher.DECRYPT_MODE, aesKey, iv, ct);
        } catch (AEADBadTagException e) {
            System.out.println("encrypt tampered    : AEADBadTagException (integrity check failed)");
        }

        // 4. Digital signature: private key signs, anyone with the public key verifies.
        KeyPair signer = KeyPairGenerator.getInstance("Ed25519").generateKeyPair();
        Signature s = Signature.getInstance("Ed25519");
        s.initSign(signer.getPrivate());
        s.update(msg);
        byte[] sig = s.sign();
        System.out.println("sign    Ed25519     : " + sig.length + "-byte signature; verify original = "
                + verify(signer.getPublic(), msg, sig) + ", verify 'pay bob 900' = "
                + verify(signer.getPublic(), "pay bob 900".getBytes(StandardCharsets.UTF_8), sig));

        // 5. Hybrid (the TLS 1.3 idea): an asymmetric key exchange agrees a shared secret, then fast symmetric crypto.
        KeyPairGenerator x = KeyPairGenerator.getInstance("XDH");
        x.initialize(new NamedParameterSpec("X25519"));
        KeyPair client = x.generateKeyPair(), server = x.generateKeyPair();               // only PUBLIC halves are sent
        byte[] clientSecret = agree(client.getPrivate(), server.getPublic());
        byte[] serverSecret = agree(server.getPrivate(), client.getPublic());
        System.out.println("ECDHE   X25519      : both sides derived the same 32-byte secret = "
                + Arrays.equals(clientSecret, serverSecret) + " (" + HEX.formatHex(clientSecret).substring(0, 12) + "...)");
        SecretKey session = new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(clientSecret), "AES"); // real TLS uses HKDF
        byte[] rec = aesGcm(Cipher.ENCRYPT_MODE, session, iv, "GET /".getBytes(StandardCharsets.UTF_8));
        System.out.println("ECDHE   record      : server decrypts client's record: "
                + new String(aesGcm(Cipher.DECRYPT_MODE, new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(serverSecret), "AES"), iv, rec), StandardCharsets.UTF_8));

        // 6. TOTP: RFC 6238 Appendix B vectors (SHA-1 seed, 8 digits, 30 s step) and a 6-digit app-style code.
        Totp rfc = new Totp("12345678901234567890".getBytes(StandardCharsets.US_ASCII), "HmacSHA1", 8, 30);
        for (long t : new long[] {59, 1111111109, 1234567890, 2000000000}) {
            System.out.printf("TOTP    t=%-11d : counter=%-9d code=%s%n", t, rfc.counterAt(t), rfc.at(t));
        }
        Totp app = new Totp("12345678901234567890".getBytes(StandardCharsets.US_ASCII), "HmacSHA1", 6, 30);
        long t = 1_700_000_000L;
        System.out.println("TOTP    6-digit app : " + app.at(t) + " valid 25 s later = " + app.verify(app.at(t), t + 25, 1)
                + ", valid 5 min later = " + app.verify(app.at(t), t + 300, 1));
    }

    static byte[] aesGcm(int mode, SecretKey key, byte[] iv, byte[] data) throws GeneralSecurityException {
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(mode, key, new GCMParameterSpec(128, iv));
        return c.doFinal(data);
    }

    static boolean verify(PublicKey pub, byte[] data, byte[] sig) throws GeneralSecurityException {
        Signature v = Signature.getInstance("Ed25519");
        v.initVerify(pub);
        v.update(data);
        return v.verify(sig);
    }

    static byte[] agree(PrivateKey mine, PublicKey theirs) throws GeneralSecurityException {
        KeyAgreement ka = KeyAgreement.getInstance("XDH");
        ka.init(mine);
        ka.doPhase(theirs, true);
        return ka.generateSecret();
    }
}
