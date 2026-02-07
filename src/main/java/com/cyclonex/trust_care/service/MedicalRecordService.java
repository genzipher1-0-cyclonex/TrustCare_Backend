package com.cyclonex.trust_care.service;

import com.cyclonex.trust_care.entity.MedicalRecord;
import com.cyclonex.trust_care.repository.MedicalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;

    @Autowired
    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
    }

    public MedicalRecord saveMedicalRecord(MedicalRecord medicalRecord) {
        return medicalRecordRepository.save(medicalRecord);
    }

    public List<MedicalRecord> getAllMedicalRecords() {
        return medicalRecordRepository.findAll();
    }

    public MedicalRecord getMedicalRecordById(int id) {
        return medicalRecordRepository.findById(id).orElse(null);
    }

    public List<MedicalRecord> getMedicalRecordsByPatientId(int patientId) {
        return medicalRecordRepository.findByPatientId(patientId);
    }

    public void deleteMedicalRecord(int id) {
        medicalRecordRepository.deleteById(id);
    }
}
