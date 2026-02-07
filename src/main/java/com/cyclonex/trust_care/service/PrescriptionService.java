package com.cyclonex.trust_care.service;

import com.cyclonex.trust_care.entity.Prescription;
import com.cyclonex.trust_care.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final EncryptionService encryptionService;

    @Autowired
    public PrescriptionService(PrescriptionRepository prescriptionRepository, EncryptionService encryptionService) {
        this.prescriptionRepository = prescriptionRepository;
        this.encryptionService = encryptionService;
    }

    public Prescription savePrescription(Prescription prescription) {
        // Encrypt sensitive medication data before saving
        if (prescription.getMedicationEncrypted() != null && !prescription.getMedicationEncrypted().isEmpty()) {
            prescription.setMedicationEncrypted(encryptionService.encrypt(prescription.getMedicationEncrypted()));
        }
        return prescriptionRepository.save(prescription);
    }

    public List<Prescription> getAllPrescriptions() {
        List<Prescription> prescriptions = prescriptionRepository.findAll();
        return prescriptions.stream().map(this::decryptPrescription).collect(Collectors.toList());
    }

    public Prescription getPrescriptionById(int id) {
        Prescription prescription = prescriptionRepository.findById(id).orElse(null);
        return prescription != null ? decryptPrescription(prescription) : null;
    }

    public List<Prescription> getPrescriptionsByPatientId(int patientId) {
        List<Prescription> prescriptions = prescriptionRepository.findByPatient_Id(patientId);
        return prescriptions.stream().map(this::decryptPrescription).collect(Collectors.toList());
    }

    public List<Prescription> getPrescriptionsByDoctorId(int doctorId) {
        List<Prescription> prescriptions = prescriptionRepository.findByDoctor_Id(doctorId);
        return prescriptions.stream().map(this::decryptPrescription).collect(Collectors.toList());
    }

    public void deletePrescription(int id) {
        prescriptionRepository.deleteById(id);
    }

    private Prescription decryptPrescription(Prescription prescription) {
        // Decrypt sensitive medication data when retrieving
        if (prescription.getMedicationEncrypted() != null && !prescription.getMedicationEncrypted().isEmpty()) {
            prescription.setMedicationEncrypted(encryptionService.decrypt(prescription.getMedicationEncrypted()));
        }
        return prescription;
    }
}
