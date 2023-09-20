package com.legend.basenet.network.util;

public class Utils {

    private Utils() {
        throw new IllegalStateException("do not instantiation me");
    }

    public static <T> T checkNotNull(T object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
        return object;
    }
}

