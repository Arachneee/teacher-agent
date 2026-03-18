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
public class FeedbackKeyword extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(nullable = false)
  private Feedback feedback;

  @Column(nullable = false)
  private String keyword;

  public static FeedbackKeyword create(Feedback feedback, String keyword) {
    FeedbackKeyword feedbackKeyword = new FeedbackKeyword();
    feedbackKeyword.feedback = checkNotNull(feedback, "feedback");
    feedbackKeyword.keyword = checkNotBlank(checkMaxLength(keyword, 100, KEYWORD), KEYWORD);

    return feedbackKeyword;
  }

  public void update(String newKeyword) {
    this.keyword = checkNotBlank(checkMaxLength(newKeyword, 100, KEYWORD), KEYWORD);
  }
}
