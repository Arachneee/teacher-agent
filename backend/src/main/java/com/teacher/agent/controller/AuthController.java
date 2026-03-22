package com.teacher.agent.controller;

import com.teacher.agent.dto.AuthResponse;
import com.teacher.agent.dto.LoginRequest;
import com.teacher.agent.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request,
      HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
    return ResponseEntity
        .ok(authService.login(request.userId(), request.password(), httpRequest, httpResponse));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletRequest request) {
    authService.logout(request);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  public ResponseEntity<AuthResponse> me(Authentication authentication) {
    return authService.getCurrentUser(authentication)
        .map(ResponseEntity::ok)
        .orElseGet(() -> ResponseEntity.status(401).build());
  }
}
