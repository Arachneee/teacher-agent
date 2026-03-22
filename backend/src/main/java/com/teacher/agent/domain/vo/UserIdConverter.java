package com.teacher.agent.domain.vo;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserIdConverter implements AttributeConverter<UserId, String> {

  @Override
  public String convertToDatabaseColumn(UserId userId) {
    return userId == null ? null : userId.value();
  }

  @Override
  public UserId convertToEntityAttribute(String value) {
    return value == null ? null : new UserId(value);
  }
}
