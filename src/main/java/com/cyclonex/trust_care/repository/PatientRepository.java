package com.cyclonex.trust_care.repository;

import com.cyclonex.trust_care.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {
    Patient findByUser_Id(int userId);
}
