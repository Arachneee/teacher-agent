package com.teacher.agent.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Feedback extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(columnDefinition = "TEXT")
    private String aiContent;

    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<FeedbackKeyword> keywords = new ArrayList<>();

    public static Feedback create(Long studentId) {
        Feedback feedback = new Feedback();
        feedback.studentId = studentId;
        return feedback;
    }

    public void addKeyword(String keyword) {
        keywords.add(FeedbackKeyword.create(this, keyword));
    }

    public void updateAiContent(String aiContent) {
        this.aiContent = aiContent;
    }

    public void removeKeyword(Long keywordId) {
        boolean removed = keywords.removeIf(feedbackKeyword -> feedbackKeyword.getId().equals(keywordId));
        if (!removed) {
            throw new IllegalArgumentException("FeedbackKeyword not found: " + keywordId);
        }
    }
}
