package com.hospital_management.hotpital_management.repository;

import com.hospital_management.hotpital_management.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatientIdOrderByAppointmentTimeDesc(Long patientId);
    List<Appointment> findByDoctorIdOrderByAppointmentTimeAsc(Long doctorId);
    List<Appointment> findByAppointmentTimeBetweenOrderByAppointmentTimeAsc(LocalDateTime startDateTime, LocalDateTime endDateTime);
    List<Appointment> findByDoctorIdAndAppointmentTimeBetweenOrderByAppointmentTimeAsc(Long doctorId, LocalDateTime startOfDay, LocalDateTime endOfDay);
    List<Appointment> findByPatientIdAndAppointmentTimeAfterOrderByAppointmentTimeAsc(Long patientId, LocalDateTime now);
    List<Appointment> findConflictingAppointmentsExcluding(Long doctorId, LocalDateTime appointmentStart, LocalDateTime appointmentEnd, Long excludeAppointmentId);
    List<Appointment> findConflictingAppointments(Long doctorId, LocalDateTime appointmentStart, LocalDateTime appointmentEnd);
    long countByDoctorIdAndAppointmentTimeBetween(Long doctorId, LocalDateTime startDateTime, LocalDateTime endDateTime);
}