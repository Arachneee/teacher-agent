package com.teacher.agent.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;

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
    int status = response.getStatus();

    log.info("[{} {}] {}ms | {}", request.getMethod(), request.getRequestURI(), duration, status);

    if (status >= 400) {
      log.warn("[{} {}] 에러 요청 파라미터 | queryString={} | params={} | body={}",
          request.getMethod(), request.getRequestURI(),
          request.getQueryString(),
          formatParams(request.getParameterMap()),
          extractBody(request));
    }
  }

  private String formatParams(Map<String, String[]> parameterMap) {
    return parameterMap.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
        .collect(Collectors.joining(", "));
  }

  private String extractBody(HttpServletRequest request) {
    if (!(request instanceof ContentCachingRequestWrapper wrapper)) {
      return "(unavailable)";
    }
    byte[] content = wrapper.getContentAsByteArray();
    if (content.length == 0) {
      return "(empty)";
    }
    String body = new String(content, StandardCharsets.UTF_8);
    return maskSensitiveFields(body);
  }

  private String maskSensitiveFields(String body) {
    return body.replaceAll("(\"password\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3");
  }
}
