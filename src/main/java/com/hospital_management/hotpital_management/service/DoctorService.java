package com.hospital_management.hotpital_management.service;

import com.hospital_management.hotpital_management.model.Department;
import com.hospital_management.hotpital_management.model.Doctor;
import com.hospital_management.hotpital_management.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public Doctor createDoctor(Doctor doctor) {
        if (doctorRepository.existsByEmail(doctor.getEmail())) {
            throw new IllegalArgumentException("Doctor with email " + doctor.getEmail() + " already exists");
        }
        Doctor savedDoctor = doctorRepository.save(doctor);
        log.info("Doctor created successfully with ID: {}", savedDoctor.getId());
        return savedDoctor;
    }

    @Transactional(readOnly = true)
    public Optional<Doctor> getDoctorById(Long doctorId) {
        log.debug("Retrieving doctor with ID: {}", doctorId);
        return doctorRepository.findById(doctorId);
    }

    @Transactional(readOnly = true)
    public Optional<Doctor> getDoctorByEmail(String email) {
        log.debug("Retrieving doctor with email: {}", email);
        return doctorRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<Doctor> getAllDoctors() {
        log.debug("Retrieving all doctors");
        return doctorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Doctor> getDoctorsBySpecialization(String specialization) {
        log.debug("Finding doctors with specialization: {}", specialization);
        return doctorRepository.findBySpecializationIgnoreCase(specialization);
    }

    public Doctor updateDoctor(Long doctorId, Doctor updatedDoctor) {
        log.info("Updating doctor with ID: {}", doctorId);

        Doctor existingDoctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));


        // Check email uniqueness if email is being changed
        if (!existingDoctor.getEmail().equals(updatedDoctor.getEmail()) &&
                doctorRepository.existsByEmail(updatedDoctor.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + updatedDoctor.getEmail());
        }

        // Update fields
        existingDoctor.setName(updatedDoctor.getName());
        existingDoctor.setEmail(updatedDoctor.getEmail());
        existingDoctor.setSpecialization(updatedDoctor.getSpecialization());

        Doctor savedDoctor = doctorRepository.save(existingDoctor);
        log.info("Doctor updated successfully: {}", savedDoctor.getId());

        return savedDoctor;
    }

    @Transactional(readOnly = true)
    public Set<Department> getDoctorDepartments(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));

        return doctor.getDepartments();
    }

    @Transactional(readOnly = true)
    public boolean isDoctorAvailable(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));

        // TODO: Implement actual availability logic based on appointments
        // This would typically check against appointment schedules
        log.debug("Checking availability for doctor: {}", doctor.getName());
        return true; // Placeholder
    }

    public void deleteDoctor(Long doctorId) {
        log.info("Deleting doctor with ID: {}", doctorId);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));

        // TODO: Check for active appointments
        doctorRepository.delete(doctor);
        log.info("Doctor deleted successfully: {}", doctorId);
    }

}
