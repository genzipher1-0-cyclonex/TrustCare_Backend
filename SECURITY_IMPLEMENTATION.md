# Security Layer Implementation

This document describes the complete security implementation for the Trust Care application following the architecture diagram.

## Architecture Overview

The security layer consists of six main components:

1. **Role-Based Access Control (RBAC)** - Dynamic permissions from database
2. **JWT Authentication Filter** - Token validation and SecurityContext population
3. **Method-Level Security** - @PreAuthorize annotations on endpoints
4. **Audit Logging with AOP** - Automatic logging of CREATE/UPDATE/DELETE operations
5. **AES Encryption for Medical Data** - Encryption of sensitive medical information
6. **Request ID Generation and Propagation** - UUID-based request tracking

## 1. Role-Based Access Control (RBAC)

### Database Structure
- **role** table: Stores role definitions (ADMIN, DOCTOR, PATIENT)
- **permission** table: Stores permission definitions
- **role_permission** table: Maps roles to their permissions
- **user** table: Links users to roles

### Implementation

#### CustomUserDetailsService
Located: `com.cyclonex.trust_care.security.CustomUserDetailsService`

This service loads user details including:
- User's role (with ROLE_ prefix for Spring Security)
- All permissions assigned to the user's role via role_permission table

```java
@Override
public UserDetails loadUserByUsername(String email) {
    User user = userRepository.findByEmail(email);
    
    // Load permissions for the user's role
    List<GrantedAuthority> authorities = new ArrayList<>();
    
    // Add role as authority
    authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getRoleName()));
    
    // Load and add all permissions
    List<RolePermission> rolePermissions = rolePermissionRepository.findByRole_Id(user.getRole().getId());
    List<GrantedAuthority> permissionAuthorities = rolePermissions.stream()
        .map(rp -> new SimpleGrantedAuthority(rp.getPermission().getPermissionName()))
        .collect(Collectors.toList());
    
    authorities.addAll(permissionAuthorities);
    
    return new org.springframework.security.core.userdetails.User(
        user.getEmail(),
        user.getPasswordHash(),
        authorities
    );
}
```

## 2. JWT Authentication Filter

Located: `com.cyclonex.trust_care.security.JwtAuthenticationFilter`

### Flow
1. Extract JWT token from Authorization header
2. Validate token and extract email
3. Load user details (including permissions) via CustomUserDetailsService
4. Set authentication in SecurityContext with all authorities

```java
protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) {
    String token = getJwtFromRequest(request);
    
    if (token != null && !token.isEmpty()) {
        String email = jwtTokenProvider.extractUsername(token);
        
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            
            if (jwtTokenProvider.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
    }
    
    filterChain.doFilter(request, response);
}
```

## 3. Method-Level Security

Located: All controller classes with `@PreAuthorize` annotations

### Security Configuration
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Enables @PreAuthorize
public class SecurityConfig {
    // Configuration
}
```

### Access Control Examples

#### Admin Operations
```java
@RestController
@RequestMapping("/admin")
public class AdminController {
    
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_ADMIN') or hasRole('ADMIN')")
    public List<Admin> getAllAdmins() { }
    
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_ADMIN') or hasRole('ADMIN')")
    public Admin createAdmin(@RequestBody Admin admin) { }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_ADMIN') or hasRole('ADMIN')")
    public void deleteAdmin(@PathVariable int id) { }
}
```

#### Doctor Operations
```java
@RestController
@RequestMapping("/doctor")
public class DoctorController {
    
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_DOCTOR') or hasRole('ADMIN') or hasRole('PATIENT')")
    public List<Doctor> getAllDoctors() { }
    
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_DOCTOR') or hasRole('ADMIN')")
    public Doctor createDoctor(@RequestBody Doctor doctor) { }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_DOCTOR') or hasRole('ADMIN') or hasRole('DOCTOR')")
    public Doctor updateDoctor(@PathVariable int id, @RequestBody Doctor doctor) { }
}
```

#### Patient Operations
```java
@RestController
@RequestMapping("/patient")
public class PatientController {
    
    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_PATIENT') or hasRole('ADMIN') or hasRole('DOCTOR')")
    public List<Patient> getAllPatients() { }
    
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_PATIENT') or hasRole('ADMIN')")
    public Patient createPatient(@RequestBody Patient patient) { }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_PATIENT') or hasRole('ADMIN') or hasRole('PATIENT')")
    public Patient updatePatient(@PathVariable int id, @RequestBody Patient patient) { }
}
```

#### Medical Records (Encrypted Data)
```java
@RestController
@RequestMapping("/medical_record")
public class MedicalRecordController {
    
    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_MEDICAL_RECORD') or hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecord> createMedicalRecord(@RequestBody MedicalRecord medicalRecord) { }
    
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAuthority('VIEW_MEDICAL_RECORD') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<List<MedicalRecord>> getMedicalRecordsByPatient(@PathVariable int patientId) { }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_MEDICAL_RECORD') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMedicalRecord(@PathVariable int id) { }
}
```

#### Audit & Request Logs (Admin Only)
```java
@RestController
@RequestMapping("/audit_log")
@PreAuthorize("hasRole('ADMIN')")  // Entire controller restricted to ADMIN
public class AuditLogController {
    // All endpoints require ADMIN role
}

