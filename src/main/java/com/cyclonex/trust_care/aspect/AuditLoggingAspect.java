package com.cyclonex.trust_care.aspect;

import com.cyclonex.trust_care.entity.User;
import com.cyclonex.trust_care.repository.UserRepository;
import com.cyclonex.trust_care.service.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuditLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLoggingAspect.class);
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    @Autowired
    public AuditLoggingAspect(AuditLogService auditLogService, UserRepository userRepository) {
        this.auditLogService = auditLogService;
        this.userRepository = userRepository;
    }

    // Pointcut for all controller POST methods (CREATE operations)
    @Pointcut("execution(* com.cyclonex.trust_care.controller.*.*(..)) && " +
              "@annotation(org.springframework.web.bind.annotation.PostMapping)")
    public void createOperations() {}

    // Pointcut for all controller PUT methods (UPDATE operations)
    @Pointcut("execution(* com.cyclonex.trust_care.controller.*.*(..)) && " +
              "@annotation(org.springframework.web.bind.annotation.PutMapping)")
    public void updateOperations() {}

    // Pointcut for all controller DELETE methods (DELETE operations)
    @Pointcut("execution(* com.cyclonex.trust_care.controller.*.*(..)) && " +
              "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void deleteOperations() {}

    @AfterReturning(pointcut = "createOperations()", returning = "result")
    public void logCreateOperation(JoinPoint joinPoint, Object result) {
        logAuditEvent("CREATE", joinPoint);
    }

    @AfterReturning(pointcut = "updateOperations()", returning = "result")
    public void logUpdateOperation(JoinPoint joinPoint, Object result) {
        logAuditEvent("UPDATE", joinPoint);
    }

    @AfterReturning(pointcut = "deleteOperations()", returning = "result")
    public void logDeleteOperation(JoinPoint joinPoint, Object result) {
        logAuditEvent("DELETE", joinPoint);
    }

    private void logAuditEvent(String action, JoinPoint joinPoint) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return; // Skip logging for unauthenticated requests
            }

            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            
            if (user == null) {
                return;
            }

            // Get the controller and method name to determine the resource
            String className = joinPoint.getSignature().getDeclaringTypeName();
            String methodName = joinPoint.getSignature().getName();
            String resource = extractResourceName(className, methodName);

            // Get request ID from current HTTP request
            String requestId = null;
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                requestId = (String) request.getAttribute("requestId");
            }

            // Log the action with request ID
            auditLogService.logAction(action, resource, requestId);
            
        } catch (Exception e) {
            // Log the exception but don't fail the operation
            log.error("Error logging audit event: {}", e.getMessage());
        }
    }

    private String extractResourceName(String className, String methodName) {
        // Extract controller name (e.g., PatientController -> Patient)
        String controllerName = className.substring(className.lastIndexOf('.') + 1);
        controllerName = controllerName.replace("Controller", "");
        
        // Format: Resource.Method (e.g., Patient.create, MedicalRecord.update)
        return controllerName + "." + methodName;
    }
}
