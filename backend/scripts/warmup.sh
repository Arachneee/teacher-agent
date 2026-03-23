#!/bin/bash

APP_URL="http://localhost:8080"
WARMUP_USER_ID="${WARMUP_TEACHER_USER_ID:-warmup}"
WARMUP_PASSWORD="${WARMUP_TEACHER_PASSWORD:-warmup-password}"
MAX_WAIT_SECONDS=600
INTERVAL=10

COOKIE_FILE=$(mktemp /tmp/warmup-cookie-XXXXXX)
STUDENT_ID=""
LESSON_ID=""

cleanup() {
  [ -n "$LESSON_ID" ] && \
    curl -s -b "$COOKIE_FILE" -c "$COOKIE_FILE" -o /dev/null \
      -X DELETE "$APP_URL/lessons/$LESSON_ID?scope=ALL"
  [ -n "$STUDENT_ID" ] && \
    curl -s -b "$COOKIE_FILE" -c "$COOKIE_FILE" -o /dev/null \
      -X DELETE "$APP_URL/students/$STUDENT_ID"
  rm -f "$COOKIE_FILE"
}
trap cleanup EXIT

log() { echo "[warmup] $1"; }
fail() { log "ERROR: $1. Aborting warmup."; exit 1; }

extract() {
  local json="$1" field="$2"
  echo "$json" | grep -o "\"$field\":[0-9]*" | head -1 | grep -o '[0-9]*$'
}

http() {
  curl -s -b "$COOKIE_FILE" -c "$COOKIE_FILE" "$@"
}

# ─── 앱 기동 대기 ──────────────────────────────────────────────
log "Waiting for application to start..."
elapsed=0
until curl -s -o /dev/null "$APP_URL/auth/me"; do
  [ $elapsed -ge $MAX_WAIT_SECONDS ] && fail "Application did not start within ${MAX_WAIT_SECONDS}s"
  sleep $INTERVAL
  elapsed=$((elapsed + INTERVAL))
  log "  Still waiting... (${elapsed}s elapsed)"
done
log "Application is up. Starting warmup..."

# ─── 날짜 계산 (GNU/BSD 호환) ────────────────────────────────
if date -d "+1 day" +%Y-%m-%d > /dev/null 2>&1; then
  # GNU date (Linux/EC2)
  DAY_OF_WEEK=$(date +%u)
  WEEK_START=$(date -d "-$((DAY_OF_WEEK - 1)) days" +%Y-%m-%d)
  TOMORROW=$(date -d "+1 day" +%Y-%m-%d)
  RECURRENCE_END=$(date -d "+30 days" +%Y-%m-%d)
  TOMORROW_DOW=$(date -d '+1 day' +%A | tr '[:lower:]' '[:upper:]')
else
  # BSD date (macOS)
  DAY_OF_WEEK=$(date +%u)
  WEEK_START=$(date -v-$((DAY_OF_WEEK - 1))d +%Y-%m-%d)
  TOMORROW=$(date -v+1d +%Y-%m-%d)
  RECURRENCE_END=$(date -v+30d +%Y-%m-%d)
  TOMORROW_DOW=$(LC_ALL=C date -v+1d +%A | tr '[:lower:]' '[:upper:]')
fi

# ─── 1. 로그인 ────────────────────────────────────────────────
RESPONSE=$(http -X POST "$APP_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"userId\":\"$WARMUP_USER_ID\",\"password\":\"$WARMUP_PASSWORD\"}")
echo "$RESPONSE" | grep -q "userId" || fail "Login failed"
log "1. Login OK"

# ─── 2. 선생님 정보 조회 ──────────────────────────────────────
http -o /dev/null "$APP_URL/teachers/me"
log "2. GET /teachers/me OK"

# ─── 3. 선생님 정보 수정 ──────────────────────────────────────
http -o /dev/null -X PUT "$APP_URL/teachers/me" \
  -H "Content-Type: application/json" \
  -d '{"name":"워밍업선생님","subject":"워밍업과목"}'
log "3. PUT /teachers/me OK"

# ─── 4. 학생 추가 ─────────────────────────────────────────────
RESPONSE=$(http -X POST "$APP_URL/students" \
  -H "Content-Type: application/json" \
  -d '{"name":"워밍업학생","memo":"워밍업용","grade":"MIDDLE_1"}')