@RestController
@RequestMapping("/request_log")
@PreAuthorize("hasRole('ADMIN')")  // Entire controller restricted to ADMIN
public class RequestLogController {
    // All endpoints require ADMIN role
}
```

## Permission Examples

### Recommended Permission Structure

#### Admin Permissions
- VIEW_ADMIN
- CREATE_ADMIN
- UPDATE_ADMIN
- DELETE_ADMIN
- VIEW_DOCTOR
- CREATE_DOCTOR
- UPDATE_DOCTOR
- DELETE_DOCTOR
- VIEW_PATIENT
- CREATE_PATIENT
- UPDATE_PATIENT
- DELETE_PATIENT
- VIEW_MEDICAL_RECORD
- DELETE_MEDICAL_RECORD

#### Doctor Permissions
- VIEW_DOCTOR (own profile)
- UPDATE_DOCTOR (own profile)
- VIEW_PATIENT
- VIEW_MEDICAL_RECORD
- CREATE_MEDICAL_RECORD
- UPDATE_MEDICAL_RECORD
- CREATE_PRESCRIPTION
- UPDATE_PRESCRIPTION

#### Patient Permissions
- VIEW_PATIENT (own profile)
- UPDATE_PATIENT (own profile)
- VIEW_DOCTOR (for finding doctors)
- VIEW_MEDICAL_RECORD (own records)
- VIEW_APPOINTMENT (own appointments)

## Authentication Flow

1. **Login** - POST `/auth/login` with email and password
2. **Token Generation** - JWT token created with user email
3. **Token Usage** - Include in Authorization header: `Bearer <token>`
4. **Filter Processing** - JwtAuthenticationFilter validates token
5. **Permission Loading** - CustomUserDetailsService loads role and permissions
6. **Authorization** - @PreAuthorize checks permissions/roles
7. **Access Granted/Denied** - Based on permission match

## Testing the Security

### 1. Create roles and permissions in database
```sql
-- Insert roles
INSERT INTO role (role_name, description) VALUES ('ADMIN', 'System Administrator');
INSERT INTO role (role_name, description) VALUES ('DOCTOR', 'Medical Doctor');
INSERT INTO role (role_name, description) VALUES ('PATIENT', 'Patient User');

-- Insert permissions
INSERT INTO permission (permission_name, description) VALUES ('VIEW_PATIENT', 'View patient information');
INSERT INTO permission (permission_name, description) VALUES ('CREATE_PATIENT', 'Create new patient');
INSERT INTO permission (permission_name, description) VALUES ('VIEW_MEDICAL_RECORD', 'View medical records');
INSERT INTO permission (permission_name, description) VALUES ('CREATE_MEDICAL_RECORD', 'Create medical record');

