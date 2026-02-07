package com.cyclonex.trust_care.service;

import com.cyclonex.trust_care.entity.Patient;
import com.cyclonex.trust_care.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository patientRepository;

    @Autowired
    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    public Patient getPatientByUserId(int userId) {
        return patientRepository.findByUser_Id(userId);
    }

    public Patient getPatientByEmail(String email) {
        return patientRepository.findByEmail(email);
    }

    public List<Patient> getAllPatients() {
        return patientRepository.findAll();
    }

    public Patient savePatient(Patient patient) {
        return patientRepository.save(patient);
    }

    public void deletePatient(int id) {
        patientRepository.deleteById(id);
    }

    public Patient getPatientById(int id) {
        return patientRepository.findById(id).orElse(null);
    }
}
