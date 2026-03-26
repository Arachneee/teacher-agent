package com.teacher.agent.util;

import lombok.experimental.UtilityClass;
import org.springframework.ai.chat.metadata.Usage;

@UtilityClass
public class UsageTokenExtractor {

  public static Integer extractPromptTokens(Usage usage) {
    if (usage == null || usage.getPromptTokens() == null) {
      return null;
    }
    return usage.getPromptTokens().intValue();
  }

  public static Integer extractCompletionTokens(Usage usage) {
    if (usage == null || usage.getCompletionTokens() == null) {
      return null;
    }
    return usage.getCompletionTokens().intValue();
  }
}
