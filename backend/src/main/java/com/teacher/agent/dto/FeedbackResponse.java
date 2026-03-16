package com.teacher.agent.dto;

import com.teacher.agent.domain.Feedback;
import com.teacher.agent.domain.FeedbackKeyword;

import java.time.LocalDateTime;
import java.util.List;

public record FeedbackResponse(
        Long id,
        Long studentId,
        String aiContent,
        List<KeywordItem> keywords,
        boolean liked,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public static FeedbackResponse withKeywords(Feedback feedback, boolean liked) {
        List<KeywordItem> keywordItems = feedback.getKeywords().stream()
                .map(KeywordItem::from)
                .toList();
        return new FeedbackResponse(
                feedback.getId(),
                feedback.getStudentId(),
                feedback.getAiContent(),
                keywordItems,
                liked,
                feedback.getCreatedAt(),
                feedback.getUpdatedAt()
        );
    }

    public record KeywordItem(Long id, String keyword, LocalDateTime createdAt) {

        public static KeywordItem from(FeedbackKeyword feedbackKeyword) {
            return new KeywordItem(
                    feedbackKeyword.getId(),
                    feedbackKeyword.getKeyword(),
                    feedbackKeyword.getCreatedAt()
            );
        }
    }
}
