'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import {
  Feedback,
  addKeyword,
  createFeedback,
  generateAiContent,
  getFeedbacks,
  removeKeyword,
} from '../lib/api';

export function useFeedback(studentId: number) {
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const [aiGenerating, setAiGenerating] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const keywordSubmittingRef = useRef(false);

  const loadLatestFeedback = useCallback(async (): Promise<Feedback | null> => {
    const feedbacks = await getFeedbacks(studentId);
    return feedbacks.length > 0 ? feedbacks[0] : null;
  }, [studentId]);

  useEffect(() => {
    loadLatestFeedback().then(setFeedback).catch(console.error);
  }, [loadLatestFeedback]);

  const handleAddKeyword = async (keyword: string): Promise<boolean> => {
    if (!keyword || keywordSubmittingRef.current) return false;
    keywordSubmittingRef.current = true;
    setErrorMessage(null);
    try {
      let feedbackId = feedback?.id;
      if (feedbackId === undefined) {
        const created = await createFeedback(studentId);
        feedbackId = created.id;
      }
      await addKeyword(feedbackId, keyword);
      setFeedback(await loadLatestFeedback());
      return true;
    } catch (error) {
      setErrorMessage('키워드를 추가하지 못했어요');
      return false;
    } finally {
      keywordSubmittingRef.current = false;
    }
  };

  const handleRemoveKeyword = async (keywordId: number) => {
    if (!feedback) return;
    const previousFeedback = feedback;
    setFeedback(prev =>
      prev ? { ...prev, keywords: prev.keywords.filter(keyword => keyword.id !== keywordId) } : null
    );
    setErrorMessage(null);
    try {
      await removeKeyword(feedback.id, keywordId);
      setFeedback(await loadLatestFeedback());
    } catch (error) {
      setErrorMessage('키워드를 삭제하지 못했어요');
      setFeedback(previousFeedback);
    }
  };

  const handleGenerate = async () => {
    if (!feedback || feedback.keywords.length === 0 || aiGenerating) return;
    setAiGenerating(true);
    setErrorMessage(null);
    try {
      const updated = await generateAiContent(feedback.id);
      setFeedback(updated);
    } catch (error) {
      setErrorMessage('AI 문자를 생성하지 못했어요');
    } finally {
      setAiGenerating(false);
    }
  };

  return { feedback, aiGenerating, errorMessage, handleAddKeyword, handleRemoveKeyword, handleGenerate };
}
