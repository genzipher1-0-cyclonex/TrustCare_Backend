package com.cyclonex.trust_care.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class OtpService {

    @Value("${otp.expiry.minutes:5}")
    private int otpExpiryMinutes;

    @Value("${otp.length:6}")
    private int otpLength;

    private final Map<String, OtpData> otpStorage = new HashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a random OTP
     */
    public String generateOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Store OTP with expiration time
     */
    public void storeOtp(String username, String otp) {
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(otpExpiryMinutes);
        otpStorage.put(username, new OtpData(otp, expiryTime));
    }

    /**
     * Verify OTP
     */
    public boolean verifyOtp(String username, String otp) {
        OtpData otpData = otpStorage.get(username);
        
        if (otpData == null) {
            return false;
        }

        // Check if OTP is expired
        if (LocalDateTime.now().isAfter(otpData.getExpiryTime())) {
            otpStorage.remove(username);
            return false;
        }

        // Verify OTP
        boolean isValid = otpData.getOtp().equals(otp);
        
        // Remove OTP after verification (one-time use)
        if (isValid) {
            otpStorage.remove(username);
        }
        
        return isValid;
    }

    /**
     * Clear OTP for a user
     */
    public void clearOtp(String username) {
        otpStorage.remove(username);
    }

    /**
     * Inner class to store OTP with expiry time
     */
    private static class OtpData {
        private final String otp;
        private final LocalDateTime expiryTime;

        public OtpData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        public String getOtp() {
            return otp;
        }

        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
    }
}
