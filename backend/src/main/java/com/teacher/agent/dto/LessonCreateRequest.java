package com.teacher.agent.dto;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.UserId;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record LessonCreateRequest(@NotBlank String title,@NotNull LocalDateTime startTime,@NotNull LocalDateTime endTime){

public Lesson toEntity(UserId userId){return Lesson.create(userId,title,startTime,endTime);}}
