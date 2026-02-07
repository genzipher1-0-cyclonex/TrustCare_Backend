package com.cyclonex.trust_care.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    @Value("${encryption.secret.key}")
    private String secretKeyString;

    // Encrypt the given plain text using AES encryption
    public String encrypt(String plainText) {
        try {
            if (plainText == null || plainText.isEmpty()) {
                return plainText;
            }

            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error encrypting data", e);
        }
    }

    // Decrypt the given encrypted text using AES decryption
    public String decrypt(String encryptedText) {
        try {
            if (encryptedText == null || encryptedText.isEmpty()) {
                return encryptedText;
            }

            SecretKey secretKey = getSecretKey();
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error decrypting data", e);
        }
    }

    // Get the secret key from the configured string, ensuring it is 32 bytes for AES-256
    private SecretKey getSecretKey() {
        try {
            // Ensure the key is exactly 32 bytes for AES-256
            byte[] keyBytes = secretKeyString.getBytes();
            byte[] normalizedKey = new byte[32];
            System.arraycopy(keyBytes, 0, normalizedKey, 0, Math.min(keyBytes.length, 32));
            
            return new SecretKeySpec(normalizedKey, ALGORITHM);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error creating secret key", e);
        }
    }

    // Utility method to generate a new random AES key (for testing or key rotation purposes)
    public static String generateNewKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256); // AES-256
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Error generating key", e);
        }
    }
}
