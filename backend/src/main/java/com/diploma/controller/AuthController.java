package com.diploma.controller;

import com.diploma.dto.JwtResponse;
import com.diploma.dto.SignInRequest;
import com.diploma.entities.User;
import com.diploma.service.auth.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final int TOKEN_TTL_SECONDS = 24 * 60 * 60;

    private final AuthService authService;

    @PostMapping("/sign-in")
    public ResponseEntity<JwtResponse> signIn(@RequestBody SignInRequest request, HttpServletResponse response) {
        JwtResponse jwtResponse = authService.login(request);
        Cookie cookie = new Cookie("jwt", jwtResponse.getToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(TOKEN_TTL_SECONDS);
        response.addCookie(cookie);

        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/protected-endpoint")
    public ResponseEntity<Void> protectedEndpoint(Authentication authentication) {
        return isAuthenticatedUser(authentication)
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(401).build();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        if (!isAuthenticatedUser(authentication)) {
            return ResponseEntity.status(401).build();
        }

        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "fullName", user.getFullName() != null ? user.getFullName() : user.getUsername(),
                "role", user.getRole().name()
        ));
    }

    @GetMapping("/check-admin")
    public ResponseEntity<Void> checkAdmin(Authentication authentication) {
        if (!isAuthenticatedUser(authentication)) {
            return ResponseEntity.status(401).build();
        }
        boolean admin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ADMIN".equals(authority.getAuthority()));

        return admin ? ResponseEntity.ok().build() : ResponseEntity.status(403).build();
    }

    private boolean isAuthenticatedUser(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof User;
    }
}
