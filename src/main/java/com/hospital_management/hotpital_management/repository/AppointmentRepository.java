package com.hospital_management.hotpital_management.repository;

import com.hospital_management.hotpital_management.model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
            "AND a.appointmentTime BETWEEN :startTime AND :endTime " +
            "AND a.id != :excludeAppointmentId")
    List<Appointment> findConflictingAppointmentsExcluding(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime appointmentStart,
            @Param("endTime") LocalDateTime appointmentEnd,
            @Param("excludeAppointmentId") Long excludeAppointmentId);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
            "AND a.appointmentTime BETWEEN :startTime AND :endTime")
    List<Appointment> findConflictingAppointments(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime appointmentStart,
            @Param("endTime") LocalDateTime appointmentEnd);

    long countByDoctorIdAndAppointmentTimeBetween(Long doctorId, LocalDateTime startDateTime, LocalDateTime endDateTime);
}