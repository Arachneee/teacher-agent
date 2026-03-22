import { useEffect, useState } from 'react';
import { Attendee, Lesson, LessonDetailAttendee, getLessonDetail } from '../lib/api';

function toAttendee({ attendeeId, student, feedback }: LessonDetailAttendee, lessonDetailId: number): Attendee {
  return {
    id: attendeeId,
    lessonId: lessonDetailId,
    student: { id: student.id, name: student.name, memo: student.memo, grade: student.grade },
    feedback: feedback ? {
      id: feedback.id,
      studentId: feedback.studentId,
      lessonId: feedback.lessonId,
      lessonTitle: null,
      lessonStartTime: null,
      aiContent: feedback.aiContent,
      keywords: feedback.keywords,
      liked: feedback.liked,
      createdAt: feedback.createdAt,
      updatedAt: feedback.updatedAt,
    } : null,
    createdAt: student.createdAt,
  };
}

export function useLessonDetail(lessonId: number, onAttendeesLoaded: (attendeeIds: number[]) => void) {
  const [lesson, setLesson] = useState<Lesson | null>(null);
  const [attendees, setAttendees] = useState<Attendee[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchData = () => {
    getLessonDetail(lessonId)
      .then(detail => {
        setLesson({ id: detail.id, title: detail.title, startTime: detail.startTime, endTime: detail.endTime, recurrenceGroupId: detail.recurrenceGroupId ?? null });
        const mapped = detail.attendees.map(attendee => toAttendee(attendee, detail.id));
        setAttendees(mapped);
        onAttendeesLoaded(mapped.map(attendee => attendee.id));
      })
      .catch(console.error)
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchData();
  }, [lessonId]);

  return { lesson, setLesson, attendees, setAttendees, loading, setLoading, fetchData };
}
