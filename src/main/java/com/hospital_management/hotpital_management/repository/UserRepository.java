package com.hospital_management.hotpital_management.repository;

import com.hospital_management.hotpital_management.model.User;
import com.hospital_management.hotpital_management.model.enums.AuthProviderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByProviderIdAndProviderType(String providerId, AuthProviderType providerType);
}