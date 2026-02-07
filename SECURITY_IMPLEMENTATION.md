# Security Layer Implementation

This document describes the complete security implementation for the Trust Care application following the architecture diagram.

## Architecture Overview

The security layer consists of three main components:

1. **Role-Based Access Control (RBAC)** - Dynamic permissions from database
2. **JWT Authentication Filter** - Token validation and SecurityContext population
3. **Method-Level Security** - @PreAuthorize annotations on endpoints

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

## Security Best Practices Implemented

1. ✅ **JWT tokens** for stateless authentication
2. ✅ **BCrypt** for password hashing
3. ✅ **Dynamic RBAC** from database
4. ✅ **Method-level security** with @PreAuthorize
5. ✅ **Encrypted medical data** storage
6. ✅ **Audit logging** for all actions
7. ✅ **Request logging** with tracking IDs
8. ✅ **Role hierarchy** (Admin > Doctor > Patient)
9. ✅ **Permission-based access** control
10. ✅ **Secure endpoints** (only /auth/** is public)

## Key Files

- `SecurityConfig.java` - Main security configuration
- `CustomUserDetailsService.java` - Loads user with permissions
- `JwtAuthenticationFilter.java` - JWT validation filter
- `JwtTokenProvider.java` - Token generation/validation
- All Controllers - @PreAuthorize annotations
- `RolePermissionRepository.java` - Permission queries
