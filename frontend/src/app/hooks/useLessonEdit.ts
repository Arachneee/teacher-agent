import { useState } from 'react';
import { Lesson, UpdateScope, updateLesson } from '../lib/api';
import { padTwoDigits, parseDateTime } from '../lib/dateTimeUtils';

interface EditForm {
  title: string;
  date: string;
  startHour: number;
  startMinute: number;
  endHour: number;
  endMinute: number;
}

export function useLessonEdit(lesson: Lesson | null, onSaved: (updated: Lesson) => void) {
  const [isEditing, setIsEditing] = useState(false);
  const [editForm, setEditForm] = useState<EditForm>({ title: '', date: '', startHour: 0, startMinute: 0, endHour: 0, endMinute: 0 });
  const [isSaving, setIsSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  const openEdit = () => {
    if (!lesson) return;
    const start = parseDateTime(lesson.startTime);
    const end = parseDateTime(lesson.endTime);
    setEditForm({ title: lesson.title, date: start.date, startHour: start.hour, startMinute: start.minute, endHour: end.hour, endMinute: end.minute });
    setSaveError(null);
    setIsEditing(true);
  };

  const closeEdit = () => setIsEditing(false);

  const handleSave = async (event: React.FormEvent, scope?: UpdateScope) => {
    event.preventDefault();
    if (!lesson || !editForm.date) return;
    setIsSaving(true);
    setSaveError(null);
    const startIso = `${editForm.date}T${padTwoDigits(editForm.startHour)}:${padTwoDigits(editForm.startMinute)}:00`;
    const endIso = `${editForm.date}T${padTwoDigits(editForm.endHour)}:${padTwoDigits(editForm.endMinute)}:00`;
    try {
      const updated = await updateLesson(lesson.id, editForm.title.trim() || lesson.title, startIso, endIso, scope);
      onSaved(updated);
      setIsEditing(false);
    } catch {
      setSaveError('수업 시간을 수정하지 못했어요.');
    } finally {
      setIsSaving(false);
    }
  };

  return { isEditing, editForm, setEditForm, isSaving, saveError, openEdit, closeEdit, handleSave };
}
