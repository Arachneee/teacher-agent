package com.teacher.agent.domain;

import static com.teacher.agent.util.Parameter.EVENT_TYPE;
import static com.teacher.agent.util.Parameter.USER_ID;
import static com.teacher.agent.util.ValidationUtil.checkNotBlank;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "id", callSuper = false)
@Table(indexes = {
    @Index(name = "idx_user_event_user_id_created_at", columnList = "userId, createdAt"),
    @Index(name = "idx_user_event_event_type_created_at", columnList = "eventType, createdAt")
})
public class UserEvent extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private String eventType;

  @Column(columnDefinition = "TEXT")
  private String metadata;

  public static UserEvent create(String userId, String eventType, String metadata) {
    UserEvent userEvent = new UserEvent();
    userEvent.userId = checkNotBlank(userId, USER_ID);
    userEvent.eventType = checkNotBlank(eventType, EVENT_TYPE);
    userEvent.metadata = metadata;
    return userEvent;
  }
}