STUDENT_ID=$(extract "$RESPONSE" "id")
[ -z "$STUDENT_ID" ] && fail "Student create failed: $RESPONSE"
log "4. POST /students OK (studentId=$STUDENT_ID)"

# ─── 5. 학생 목록 조회 ────────────────────────────────────────
http -o /dev/null "$APP_URL/students"
log "5. GET /students OK"

# ─── 6. 학생 단건 조회 ────────────────────────────────────────
http -o /dev/null "$APP_URL/students/$STUDENT_ID"
log "6. GET /students/$STUDENT_ID OK"

# ─── 7. 학생 수정 ─────────────────────────────────────────────
http -o /dev/null -X PUT "$APP_URL/students/$STUDENT_ID" \
  -H "Content-Type: application/json" \
  -d '{"name":"워밍업학생(수정)","memo":"워밍업용","grade":"MIDDLE_1"}'
log "7. PUT /students/$STUDENT_ID OK"

# ─── 8. 반복 레슨 추가 ────────────────────────────────────────
RESPONSE=$(http -X POST "$APP_URL/lessons" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\":\"워밍업레슨\",
    \"startTime\":\"${TOMORROW}T10:00:00\",
    \"endTime\":\"${TOMORROW}T11:00:00\",
    \"recurrence\":{
      \"recurrenceType\":\"WEEKLY\",
      \"intervalValue\":1,
      \"daysOfWeek\":[\"$TOMORROW_DOW\"],
      \"endDate\":\"$RECURRENCE_END\"
    }
  }")
LESSON_ID=$(extract "$RESPONSE" "id")
[ -z "$LESSON_ID" ] && fail "Lesson create failed: $RESPONSE"
log "8. POST /lessons OK (lessonId=$LESSON_ID)"

# ─── 9. 레슨 수정 ─────────────────────────────────────────────
http -o /dev/null -X PUT "$APP_URL/lessons/$LESSON_ID" \
  -H "Content-Type: application/json" \
  -d "{
    \"title\":\"워밍업레슨(수정)\",
    \"startTime\":\"${TOMORROW}T10:00:00\",
    \"endTime\":\"${TOMORROW}T11:00:00\",
    \"scope\":\"SINGLE\"
  }"
log "9. PUT /lessons/$LESSON_ID OK"

# ─── 10. 수강생 추가 (attendee API) ──────────────────────────
RESPONSE=$(http -X POST "$APP_URL/lessons/$LESSON_ID/attendees" \
  -H "Content-Type: application/json" \
  -d "{\"studentId\":$STUDENT_ID,\"scope\":\"SINGLE\"}")
ATTENDEE_ID=$(extract "$RESPONSE" "id")
[ -z "$ATTENDEE_ID" ] && fail "Attendee add failed: $RESPONSE"
log "10. POST /lessons/$LESSON_ID/attendees OK (attendeeId=$ATTENDEE_ID)"

# ─── 11. 수강생 목록 조회 ─────────────────────────────────────
http -o /dev/null "$APP_URL/lessons/$LESSON_ID/attendees"
log "11. GET /lessons/$LESSON_ID/attendees OK"

# ─── 12. 레슨 목록 조회 ───────────────────────────────────────
http -o /dev/null "$APP_URL/lessons?weekStart=$WEEK_START"
log "12. GET /lessons OK"

# ─── 13. 레슨 상세 조회 (feedbackId 추출) ─────────────────────
RESPONSE=$(http "$APP_URL/lessons/$LESSON_ID/detail")
FEEDBACK_ID=$(echo "$RESPONSE" | grep -o '"feedback":{"id":[0-9]*' | grep -o '[0-9]*$')
[ -z "$FEEDBACK_ID" ] && fail "Lesson detail failed or feedbackId not found: $RESPONSE"
log "13. GET /lessons/$LESSON_ID/detail OK (feedbackId=$FEEDBACK_ID)"

# ─── 14. 피드백 단건 조회 ─────────────────────────────────────
http -o /dev/null "$APP_URL/feedbacks/$FEEDBACK_ID"
log "14. GET /feedbacks/$FEEDBACK_ID OK"

