# Domain Model: Academy Management System
1. Teacher (선생님)
   수업을 개설하고 진행하는 주체입니다.

| Field | Type | Constraint | Description |
|---|---|---|---|
| id | Long | PK | 선생님 고유 ID |
| name | String | Not Null | 선생님 이름 |
| subject | String | Nullable | 담당 과목 (예: 수학, 영어) |
2. Student (학생)
학원에 등록되어 수업을 듣는 주체입니다.

| Field | Type | Constraint | Description |
|---|---|---|---|
| id | Long | PK | 학생 고유 ID |
| name | String | Not Null | 학생 이름 |
| grade | String | Nullable | 학년 (예: 중2, 고1) |
3. Lesson (1타임 수업)
   특정 날짜와 시간에 진행되는 1회성 물리적 강의입니다.

| Field | Type | Constraint | Description                            |
|---|---|---|----------------------------------------|
| id | Long | PK | 수업 고유 ID                               |
| teacherId | Long | FK | 담당 선생님 ID                              |
| groupId | Long | Nullable | [확장 대비용] 추후 정규 강좌(Course)가 생기면 연결할 외래키 |
| title | String | Not Null | 수업명 (예: 3월 15일 수학 정규반)                 |
| startTime | DateTime | Not Null | 수업 시작 일시                               |
| endTime | DateTime | Not Null | 수업 종료 일시                               |
4. Attendee (수강생 / 출결 내역)
   특정 1타임 수업(Lesson)에 참여하는 학생(Student)의 매핑 정보 및 출결 상태입니다.

| Field | Type | Constraint | Description |
|---|---|---|---|
| id | Long | PK | 참석 내역 고유 ID |
| lessonId | Long | FK | 참여한 수업 ID |
| studentId | Long | FK | 참여한 학생 ID |
| status | Enum | Not Null | 출결 상태 (예: PRESENT, LATE, ABSENT) |
5. Feedback (피드백)
   선생님이 특정 수업을 들은 수강생(Attendee)에게 남기는 개별 코멘트입니다.

| Field | Type | Constraint | Description |
|---|---|---|---|
| id | Long | PK | 피드백 고유 ID |
| attendeeId | Long | FK, Unique | 대상 수강생 내역 ID (1:1 관계이므로 Unique) |
| content | Text | Not Null | 피드백 상세 내용 |
| createdAt | DateTime | Not Null | 작성 일시 |
6. Keyword (피드백 키워드)
   피드백에 부여되는 태그들입니다.

| Field | Type | Constraint | Description |
|---|---|---|---|
| id | Long | PK | 키워드 고유 ID |
| feedbackId | Long | FK | 속한 피드백 ID |
| word | String | Not Null | 키워드명 (예: 숙제우수, 집중력부족) |

Relationships (엔티티 간 관계 정의)

* Teacher (1) - (N) Lesson : 한 명의 선생님은 여러 개의 수업을 가질 수 있습니다.
* Student (1) - (N) Attendee : 한 명의 학생은 여러 수업의 참석 내역을 가질 수 있습니다.
* Lesson (1) - (N) Attendee : 하나의 수업에는 여러 명의 학생 참석 내역이 존재합니다.
* Attendee (1) - (1) Feedback : 하나의 참석 내역에는 단 하나의 피드백만 작성됩니다.
* Feedback (1) - (N) Keyword : 하나의 피드백은 여러 개의 키워드를 가질 수 있습니다.

