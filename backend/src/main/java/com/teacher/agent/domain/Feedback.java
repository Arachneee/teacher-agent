package com.teacher.agent.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.teacher.agent.util.Parameter.AI_CONTENT;
import static com.teacher.agent.util.Parameter.LESSON_ID;
import static com.teacher.agent.util.Parameter.STUDENT_ID;
import static com.teacher.agent.util.ValidationUtil.checkNotBlank;
import static com.teacher.agent.util.ValidationUtil.checkPositive;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = @Index(name = "idx_feedback_student_id", columnList = "studentId"))
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

  @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<FeedbackKeyword> keywords = new ArrayList<>();

  @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, orphanRemoval = true,
      fetch = FetchType.LAZY)
  @OrderBy("id ASC")
  private List<FeedbackLike> likes = new ArrayList<>();

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
  }

  public void clearAiContent() {
    this.aiContent = null;
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
    if (isLiked()) {
      throw new IllegalStateException("이미 좋아요를 누른 상태입니다.");
    }
    String keywordsSnapshot =
        keywords.stream().map(FeedbackKeyword::getKeyword).reduce((a, b) -> a + "," + b).orElse("");
    likes.add(FeedbackLike.create(this, aiContent, keywordsSnapshot));
  }

  public boolean isLiked() {
    if (likes.isEmpty() || aiContent == null) {
      return false;
    }
    return likes.getLast().getAiContentSnapshot().equals(aiContent);
  }
}
