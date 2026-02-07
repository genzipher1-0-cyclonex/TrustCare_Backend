package com.cyclonex.trust_care.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Send OTP email to user
     */
    @Async
    public void sendOtpEmail(String toEmail, String otp, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("TrustCare - Your Login OTP");
            message.setText(buildOtpEmailBody(username, otp));
            
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email: " + e.getMessage());
        }
    }

    /**
     * Build email body for OTP
     */
    private String buildOtpEmailBody(String username, String otp) {
        return String.format(
            "Hello %s,\n\n" +
            "Your One-Time Password (OTP) for TrustCare login is:\n\n" +
            "%s\n\n" +
            "This OTP is valid for 5 minutes.\n\n" +
            "If you did not request this OTP, please ignore this email.\n\n" +
            "Best regards,\n" +
            "TrustCare Team",
            username, otp
        );
    }

    /**
     * Send registration confirmation email
     */
    public void sendRegistrationEmail(String toEmail, String username) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Welcome to TrustCare!");
            message.setText(buildRegistrationEmailBody(username));
            
            mailSender.send(message);
        } catch (Exception e) {
            // Log but don't throw exception for registration emails
            System.err.println("Failed to send registration email: " + e.getMessage());
        }
    }

    /**
     * Build email body for registration
     */
    private String buildRegistrationEmailBody(String username) {
        return String.format(
            "Hello %s,\n\n" +
            "Welcome to TrustCare!\n\n" +
            "Your account has been successfully created.\n" +
            "You can now log in using your username and password.\n" +
            "For security, we will send you an OTP to your email during login.\n\n" +
            "Best regards,\n" +
            "TrustCare Team",
            username
        );
    }

    /**
     * Mask email for display (show first 2 and last part after @)
     */
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        
        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***@" + domain;
        }
        
        return localPart.substring(0, 2) + "***@" + domain;
    }
}
