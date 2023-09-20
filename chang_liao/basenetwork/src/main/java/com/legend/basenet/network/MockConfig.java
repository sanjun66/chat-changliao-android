package com.legend.basenet.network;

import android.content.Context;
import android.os.Environment;

import com.legend.basenet.network.util.FileUtils;

import java.io.File;

public class MockConfig {

    private final Context context;
    /**
     * 模拟最小请求时间
     */
    private int minDelayMilliseconds = 0;
    /**
     * 模拟最大请求时间
     */
    private int maxDelayMilliseconds = 0;
    /**
     * 模拟错误率（0~100）
     */
    private int failurePercentage = -1;
    /**
     * mock 数据读取模式（支持assets和sdcard）
     */
    private int mode;

    private MockConfig(Context context) {
        this.context = context;
    }

    public static MockConfig create(Context context) {
        return new MockConfig(context);
    }

    public int getMinDelayMilliseconds() {
        return minDelayMilliseconds;
    }

    public int getMaxDelayMilliseconds() {
        return maxDelayMilliseconds;
    }

    public int getFailurePercentage() {
        return failurePercentage;
    }

    public MockConfig setMinDelayMilliseconds(int minDelayMilliseconds) {
        this.minDelayMilliseconds = minDelayMilliseconds;
        return this;
    }

    public MockConfig setMaxDelayMilliseconds(int maxDelayMilliseconds) {
        this.maxDelayMilliseconds = maxDelayMilliseconds;
        return this;
    }

    public MockConfig setFailurePercentage(int failurePercentage) {
        this.failurePercentage = failurePercentage;
        return this;
    }

    /**
     * 设置mock数据读取模式（assets / sdcard）
     *
     * MODE_SDCARD
     * MODE_ASSETS
     */
    public MockConfig setMode(int mode) {
        this.mode = mode;
        return this;
    }

    public static final int MODE_SDCARD = 0;
    public static final int MODE_ASSETS = 1;

    public String getMockString(String path) {
        if (mode == 1) {
            return getAssetsAsString(path);
        } else {
            return getSdcardAsString(path);
        }
    }

    private String getAssetsAsString(String path) {
        if (context == null) {
            throw new IllegalArgumentException("MockConfig must be created with context!");
        }
        if (path == null) {
            return null;
        }
        String mockPath = "mock";
        if (!path.startsWith("/")) {
            mockPath += "/";
        }
        return FileUtils.getAssetsAsString(context, mockPath + path);
    }

    private String getSdcardAsString(String path) {
        if (context == null) {
            throw new IllegalArgumentException("MockConfig must be created with context!");
        }

        if (path == null) {
            return null;
        }

        String mockPath = new File(Environment.getExternalStorageDirectory(), "mock/" + path).getPath();

        return FileUtils.getSdcardFileAsString(mockPath);
    }

}
