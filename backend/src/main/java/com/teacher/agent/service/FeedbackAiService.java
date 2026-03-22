package com.teacher.agent.service;

import static com.teacher.agent.util.ErrorMessages.PROMPT_FILE_READ_ERROR;

import com.teacher.agent.config.OpenAiLoggingAdvisor;
import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.FeedbackKeyword;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class FeedbackAiService {

  private final ChatClient chatClient;
  private final String feedbackMessagePrompt;

  public FeedbackAiService(ChatClient.Builder chatClientBuilder,
      @Value("classpath:prompts/feedback_message.md") Resource feedbackMessagePromptResource,
      OpenAiLoggingAdvisor openAiLoggingAdvisor) {
    this.chatClient = chatClientBuilder.defaultAdvisors(openAiLoggingAdvisor).build();

    try {
      this.feedbackMessagePrompt =
          feedbackMessagePromptResource.getContentAsString(StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalStateException(
          PROMPT_FILE_READ_ERROR + feedbackMessagePromptResource.getFilename(), e);
    }
  }

  public String generateFeedbackContent(Feedback feedback, String studentName) {
    String keywordText = feedback.getKeywords().stream().map(FeedbackKeyword::getKeyword)
        .collect(Collectors.joining(", "));

    return chatClient.prompt(feedbackMessagePrompt.formatted(studentName, keywordText))
        .call()
        .content();
  }
}
