-- TrustCare Database Schema
-- Updated to match JPA Entity definitions

-- Roles table (matches Role entity)
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL UNIQUE,
    secret_key VARCHAR(255),
    description TEXT
);

-- Permissions table (matches Permission entity)
CREATE TABLE IF NOT EXISTS permissions (
    id BIGSERIAL PRIMARY KEY,
    permission_name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT
);

-- Role_Permissions junction table (matches RolePermission entity)
CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGSERIAL PRIMARY KEY,
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    UNIQUE(role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- Users table (matches User entity)
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role_id BIGINT,
    status VARCHAR(20) DEFAULT 'active',
    FOREIGN KEY (role_id) REFERENCES roles(id)
);

-- Admins table (matches Admin entity)
CREATE TABLE IF NOT EXISTS admins (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    admin_level VARCHAR(50),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Patients table (matches Patient entity)
CREATE TABLE IF NOT EXISTS patients (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    date_of_birth DATE,
    gender VARCHAR(10),
    address TEXT,
    contact_number VARCHAR(20),
    email VARCHAR(100),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Doctors table (matches Doctor entity)
CREATE TABLE IF NOT EXISTS doctors (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    specialization VARCHAR(100),
    license_number VARCHAR(50) UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Inventory table
CREATE TABLE IF NOT EXISTS inventory (
    id BIGSERIAL PRIMARY KEY,
    item_name VARCHAR(200) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    unit VARCHAR(50),
    category VARCHAR(100),
    expiry_date DATE,
    reorder_level INTEGER
);

-- Appointment table
CREATE TABLE IF NOT EXISTS appointment (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    appointment_date TIMESTAMP NOT NULL,
    status VARCHAR(50) DEFAULT 'scheduled',
    notes TEXT,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
);

-- School_Record table
CREATE TABLE IF NOT EXISTS school_record (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    diagnosis_description TEXT,
    treatment_description TEXT,
    visit_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE
);

-- Prescription table
CREATE TABLE IF NOT EXISTS prescription (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    prescription_date DATE NOT NULL,
    medication TEXT,
    dosage TEXT,
    instructions TEXT,
    status VARCHAR(50) DEFAULT 'active',
    FOREIGN KEY (patient_id) REFERENCES patients(id) ON DELETE CASCADE,
    FOREIGN KEY (doctor_id) REFERENCES doctors(id) ON DELETE CASCADE
);

-- Required_Labs table
CREATE TABLE IF NOT EXISTS required_labs (
    id BIGSERIAL PRIMARY KEY,
    prescription_id BIGINT NOT NULL,
    lab_test_name VARCHAR(200) NOT NULL,
    test_status VARCHAR(50) DEFAULT 'pending',
    results TEXT,
    test_date DATE,
    FOREIGN KEY (prescription_id) REFERENCES prescription(id) ON DELETE CASCADE
);

-- Create indexes for frequently queried columns
CREATE INDEX IF NOT EXISTS idx_user_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_user_role_id ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_patient_user_id ON patients(user_id);
CREATE INDEX IF NOT EXISTS idx_doctor_user_id ON doctors(user_id);
CREATE INDEX IF NOT EXISTS idx_appointment_patient_id ON appointment(patient_id);
CREATE INDEX IF NOT EXISTS idx_appointment_doctor_id ON appointment(doctor_id);
CREATE INDEX IF NOT EXISTS idx_appointment_date ON appointment(appointment_date);
CREATE INDEX IF NOT EXISTS idx_prescription_patient_id ON prescription(patient_id);
CREATE INDEX IF NOT EXISTS idx_prescription_doctor_id ON prescription(doctor_id);
CREATE INDEX IF NOT EXISTS idx_school_record_patient_id ON school_record(patient_id);

-- Insert default roles
INSERT INTO roles (role_name, description) VALUES 
    ('ADMIN', 'System Administrator with full access'),
    ('DOCTOR', 'Medical Doctor with patient care access'),
    ('PATIENT', 'Patient with limited access to own records')
ON CONFLICT (role_name) DO NOTHING;

-- Insert default permissions
INSERT INTO permissions (permission_name, description) VALUES 
    ('USER_READ', 'Read user information'),
    ('USER_WRITE', 'Create and update user information'),
    ('USER_DELETE', 'Delete user accounts'),
    ('PATIENT_READ', 'Read patient records'),
    ('PATIENT_WRITE', 'Create and update patient records'),
    ('DOCTOR_READ', 'Read doctor information'),
    ('DOCTOR_WRITE', 'Manage doctor information'),
    ('APPOINTMENT_READ', 'View appointments'),
    ('APPOINTMENT_WRITE', 'Schedule and manage appointments'),
    ('PRESCRIPTION_READ', 'View prescriptions'),
    ('PRESCRIPTION_WRITE', 'Create and manage prescriptions'),
    ('INVENTORY_READ', 'View inventory'),
    ('INVENTORY_WRITE', 'Manage inventory'),
    ('ROLE_MANAGE', 'Manage roles and permissions')
ON CONFLICT (permission_name) DO NOTHING;

-- Assign permissions to roles
-- Admin gets all permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.role_name = 'ADMIN'
ON CONFLICT DO NOTHING;

-- Doctor gets patient, appointment, and prescription permissions
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.role_name = 'DOCTOR' 
AND p.permission_name IN ('PATIENT_READ', 'PATIENT_WRITE', 'APPOINTMENT_READ', 
                          'APPOINTMENT_WRITE', 'PRESCRIPTION_READ', 'PRESCRIPTION_WRITE')
ON CONFLICT DO NOTHING;

-- Patient gets read permissions only
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.role_name = 'PATIENT' 
AND p.permission_name IN ('PATIENT_READ', 'APPOINTMENT_READ', 'PRESCRIPTION_READ')
ON CONFLICT DO NOTHING;
