package com.cyclonex.trust_care.controller;

import com.cyclonex.trust_care.entity.MedicalRecord;
import com.cyclonex.trust_care.service.MedicalRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/medical_record")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @Autowired
    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_MEDICAL_RECORD') or hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecord> createMedicalRecord(@RequestBody MedicalRecord medicalRecord) {
        MedicalRecord savedRecord = medicalRecordService.saveMedicalRecord(medicalRecord);
        return ResponseEntity.ok(savedRecord);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_MEDICAL_RECORD') or hasRole('ADMIN') or hasRole('DOCTOR')")
    public ResponseEntity<List<MedicalRecord>> getAllMedicalRecords() {
        return ResponseEntity.ok(medicalRecordService.getAllMedicalRecords());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_MEDICAL_RECORD') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<MedicalRecord> getMedicalRecordById(@PathVariable int id) {
        MedicalRecord record = medicalRecordService.getMedicalRecordById(id);
        if (record != null) {
            return ResponseEntity.ok(record);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAuthority('VIEW_MEDICAL_RECORD') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('PATIENT')")
    public ResponseEntity<List<MedicalRecord>> getMedicalRecordsByPatient(@PathVariable int patientId) {
        return ResponseEntity.ok(medicalRecordService.getMedicalRecordsByPatientId(patientId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_MEDICAL_RECORD') or hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecord> updateMedicalRecord(@PathVariable int id, @RequestBody MedicalRecord medicalRecord) {
        medicalRecord.setId(id);
        MedicalRecord updatedRecord = medicalRecordService.saveMedicalRecord(medicalRecord);
        return ResponseEntity.ok(updatedRecord);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_MEDICAL_RECORD') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMedicalRecord(@PathVariable int id) {
        medicalRecordService.deleteMedicalRecord(id);
        return ResponseEntity.noContent().build();
    }
}
