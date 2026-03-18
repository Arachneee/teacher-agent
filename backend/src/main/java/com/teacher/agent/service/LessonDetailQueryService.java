package com.teacher.agent.service;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.teacher.agent.domain.Lesson;
import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.FeedbackResponse;
import com.teacher.agent.dto.LessonDetailResponse;
import com.teacher.agent.dto.LessonDetailRow;
import com.teacher.agent.dto.StudentResponse;
import java.util.LinkedHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LessonDetailQueryService {

  private final LessonQueryService lessonQueryService;
  private final LessonRepository lessonRepository;

  public LessonDetailResponse getDetail(UserId userId, Long lessonId) {
    Lesson lesson = lessonQueryService.findByIdAndVerifyOwner(lessonId, userId);
    List<LessonDetailRow> rows = lessonRepository.findDetailRows(lessonId, lesson.getUserId());
    List<LessonDetailResponse.AttendeeDetailItem> attendees = aggregateRows(rows);
    return LessonDetailResponse.from(lesson, attendees);
  }

  private List<LessonDetailResponse.AttendeeDetailItem> aggregateRows(List<LessonDetailRow> rows) {
    return rows.stream()
        .collect(groupingBy(LessonDetailRow::attendeeId, LinkedHashMap::new, toList())).entrySet()
        .stream().map(entry -> toAttendeeDetailItem(entry.getKey(), entry.getValue())).toList();
  }

  private LessonDetailResponse.AttendeeDetailItem toAttendeeDetailItem(Long attendeeId,
      List<LessonDetailRow> rows) {
    LessonDetailRow first = rows.getFirst();

    StudentResponse student = new StudentResponse(first.studentId(), first.studentName(),
        first.studentMemo(), first.studentCreatedAt(), first.studentUpdatedAt());

    List<FeedbackResponse.KeywordItem> keywords =
        rows.stream().filter(row -> row.keywordId() != null)
            .collect(toMap(LessonDetailRow::keywordId,
                row -> new FeedbackResponse.KeywordItem(row.keywordId(), row.keyword(),
                    row.keywordCreatedAt()),
                (existing, duplicate) -> existing, LinkedHashMap::new))
            .values().stream().toList();

    FeedbackResponse feedback = new FeedbackResponse(first.feedbackId(), first.feedbackStudentId(),
        first.feedbackLessonId(), first.aiContent(), keywords, first.liked(),
        first.feedbackCreatedAt(), first.feedbackUpdatedAt());

    return new LessonDetailResponse.AttendeeDetailItem(attendeeId, student, feedback);
  }
}
