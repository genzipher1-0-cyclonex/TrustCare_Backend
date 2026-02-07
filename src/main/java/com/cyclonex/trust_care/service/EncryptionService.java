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

    @Value("${encryption.secret.key:YourSecretKeyForEncryptionMustBe32Bytes!}")
    private String secretKeyString;

    /**
     * Encrypts the given plain text using AES encryption
     * @param plainText The text to encrypt
     * @return Base64 encoded encrypted text
     */
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
            throw new RuntimeException("Error encrypting data", e);
        }
    }

    /**
     * Decrypts the given encrypted text using AES decryption
     * @param encryptedText The Base64 encoded encrypted text
     * @return Decrypted plain text
     */
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
            throw new RuntimeException("Error decrypting data", e);
        }
    }

    /**
     * Gets the secret key for encryption/decryption
     * In production, this should be loaded from a secure vault or environment variable
     */
    private SecretKey getSecretKey() {
        try {
            // Ensure the key is exactly 32 bytes for AES-256
            byte[] keyBytes = secretKeyString.getBytes();
            byte[] normalizedKey = new byte[32];
            System.arraycopy(keyBytes, 0, normalizedKey, 0, Math.min(keyBytes.length, 32));
            
            return new SecretKeySpec(normalizedKey, ALGORITHM);
        } catch (Exception e) {
            throw new RuntimeException("Error creating secret key", e);
        }
    }

    /**
     * Generates a new random AES key (for initial setup)
     * Use this method to generate a secure key and store it in environment variables
     */
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
