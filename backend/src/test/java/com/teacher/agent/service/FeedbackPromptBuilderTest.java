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
  void instruction이_한_개이면_단일_텍스트로_포함된다() {
    String prompt =
        feedbackPromptBuilder.build(feedback, student, "수학", List.of(), List.of("더 따뜻하게"));

    assertThat(prompt).contains("더 따뜻하게");
  }

  @Test
  void instruction이_여러_개이면_번호_목록으로_모두_포함된다() {
    String prompt =
        feedbackPromptBuilder.build(feedback, student, "수학", List.of(),
            List.of("더 따뜻하게", "더 짧게"));

    assertThat(prompt).contains("1. 더 따뜻하게");
    assertThat(prompt).contains("2. 더 짧게");
  }

  @Test
  void instruction이_세_개이면_번호_목록으로_모두_포함된다() {
    String prompt =
        feedbackPromptBuilder.build(feedback, student, "수학", List.of(),
            List.of("더 따뜻하게", "더 짧게", "존댓말로"));

    assertThat(prompt).contains("1. 더 따뜻하게");
    assertThat(prompt).contains("2. 더 짧게");
    assertThat(prompt).contains("3. 존댓말로");
  }

  @Test
  void instruction이_빈_목록이면_없음으로_치환된다() {
    String prompt =
        feedbackPromptBuilder.build(feedback, student, "수학", List.of(), List.of());

    assertThat(prompt).contains("없음");
  }
}
