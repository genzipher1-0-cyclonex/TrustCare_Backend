package com.cyclonex.trust_care.repository;

import com.cyclonex.trust_care.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Integer> {
    List<Prescription> findByPatient_Id(int patientId);
    List<Prescription> findByDoctor_Id(int doctorId);
}
