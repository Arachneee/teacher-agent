package com.teacher.agent.service;

import com.teacher.agent.domain.StudentRepository;
import com.teacher.agent.dto.StudentCreateRequest;
import com.teacher.agent.dto.StudentResponse;
import com.teacher.agent.dto.StudentUpdateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(StudentService.class)
class StudentServiceTest {

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentRepository studentRepository;

    @AfterEach
    void tearDown() {
        studentRepository.deleteAllInBatch();
    }

    @Test
    void 학생을_생성한다() {
        StudentResponse response = studentService.create(new StudentCreateRequest("홍길동", "성실한 학생"));

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("홍길동");
        assertThat(response.memo()).isEqualTo("성실한 학생");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
    }

    @Test
    void 전체_학생_목록을_조회한다() {
        studentService.create(new StudentCreateRequest("홍길동", null));
        studentService.create(new StudentCreateRequest("김철수", null));

        List<StudentResponse> students = studentService.getAll();

        assertThat(students).hasSize(2);
    }

    @Test
    void 학생을_단건_조회한다() {
        StudentResponse created = studentService.create(new StudentCreateRequest("홍길동", "메모"));

        StudentResponse found = studentService.getOne(created.id());

        assertThat(found.id()).isEqualTo(created.id());
        assertThat(found.name()).isEqualTo("홍길동");
        assertThat(found.memo()).isEqualTo("메모");
    }

    @Test
    void 존재하지_않는_학생_조회_시_예외가_발생한다() {
        assertThatThrownBy(() -> studentService.getOne(999L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 학생_정보를_수정한다() {
        StudentResponse created = studentService.create(new StudentCreateRequest("홍길동", "메모"));

        StudentResponse updated = studentService.update(created.id(), new StudentUpdateRequest("김철수", "새 메모"));

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.name()).isEqualTo("김철수");
        assertThat(updated.memo()).isEqualTo("새 메모");
    }

    @Test
    void 존재하지_않는_학생_수정_시_예외가_발생한다() {
        assertThatThrownBy(() -> studentService.update(999L, new StudentUpdateRequest("이름", "메모")))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 학생을_삭제한다() {
        StudentResponse created = studentService.create(new StudentCreateRequest("홍길동", null));

        studentService.delete(created.id());

        assertThatThrownBy(() -> studentService.getOne(created.id()))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void 존재하지_않는_학생_삭제_시_예외가_발생한다() {
        assertThatThrownBy(() -> studentService.delete(999L))
                .isInstanceOf(ResponseStatusException.class);
    }
}
