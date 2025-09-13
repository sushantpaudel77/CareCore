package com.hospital_management.hotpital_management.service;

import com.hospital_management.hotpital_management.dto.LoginRequest;
import com.hospital_management.hotpital_management.dto.LoginResponse;
import com.hospital_management.hotpital_management.dto.SignupRequest;
import com.hospital_management.hotpital_management.dto.SignupResponse;
import com.hospital_management.hotpital_management.model.User;
import com.hospital_management.hotpital_management.repository.UserRepository;
import com.hospital_management.hotpital_management.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        User user = (User) authentication.getPrincipal();

        String token = authUtil.generateAccessToken(user);

        return new LoginResponse(token, user.getId());
    }

    public SignupResponse signup(SignupRequest signupRequest) {

        Optional<User> existingUser = userRepository.findByUsername(signupRequest.getUsername());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("User already exists");
        }

        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        User user = userRepository.save(
                User.builder()
                        .username(signupRequest.getUsername())
                        .password(encodedPassword)
                        .build()
        );

        return new SignupResponse(user.getId(), user.getUsername());
    }

}
