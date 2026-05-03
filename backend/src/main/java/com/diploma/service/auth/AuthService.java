package com.diploma.service.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import com.diploma.repository.UserRepository;
import com.diploma.entities.User;
import com.diploma.dto.JwtResponse;
import com.diploma.dto.SignInRequest;
import com.diploma.service.security.JwtService;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;

    public JwtResponse login(SignInRequest request) {
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        User user = userRepository.findByUsername(request.getUsername()).get();
        String token = jwtService.generateToken(user);
        return new JwtResponse(token);
    }
}