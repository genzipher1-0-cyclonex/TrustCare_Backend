package com.cyclonex.trust_care.controller;

import com.cyclonex.trust_care.entity.Prescription;
import com.cyclonex.trust_care.service.PrescriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    @Autowired
    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('CREATE_PRESCRIPTION') or hasRole('DOCTOR')")
    public ResponseEntity<Prescription> createPrescription(@RequestBody Prescription prescription) {
        Prescription savedPrescription = prescriptionService.savePrescription(prescription);
        return new ResponseEntity<>(savedPrescription, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('VIEW_PRESCRIPTION') or hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<Prescription>> getAllPrescriptions() {
        List<Prescription> prescriptions = prescriptionService.getAllPrescriptions();
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('VIEW_PRESCRIPTION') or hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<Prescription> getPrescriptionById(@PathVariable int id) {
        Prescription prescription = prescriptionService.getPrescriptionById(id);
        if (prescription != null) {
            return ResponseEntity.ok(prescription);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyAuthority('VIEW_PRESCRIPTION') or hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<List<Prescription>> getPrescriptionsByPatientId(@PathVariable int patientId) {
        List<Prescription> prescriptions = prescriptionService.getPrescriptionsByPatientId(patientId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyAuthority('VIEW_PRESCRIPTION') or hasAnyRole('ADMIN', 'DOCTOR')")
    public ResponseEntity<List<Prescription>> getPrescriptionsByDoctorId(@PathVariable int doctorId) {
        List<Prescription> prescriptions = prescriptionService.getPrescriptionsByDoctorId(doctorId);
        return ResponseEntity.ok(prescriptions);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('UPDATE_PRESCRIPTION') or hasRole('DOCTOR')")
    public ResponseEntity<Prescription> updatePrescription(@PathVariable int id, @RequestBody Prescription prescription) {
        Prescription existingPrescription = prescriptionService.getPrescriptionById(id);
        if (existingPrescription != null) {
            prescription.setId(id);
            Prescription updatedPrescription = prescriptionService.savePrescription(prescription);
            return ResponseEntity.ok(updatedPrescription);
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('DELETE_PRESCRIPTION') or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePrescription(@PathVariable int id) {
        Prescription existingPrescription = prescriptionService.getPrescriptionById(id);
        if (existingPrescription != null) {
            prescriptionService.deletePrescription(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
