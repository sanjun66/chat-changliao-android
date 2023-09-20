package com.legend.base.utils;

import java.lang.reflect.Type;

public class CloneUtils {
    private CloneUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static <T> T deepClone(final T data, final Type type) {
        try {
            return GlobalGsonUtils.fromJson(GlobalGsonUtils.toJson(data), type);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
