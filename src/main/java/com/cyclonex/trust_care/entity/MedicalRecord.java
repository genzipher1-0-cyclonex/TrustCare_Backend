package com.cyclonex.trust_care.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "medical_record")
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "patient_id", referencedColumnName = "id")
    private Patient patient;

    @Column(name = "diagnosis_encrypted")
    private String diagnosisEncrypted;

    @Column(name = "treatment_encrypted")
    private String treatmentEncrypted;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public String getDiagnosisEncrypted() {
        return diagnosisEncrypted;
    }

    public void setDiagnosisEncrypted(String diagnosisEncrypted) {
        this.diagnosisEncrypted = diagnosisEncrypted;
    }

    public String getTreatmentEncrypted() {
        return treatmentEncrypted;
    }

    public void setTreatmentEncrypted(String treatmentEncrypted) {
        this.treatmentEncrypted = treatmentEncrypted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