-- Map permissions to roles
INSERT INTO role_permission (role_id, permission_id) 
SELECT r.id, p.id FROM role r, permission p 
WHERE r.role_name = 'DOCTOR' AND p.permission_name = 'CREATE_MEDICAL_RECORD';
```

### 2. Register a user
```bash
POST /auth/register
{
  "username": "doctor1",
  "email": "doctor@example.com",
  "password": "password123",
  "roleName": "DOCTOR"
}
```

### 3. Login and get token
```bash
POST /auth/login
{
  "email": "doctor@example.com",
  "password": "password123"
}
```

### 4. Use token to access protected endpoints
```bash
GET /medical_record
Authorization: Bearer <your-jwt-token>
```

## 4. Audit Logging with AOP

Located: `com.cyclonex.trust_care.aspect.AuditLoggingAspect`

### Automatic Audit Logging
Uses Aspect-Oriented Programming (AOP) to automatically log all CREATE, UPDATE, and DELETE operations without manual code in controllers.

```java
@Aspect
@Component
public class AuditLoggingAspect {
    
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
}
```

### Features
- **Automatic logging** of CREATE/UPDATE/DELETE operations
- **No manual code** required in controllers
- **Resource extraction** from controller and method names
- **Request ID tracking** for correlation
- **User attribution** via SecurityContext
- **Sensitive data protection** - only logs action and resource, not parameters

### AuditLog Entity
```java
@Entity
@Table(name = "audit_log")
public class AuditLog {
    private int id;
    private User user;           // Who performed the action
    private String action;       // CREATE, UPDATE, DELETE
    private String resource;     // e.g., "Patient.createPatient"
    private LocalDateTime timestamp;
    private String requestId;    // For request correlation
}
```

## 5. AES Encryption for Medical Data

Located: `com.cyclonex.trust_care.service.EncryptionService`

### Encryption Service
Provides AES-256 encryption/decryption for sensitive medical data to ensure HIPAA compliance and data protection at rest.

```java
@Service
public class EncryptionService {
    
    @Value("${encryption.secret.key}")
    private String secretKey;
    
    public String encrypt(String plainText) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(
            hexStringToByteArray(secretKey), "AES"
        );
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    
    public String decrypt(String encryptedText) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(
            hexStringToByteArray(secretKey), "AES"
        );
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}
```

### Implementation in Services

#### MedicalRecordService
```java
@Service
public class MedicalRecordService {
    
    @Autowired
    private EncryptionService encryptionService;
    
    public MedicalRecord saveMedicalRecord(MedicalRecord record) {
        // Encrypt sensitive fields before saving
        if (record.getDiagnosisEncrypted() != null) {
            record.setDiagnosisEncrypted(
                encryptionService.encrypt(record.getDiagnosisEncrypted())
            );
        }
        if (record.getTreatmentEncrypted() != null) {
            record.setTreatmentEncrypted(
                encryptionService.encrypt(record.getTreatmentEncrypted())
            );
        }
        return medicalRecordRepository.save(record);
    }
    
    public MedicalRecord getMedicalRecordById(int id) {
        MedicalRecord record = medicalRecordRepository.findById(id).orElse(null);
        return record != null ? decryptMedicalRecord(record) : null;
    }
    
    private MedicalRecord decryptMedicalRecord(MedicalRecord record) {
        // Decrypt sensitive fields when retrieving
        if (record.getDiagnosisEncrypted() != null) {
            record.setDiagnosisEncrypted(
                encryptionService.decrypt(record.getDiagnosisEncrypted())
            );
        }
        if (record.getTreatmentEncrypted() != null) {
            record.setTreatmentEncrypted(
                encryptionService.decrypt(record.getTreatmentEncrypted())
            );
        }
        return record;
    }
}
```

#### PrescriptionService
```java
@Service
public class PrescriptionService {
    
    @Autowired
    private EncryptionService encryptionService;
    
    public Prescription savePrescription(Prescription prescription) {
        // Encrypt medication data before saving
        if (prescription.getMedicationEncrypted() != null) {
            prescription.setMedicationEncrypted(
                encryptionService.encrypt(prescription.getMedicationEncrypted())
            );
        }
        return prescriptionRepository.save(prescription);
    }
    
