package com.teacher.agent.domain;

import static com.teacher.agent.util.Parameter.AI_CONTENT;
import static com.teacher.agent.util.Parameter.LESSON_ID;
import static com.teacher.agent.util.Parameter.STUDENT_ID;
import static com.teacher.agent.util.ValidationUtil.checkNotBlank;
import static com.teacher.agent.util.ValidationUtil.checkPositive;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

  public void addKeyword(String keyword) {
    keywords.add(FeedbackKeyword.create(this, keyword));
  }

  public void updateAiContent(String aiContent) {
    this.aiContent = checkNotBlank(aiContent, AI_CONTENT);
    this.liked = false;
  }

  public void clearAiContent() {
    this.aiContent = null;
    this.liked = false;
  }

  public void removeKeyword(Long keywordId) {
    boolean removed =
        keywords.removeIf(feedbackKeyword -> feedbackKeyword.getId().equals(keywordId));
    if (!removed) {
      throw new IllegalArgumentException("FeedbackKeyword not found: " + keywordId);
    }
  }

  public void like() {
    if (aiContent == null || aiContent.isBlank()) {
      throw new IllegalStateException("AI 콘텐츠가 없으면 좋아요를 할 수 없습니다.");
    }
    if (liked) {
      throw new IllegalStateException("이미 좋아요를 누른 상태입니다.");
    }
    this.liked = true;
  }

  public String buildKeywordsSnapshot() {
    return keywords.stream().map(FeedbackKeyword::getKeyword).reduce((a, b) -> a + "," + b)
        .orElse("");
  }
}