# ─── 15. 키워드 3개 추가 ──────────────────────────────────────
RESPONSE=$(http -X POST "$APP_URL/feedbacks/$FEEDBACK_ID/keywords" \
  -H "Content-Type: application/json" \
  -d '{"keyword":"워밍업키워드1","required":true}')
KEYWORD_ID=$(echo "$RESPONSE" | grep -o '"keywords":\[{"id":[0-9]*' | grep -o '[0-9]*$')
log "15-1. POST /feedbacks/$FEEDBACK_ID/keywords OK (keywordId=$KEYWORD_ID, required=true)"

http -o /dev/null -X POST "$APP_URL/feedbacks/$FEEDBACK_ID/keywords" \
  -H "Content-Type: application/json" \
  -d '{"keyword":"워밍업키워드2"}'
log "15-2. POST /feedbacks/$FEEDBACK_ID/keywords OK"

http -o /dev/null -X POST "$APP_URL/feedbacks/$FEEDBACK_ID/keywords" \
  -H "Content-Type: application/json" \
  -d '{"keyword":"워밍업키워드3"}'
log "15-3. POST /feedbacks/$FEEDBACK_ID/keywords OK"

# ─── 16. 키워드 수정 ─────────────────────────────────────────
[ -n "$KEYWORD_ID" ] && \
  http -o /dev/null -X PUT "$APP_URL/feedbacks/$FEEDBACK_ID/keywords/$KEYWORD_ID" \
    -H "Content-Type: application/json" \
    -d '{"keyword":"워밍업키워드1(수정)","required":false}'
log "16. PUT /feedbacks/$FEEDBACK_ID/keywords/$KEYWORD_ID OK"

# ─── 17. 키워드 삭제 ─────────────────────────────────────────
[ -n "$KEYWORD_ID" ] && \
  http -o /dev/null -X DELETE "$APP_URL/feedbacks/$FEEDBACK_ID/keywords/$KEYWORD_ID"
log "17. DELETE /feedbacks/$FEEDBACK_ID/keywords/$KEYWORD_ID OK"

# ─── 18. AI 피드백 스트리밍 생성 ────────────────────────────
curl -s --no-buffer -b "$COOKIE_FILE" -c "$COOKIE_FILE" \
  -o /dev/null "$APP_URL/feedbacks/$FEEDBACK_ID/generate/stream"
log "18. GET /feedbacks/$FEEDBACK_ID/generate/stream OK"

# ─── 19. AI 피드백 수정 ──────────────────────────────────────
http -o /dev/null -X PATCH "$APP_URL/feedbacks/$FEEDBACK_ID" \
  -H "Content-Type: application/json" \
  -d '{"aiContent":"워밍업 AI 내용"}'
log "19. PATCH /feedbacks/$FEEDBACK_ID OK"

# ─── 20. 따봉 ────────────────────────────────────────────────
http -o /dev/null -X POST "$APP_URL/feedbacks/$FEEDBACK_ID/like"
log "20. POST /feedbacks/$FEEDBACK_ID/like OK"

# ─── 21. 학생 피드백 목록 조회 ───────────────────────────────
http -o /dev/null "$APP_URL/feedbacks?studentId=$STUDENT_ID"
log "21. GET /feedbacks?studentId=$STUDENT_ID OK"

# ─── 22. 수강생 삭제 ─────────────────────────────────────────
http -o /dev/null -X DELETE "$APP_URL/lessons/$LESSON_ID/attendees/$ATTENDEE_ID"
log "22. DELETE /lessons/$LESSON_ID/attendees/$ATTENDEE_ID OK"

# ─── 23. 레슨 삭제 (ALL) ─────────────────────────────────────
http -o /dev/null -X DELETE "$APP_URL/lessons/$LESSON_ID?scope=ALL"
log "23. DELETE /lessons/$LESSON_ID?scope=ALL OK"

# ─── 24. 학생 삭제 ───────────────────────────────────────────
http -o /dev/null -X DELETE "$APP_URL/students/$STUDENT_ID"
log "24. DELETE /students/$STUDENT_ID OK"

# ─── 25. 로그아웃 ────────────────────────────────────────────
http -o /dev/null -X POST "$APP_URL/auth/logout"
log "25. POST /auth/logout OK"

log "Warmup complete."
