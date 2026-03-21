package com.teacher.agent.util;

import static com.teacher.agent.util.ErrorMessages.*;

import java.util.Collection;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationUtil {

  public static <T> T checkNotNull(T value, String name) {
    if (value == null) {
      throw new IllegalArgumentException(NOT_NULL_TEMPLATE.formatted(name));
    }
    return value;
  }

  public static String checkNotBlank(String value, String name) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(NOT_BLANK_TEMPLATE.formatted(name));
    }
    return value;
  }

  public static String checkMaxLength(String value, int max, String name) {
    if (value != null && value.length() > max) {
      throw new IllegalArgumentException(MAX_LENGTH_TEMPLATE.formatted(name, max));
    }
    return value;
  }

  public static long checkPositive(long value, String name) {
    if (value <= 0) {
      throw new IllegalArgumentException(POSITIVE_TEMPLATE.formatted(name));
    }
    return value;
  }

  public static long checkNotNegative(long value, String name) {
    if (value < 0) {
      throw new IllegalArgumentException(NOT_NEGATIVE_TEMPLATE.formatted(name));
    }
    return value;
  }

  public static <T extends Collection<?>> T checkNotEmpty(T collection, String name) {
    if (collection == null || collection.isEmpty()) {
      throw new IllegalArgumentException(NOT_EMPTY_TEMPLATE.formatted(name));
    }
    return collection;
  }

  public static void checkArgument(boolean condition, String name) {
    if (!condition) {
      throw new IllegalArgumentException(INVALID_ARGUMENT_TEMPLATE.formatted(name));
    }
  }
}
