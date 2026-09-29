package ch11.variants;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

/**
 * One RSA key pair per tenant (issuer). The resource server only needs the PUBLIC keys;
 * the private keys are here so the tests can play each tenant's identity provider.
 */
public class TenantKeys {

    public static final String TENANT_A = "https://idp.tenant-a.test";
    public static final String TENANT_B = "https://idp.tenant-b.test";

    private final Map<String, KeyPair> keys = new LinkedHashMap<>();

    public TenantKeys() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        keys.put(TENANT_A, gen.generateKeyPair());
        keys.put(TENANT_B, gen.generateKeyPair());
    }

    public boolean isTrusted(String issuer) { return keys.containsKey(issuer); }

    public RSAPublicKey publicKey(String issuer) { return (RSAPublicKey) keys.get(issuer).getPublic(); }

    public JwtEncoder encoderFor(String issuer) {
        KeyPair kp = keys.get(issuer);
        return NimbusJwtEncoder.withKeyPair((RSAPublicKey) kp.getPublic(), (RSAPrivateKey) kp.getPrivate()).build();
    }
}
