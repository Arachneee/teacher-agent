package com.teacher.agent.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.teacher.agent.util.Parameter.*;
import static com.teacher.agent.util.ValidationUtil.checkNotBlank;
import static com.teacher.agent.util.ValidationUtil.checkMaxLength;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Teacher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    private String subject;

    public static Teacher create(String userId, String encodedPassword, String name, String subject) {
        Teacher teacher = new Teacher();
        teacher.userId = checkNotBlank(userId, USER_ID);
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
