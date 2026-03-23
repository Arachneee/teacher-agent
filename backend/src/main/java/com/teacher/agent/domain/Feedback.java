package com.teacher.agent.domain;

import static com.teacher.agent.util.Parameter.AI_CONTENT;
import static com.teacher.agent.util.Parameter.LESSON_ID;
import static com.teacher.agent.util.Parameter.STUDENT_ID;
import static com.teacher.agent.util.ValidationUtil.checkNotBlank;
import static com.teacher.agent.util.ValidationUtil.checkPositive;

import com.teacher.agent.util.ErrorMessages;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(indexes = @Index(name = "idx_feedback_student_id", columnList = "studentId"),
    uniqueConstraints = @UniqueConstraint(name = "uk_feedback_student_lesson",
        columnNames = {"studentId", "lessonId"}))
public class Feedback extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long studentId;

  @Column(nullable = false)
  private Long lessonId;

  @Column(columnDefinition = "TEXT")
  private String aiContent;

  @Column(nullable = false)
  private boolean liked;

  @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<FeedbackKeyword> keywords = new ArrayList<>();

  public static Feedback create(Long studentId, Long lessonId) {
    Feedback feedback = new Feedback();

    feedback.studentId = checkPositive(studentId, STUDENT_ID);
    feedback.lessonId = checkPositive(lessonId, LESSON_ID);

    return feedback;
  }

  public static List<Feedback> createAll(List<Long> studentIds, Long lessonId) {
    return studentIds.stream()
        .map(studentId -> Feedback.create(studentId, lessonId))
        .toList();
  }

  public void addKeyword(String keyword, boolean required) {
    keywords.add(FeedbackKeyword.create(this, keyword, required));
  }

  public void removeKeyword(Long keywordId) {
    boolean removed =
        keywords.removeIf(feedbackKeyword -> feedbackKeyword.getId().equals(keywordId));
    if (!removed) {
      throw new IllegalArgumentException(ErrorMessages.FEEDBACK_KEYWORD_NOT_FOUND + keywordId);
    }
  }

  public void updateKeyword(Long keywordId, String newKeyword, boolean required) {
    FeedbackKeyword feedbackKeyword = keywords.stream()
        .filter(keyword -> keyword.getId().equals(keywordId))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException(
            ErrorMessages.FEEDBACK_KEYWORD_NOT_FOUND + keywordId));

    feedbackKeyword.update(newKeyword, required);
  }

  public void updateAiContent(String aiContent) {
    this.aiContent = checkNotBlank(aiContent, AI_CONTENT);
    this.liked = false;
  }

  public void clearAiContent() {
    this.aiContent = null;
    this.liked = false;
  }

  public void like() {
    if (aiContent == null || aiContent.isBlank()) {
      throw new IllegalStateException(ErrorMessages.AI_CONTENT_REQUIRED_FOR_LIKE);
    }

    if (liked) {
      throw new IllegalStateException(ErrorMessages.ALREADY_LIKED);
    }

    this.liked = true;
  }

  public String buildKeywordsSnapshot() {
    return keywords.stream().map(FeedbackKeyword::getKeyword).reduce((a, b) -> a + "," + b)
        .orElse("");
  }
}
