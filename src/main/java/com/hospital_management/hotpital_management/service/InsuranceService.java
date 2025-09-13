package com.hospital_management.hotpital_management.service;

import com.hospital_management.hotpital_management.model.Insurance;
import com.hospital_management.hotpital_management.repository.InsuranceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InsuranceService {

    private final InsuranceRepository insuranceRepository;


    public Insurance createInsurance(Insurance insurance) {

        if (insuranceRepository.existsByPolicyNumber(insurance.getPolicyNumber())) {
            throw new IllegalArgumentException("Insurance policy already exists: " + insurance.getPolicyNumber());
        }

        Insurance savedInsurance = insuranceRepository.save(insurance);
        log.info("Insurance policy created successfully with ID: {}", savedInsurance.getId());

        return savedInsurance;
    }

    @Transactional(readOnly = true)
    public Optional<Insurance> getInsuranceById(Long insuranceId) {
        log.debug("Retrieving insurance with ID: {}", insuranceId);
        return insuranceRepository.findById(insuranceId);
    }

    @Transactional(readOnly = true)
    public Optional<Insurance> getInsuranceByPolicyNumber(String policyNumber) {
        log.debug("Retrieving insurance with policy number: {}", policyNumber);
        return insuranceRepository.findByPolicyNumber(policyNumber);
    }

    public boolean isInsuranceValid(Long insuranceId) {
        Insurance insurance = insuranceRepository.findById(insuranceId)
                .orElseThrow(() -> new IllegalArgumentException("Insurance not found with ID: " + insuranceId));

        return insurance.getValidUntil().isAfter(LocalDate.now()) ||
                insurance.getValidUntil().isEqual(LocalDate.now());
    }

    public List<Insurance> getExpiringInsurance(int daysAhead) {
        LocalDate cutOffDate = LocalDate.now().plusDays(daysAhead);
        return insuranceRepository.findByValidUntilBefore(cutOffDate);
    }

    public Insurance updateInsurance(Long insuranceId, Insurance updateInsurance) {
        Insurance existingInsurance = insuranceRepository.findById(insuranceId)
                .orElseThrow(() -> new IllegalArgumentException("Insurance not found with ID: " + insuranceId));

        // check policy number uniqueness if it's being changed
        if (!existingInsurance.getPolicyNumber().equals(updateInsurance.getPolicyNumber()) &&
        insuranceRepository.existsByPolicyNumber(updateInsurance.getPolicyNumber())) {
            throw new IllegalArgumentException("Policy number already exists: " + updateInsurance.getPolicyNumber());
        }
        // Update fields
        existingInsurance.setPolicyNumber(updateInsurance.getPolicyNumber());
        existingInsurance.setProvider(updateInsurance.getProvider());
        existingInsurance.setValidUntil(updateInsurance.getValidUntil());

        Insurance savedInsurance = insuranceRepository.save(existingInsurance);
        log.info("Insurance updated successfully: {}", savedInsurance.getId());

        return savedInsurance;
    }
}
