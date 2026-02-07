package com.cyclonex.trust_care.controller;

import com.cyclonex.trust_care.entity.Patient;
import com.cyclonex.trust_care.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patient")
public class PatientController {

    private final PatientService patientService;

    @Autowired
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('VIEW_PATIENT') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('PATIENT')")
    public Patient getPatientByUserId(@PathVariable int userId) {
        return patientService.getPatientByUserId(userId);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_PATIENT') or hasRole('ADMIN') or hasRole('DOCTOR')")
    public List<Patient> getAllPatients() {
        return patientService.getAllPatients();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_PATIENT') or hasRole('ADMIN') or hasRole('DOCTOR')")
    public Patient getPatientById(@PathVariable int id) {
        return patientService.getPatientById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_PATIENT') or hasRole('ADMIN')")
    public Patient createPatient(@RequestBody Patient patient) {
        return patientService.savePatient(patient);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_PATIENT') or hasRole('ADMIN') or hasRole('PATIENT')")
    public Patient updatePatient(@PathVariable int id, @RequestBody Patient patient) {
        patient.setId(id);
        return patientService.savePatient(patient);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_PATIENT') or hasRole('ADMIN')")
    public void deletePatient(@PathVariable int id) {
        patientService.deletePatient(id);
    }
}
