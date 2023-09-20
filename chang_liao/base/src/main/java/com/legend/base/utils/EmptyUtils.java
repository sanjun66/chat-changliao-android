package com.legend.base.utils;

import androidx.collection.SparseArrayCompat;
import java.util.List;
import java.util.Map;

public class EmptyUtils {
    public static boolean isEmpty(List list) {
        return null == list || list.size() == 0;
    }

    public static boolean isEmpty(Object[] arrays) {
        return null == arrays || arrays.length == 0;
    }

    public static boolean isEmpty(int[] arrays) {
        return null == arrays || arrays.length == 0;
    }

    public static boolean isEmpty(SparseArrayCompat array) {
        return null == array || array.isEmpty();
    }

    public static boolean isEmpty(Map map) {
        return null == map || map.isEmpty();
    }
}
