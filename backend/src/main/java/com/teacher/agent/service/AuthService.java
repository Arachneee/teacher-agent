package com.teacher.agent.service;

import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.AuthResponse;
import com.teacher.agent.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

  private final AuthenticationManager authenticationManager;
  private final TeacherRepository teacherRepository;
  private final SecurityContextRepository securityContextRepository =
      new HttpSessionSecurityContextRepository();

  public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest,
      HttpServletResponse httpResponse) {
    log.info("로그인 시도: userId={}", request.userId());

    UsernamePasswordAuthenticationToken authToken =
        new UsernamePasswordAuthenticationToken(request.userId(), request.password());
    Authentication authentication = authenticationManager.authenticate(authToken);

    SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
    securityContext.setAuthentication(authentication);
    SecurityContextHolder.setContext(securityContext);
    securityContextRepository.saveContext(securityContext, httpRequest, httpResponse);

    log.info("로그인 성공: userId={}", request.userId());
    return buildAuthResponse(authentication.getName());
  }

  public void logout(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    String userId = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
        .map(Authentication::getName)
        .orElse("unknown");

    if (session != null) {
      session.invalidate();
    }
    SecurityContextHolder.clearContext();

    log.info("로그아웃: userId={}", userId);
  }

  public Optional<AuthResponse> getCurrentUser(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      return Optional.empty();
    }
    return Optional.of(buildAuthResponse(authentication.getName()));
  }

  private AuthResponse buildAuthResponse(String userId) {
    return teacherRepository.findByUserId(new UserId(userId))
        .map(AuthResponse::from)
        .orElseGet(() -> new AuthResponse(userId, null, null));
  }
}
