package com.teacher.agent.domain;

import static com.teacher.agent.util.Parameter.*;
import static com.teacher.agent.util.ValidationUtil.*;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = @Index(name = "idx_feedback_like_feedback_id_id", columnList = "feedback_id, id"))
public class FeedbackLike extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long feedbackId;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String aiContentSnapshot;

  @Column(columnDefinition = "TEXT", nullable = false)
  private String keywordsSnapshot;

  public static FeedbackLike create(Long feedbackId, String aiContentSnapshot,
      String keywordsSnapshot) {
    FeedbackLike feedbackLike = new FeedbackLike();

    feedbackLike.feedbackId = checkPositive(feedbackId, FEEDBACK_ID);
    feedbackLike.aiContentSnapshot = checkNotBlank(aiContentSnapshot, AI_CONTENT_SNAPSHOT);
    feedbackLike.keywordsSnapshot = checkNotNull(keywordsSnapshot, KEYWORDS_SNAPSHOT);

    return feedbackLike;
  }
}
