package com.teacher.agent.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.teacher.agent.domain.UserEvent;
import com.teacher.agent.domain.repository.UserEventRepository;
import com.teacher.agent.dto.UserEventResponse;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@Import(UserEventCommandService.class)
class UserEventCommandServiceTest {

  @Autowired
  private UserEventCommandService userEventCommandService;

  @Autowired
  private UserEventRepository userEventRepository;

  @AfterEach
  void tearDown() {
    userEventRepository.deleteAllInBatch();
  }

  @Test
  void 이벤트를_저장한다() {
    UserEventResponse response = userEventCommandService.save("admin", "feedback_copy",
        "{\"feedbackId\": 42}");

    assertThat(response.id()).isNotNull();

    List<UserEvent> events = userEventRepository.findAll();
    assertThat(events).hasSize(1);

    UserEvent event = events.get(0);
    assertThat(event.getUserId()).isEqualTo("admin");
    assertThat(event.getEventType()).isEqualTo("feedback_copy");
    assertThat(event.getMetadata()).isEqualTo("{\"feedbackId\": 42}");
    assertThat(event.getCreatedAt()).isNotNull();
  }

  @Test
  void metadata가_null인_이벤트를_저장한다() {
    UserEventResponse response = userEventCommandService.save("admin", "feedback_edit", null);

    assertThat(response.id()).isNotNull();

    List<UserEvent> events = userEventRepository.findAll();
    assertThat(events).hasSize(1);

    UserEvent event = events.get(0);
    assertThat(event.getMetadata()).isNull();
  }
}
