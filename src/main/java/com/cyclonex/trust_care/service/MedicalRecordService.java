package com.cyclonex.trust_care.service;

import com.cyclonex.trust_care.entity.MedicalRecord;
import com.cyclonex.trust_care.repository.MedicalRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final EncryptionService encryptionService;

    @Autowired
    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository, EncryptionService encryptionService) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.encryptionService = encryptionService;
    }

    public MedicalRecord saveMedicalRecord(MedicalRecord medicalRecord) {
        // Encrypt sensitive data before saving
        if (medicalRecord.getDiagnosisEncrypted() != null && !medicalRecord.getDiagnosisEncrypted().isEmpty()) {
            medicalRecord.setDiagnosisEncrypted(encryptionService.encrypt(medicalRecord.getDiagnosisEncrypted()));
        }
        if (medicalRecord.getTreatmentEncrypted() != null && !medicalRecord.getTreatmentEncrypted().isEmpty()) {
            medicalRecord.setTreatmentEncrypted(encryptionService.encrypt(medicalRecord.getTreatmentEncrypted()));
        }
        return medicalRecordRepository.save(medicalRecord);
    }

    public List<MedicalRecord> getAllMedicalRecords() {
        List<MedicalRecord> records = medicalRecordRepository.findAll();
        return records.stream().map(this::decryptMedicalRecord).collect(Collectors.toList());
    }

    public MedicalRecord getMedicalRecordById(int id) {
        MedicalRecord record = medicalRecordRepository.findById(id).orElse(null);
        return record != null ? decryptMedicalRecord(record) : null;
    }

    public List<MedicalRecord> getMedicalRecordsByPatientId(int patientId) {
        List<MedicalRecord> records = medicalRecordRepository.findByPatientId(patientId);
        return records.stream().map(this::decryptMedicalRecord).collect(Collectors.toList());
    }

    public void deleteMedicalRecord(int id) {
        medicalRecordRepository.deleteById(id);
    }

    private MedicalRecord decryptMedicalRecord(MedicalRecord record) {
        // Decrypt sensitive data when retrieving
        if (record.getDiagnosisEncrypted() != null && !record.getDiagnosisEncrypted().isEmpty()) {
            record.setDiagnosisEncrypted(encryptionService.decrypt(record.getDiagnosisEncrypted()));
        }
        if (record.getTreatmentEncrypted() != null && !record.getTreatmentEncrypted().isEmpty()) {
            record.setTreatmentEncrypted(encryptionService.decrypt(record.getTreatmentEncrypted()));
        }
        return record;
    }
}
