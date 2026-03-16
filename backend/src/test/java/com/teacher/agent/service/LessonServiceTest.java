package com.teacher.agent.service;

import com.teacher.agent.domain.LessonRepository;
import com.teacher.agent.domain.Teacher;
import com.teacher.agent.domain.TeacherRepository;
import com.teacher.agent.domain.UserId;
import com.teacher.agent.dto.LessonCreateRequest;
import com.teacher.agent.dto.LessonResponse;
import com.teacher.agent.dto.LessonUpdateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(LessonService.class)
class LessonServiceTest {

    @Autowired
    private LessonService lessonService;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    private static final LocalDateTime START = LocalDateTime.of(2026, 3, 16, 9, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 3, 16, 10, 0);

    private Teacher teacher;

    @BeforeEach
    void setUp() {
        teacher = teacherRepository.save(Teacher.create("testteacher", "encodedPassword", "테스트교사", null));
    }

    @AfterEach
    void tearDown() {
        lessonRepository.deleteAllInBatch();
        teacherRepository.deleteAllInBatch();
    }

    @Test
    void 수업을_생성한다() {
        LessonResponse response = lessonService.create(teacher.getUserId(),
                new LessonCreateRequest("수학 1교시", START, END));

        assertThat(response.id()).isNotNull();
        assertThat(response.teacherId()).isEqualTo(teacher.getId());
        assertThat(response.title()).isEqualTo("수학 1교시");
        assertThat(response.startTime()).isEqualTo(START);
        assertThat(response.endTime()).isEqualTo(END);
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
    }

    @Test
    void 해당_교사의_수업_목록을_조회한다() {
        lessonService.create(teacher.getUserId(), new LessonCreateRequest("수학", START, END));
        lessonService.create(teacher.getUserId(), new LessonCreateRequest("영어", START.plusHours(2), END.plusHours(2)));

        List<LessonResponse> lessons = lessonService.getAllByTeacher(teacher.getUserId());

        assertThat(lessons).hasSize(2);
    }

    @Test
    void 다른_교사의_수업은_조회되지_않는다() {
        Teacher otherTeacher = teacherRepository.save(Teacher.create("otherteacher", "encodedPassword", "다른교사", null));
        lessonService.create(teacher.getUserId(), new LessonCreateRequest("수학", START, END));
        lessonService.create(otherTeacher.getUserId(), new LessonCreateRequest("영어", START, END));

        List<LessonResponse> lessons = lessonService.getAllByTeacher(teacher.getUserId());

        assertThat(lessons).hasSize(1);
        assertThat(lessons.get(0).title()).isEqualTo("수학");
    }

    @Test
    void 수업을_단건_조회한다() {
        LessonResponse created = lessonService.create(teacher.getUserId(),
                new LessonCreateRequest("수학", START, END));

        LessonResponse found = lessonService.getOne(teacher.getUserId(), created.id());

        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.title()).isEqualTo("수학");
    }

    @Test
    void 존재하지_않는_수업_조회_시_예외가_발생한다() {
        assertThatThrownBy(() -> lessonService.getOne(teacher.getUserId(), 999L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 수업_정보를_수정한다() {
        LessonResponse created = lessonService.create(teacher.getUserId(),
                new LessonCreateRequest("수학", START, END));
        LocalDateTime newStart = LocalDateTime.of(2026, 3, 17, 14, 0);
        LocalDateTime newEnd = LocalDateTime.of(2026, 3, 17, 15, 0);

        LessonResponse updated = lessonService.update(teacher.getUserId(), created.id(),
                new LessonUpdateRequest("영어 2교시", newStart, newEnd));

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.title()).isEqualTo("영어 2교시");
        assertThat(updated.startTime()).isEqualTo(newStart);
        assertThat(updated.endTime()).isEqualTo(newEnd);
    }

    @Test
    void 존재하지_않는_수업_수정_시_예외가_발생한다() {
        assertThatThrownBy(() -> lessonService.update(teacher.getUserId(), 999L,
                new LessonUpdateRequest("수학", START, END)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 수업을_삭제한다() {
        LessonResponse created = lessonService.create(teacher.getUserId(),
                new LessonCreateRequest("수학", START, END));

        lessonService.delete(teacher.getUserId(), created.id());

        assertThatThrownBy(() -> lessonService.getOne(teacher.getUserId(), created.id()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 존재하지_않는_수업_삭제_시_예외가_발생한다() {
        assertThatThrownBy(() -> lessonService.delete(teacher.getUserId(), 999L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 다른_교사의_수업_수정_시_예외가_발생한다() {
        Teacher otherTeacher = teacherRepository.save(Teacher.create("otherteacher2", "encodedPassword", "다른교사2", null));
        LessonResponse created = lessonService.create(teacher.getUserId(),
                new LessonCreateRequest("수학", START, END));

        assertThatThrownBy(() -> lessonService.update(otherTeacher.getUserId(), created.id(),
                new LessonUpdateRequest("영어", START, END)))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 다른_교사의_수업_삭제_시_예외가_발생한다() {
        Teacher otherTeacher = teacherRepository.save(Teacher.create("otherteacher3", "encodedPassword", "다른교사3", null));
        LessonResponse created = lessonService.create(teacher.getUserId(),
                new LessonCreateRequest("수학", START, END));

        assertThatThrownBy(() -> lessonService.delete(otherTeacher.getUserId(), created.id()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 존재하지_않는_교사로_수업_생성_시_예외가_발생한다() {
        assertThatThrownBy(() -> lessonService.create(new UserId("nonexistent"),
                new LessonCreateRequest("수학", START, END)))
                .isInstanceOf(ResponseStatusException.class);
    }
}
