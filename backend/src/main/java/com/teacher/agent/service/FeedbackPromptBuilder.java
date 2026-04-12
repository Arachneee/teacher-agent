package com.teacher.agent.service;

import static com.teacher.agent.util.ErrorMessages.PROMPT_FILE_READ_ERROR;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.FeedbackKeyword;
import com.teacher.agent.domain.FeedbackLike;
import com.teacher.agent.domain.Student;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class FeedbackPromptBuilder {

  private final String feedbackMessagePrompt;

  public FeedbackPromptBuilder(
      @Value("classpath:prompts/feedback_message.md") Resource feedbackMessagePromptResource) {
    try {
      this.feedbackMessagePrompt =
          feedbackMessagePromptResource.getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException(
          PROMPT_FILE_READ_ERROR + feedbackMessagePromptResource.getFilename(), e);
    }
  }

  public String build(Feedback feedback, Student student, String subject,
      List<FeedbackLike> likedExamples, String instruction) {
    Map<Boolean, List<FeedbackKeyword>> partitioned = feedback.getKeywords().stream()
        .collect(Collectors.partitioningBy(FeedbackKeyword::isRequired));

    return feedbackMessagePrompt
        .replace("{student_name}", extractFirstName(student.getName()))
        .replace("{grade}", student.getGrade().displayName())
        .replace("{subject}", subject != null && !subject.isBlank() ? subject : "")
        .replace("{keywords}", resolveNormalKeywords(partitioned.get(false)))
        .replace("{required_keywords}", resolveRequiredKeywords(partitioned.get(true)))
        .replace("{previous_content}", resolvePreviousContent(feedback))
        .replace("{liked_examples}", resolveLikedExamples(likedExamples))
        .replace("{instruction}", resolveInstruction(instruction));
  }

  private String resolveNormalKeywords(List<FeedbackKeyword> keywords) {
    String text = keywords.stream()
        .map(FeedbackKeyword::getKeyword)
        .collect(Collectors.joining(", "));
    return text.isBlank() ? "없음" : text;
  }

  private String resolveRequiredKeywords(List<FeedbackKeyword> keywords) {
    return keywords.isEmpty()
        ? "없음"
        : keywords.stream()
            .map(keyword -> "- " + keyword.getKeyword())
            .collect(Collectors.joining("\n"));
  }

  private String resolvePreviousContent(Feedback feedback) {
    String previousContent = feedback.getAiContent();
    return (previousContent != null && !previousContent.isBlank()) ? previousContent : "없음";
  }

  private String resolveInstruction(String instruction) {
    return (instruction != null && !instruction.isBlank()) ? instruction : "없음";
  }

  private String extractFirstName(String fullName) {
    if (fullName == null || fullName.length() < 3) {
      return fullName;
    }
    return fullName.substring(1);
  }

  private String resolveLikedExamples(List<FeedbackLike> likedExamples) {
    if (likedExamples == null || likedExamples.isEmpty()) {
      return "없음";
    }
    return likedExamples.stream()
        .map(like -> "키워드: " + like.getKeywordsSnapshot() + "\n문자: "
            + like.getAiContentSnapshot())
        .collect(Collectors.joining("\n\n"));
  }
}
