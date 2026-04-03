package com.teacher.agent.controller;

import com.teacher.agent.domain.vo.UserId;
import com.teacher.agent.dto.DailyUsageResponse;
import com.teacher.agent.dto.TopKeywordResponse;
import com.teacher.agent.dto.UsageSummaryResponse;
import com.teacher.agent.service.UsageQueryService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/usage")
@RequiredArgsConstructor
@Validated
public class UsageController {

  private final UsageQueryService usageQueryService;

  @GetMapping("/summary")
  public ResponseEntity<UsageSummaryResponse> getSummary(UserId userId) {
    return ResponseEntity.ok(usageQueryService.getUsageSummary(userId));
  }

  @GetMapping("/daily")
  public ResponseEntity<List<DailyUsageResponse>> getDailyUsage(
      UserId userId, @RequestParam(defaultValue = "30") @Min(1) @Max(90) int days) {
    return ResponseEntity.ok(usageQueryService.getDailyUsage(days, userId));
  }

  @GetMapping("/keywords/top")
  public ResponseEntity<List<TopKeywordResponse>> getTopKeywords(
      UserId userId, @RequestParam(defaultValue = "20") @Min(1) @Max(50) int limit) {
    return ResponseEntity.ok(usageQueryService.getTopKeywords(limit, userId));
  }
}
