package com.hospital_management.hotpital_management.service;

import com.hospital_management.hotpital_management.model.Appointment;
import com.hospital_management.hotpital_management.model.Doctor;
import com.hospital_management.hotpital_management.model.Patient;
import com.hospital_management.hotpital_management.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentRepository appointmentRepository;

    public Appointment scheduleAppointment(Appointment appointment) {
        log.info("Scheduling new appointment for patient {} with doctor {} at {}",
                appointment.getPatient().getId(),
                appointment.getDoctor().getId(),
                appointment.getAppointmentTime());

        // Verify patient exists
        Patient patient = patientService.getPatientById(appointment.getPatient().getId())
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + appointment.getPatient().getId()));

        // Verify doctor exists
        Doctor doctor = doctorService.getDoctorById(appointment.getDoctor().getId())
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + appointment.getDoctor().getId()));

        appointment.setPatient(patient);
        appointment.setDoctor(doctor);

        Appointment savedAppointment = appointmentRepository.save(appointment);
        log.info("Appointment scheduled successfully with ID: {}", savedAppointment.getId());

        return savedAppointment;
    }

    @Transactional(readOnly = true)
    public Optional<Appointment> getAppointmentById(Long appointmentId) {
        log.debug("Retrieving appointment with ID: {}", appointmentId);
        return appointmentRepository.findById(appointmentId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByPatient(Long patientId) {
        log.debug("Retrieving appointments for patient: {}", patientId);

        // Verify patient exists
        patientService.getPatientById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("Patient not found with ID: " + patientId));

        return appointmentRepository.findByPatientIdOrderByAppointmentTimeDesc(patientId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByDoctor(Long doctorId) {
        log.debug("Retrieving appointments for doctor: {}", doctorId);

        // Verify doctor exists
        doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + doctorId));

        return appointmentRepository.findByDoctorIdOrderByAppointmentTimeAsc(doctorId);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getAppointmentsByDateRange(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        log.debug("Retrieving appointments between {} and {}", startDateTime, endDateTime);
        return appointmentRepository.findByAppointmentTimeBetweenOrderByAppointmentTimeAsc(startDateTime, endDateTime);
    }

    public List<Appointment> getTodayAppointmentsByDoctor(Long doctorId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay().minusNanos(1);

        log.debug("Retrieving today's appointments for doctor: {}", doctorId);
        return appointmentRepository.findByDoctorIdAndAppointmentTimeBetweenOrderByAppointmentTimeAsc(
                doctorId, startOfDay, endOfDay);
    }

    @Transactional(readOnly = true)
    public List<Appointment> getUpcomingAppointmentsByPatient(Long patientId) {
        log.debug("Retrieving upcoming appointments for patient: {}", patientId);

        LocalDateTime now = LocalDateTime.now();
        return appointmentRepository.findByPatientIdAndAppointmentTimeAfterOrderByAppointmentTimeAsc(patientId, now);
    }

    public Appointment updateAppointment(Long appointmentId, Appointment updatedAppointment) {
        log.info("Updating appointment with ID: {}", appointmentId);

        Appointment existingAppointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + appointmentId));

        // If appointment time is being changed, check for conflicts
        if (!existingAppointment.getAppointmentTime().equals(updatedAppointment.getAppointmentTime()) ||
        !existingAppointment.getDoctor().getId().equals(updatedAppointment.getDoctor().getId())) {
            validateAppointmentConflicts(updatedAppointment, appointmentId);
        }

        if (!existingAppointment.getDoctor().getId().equals(updatedAppointment.getDoctor().getId())) {
            Doctor doctor = doctorService.getDoctorById(updatedAppointment.getDoctor().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Doctor not found with ID: " + updatedAppointment.getDoctor().getId()));
            existingAppointment.setDoctor(doctor);
        }

        // Update fields
        existingAppointment.setAppointmentTime(updatedAppointment.getAppointmentTime());
        existingAppointment.setReason(updatedAppointment.getReason());

        Appointment savedAppointment = appointmentRepository.save(existingAppointment);
        log.info("Appointment updated successfully: {}", savedAppointment.getId());

        return savedAppointment;
    }

    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + appointmentId));

        // check if appointment is in the past
        if (appointment.getAppointmentTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot cancel past appointments");
        }

        appointmentRepository.delete(appointment);
        log.info("Appointment canceled successfully: {}", appointmentId);
    }

    @Transactional(readOnly = true)
    public boolean isDoctorAvailable(Long doctorId, LocalDateTime appointmentTime, Long excludeAppointmentId) {
        // Define appointment duration (e.g., 30 minutes)
        LocalDateTime appointmentEnd = appointmentTime.plusMinutes(30);
        LocalDateTime appointmentStart = appointmentTime.minusMinutes(30);

        List<Appointment> conflictingAppointments;

        if (excludeAppointmentId != null) {
            conflictingAppointments = appointmentRepository
                    .findConflictingAppointmentsExcluding(doctorId, appointmentStart, appointmentEnd, excludeAppointmentId);
        } else {
            conflictingAppointments = appointmentRepository
                    .findConflictingAppointments(doctorId, appointmentStart, appointmentEnd);
        }
        return conflictingAppointments.isEmpty();
    }

    @Transactional(readOnly = true)
    public long getAppointmentCountByDoctorAndDateRange(Long doctorId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        return appointmentRepository.countByDoctorIdAndAppointmentTimeBetween(doctorId, startDateTime, endDateTime);
    }
    private void validateAppointmentConflicts(Appointment appointment, Long excludeAppointmentId) {
        if (!isDoctorAvailable(appointment.getDoctor().getId(), appointment.getAppointmentTime(), excludeAppointmentId)) {
            throw new IllegalArgumentException("Doctor is not available at the requested time");
        }
    }

    private void validateAppointmentConflicts(Appointment appointment) {
        validateAppointmentConflicts(appointment, null);
    }

}
