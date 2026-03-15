package com.teacher.agent.domain;

import static com.teacher.agent.util.Parameter.*;
import static com.teacher.agent.util.ValidationUtil.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Student extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String memo;

    public static Student create(String name, String memo) {
        Student student = new Student();
        student.name = checkNotBlank(name, NAME);
        student.memo = checkMaxLength(memo, 500, MEMO);
        return student;
    }

    public void update(String name, String memo) {
        this.name = checkNotBlank(name, NAME);
        this.memo = checkMaxLength(memo, 500, MEMO);
    }
}
