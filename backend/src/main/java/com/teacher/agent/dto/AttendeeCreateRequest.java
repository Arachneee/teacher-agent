package com.teacher.agent.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AttendeeCreateRequest(@NotNull @Positive Long studentId){}
