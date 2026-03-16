package com.teacher.agent.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentUpdateRequest(@NotBlank String name,@Size(max=500)String memo){}
