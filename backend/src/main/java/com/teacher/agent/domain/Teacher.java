package com.teacher.agent.domain;

import static com.teacher.agent.util.Parameter.*;
import static com.teacher.agent.util.ValidationUtil.checkMaxLength;
import static com.teacher.agent.util.ValidationUtil.checkNotBlank;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Teacher extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Convert(converter = UserIdConverter.class)
  @Column(nullable = false, unique = true)
  private UserId userId;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String name;

  private String subject;

  public static Teacher create(String userId, String encodedPassword, String name, String subject) {
    Teacher teacher = new Teacher();

    teacher.userId = new UserId(checkNotBlank(userId, USER_ID));
    teacher.password = checkNotBlank(encodedPassword, PASSWORD);
    teacher.name = checkNotBlank(name, NAME);
    teacher.subject = checkMaxLength(subject, 100, SUBJECT);

    return teacher;
  }

  public void updatePassword(String encodedPassword) {
    this.password = checkNotBlank(encodedPassword, PASSWORD);
  }

  public void updateProfile(String name, String subject) {
    this.name = checkNotBlank(name, NAME);
    this.subject = checkMaxLength(subject, 100, SUBJECT);
  }
}
