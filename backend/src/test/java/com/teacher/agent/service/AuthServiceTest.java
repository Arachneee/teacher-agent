package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.AuthResponse;
import com.teacher.agent.dto.LoginRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock
  private AuthenticationManager authenticationManager;

  @Mock
  private TeacherRepository teacherRepository;

  private AuthService authService;

  @BeforeEach
  void setUp() {
    authService = new AuthService(authenticationManager, teacherRepository);
  }

  @Test
  void 로그인에_성공한다() {
    HttpServletRequest httpRequest = mock(HttpServletRequest.class);
    HttpServletResponse httpResponse = mock(HttpServletResponse.class);
    LoginRequest request = new LoginRequest("teacher1", "password");
    Authentication authentication = mock(Authentication.class);
    given(authentication.getName()).willReturn("teacher1");
    given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .willReturn(authentication);

    Teacher teacher = Teacher.create("teacher1", "encoded", "김선생", "수학");
    given(teacherRepository.findByUserId(new UserId("teacher1")))
        .willReturn(Optional.of(teacher));

    AuthResponse response = authService.login(request, httpRequest, httpResponse);

    assertThat(response.userId()).isEqualTo("teacher1");
    assertThat(response.name()).isEqualTo("김선생");
    assertThat(response.subject()).isEqualTo("수학");
  }

  @Test
  void 잘못된_비밀번호로_로그인_시_예외가_발생한다() {
    HttpServletRequest httpRequest = mock(HttpServletRequest.class);
    HttpServletResponse httpResponse = mock(HttpServletResponse.class);
    LoginRequest request = new LoginRequest("teacher1", "wrongPassword");
    given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .willThrow(new BadCredentialsException("Bad credentials"));

    assertThatThrownBy(() -> authService.login(request, httpRequest, httpResponse))
        .isInstanceOf(BadCredentialsException.class);
  }

  @Test
  void 존재하지_않는_교사로_로그인_성공_시_기본_응답을_반환한다() {
    HttpServletRequest httpRequest = mock(HttpServletRequest.class);
    HttpServletResponse httpResponse = mock(HttpServletResponse.class);
    LoginRequest request = new LoginRequest("unknown", "password");
    Authentication authentication = mock(Authentication.class);
    given(authentication.getName()).willReturn("unknown");
    given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .willReturn(authentication);
    given(teacherRepository.findByUserId(new UserId("unknown")))
        .willReturn(Optional.empty());

    AuthResponse response = authService.login(request, httpRequest, httpResponse);

    assertThat(response.userId()).isEqualTo("unknown");
    assertThat(response.name()).isNull();
    assertThat(response.subject()).isNull();
  }

  @Test
  void 로그아웃에_성공한다() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    given(request.getSession(false)).willReturn(session);

    authService.logout(request);

    verify(session).invalidate();
  }

  @Test
  void 세션이_없어도_로그아웃에_성공한다() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    given(request.getSession(false)).willReturn(null);

    authService.logout(request);
  }

  @Test
  void 인증된_사용자_정보를_반환한다() {
    Authentication authentication = mock(Authentication.class);
    given(authentication.isAuthenticated()).willReturn(true);
    given(authentication.getName()).willReturn("teacher1");

    Teacher teacher = Teacher.create("teacher1", "encoded", "김선생", "수학");
    given(teacherRepository.findByUserId(new UserId("teacher1")))
        .willReturn(Optional.of(teacher));

    Optional<AuthResponse> response = authService.getCurrentUser(authentication);

    assertThat(response).isPresent();
    assertThat(response.get().userId()).isEqualTo("teacher1");
    assertThat(response.get().name()).isEqualTo("김선생");
  }

  @Test
  void 인증되지_않은_경우_빈_Optional을_반환한다() {
    Authentication authentication = mock(Authentication.class);
    given(authentication.isAuthenticated()).willReturn(false);

    Optional<AuthResponse> response = authService.getCurrentUser(authentication);

    assertThat(response).isEmpty();
  }

  @Test
  void authentication이_null이면_빈_Optional을_반환한다() {
    Optional<AuthResponse> response = authService.getCurrentUser(null);

    assertThat(response).isEmpty();
  }
}
