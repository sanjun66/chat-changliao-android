package com.legend.base.utils;

import android.text.TextUtils;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class DownloadUtils {
    private static final String TAG = "DownloadUtils";
    private static DownloadUtils sInstance;
    private Map<String, Boolean> mDownloadingMap = new HashMap<>();

    private DownloadUtils() {
    }

    public static DownloadUtils getInstance() {
        synchronized (DownloadUtils.class) {
            if (null == sInstance)
                sInstance = new DownloadUtils();
        }
        return sInstance;
    }

    private boolean canDownload(String url, String destFileDir, final String destFileName) {
        String key = new StringBuffer().append(url).append(destFileDir).append(destFileName).toString();
        synchronized (mDownloadingMap) {
            return !mDownloadingMap.containsKey(key);
        }
    }

    private void finishDownload(String url, String destFileDir, final String destFileName) {
        String key = new StringBuffer().append(url).append(destFileDir).append(destFileName).toString();
        synchronized (mDownloadingMap) {
            mDownloadingMap.remove(key);
        }
    }


    /**
     * @param url          下载连接
     * @param destFileDir  下载的文件储存目录
     * @param destFileName 下载文件名称，后面记得拼接后缀，否则手机没法识别文件类型
     * @param listener     下载监听
     */
    public void download(String url, final String destFileDir, final String destFileName, OnDownloadListener listener) {
        if (!canDownload(url, destFileDir, destFileName)) {
            if (null != listener)
                listener.onDownloadFailed(null, ErrorCode.URL_DOWNLOADING);
            return;
        }

        File dir = new File(destFileDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                if (null != listener) {
                    listener.log(String.format("mkdir fail, name : %s", destFileDir));
                    listener.onDownloadFailed(null, ErrorCode.MKDIR_FAIL);
                }
                return;
            }
        }


        InputStream inputStream = null;
        HttpURLConnection connection = null;
        boolean success = false;
        RandomAccessFile randomAccessFile = null;
        File tempFile = null;
        boolean callFlag = true;
        try {
            tempFile = new File(destFileDir, destFileName);
            if (null != listener)
                listener.log(String.format("download file save path:%s", tempFile.getAbsolutePath()));

            if (!tempFile.exists()) {
                if (!tempFile.createNewFile()) {
                    if (null != listener) {
                        listener.log("create file fail [" + tempFile.getName() + "]");
                        listener.onDownloadFailed(null, ErrorCode.MKDIR_FAIL);
                    }
                    callFlag = false;
                    return;
                }
            }

            url = encodeHttpUrl(url);
            URL sizeUrl = new URL(url);
            connection = (HttpURLConnection) sizeUrl.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            long totalSize = connection.getContentLength();
            connection.disconnect();

            if (totalSize <= 0) {
                if (null != listener) {
                    listener.log("create file fail [" + tempFile.getName() + "]");
                    listener.onDownloadFailed(null, ErrorCode.REMOTE_FILE_EXCEPTION);
                }
                callFlag = false;
                return;
            }

            randomAccessFile = new RandomAccessFile(tempFile, "rw");

            if (randomAccessFile.length() == totalSize) {
                if (null != listener)
                    listener.onDownloadSuccess(tempFile);
                callFlag = false;
                if (null != listener)
                    listener.log("file download done, [" + tempFile.getName() + "]");
                success = true;
                return;
            } else if (randomAccessFile.length() > totalSize) {
                tempFile.delete();
                tempFile = new File(destFileDir, destFileName);
                if (!tempFile.createNewFile()) {
                    if (null != listener) {
                        listener.log("create file fail [" + tempFile.getName() + "]");
                        listener.onDownloadFailed(null, ErrorCode.MKDIR_FAIL);
                    }
                    callFlag = false;
                    return;
                }
                randomAccessFile = new RandomAccessFile(tempFile, "rw");
            }

            randomAccessFile.seek(randomAccessFile.length());

            URL connectUrl = new URL(url);
            if (null != listener)
                listener.log(String.format("download file url:%s", url));

            connection = (HttpURLConnection) connectUrl.openConnection();
            connection.setRequestProperty("Range", "bytes=" + randomAccessFile.length() + "-");
            connection.setConnectTimeout(30 * 1000);  //链接超时30秒
            connection.setReadTimeout(60 * 10 * 1000); //下载超时10分钟
            int responseCode = connection.getResponseCode();
            if (responseCode == 200 || responseCode == 206) {
                int total = connection.getContentLength();
                inputStream = connection.getInputStream();
                byte[] buffer = new byte[4096];
                int len;
                long sum = randomAccessFile.length();
                if (null != listener)
                    listener.log("download file start...");
                total += sum;
                while ((len = inputStream.read(buffer)) > 0) {
                    randomAccessFile.write(buffer, 0, len);
                    sum += len;
                    int progress = (int) (sum * 1.0f / total * 100);
                    if (null != listener) ;
                    listener.onDownloading(progress);
                }
                if (null != listener)
                    listener.log("download file finish...");
                success = true;
            } else {
                String responseMessage = connection.getResponseMessage();
                if (null != listener)
                    listener.log(String.format(Locale.CHINESE, "download file connect failed... | responseCode:%d;responseMsg:%s", responseCode, responseMessage));
            }
        } catch (Exception e) {
            if (null != listener)
                listener.log(null != e ? e.getMessage() : "download fail ,has unknown exception");
        } finally {
            finishDownload(url, destFileDir, destFileName);

            if (!success)
                tempFile.delete();

            if (null != listener && callFlag) {
                if (success)
                    listener.onDownloadSuccess(tempFile);
                else
                    listener.onDownloadFailed(null, ErrorCode.UNKNOWN);
            }

            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (Exception e) {
                    if (null != listener)
                        listener.log(null != e ? e.getMessage() : "randomAccessFile close fail ,has unknown exception");
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) {
                    if (null != listener)
                        listener.log(null != e ? e.getMessage() : "inputStream close fail ,has unknown exception");
                }
            }
            if (connection != null) {
                try {
                    connection.disconnect();
                } catch (Exception e) {
                    if (null != listener)
                        listener.log(null != e ? e.getMessage() : "connection close fail ,has unknown exception");
                }
            }
        }

    }


    /**
     * 处理http请求url中包含的中文字符
     *
     * @param url
     * @return
     */
    public static String encodeHttpUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        char[] chars = url.toCharArray();
        StringBuilder result = new StringBuilder();
        for (char c : chars) {
            if (c >= 0 && c <= 255) {
                result.append(c);
            } else {
                try {
                    String encode = URLEncoder.encode(String.valueOf(c), "UTF-8");
                    result.append(encode);
                } catch (Exception e) {
                }
            }
        }
        return result.toString();
    }


    public interface OnDownloadListener {

        /**
         * 下载成功之后的文件
         */
        void onDownloadSuccess(File file);

        /**
         * 下载进度
         */
        void onDownloading(int progress);

        /**
         * 下载异常信息
         */

        void onDownloadFailed(Exception e, ErrorCode code);

        /**
         * log记录
          * @param msg
         */
        void log(String msg);
    }

    public enum ErrorCode {
        MKDIR_FAIL,
        URL_DOWNLOADING,
        REMOTE_FILE_EXCEPTION,
        UNKNOWN;
    }
}