PRD: 학원 수업 및 학생 관리 시스템
1. 프로젝트 개요 (Overview)
본 프로젝트는 학원 선생님이 개별 수업(1타임 강의)을 생성하고, 해당 수업에 참여한 학생들의 출결을 관리하며, 각 수강생에게 맞춤형 피드백(키워드 포함)을 제공할 수 있는 시스템을 구축하는 것을 목표로 합니다.
현재는 단일 수업 관리에 집중하지만, 향후 정규 강좌(반복 수업) 확장을 고려하여 유연한 데이터 모델을 채택합니다.
2. 핵심 사용자 (Target Users)
* 선생님 (Teacher): 수업을 개설하고, 학생들의 출결을 체크하며, 피드백을 작성하는 시스템의 주 사용자.
* 학생 (Student): 시스템에 등록되어 수업에 참여하고 피드백을 받는 대상. (현재 버전에서는 관리 대상 객체로 주로 활용)
3. 주요 기능 요구사항 (Key Features)
* 수업 관리: 선생님은 특정 일시에 진행되는 1타임 수업(Lesson)을 생성, 조회, 수정할 수 있습니다.
* 출결 관리: 생성된 수업에 학생을 추가하여 수강생(Attendee) 명단을 구성하고, 개별 출결 상태를 기록합니다.
* 피드백 작성: 선생님은 특정 수업을 수강한 학생의 참석 내역에 대해 1:1로 매핑되는 피드백(Feedback)을 텍스트로 남길 수 있습니다.
* 키워드 태깅: 작성된 피드백에 핵심 키워드(Keyword)를 여러 개 달아 시각적 요약 및 추후 통계에 활용할 수 있습니다.
4. 데이터 모델 (Domain Model)
> 확장성 고려사항 (Future Scalability): > 향후 '매주 반복되는 정규 강좌(Course)' 기능이 추가될 것을 대비해 Lesson 엔티티에 groupId 속성을 미리 포함했습니다. 현재는 비워두거나 단순 그룹핑용 문자열로 사용하며, 추후 Course 테이블이 추가될 때 외래키(FK)로 전환하여 기존 로직의 수정 없이 부드럽게 확장합니다.
>
4.1. Teacher (선생님)

| Field | Type | Constraint | Description |
|---|---|---|---|
| id | Long | PK | 선생님 고유 ID |
| name | String | Not Null | 선생님 이름 |
| subject | String | Nullable | 담당 과목 (예: 수학, 영어) |
4.2. Student (학생)

| Field | Type | Constraint | Description |
|---|---|---|---|
| id | Long | PK | 학생 고유 ID |
| name | String | Not Null | 학생 이름 |
| grade | String | Nullable | 학년 (예: 중2, 고1) |
4.3. Lesson (1타임 수업)

| Field | Type | Constraint | Description |
|---|---|---|---|
| id | Long | PK | 수업 고유 ID |
| teacherId | Long | FK | 담당 선생님 ID |
| groupId | Long | Nullable | [확장 대비용] 추후 정규 강좌 연결용 FK |
| title | String | Not Null | 수업명 (예: 3월 15일 수학 특강) |
| startTime | DateTime | Not Null | 수업 시작 일시 |
| endTime | DateTime | Not Null | 수업 종료 일시 |
4.4. Attendee (수강생 / 출결 내역)

| Field | Type | Constraint | Description |
|---|---|---|---|
| id | Long | PK | 참석 내역 고유 ID |
| lessonId | Long | FK | 참여한 수업 ID |
| studentId | Long | FK | 참여한 학생 ID |
| status | Enum | Not Null | 출결 상태 (예: PRESENT, LATE, ABSENT) |
4.5. Feedback (피드백)

| Field | Type | Constraint | Description |
|---|---|---|---|
| id | Long | PK | 피드백 고유 ID |
| attendeeId | Long | FK, Unique | 대상 수강생 내역 ID (1:1 관계) |
| content | Text | Not Null | 피드백 상세 내용 |
| createdAt | DateTime | Not Null | 작성 일시 |
4.6. Keyword (피드백 키워드)

| Field | Type | Constraint | Description |
|---|---|---|---|
| id | Long | PK | 키워드 고유 ID |
| feedbackId | Long | FK | 속한 피드백 ID |
| word | String | Not Null | 키워드명 (예: 숙제우수, 집중력부족) |
5. 엔티티 관계 (Entity Relationships)
* Teacher 1 : N Lesson
* Student 1 : N Attendee
* Lesson 1 : N Attendee
* Attendee 1 : 1 Feedback
* Feedback 1 : N Keyword
