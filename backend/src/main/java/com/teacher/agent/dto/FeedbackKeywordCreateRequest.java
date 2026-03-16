package com.teacher.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FeedbackKeywordCreateRequest(@NotBlank @Size(max=100)String keyword){}
