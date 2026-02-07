package com.cyclonex.trust_care.repository;

import com.cyclonex.trust_care.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Integer> {
    Doctor findByUser_Id(int userId);
    Doctor findByLicenseNumber(String licenseNumber);
    List<Doctor> findBySpecialization(String specialization);
}