    private Prescription decryptPrescription(Prescription prescription) {
        // Decrypt medication data when retrieving
        if (prescription.getMedicationEncrypted() != null) {
            prescription.setMedicationEncrypted(
                encryptionService.decrypt(prescription.getMedicationEncrypted())
            );
        }
        return prescription;
    }
}
```

### Encrypted Fields
- **MedicalRecord**: `diagnosisEncrypted`, `treatmentEncrypted`
- **Prescription**: `medicationEncrypted`
- **Algorithm**: AES-256 with ECB mode and PKCS5 padding
- **Key Storage**: `encryption.secret.key` in application.properties/environment

## 6. Request ID Generation and Propagation

Located: `com.cyclonex.trust_care.interceptor.RequestLoggingInterceptor`

### Request ID Flow
1. **Generation** - UUID created for each incoming request
2. **Storage** - Stored in request attributes for internal use
3. **Response Header** - Added to response as `X-Request-ID` for client tracking
4. **Audit Logging** - Included in audit logs for correlation
5. **Request Logging** - Stored in request_log table

```java
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String requestId = UUID.randomUUID().toString();
        request.setAttribute("requestId", requestId);
        // Add request ID to response header for client tracking
        response.setHeader("X-Request-ID", requestId);
        return true;
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        String requestId = (String) request.getAttribute("requestId");
        String method = request.getMethod();
        String endpoint = request.getRequestURI();
        int responseStatus = response.getStatus();
        
        RequestLog requestLog = new RequestLog();
        requestLog.setRequestId(requestId);
        requestLog.setUser(getCurrentUser());
        requestLog.setOperation(method + " " + endpoint);
        requestLog.setOutcome(responseStatus >= 200 && responseStatus < 300 ? "success" : "failure");
        
        requestLogService.saveRequestLog(requestLog);
    }
}
```

### Benefits
- **Request Correlation** - Track requests across logs and systems
- **Debugging** - Identify specific request flows
- **Security Monitoring** - Correlate audit logs with requests
- **Client Tracking** - Clients can reference request IDs for support
- **Distributed Tracing** - Foundation for microservices tracing

### RequestLog Entity
```java
@Entity
@Table(name = "request_log")
public class RequestLog {
    private int id;
    private String requestId;      // UUID for tracking
    private User user;             // Who made the request
    private String operation;      // HTTP method + endpoint
    private String outcome;        // success/failure
    private LocalDateTime timestamp;
}
```

## Configuration

### Application Properties
```properties
# JWT Configuration
jwt.secret=${JWT_Secret_Key}
jwt.expiration=86400000

# Encryption Configuration (AES-256 key for medical data)
encryption.secret.key=${AES_256_Key}
```

### Docker Compose Environment Variables
```yaml
environment:
  # Security Configuration
  JWT_Secret_Key: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
  AES_256_Key: 4D635166546A576E5A7234753778214125442A472D4B614E645267556B587032
```

### Security Configuration
```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Required for @PreAuthorize
public class SecurityConfig {
    // JWT filter, CORS, CSRF configuration
}
```

### Dependencies
```xml
<!-- Spring AOP for audit logging -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.3</version>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

## Security Best Practices Implemented

1. ✅ **JWT tokens** for stateless authentication (24-hour expiration)
2. ✅ **BCrypt** for password hashing
3. ✅ **Dynamic RBAC** from database
4. ✅ **Method-level security** with @PreAuthorize
5. ✅ **AES-256 encryption** for medical data at rest
6. ✅ **Automatic audit logging** with AOP for compliance
7. ✅ **Request ID tracking** for debugging and correlation
8. ✅ **Request logging** with user attribution
9. ✅ **Response headers** with X-Request-ID for client tracking
10. ✅ **Role hierarchy** (Admin > Doctor > Patient)
11. ✅ **Permission-based access** control
12. ✅ **Secure endpoints** (only /auth/** is public)
13. ✅ **Sensitive data protection** in logs
14. ✅ **HIPAA compliance** ready with encryption

## Key Files

### Security Core
- `SecurityConfig.java` - Main security configuration with @EnableMethodSecurity
- `CustomUserDetailsService.java` - Loads user with dynamic permissions
- `JwtAuthenticationFilter.java` - JWT validation and SecurityContext setup
- `JwtTokenProvider.java` - Token generation/validation

### Audit & Logging
- `AuditLoggingAspect.java` - AOP aspect for automatic audit logging
- `RequestLoggingInterceptor.java` - Request ID generation and request logging
- `AuditLogService.java` - Audit log persistence with request ID
- `RequestLogService.java` - Request log persistence

### Encryption
- `EncryptionService.java` - AES-256 encryption/decryption service
- `MedicalRecordService.java` - Encrypts diagnosis and treatment fields
- `PrescriptionService.java` - Encrypts medication field

### Controllers with Security
- All Controllers - @PreAuthorize annotations for permission checks
- `AuditLogController.java` - Admin-only access to audit logs
- `RequestLogController.java` - Admin-only access to request logs
- `MedicalRecordController.java` - Encrypted data handling
- `PrescriptionController.java` - Encrypted prescription data

### Repositories
- `RolePermissionRepository.java` - Permission queries for RBAC
- `UserRepository.java` - User lookup by email
- `AuditLogRepository.java` - Audit log persistence
- `RequestLogRepository.java` - Request log persistence
