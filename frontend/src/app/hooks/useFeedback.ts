'use client';

import { useCallback, useEffect, useRef, useState } from 'react';
import {
  Feedback,
  addKeyword,
  createFeedback,
  generateAiContent,
  getFeedback,
  getFeedbacks,
  likeFeedback,
  removeKeyword,
  updateFeedback,
  updateKeyword,
} from '../lib/api';

export function useFeedback(studentId: number, initialFeedback?: Feedback | null) {
  const [feedback, setFeedback] = useState<Feedback | null>(initialFeedback ?? null);
  const [aiGenerating, setAiGenerating] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [isEditingAiContent, setIsEditingAiContent] = useState(false);
  const keywordSubmittingRef = useRef(false);
  const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const feedbackIdRef = useRef<number | null>(initialFeedback?.id ?? null);
  const skipInitialFetchRef = useRef(initialFeedback !== undefined);

  const reloadFeedback = useCallback(async (feedbackId: number): Promise<Feedback> => {
    return getFeedback(feedbackId);
  }, []);

  const loadLatestFeedback = useCallback(async (): Promise<Feedback | null> => {
    const feedbacks = await getFeedbacks(studentId);
    return feedbacks.length > 0 ? feedbacks[0] : null;
  }, [studentId]);

  useEffect(() => {
    if (skipInitialFetchRef.current) return;
    loadLatestFeedback().then((loaded) => {
      setFeedback(loaded);
      feedbackIdRef.current = loaded?.id ?? null;
    }).catch(console.error);
  }, [loadLatestFeedback]);

  // 부모가 re-fetch 후 새 prop을 내려줄 때 로컬 상태를 동기화
  // 단, 로컬에서 키워드를 추가/수정한 경우 로컬 상태를 유지
  useEffect(() => {
    if (!skipInitialFetchRef.current) return;
    setFeedback(prev => {
      // 로컬 상태가 더 최신인 경우 (키워드 수가 더 많거나 updatedAt이 더 최신) 동기화 건너뛰기
      if (prev && initialFeedback) {
        const localKeywordCount = prev.keywords?.length ?? 0;
        const initialKeywordCount = initialFeedback.keywords?.length ?? 0;
        if (localKeywordCount > initialKeywordCount) {
          return prev;
        }
        // updatedAt 비교: 로컬이 더 최신이면 유지
        if (prev.updatedAt && initialFeedback.updatedAt) {
          const localTime = new Date(prev.updatedAt).getTime();
          const initialTime = new Date(initialFeedback.updatedAt).getTime();
          if (localTime > initialTime) {
            return prev;
          }
        }
      }
      // 디바운스 타이머가 활성 중이면 사용자가 편집 중인 aiContent를 보존
      if (initialFeedback && debounceTimerRef.current !== null) {
        return { ...initialFeedback, aiContent: prev?.aiContent ?? initialFeedback.aiContent };
      }
      return initialFeedback ?? null;
    });
    feedbackIdRef.current = initialFeedback?.id ?? null;
  }, [initialFeedback]);

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
        const loaded = await reloadFeedback(feedbackId);
        setFeedback(prev => prev ? { ...loaded, aiContent: prev.aiContent } : loaded);
      } catch {
        // 조용히 실패
      } finally {
        setIsEditingAiContent(false);
      }
    }, 1000);
  };

  const handleAddKeyword = async (keyword: string, required = false): Promise<boolean> => {
    if (!keyword || keywordSubmittingRef.current) return false;
    keywordSubmittingRef.current = true;
    setErrorMessage(null);
    try {
      let feedbackId = feedback?.id;
      if (feedbackId === undefined) {
        const created = await createFeedback(studentId);
        feedbackId = created.id;
      }
      await addKeyword(feedbackId, keyword, required);
      const loaded = await reloadFeedback(feedbackId);
      const mergedFeedback = debounceTimerRef.current !== null
        ? { ...loaded, aiContent: feedback?.aiContent ?? loaded.aiContent }
        : loaded;
      setFeedback(mergedFeedback);
      feedbackIdRef.current = mergedFeedback.id;
      return true;
    } catch {
      setErrorMessage('키워드를 추가하지 못했어요');
      return false;
    } finally {
      keywordSubmittingRef.current = false;
    }
  };

  const handleUpdateKeyword = async (keywordId: number, newKeyword: string, required: boolean): Promise<boolean> => {
    if (!feedback) return false;
    if (!newKeyword.trim()) {
      await handleRemoveKeyword(keywordId);
      return true;
    }
    const previousFeedback = feedback;
    setFeedback(prev =>
      prev ? {
        ...prev,
        keywords: prev.keywords.map(keyword =>
          keyword.id === keywordId ? { ...keyword, keyword: newKeyword, required } : keyword
        ),
      } : null
    );
    setErrorMessage(null);
    try {
      await updateKeyword(feedback.id, keywordId, newKeyword, required);
      const loaded = await reloadFeedback(feedback.id);
      setFeedback(loaded);
      feedbackIdRef.current = loaded.id;
      return true;
    } catch {
      setErrorMessage('키워드를 수정하지 못했어요');
      setFeedback(previousFeedback);
      return false;
    }
  };

  const handleToggleKeywordRequired = async (keywordId: number) => {
    if (!feedback) return;
    const target = feedback.keywords.find(keyword => keyword.id === keywordId);
    if (!target) return;
    const newRequired = !target.required;
    const previousFeedback = feedback;
    setFeedback(prev =>
      prev ? {
        ...prev,
        keywords: prev.keywords.map(keyword =>
          keyword.id === keywordId ? { ...keyword, required: newRequired } : keyword
        ),
      } : null
    );
    try {
      await updateKeyword(feedback.id, keywordId, target.keyword, newRequired);
      setFeedback(await reloadFeedback(feedback.id));
    } catch {
      setErrorMessage('키워드 고정을 변경하지 못했어요');
      setFeedback(previousFeedback);
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
      setFeedback(await reloadFeedback(feedback.id));
    } catch {
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
      setFeedback(await reloadFeedback(feedback.id));
    } catch {
      setErrorMessage('AI 문자를 생성하지 못했어요');
    } finally {
      setAiGenerating(false);
    }
  };

  const handleLike = async () => {
    if (!feedback || feedback.liked) return;
    try {
      await likeFeedback(feedback.id);
      const loaded = await reloadFeedback(feedback.id);
      const mergedFeedback = debounceTimerRef.current !== null
        ? { ...loaded, aiContent: feedback?.aiContent ?? loaded.aiContent }
        : loaded;
      setFeedback(mergedFeedback);
      feedbackIdRef.current = mergedFeedback.id;
    } catch {
      setErrorMessage('좋아요 처리에 실패했어요');
    }
  };

  return { feedback, aiGenerating, isEditingAiContent, errorMessage, handleAddKeyword, handleUpdateKeyword, handleRemoveKeyword, handleToggleKeywordRequired, handleGenerate, handleUpdateAiContent, handleLike };
}
