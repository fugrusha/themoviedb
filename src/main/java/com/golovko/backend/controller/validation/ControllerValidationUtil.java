package com.golovko.backend.controller.validation;

import com.golovko.backend.exception.ControllerValidationException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ControllerValidationUtil {

    public static <T extends Comparable<T>> void validateEquals(T value1, T value2,
                                                                String fieldName1, String fieldName2) {
        if (value1.compareTo(value2) != 0) {
            throw new ControllerValidationException(String.format("Field %s=%s should equal to field %s=%s",
                    fieldName1, value1, fieldName2, value2));
        }
    }

    public static <T extends Comparable<T>> void validateLessThan(T value1, T value2,
                                                                String fieldName1, String fieldName2) {
        if (value1.compareTo(value2) >= 0) {
            throw new ControllerValidationException(String.format("Field %s=%s should be less than %s=%s",
                    fieldName1, value1, fieldName2, value2));
        }
    }
}
