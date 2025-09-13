package com.hospital_management.hotpital_management.service;

import com.hospital_management.hotpital_management.model.Insurance;
import com.hospital_management.hotpital_management.model.Patient;
import com.hospital_management.hotpital_management.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final InsuranceService insuranceService;

    public Patient createPatient(Patient patient) {

        if (patientRepository.existsByEmail(patient.getEmail())) {
            throw new IllegalArgumentException("Patient with email" + patient.getEmail() + "already exists.");
        }

        if (patientRepository.existsByNameAndBirthDate(patient.getName(), patient.getBirthDate())) {
            throw new IllegalArgumentException("Patient with same name and birth already exists");
        }

        Patient savedPatient = patientRepository.save(patient);
        log.info("Patient created successfully with ID: {}", savedPatient.getId());

        return savedPatient;
    }

    @Transactional(readOnly = true)
    public Optional<Patient> getPatientById(Long patientId) {
        log.debug("Retrieving patient with ID: {}", patientId);
        return patientRepository.findById(patientId);
    }

    @Transactional(readOnly = true)
    public Optional<Patient> getPatientByEmail(String email) {
        log.debug("Retrieving patient with email: {}", email);
        return patientRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<Patient> getAllPatients() {
        log.debug("Retrieving all patients");
        return patientRepository.findAll();
    }

    public Patient updatePatient(Long patientId, Patient updatedPatient) {
        log.info("Updating patient with ID: {}", patientId);

        Patient existingPatient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + patientId));

        if(!existingPatient.getEmail().equals(updatedPatient.getEmail()) &&
                patientRepository.existsByEmail(updatedPatient.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + updatedPatient.getEmail());
        }

        // Update fields
        existingPatient.setName(updatedPatient.getName());
        existingPatient.setEmail(updatedPatient.getEmail());
        existingPatient.setGender(updatedPatient.getGender());
        existingPatient.setBloodGroup(updatedPatient.getBloodGroup());

        Patient savedPatient = patientRepository.save(existingPatient);
        log.info("Patient updated successfully: {}", savedPatient.getId());

        return savedPatient;
    }

    public Patient addInsuranceToPatient(Long patientId, Insurance insurance) {

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + patientId));

        // save insurance first
        Insurance savedInsurance = insuranceService.createInsurance(insurance);

        // associate with patient
        patient.setInsurance(savedInsurance);

        Patient updatedPatient = patientRepository.save(patient);
        log.info("Insurance added successfully to patient: {}", patientId);

        return updatedPatient;
    }

    @Transactional(readOnly = true)
    public int calculatePatientAge(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + patientId));

        return Period.between(patient.getBirthDate(), LocalDate.now()).getYears();
    }

    public List<Patient> getPatientsByBirthDateRange(LocalDate startDate, LocalDate endDate) {
        log.debug("Finding patients born between {} and {}", startDate, endDate);
        return patientRepository.findByBirthDateBetween(startDate, endDate);
    }

    public void deletePatient(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + patientId));

        if (patient.getAppointments() != null && !patient.getAppointments().isEmpty()) {
            throw new IllegalStateException("Cannot delete patient with existing appointments");
        }

        patientRepository.delete(patient);
        log.info("Patient deleted successfully: {}", patientId);
    }
}
