package com.teacher.agent.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.teacher.agent.util.Parameter.PASSWORD;
import static com.teacher.agent.util.Parameter.USERNAME;
import static com.teacher.agent.util.ValidationUtil.checkNotBlank;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Teacher extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    public static Teacher create(String username, String encodedPassword) {
        Teacher teacher = new Teacher();
        teacher.username = checkNotBlank(username, USERNAME);
        teacher.password = checkNotBlank(encodedPassword, PASSWORD);
        return teacher;
    }

    public void updatePassword(String encodedPassword) {
        this.password = checkNotBlank(encodedPassword, PASSWORD);
    }
}
