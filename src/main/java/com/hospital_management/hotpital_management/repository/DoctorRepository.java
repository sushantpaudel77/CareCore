package com.hospital_management.hotpital_management.repository;

import com.hospital_management.hotpital_management.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    boolean existsByEmail(String email);
    Optional<Doctor> findByEmail(String email);
    List<Doctor> findBySpecializationIgnoreCase(String specialization);
}