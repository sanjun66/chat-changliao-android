package com.legend.baseui.ui.util;

import android.net.Uri;
import android.text.TextUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 界面中需要用到的工具类
 */
public class UiUtils {
    private static long lastClickTime;
    private static Map<String, Long> mapLastClickTime = new HashMap<>();

    /**
     * 防止用户快速点击，延迟0.5秒
     *
     * @return
     */
    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 500) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    /** double click */
    public static boolean isFastDoubleClick() {
        return isFastDoubleClick("Default");
    }

    public static boolean isFastDoubleClick(String eventType) {
        Long lastClickTime = mapLastClickTime.get(eventType);
        if (lastClickTime == null) {
            lastClickTime = 0l;
        }
        long curTime = System.currentTimeMillis();
        long timeD = curTime - lastClickTime;
        if (timeD > 0 && timeD < 800) {
            return true;
        }
        mapLastClickTime.put(eventType, curTime);
        return false;
    }

    /**
     * 从url地址解析图片真实宽高
     *
     * @return
     */
    public static int[] parsedPixelFromUrl(String url) {
        try {
            Uri parse = Uri.parse(url);
            String lastPathSegment = parse.getLastPathSegment();
            String[] pixel = lastPathSegment.split("_");
            String pix = pixel[0];
            String[] size = pix.split("x");

            int width = Integer.parseInt(size[0]);
            int height = Integer.parseInt(size[1]);
            return new int[]{width, height};
        } catch (Exception e) {
            return new int[]{1, 0};
        }
    }


    /**
     * 手机号码加*
     * @param phone 未加密11位手机号码
     * @return 4-8位加*处理的手机号
     */
    public static String phoneEncode(String phone) {
        if (!TextUtils.isEmpty(phone) && phone.length() > 8) {
            char[] chars = phone.toCharArray();
            for (int i = 3; i < 7; i++) {
                chars[i] = '*';
            }
            return new String(chars);
        }
        return phone;
    }
}
