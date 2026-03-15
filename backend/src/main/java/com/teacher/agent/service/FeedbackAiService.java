package com.teacher.agent.service;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.FeedbackKeyword;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedbackAiService {

    private final ChatClient.Builder chatClientBuilder;

    public String generateFeedbackContent(Feedback feedback, String studentName) {
        String keywordText = feedback.getKeywords().stream()
                .map(FeedbackKeyword::getKeyword)
                .collect(Collectors.joining(", "));

        String prompt = """
                당신은 학생의 학습 상황을 학부모에게 전달하는 선생님입니다.
                아래 키워드들을 바탕으로 학부모에게 보낼 따뜻하고 구체적인 피드백 문자 메시지를 작성해주세요.

                학생 이름: %s
                키워드: %s

                규칙:
                - 학부모에게 보내는 문자이므로 존댓말을 사용하세요.
                - 200자 이내로 작성하세요.
                - 키워드의 내용을 자연스럽게 녹여서 작성하세요.
                - 학생의 긍정적인 면을 강조하고, 개선이 필요한 부분은 부드럽게 전달하세요.
                - 가정에서의 협조를 부탁하는 내용을 포함할 수 있습니다.
                """.formatted(studentName, keywordText);

        return chatClientBuilder.build()
                .prompt(prompt)
                .call()
                .content();
    }
}
