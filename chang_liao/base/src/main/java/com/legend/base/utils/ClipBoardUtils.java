package com.legend.base.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

import java.util.List;

public class ClipBoardUtils {


    /**
     * 复制文本到剪切板
     */
    public static boolean clipboardCopyText(Context context, String label, CharSequence text) {
        //获取剪贴板管理器：
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            // 创建普通字符型ClipData
            ClipData clipData = ClipData.newPlainText(label, text);
            // 将ClipData内容放到系统剪贴板里。
            cm.setPrimaryClip(clipData);
            return true;
        }
        return false;
    }

    public interface Function {
        void invoke(List<String> textList);
    }
}
