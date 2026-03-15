package com.teacher.agent.util;

import lombok.experimental.UtilityClass;

import java.util.Collection;

@UtilityClass
public class ValidationUtil {

    public static <T> T checkNotNull(T value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + "은(는) null일 수 없습니다.");
        }
        return value;
    }

    public static String checkNotBlank(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + "은(는) 비어 있을 수 없습니다.");
        }
        return value;
    }

    public static String checkMaxLength(String value, int max, String name) {
        if (value != null && value.length() > max) {
            throw new IllegalArgumentException(name + "은(는) " + max + "자 이하여야 합니다.");
        }
        return value;
    }

    public static long checkPositive(long value, String name) {
        if (value <= 0) {
            throw new IllegalArgumentException(name + "은(는) 양수여야 합니다.");
        }
        return value;
    }

    public static long checkNotNegative(long value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException(name + "은(는) 0 이상이어야 합니다.");
        }
        return value;
    }

    public static <T extends Collection<?>> T checkNotEmpty(T collection, String name) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(name + "은(는) 비어 있을 수 없습니다.");
        }
        return collection;
    }

    public static void checkArgument(boolean condition, String name) {
        if (!condition) {
            throw new IllegalArgumentException(name + "이(가) 유효하지 않습니다.");
        }
    }
}
