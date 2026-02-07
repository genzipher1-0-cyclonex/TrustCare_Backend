package com.cyclonex.trust_care.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginInitiateResponse {
    private String message;
    private String username;
    private boolean otpSent;
    private String maskedEmail;
}
