package com.teacher.agent.dto;

import java.time.LocalDate;

public record DailyUsageResponse(
    LocalDate date,
    long generations,
    long copies,
    long likes,
    long regenerations) {
}
