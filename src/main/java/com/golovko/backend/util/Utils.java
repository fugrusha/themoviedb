package com.golovko.backend.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Utils {

    public static boolean empty(final String s) {
        return s == null || s.trim().isEmpty();
    }
}
