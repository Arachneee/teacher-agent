'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import {
  Feedback,
  addKeyword,
  createFeedback,
  generateAiContent,
  getFeedbacks,
  likeFeedback,
  removeKeyword,
  updateFeedback,
} from '../lib/api';

export function useFeedback(studentId: number) {
  const [feedback, setFeedback] = useState<Feedback | null>(null);
  const [aiGenerating, setAiGenerating] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isEditingAiContent, setIsEditingAiContent] = useState(false);
  const keywordSubmittingRef = useRef(false);
  const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const feedbackIdRef = useRef<number | null>(null);

  const loadLatestFeedback = useCallback(async (): Promise<Feedback | null> => {
    const feedbacks = await getFeedbacks(studentId);
    return feedbacks.length > 0 ? feedbacks[0] : null;
  }, [studentId]);

  useEffect(() => {
    loadLatestFeedback().then((loaded) => {
      setFeedback(loaded);
      feedbackIdRef.current = loaded?.id ?? null;
    }).catch(console.error);
  }, [loadLatestFeedback]);

  useEffect(() => {
    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
    };
  }, []);

  const handleUpdateAiContent = (content: string) => {
    setFeedback(prev => prev ? { ...prev, aiContent: content || null, liked: false } : null);
    setIsEditingAiContent(true);

    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    debounceTimerRef.current = setTimeout(async () => {
      debounceTimerRef.current = null;
      const feedbackId = feedbackIdRef.current;
      if (feedbackId === null) return;
      try {
        await updateFeedback(feedbackId, content);
      } catch {
        // 조용히 실패
      } finally {
        setIsEditingAiContent(false);
      }
    }, 1000);
  };

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
      const loaded = await loadLatestFeedback();
      const mergedFeedback = loaded && debounceTimerRef.current !== null
        ? { ...loaded, aiContent: feedback?.aiContent ?? loaded.aiContent }
        : loaded;
      setFeedback(mergedFeedback);
      feedbackIdRef.current = mergedFeedback?.id ?? null;
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
      await generateAiContent(feedback.id);
      setFeedback(await loadLatestFeedback());
    } catch (error) {
      setErrorMessage('AI 문자를 생성하지 못했어요');
    } finally {
      setAiGenerating(false);
    }
  };

  const handleLike = async () => {
    if (!feedback || feedback.liked) return;
    try {
      await likeFeedback(feedback.id);
      const loaded = await loadLatestFeedback();
      const mergedFeedback = loaded && debounceTimerRef.current !== null
        ? { ...loaded, aiContent: feedback?.aiContent ?? loaded.aiContent }
        : loaded;
      setFeedback(mergedFeedback);
      feedbackIdRef.current = mergedFeedback?.id ?? null;
    } catch {
      setErrorMessage('좋아요 처리에 실패했어요');
    }
  };

  return { feedback, aiGenerating, isEditingAiContent, errorMessage, handleAddKeyword, handleRemoveKeyword, handleGenerate, handleUpdateAiContent, handleLike };
}
