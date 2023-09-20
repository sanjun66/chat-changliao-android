package com.legend.basenet.network.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public final class FileUtils {

    public static final String TAG = FileUtils.class.getSimpleName();

    private FileUtils() {
    }

    /**
     * 获取Assets目录下文本文件内容
     * @param context context
     * @param path assets file path
     * @return assets file content
     */
    public static String getAssetsAsString(Context context, String path) {
        try {
            InputStream inputStream = context.getAssets().open(path);
            return loadInputStreamAsString(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 读取File文本
     * @param path file path
     * @return file text content
     */
    public static String getSdcardFileAsString(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return null;
        }
        try {
            FileInputStream inputStream = new FileInputStream(file);
            return loadInputStreamAsString(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String loadInputStreamAsString(InputStream inputStream) {
        BufferedReader bufferedReader = null;
        try {
            StringBuilder stringBuilder = new StringBuilder();
            if(inputStream == null) {
                return null;
            }
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

            String str;
            boolean isFirst = true;
            while ((str = bufferedReader.readLine()) != null) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    stringBuilder.append('\n');
                }
                stringBuilder.append(str);
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            Log.e(TAG, "Error: opening stream :" + e.getMessage());
        } finally {
            close(bufferedReader);
        }
        return null;
    }

    private static void close(Closeable c) {
        try {
            c.close();
        } catch (Exception e) {
            Log.e(TAG, "Error: closing stream :" + e.getMessage());
        }
    }

}