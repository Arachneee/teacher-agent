package com.teacher.agent.service;

import static com.teacher.agent.util.UsageTokenExtractor.extractCompletionTokens;
import static com.teacher.agent.util.UsageTokenExtractor.extractPromptTokens;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.FeedbackLike;
import com.teacher.agent.domain.Student;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class FeedbackAiService {

  private final ChatClient chatClient;
  private final FeedbackPromptBuilder feedbackPromptBuilder;
  private final AiGenerationLogCommandService aiGenerationLogCommandService;

  public FeedbackAiService(ChatClient chatClient, FeedbackPromptBuilder feedbackPromptBuilder,
      AiGenerationLogCommandService aiGenerationLogCommandService) {
    this.chatClient = chatClient;
    this.feedbackPromptBuilder = feedbackPromptBuilder;
    this.aiGenerationLogCommandService = aiGenerationLogCommandService;
  }

  public String generateFeedbackContent(Feedback feedback, Student student, String lessonTitle,
      String subject, List<FeedbackLike> likedExamples, String instruction) {
    String promptContent =
        feedbackPromptBuilder.build(feedback, student, lessonTitle, subject, likedExamples,
            instruction);
    long startTime = System.currentTimeMillis();

    ChatResponse chatResponse = chatClient.prompt(promptContent).call().chatResponse();
    long durationMs = System.currentTimeMillis() - startTime;

    String completionContent = chatResponse.getResult().getOutput().getText();
    Usage usage = chatResponse.getMetadata().getUsage();

    aiGenerationLogCommandService.save(feedback.getId(), promptContent, completionContent,
        durationMs, false, extractPromptTokens(usage), extractCompletionTokens(usage));

    return completionContent;
  }

  public Flux<String> streamFeedbackContent(Feedback feedback, Student student, String lessonTitle,
      String subject, List<FeedbackLike> likedExamples, String instruction) {
    String promptContent =
        feedbackPromptBuilder.build(feedback, student, lessonTitle, subject, likedExamples,
            instruction);
    long startTime = System.currentTimeMillis();
    StringBuilder accumulatedContent = new StringBuilder(512);
    AtomicReference<Integer> capturedPromptTokens = new AtomicReference<>(null);
    AtomicReference<Integer> capturedCompletionTokens = new AtomicReference<>(null);

    return chatClient.prompt(promptContent).stream().chatResponse()
        .map(chatResponse -> {
          if (chatResponse.getResult() == null) {
            return "";
          }
          String chunk = chatResponse.getResult().getOutput().getText();
          if (chunk == null) {
            return "";
          }
          accumulatedContent.append(chunk);

          Usage usage = chatResponse.getMetadata().getUsage();
          if (usage != null && usage.getTotalTokens() != null && usage.getTotalTokens() > 0) {
            capturedPromptTokens.set(extractPromptTokens(usage));
            capturedCompletionTokens.set(extractCompletionTokens(usage));
          }

          return chunk;
        })
        .doOnComplete(() -> {
          long durationMs = System.currentTimeMillis() - startTime;
          aiGenerationLogCommandService.save(feedback.getId(), promptContent,
              accumulatedContent.toString(), durationMs, true,
              capturedPromptTokens.get(), capturedCompletionTokens.get());
        });
  }
}
