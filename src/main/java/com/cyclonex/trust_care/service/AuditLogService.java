package com.cyclonex.trust_care.service;

import com.cyclonex.trust_care.entity.AuditLog;
import com.cyclonex.trust_care.entity.User;
import com.cyclonex.trust_care.repository.AuditLogRepository;
import com.cyclonex.trust_care.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Autowired
    public AuditLogService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    public AuditLog saveAuditLog(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    public void logAction(String action, String resource) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setResource(resource);
        
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            User user = userRepository.findByUsername(authentication.getName());
            auditLog.setUser(user);
        }
        
        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAllAuditLogs() {
        return auditLogRepository.findAll();
    }

    public List<AuditLog> getAuditLogsByUserId(Integer userId) {
        return auditLogRepository.findByUser_Id(userId);
    }

    public List<AuditLog> getAuditLogsByAction(String action) {
        return auditLogRepository.findByAction(action);
    }

    public List<AuditLog> getAuditLogsByResource(String resource) {
        return auditLogRepository.findByResource(resource);
    }

    public List<AuditLog> getAuditLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetween(start, end);
    }

    public AuditLog getAuditLogById(int id) {
        return auditLogRepository.findById(id).orElse(null);
    }

    public void deleteAuditLog(int id) {
        auditLogRepository.deleteById(id);
    }
}
