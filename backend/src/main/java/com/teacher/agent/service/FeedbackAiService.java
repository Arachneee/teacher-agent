package com.teacher.agent.service;

import static com.teacher.agent.util.ErrorMessages.PROMPT_FILE_READ_ERROR;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.FeedbackKeyword;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class FeedbackAiService {

  private final ChatClient chatClient;
  private final String feedbackMessagePrompt;

  public FeedbackAiService(ChatClient chatClient,
      @Value("classpath:prompts/feedback_message.md") Resource feedbackMessagePromptResource) {
    this.chatClient = chatClient;

    try {
      this.feedbackMessagePrompt =
          feedbackMessagePromptResource.getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException(
          PROMPT_FILE_READ_ERROR + feedbackMessagePromptResource.getFilename(), e);
    }
  }

  public String generateFeedbackContent(Feedback feedback, String studentName) {
    List<FeedbackKeyword> keywords = feedback.getKeywords();

    String normalKeywordText = keywords.stream()
        .filter(keyword -> !keyword.isRequired())
        .map(FeedbackKeyword::getKeyword)
        .collect(Collectors.joining(", "));

    List<String> requiredKeywords = keywords.stream()
        .filter(FeedbackKeyword::isRequired)
        .map(FeedbackKeyword::getKeyword)
        .toList();

    String prompt = feedbackMessagePrompt.formatted(studentName, normalKeywordText);

    if (!requiredKeywords.isEmpty()) {
      prompt += buildRequiredKeywordsSection(requiredKeywords);
    }

    return chatClient.prompt(prompt).call().content();
  }

  private String buildRequiredKeywordsSection(List<String> requiredKeywords) {
    String items = requiredKeywords.stream()
        .map(keyword -> "- " + keyword)
        .collect(Collectors.joining("\n"));
    return "\n\n## 반드시 원문 그대로 포함할 문장 (한 글자도 변경 금지)\n\n" + items;
  }
}
