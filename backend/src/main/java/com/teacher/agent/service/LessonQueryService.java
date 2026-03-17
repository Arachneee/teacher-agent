package com.teacher.agent.service;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.LessonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.teacher.agent.util.RepositoryUtil.findLessonByIdAndUserIdOrThrow;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonQueryService {

  private final LessonRepository lessonRepository;

  public List<LessonResponse> getAllByTeacher(UserId userId) {
    return lessonRepository.findAllByUserId(userId).stream().map(LessonResponse::from).toList();
  }

  public LessonResponse getOne(UserId userId, Long id) {
    return LessonResponse.from(findByIdAndVerifyOwner(id, userId));
  }

  Lesson findByIdAndVerifyOwner(Long id, UserId userId) {
    return findLessonByIdAndUserIdOrThrow(lessonRepository, id, userId);
  }
}
