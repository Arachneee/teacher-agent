package com.teacher.agent.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OpenAiLoggingAdvisor implements CallAdvisor {

  @Override
  public String getName() {
    return "OpenAiLoggingAdvisor";
  }

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
    log.info("OpenAI 요청 시작: model={}", request.prompt().getOptions().getModel());

    long start = System.currentTimeMillis();
    ChatClientResponse response = chain.nextCall(request);
    long duration = System.currentTimeMillis() - start;

    var usage = response.chatResponse().getMetadata().getUsage();
    log.info("OpenAI 응답 완료: {}ms | inputTokens={} | outputTokens={}",
        duration, usage.getPromptTokens(), usage.getCompletionTokens());

    return response;
  }
}
