package com.cyclonex.trust_care.controller;

import com.cyclonex.trust_care.entity.Doctor;
import com.cyclonex.trust_care.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctor")
public class DoctorController {

    private final DoctorService doctorService;

    @Autowired
    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('VIEW_DOCTOR') or hasRole('ADMIN') or hasRole('DOCTOR')")
    public Doctor getDoctorByUserId(@PathVariable int userId) {
        return doctorService.getDoctorByUserId(userId);
    }

    @GetMapping("/license/{licenseNumber}")
    @PreAuthorize("hasAuthority('VIEW_DOCTOR') or hasRole('ADMIN')")
    public Doctor getDoctorByLicenseNumber(@PathVariable String licenseNumber) {
        return doctorService.getDoctorByLicenseNumber(licenseNumber);
    }

    @GetMapping("/specialization/{specialization}")
    @PreAuthorize("hasAuthority('VIEW_DOCTOR') or hasRole('ADMIN') or hasRole('PATIENT')")
    public List<Doctor> getDoctorsBySpecialization(@PathVariable String specialization) {
        return doctorService.getDoctorsBySpecialization(specialization);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_DOCTOR') or hasRole('ADMIN') or hasRole('PATIENT')")
    public List<Doctor> getAllDoctors() {
        return doctorService.getAllDoctors();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('VIEW_DOCTOR') or hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('PATIENT')")
    public Doctor getDoctorById(@PathVariable int id) {
        return doctorService.getDoctorById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREATE_DOCTOR') or hasRole('ADMIN')")
    public Doctor createDoctor(@RequestBody Doctor doctor) {
        return doctorService.saveDoctor(doctor);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UPDATE_DOCTOR') or hasRole('ADMIN') or hasRole('DOCTOR')")
    public Doctor updateDoctor(@PathVariable int id, @RequestBody Doctor doctor) {
        doctor.setId(id);
        return doctorService.saveDoctor(doctor);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('DELETE_DOCTOR') or hasRole('ADMIN')")
    public void deleteDoctor(@PathVariable int id) {
        doctorService.deleteDoctor(id);
    }
}
