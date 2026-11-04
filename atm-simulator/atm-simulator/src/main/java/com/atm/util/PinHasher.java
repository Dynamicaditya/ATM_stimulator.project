package com.atm.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Secure PIN hashing using PBKDF2-WithHmac-SHA256 with a unique random salt per account.
 * PINs are never stored or logged in plain text.
 */
public final class PinHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_BITS = 256;
    private static final int SALT_BYTES = 16;
    private static final SecureRandom RANDOM = new SecureRandom();

    private PinHasher() { }

    /** @return a new random salt, Base64 encoded. */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /** @return Base64 encoded PBKDF2 hash of the PIN. */
    public static String hash(char[] pin, String saltBase64) {
        PBEKeySpec spec = new PBEKeySpec(pin, Base64.getDecoder().decode(saltBase64), ITERATIONS, KEY_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return Base64.getEncoder().encodeToString(factory.generateSecret(spec).getEncoded());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PIN hashing is not available on this JVM", e);
        } finally {
            spec.clearPassword();
        }
    }

    /** Constant-time comparison of the supplied PIN against the stored hash. */
    public static boolean verify(char[] pin, String saltBase64, String expectedHashBase64) {
        byte[] actual = Base64.getDecoder().decode(hash(pin, saltBase64));
        byte[] expected = Base64.getDecoder().decode(expectedHashBase64);
        return MessageDigest.isEqual(actual, expected);
    }

    /** Overwrites a PIN array in memory. */
    public static void wipe(char[] pin) {
        if (pin != null) {
            Arrays.fill(pin, '\0');
        }
    }
}
