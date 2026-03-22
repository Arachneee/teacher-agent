package com.teacher.agent.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class LoggingInterceptor implements HandlerInterceptor {

  private static final String START_TIME_ATTRIBUTE = "requestStartTime";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {
    long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
    long duration = System.currentTimeMillis() - startTime;

    log.info("[{} {}] {}ms | {}", request.getMethod(), request.getRequestURI(), duration,
        response.getStatus());
  }
}
