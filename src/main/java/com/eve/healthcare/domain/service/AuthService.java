package com.eve.healthcare.domain.service;

import com.eve.healthcare.common.exception.ConflictException;
import com.eve.healthcare.common.exception.UnauthorizedException;
import com.eve.healthcare.common.security.JwtService;
import com.eve.healthcare.domain.model.User;
import com.eve.healthcare.domain.repository.UserRepository;
import com.eve.healthcare.web.dto.request.LoginRequest;
import com.eve.healthcare.web.dto.request.SignupRequest;
import com.eve.healthcare.web.dto.response.AuthResponse;
import com.eve.healthcare.web.dto.response.UserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already registered");
        }

        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword())
        );
        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getId(), savedUser.getEmail());
        return new AuthResponse(UserResponse.fromUser(savedUser), token);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(UserResponse.fromUser(user), token);
    }
}
