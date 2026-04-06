package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.Student;
import com.teacher.agent.domain.vo.SchoolGrade;
import com.teacher.agent.domain.vo.UserId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class FeedbackPromptBuilderTest {

  private FeedbackPromptBuilder feedbackPromptBuilder;
  private Feedback feedback;
  private Student student;

  @BeforeEach
  void setUp() {
    feedbackPromptBuilder =
        new FeedbackPromptBuilder(new ClassPathResource("prompts/feedback_message.md"));
    feedback = Feedback.create(1L, 1L);
    feedback.addKeyword("성실함", false);
    student = Student.create(new UserId("teacher1"), "홍길동", null, SchoolGrade.ELEMENTARY_1);
  }

  @Test
  void instruction이_있으면_프롬프트에_포함된다() {
    String prompt =
        feedbackPromptBuilder.build(feedback, student, "수학", "수학", List.of(), "더 따뜻하게");

    assertThat(prompt).contains("<instruction>더 따뜻하게</instruction>");
    assertThat(prompt).doesNotContain("<instruction>없음</instruction>");
  }

  @Test
  void instruction이_null이면_없음으로_치환된다() {
    String prompt =
        feedbackPromptBuilder.build(feedback, student, "수학", "수학", List.of(), null);

    assertThat(prompt).contains("<instruction>없음</instruction>");
  }

  @Test
  void instruction이_blank이면_없음으로_치환된다() {
    String prompt =
        feedbackPromptBuilder.build(feedback, student, "수학", "수학", List.of(), "   ");

    assertThat(prompt).contains("<instruction>없음</instruction>");
  }
}
