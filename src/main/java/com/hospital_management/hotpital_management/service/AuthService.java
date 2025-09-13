package com.hospital_management.hotpital_management.service;

import com.hospital_management.hotpital_management.dto.LoginRequest;
import com.hospital_management.hotpital_management.dto.LoginResponse;
import com.hospital_management.hotpital_management.dto.SignupRequest;
import com.hospital_management.hotpital_management.dto.SignupResponse;
import com.hospital_management.hotpital_management.model.User;
import com.hospital_management.hotpital_management.model.enums.AuthProviderType;
import com.hospital_management.hotpital_management.repository.UserRepository;
import com.hospital_management.hotpital_management.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Username/password login
     */
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        User user = (User) authentication.getPrincipal();
        String token = authUtil.generateAccessToken(user);

        return new LoginResponse(token, user.getId());
    }

    /**
     * Manual signup
     */
    @Transactional
    public SignupResponse signup(SignupRequest signupRequest) {
        if (userRepository.findByUsername(signupRequest.getUsername()).isPresent()) {
            throw new BadCredentialsException("User already exists with username: " + signupRequest.getUsername());
        }

        User newUser = User.builder().username(signupRequest.getUsername()).password(passwordEncoder.encode(signupRequest.getPassword())).build();

        User savedUser = userRepository.save(newUser);
        return new SignupResponse(savedUser.getId(), savedUser.getUsername());
    }

    @Transactional
    public ResponseEntity<LoginResponse> handleOAuth2LoginRequest(OAuth2User oAuth2User, String registrationId) {
        AuthProviderType providerType = authUtil.getProviderTypeFromRegistrationId(registrationId);
        String providerId = authUtil.determineProviderIdFromOAuth2User(oAuth2User, registrationId);

        User user = userRepository.findByProviderIdAndProviderType(providerId, providerType).orElse(null);
        String email = oAuth2User.getAttribute("email");

        User emailUser;
        if (email != null) {
            emailUser = userRepository.findByUsername(email).orElse(null);
        } else {
            emailUser = null;
        }

        if (user == null && emailUser == null) {
            // auth -register new oauth user
            String username = authUtil.determineUsernameFromOAuth2User(oAuth2User, registrationId, providerId);
            user = userRepository.save(User.builder().username(username).providerId(providerId).providerType(providerType).build());
        } else if (user != null) {
            // update username with latest email if changed
            if (email != null && !email.isBlank() && !email.equals(user.getUsername())) {
                user.setUsername(email);
                userRepository.save(user);
            }
        } else {
            // Email already registered with another provider
            throw new BadCredentialsException("Email already registered: " + email);
        }
        LoginResponse loginResponse = new LoginResponse(authUtil.generateAccessToken(user), user.getId());
        return ResponseEntity.ok(loginResponse);
    }
}