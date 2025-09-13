package com.hospital_management.hotpital_management.repository;

import com.hospital_management.hotpital_management.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    boolean existsByEmail(String email);

    boolean existsByNameAndBirthDate(String name, LocalDate birthDate);

    Optional<Patient> findByEmail(String email);

    List<Patient> findByBirthDateBetween(LocalDate startDate, LocalDate endDate);
}
