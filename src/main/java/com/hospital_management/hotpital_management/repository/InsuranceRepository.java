package com.hospital_management.hotpital_management.repository;

import com.hospital_management.hotpital_management.model.Insurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceRepository extends JpaRepository<Insurance, Long> {
    boolean existsByPolicyNumber(String policyNumber);
    Optional<Insurance> findByPolicyNumber(String policyNumber);
    List<Insurance> findByValidUntilBefore(LocalDate cutOffDate);
}