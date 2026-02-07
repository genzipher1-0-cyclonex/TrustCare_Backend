package com.cyclonex.trust_care.controller;

import com.cyclonex.trust_care.entity.AuditLog;
import com.cyclonex.trust_care.service.AuditLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/audit_log")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @Autowired
    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public List<AuditLog> getAllAuditLogs() {
        return auditLogService.getAllAuditLogs();
    }

    @GetMapping("/{id}")
    public AuditLog getAuditLogById(@PathVariable int id) {
        return auditLogService.getAuditLogById(id);
    }

    @GetMapping("/user/{userId}")
    public List<AuditLog> getAuditLogsByUserId(@PathVariable Integer userId) {
        return auditLogService.getAuditLogsByUserId(userId);
    }

    @GetMapping("/action/{action}")
    public List<AuditLog> getAuditLogsByAction(@PathVariable String action) {
        return auditLogService.getAuditLogsByAction(action);
    }

    @GetMapping("/resource/{resource}")
    public List<AuditLog> getAuditLogsByResource(@PathVariable String resource) {
        return auditLogService.getAuditLogsByResource(resource);
    }

    @GetMapping("/date-range")
    public List<AuditLog> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return auditLogService.getAuditLogsByDateRange(start, end);
    }

    @PostMapping
    public AuditLog createAuditLog(@RequestBody AuditLog auditLog) {
        return auditLogService.saveAuditLog(auditLog);
    }

    @DeleteMapping("/{id}")
    public void deleteAuditLog(@PathVariable int id) {
        auditLogService.deleteAuditLog(id);
    }
}
