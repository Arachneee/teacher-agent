package com.teacher.agent.domain;

import static com.teacher.agent.util.Parameter.*;
import static com.teacher.agent.util.ValidationUtil.*;

import com.teacher.agent.domain.vo.SchoolGrade;
import com.teacher.agent.domain.vo.UserId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(indexes = @Index(name = "idx_student_user_id", columnList = "userId"))
public class Student extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private UserId userId;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String memo;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private SchoolGrade grade;

  public static Student create(UserId userId, String name, String memo, SchoolGrade grade) {
    Student student = new Student();

    checkNotNull(userId, USER_ID);
    student.userId = new UserId(checkNotBlank(userId.value(), USER_ID));
    student.name = checkNotBlank(name, NAME);
    student.memo = checkMaxLength(memo, 500, MEMO);
    student.grade = checkNotNull(grade, GRADE);

    return student;
  }

  public void update(String name, String memo, SchoolGrade grade) {
    this.name = checkNotBlank(name, NAME);
    this.memo = checkMaxLength(memo, 500, MEMO);
    this.grade = checkNotNull(grade, GRADE);
  }
}
