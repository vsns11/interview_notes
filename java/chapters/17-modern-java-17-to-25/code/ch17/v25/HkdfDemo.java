package ch17.v25;

import javax.crypto.KDF;
import javax.crypto.spec.HKDFParameterSpec;
import java.util.HexFormat;

/** JEP 510 (Java 25): the Key Derivation Function API, checked against RFC 5869 test case 1. */
public class HkdfDemo {
    public static void main(String[] args) throws Exception {
        HexFormat hex = HexFormat.of();
        byte[] ikm  = hex.parseHex("0b".repeat(22));                   // input keying material
        byte[] salt = hex.parseHex("000102030405060708090a0b0c");
        byte[] info = hex.parseHex("f0f1f2f3f4f5f6f7f8f9");

        KDF hkdf = KDF.getInstance("HKDF-SHA256");
        byte[] okm = hkdf.deriveData(HKDFParameterSpec.ofExtract()
                .addIKM(ikm).addSalt(salt).thenExpand(info, 42));    // extract-then-expand, 42 bytes out

        String expected = "3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf34007208d5b887185865";
        System.out.println("OKM      = " + hex.formatHex(okm));
        System.out.println("RFC 5869 = " + expected);
        System.out.println("match    : " + hex.formatHex(okm).equals(expected));
    }
}
