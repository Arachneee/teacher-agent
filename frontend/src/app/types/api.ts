export type SchoolGrade =
  | 'ELEMENTARY_1' | 'ELEMENTARY_2' | 'ELEMENTARY_3'
  | 'ELEMENTARY_4' | 'ELEMENTARY_5' | 'ELEMENTARY_6'
  | 'MIDDLE_1' | 'MIDDLE_2' | 'MIDDLE_3'
  | 'HIGH_1' | 'HIGH_2' | 'HIGH_3';

export interface Student {
  id: number;
  name: string;
  memo: string;
  grade: SchoolGrade | null;
  createdAt: string;
  updatedAt: string;
}

export interface FeedbackKeyword {
  id: number;
  keyword: string;
  createdAt: string;
}

export interface Feedback {
  id: number;
  studentId: number;
  lessonId: number;
  lessonTitle: string | null;
  lessonStartTime: string | null;
  aiContent: string | null;
  keywords: FeedbackKeyword[];
  liked: boolean;
  createdAt: string;
  updatedAt: string;
}

export type UpdateScope = 'SINGLE' | 'THIS_AND_FOLLOWING' | 'ALL';

export interface Lesson {
  id: number;
  title: string;
  startTime: string;
  endTime: string;
  recurrenceGroupId: string | null;
}

export interface AttendeeStudent {
  id: number;
  name: string;
  memo: string;
  grade: SchoolGrade | null;
}

export interface Attendee {
  id: number;
  lessonId: number;
  student: AttendeeStudent;
  feedback: Feedback | null;
  createdAt: string;
}

export interface LessonDetailFeedback {
  id: number;
  studentId: number;
  lessonId: number;
  aiContent: string | null;
  keywords: FeedbackKeyword[];
  liked: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface LessonDetailAttendee {
  attendeeId: number;
  student: Student;
  feedback: LessonDetailFeedback | null;
}

export interface LessonDetail {
  id: number;
  title: string;
  startTime: string;
  endTime: string;
  recurrenceGroupId: string | null;
  createdAt: string;
  updatedAt: string;
  attendees: LessonDetailAttendee[];
}

export interface AuthResponse {
  userId: string;
}

export type RecurrenceType = 'DAILY' | 'WEEKLY' | 'MONTHLY';

export type DayOfWeek = 'MONDAY' | 'TUESDAY' | 'WEDNESDAY' | 'THURSDAY' | 'FRIDAY' | 'SATURDAY' | 'SUNDAY';

export interface RecurrenceCreateRequest {
  recurrenceType: RecurrenceType;
  intervalValue: number;
  daysOfWeek?: DayOfWeek[];
  endDate: string;
}
