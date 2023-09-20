package com.legend.base.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;

import com.legend.base.Applications;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class FileUtils {
    /**
     * APP保存根目录
     */
    public static final int FILE_TYPE_ROOT = 0;

    /**
     * 图片文件
     */
    public static final int FILE_TYPE_IMAGE = 1;

    /**
     * 视频文件
     */
    public static final int FILE_TYPE_VIDEO = 2;

    /**
     * 视频录制工具
     */
    public static final int FILE_TYPE_RECORD_TOOLS = 3;

    /**
     * 语音文件
     */
    public static final int FILE_TYPE_VOICE = 4;

    /**
     * 其它file文件
     */
    public static final int FILE_TYPE_FILE = 5;


    public static boolean hasFile(String filePath) {
        if (TextUtils.isEmpty(filePath))
            return false;
        File f = new File(filePath);
        if (!f.exists()) {
            return false;
        }
        return true;
    }

    public static String getFileProviderAuthorities(@NonNull Context context) {
        return context.getPackageName() + ".FileProvider";
    }


    /**
     * 获取App内部文件存储路径
     */
    public static String getAppFiles(Context context, int fileType) {
//        String rootPath = context.getFilesDir().getAbsolutePath() + File.separator + context.getPackageName() + File.separator;
        String rootPath = context.getExternalFilesDir("").getAbsolutePath() + File.separator + "ab235ddvvd22" + File.separator;
        String path;
        switch (fileType) {
            case FILE_TYPE_IMAGE:
                path = rootPath + "image" + File.separator;
                break;
            case FILE_TYPE_VIDEO:
                path = rootPath + "video" + File.separator;
                break;
            case FILE_TYPE_RECORD_TOOLS:
                path = rootPath + "recordTools" + File.separator;
                break;
            case FILE_TYPE_VOICE:
                path = rootPath + "voice" +File.separator;
                break;
            case FILE_TYPE_FILE:
                path = rootPath + "file" +File.separator;
                break;
            default:
                path = rootPath;
                break;
        }
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public static String getAppRootPath(Context context) {
//        return context.getFilesDir().getAbsolutePath() + File.separator + context.getPackageName();
        return context.getExternalFilesDir("").getAbsolutePath() + File.separator + "ab235ddvvd22";
    }

    public static String getAppRootPath(Context context, String sessionId) {
//        return context.getFilesDir().getAbsolutePath() + File.separator + context.getPackageName() + File.separator + sessionId;
        return context.getExternalFilesDir("").getAbsolutePath() + File.separator + "ab235ddvvd22" + File.separator + sessionId;
    }

    public static String getAppFiles(Context context, String sessionId, int fileType) {
        String rootPath = getAppRootPath(context, sessionId) + File.separator;
        String path;
        switch (fileType) {
            case FILE_TYPE_IMAGE:
                path = rootPath + "image" + File.separator;
                break;
            case FILE_TYPE_VIDEO:
                path = rootPath + "video" + File.separator;
                break;
            case FILE_TYPE_RECORD_TOOLS:
                path = rootPath + "recordTools" + File.separator;
                break;
            case FILE_TYPE_VOICE:
                path = rootPath + "voice" + File.separator;
                break;
            case FILE_TYPE_FILE:
                path = rootPath + "file" + File.separator;
                break;
            default:
                path = rootPath;
                break;
        }
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return path;
    }


    /**
     * 写入文件
     *
     * @param filePath 文件路径
     * @param content  上下文
     * @param append   是否追加
     * @return 写入是否完成
     */
    public static boolean writeFile(String filePath, String content, boolean append) throws IOException {

        if (TextUtils.isEmpty(content)) {
            return false;
        }

        FileWriter fileWriter = null;
        try {
            makeDirs(filePath);
            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content);
            fileWriter.close();
            return true;
        } finally {
            close(fileWriter);
        }
    }

    /**
     * @param filePath 路径
     * @return 是否创建成功
     */
    public static boolean makeDirs(String filePath) {

        String folderName = getFolderName(filePath);
        if (TextUtils.isEmpty(folderName)) {
            return false;
        }

        File folder = new File(folderName);
        return (folder.exists() && folder.isDirectory()) || folder.mkdirs();
    }


    public static String getFolderName(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return filePath;
        }
        int fileIndex = filePath.lastIndexOf(File.separator);
        return (fileIndex == -1) ? "" : filePath.substring(0, fileIndex);
    }

    public static void close(Closeable closeable) throws IOException {
        if (closeable != null)
            closeable.close();
    }


    public static File add2FileCache(Bitmap bitmap, Context context) throws FileNotFoundException, IOException {
        return add2FileCache(bitmap, context, null);
    }

    public static File add2FileCache(Bitmap bitmap, Context context, String token) throws FileNotFoundException, IOException {
        File file = null;
        File cacheDir = context.getCacheDir();
        if (!cacheDir.exists()) {// 创建目录
            cacheDir.mkdirs();
        } else if (cacheDir.isDirectory()) {// 删除目录中过期缓存文件
            File[] files = cacheDir.listFiles();
            long currentTime = System.currentTimeMillis();
            if (files != null) {
                for (File f : files) {
                    if (currentTime - f.lastModified() > 43200000) {
                        f.delete();
                        continue;
                    }
                }
            }
        }

        file = new File(cacheDir,
                token + new SimpleDateFormat("yyMMddHHmmss").format(new Date())
                        + ".jpg");
        FileOutputStream fos = new FileOutputStream(file);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, bos);
        byte[] array = bos.toByteArray();
        fos.write(array);
        fos.flush();
        fos.close();
        return file;
    }

    public static byte[] fileToBytes(String filePath) throws FileNotFoundException, IOException {
        byte[] buffer = null;
        File file = new File(filePath);

        FileInputStream fis = null;
        ByteArrayOutputStream bos = null;

        try {
            fis = new FileInputStream(file);
            bos = new ByteArrayOutputStream();

            byte[] b = new byte[1024];

            int n;

            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }

            buffer = bos.toByteArray();
        } finally {
            try {
                if (null != bos) {
                    bos.close();
                }
            } finally {
                if (null != fis) {
                    fis.close();
                }
            }
        }

        return buffer;
    }

    /**
     * Return the file by path.
     *
     * @param filePath The path of file.
     * @return the file
     */
    public static File getFileByPath(final String filePath) {
        return StringUtils.isSpace(filePath) ? null : new File(filePath);
    }

    /**
     * Create a file if it doesn't exist, otherwise delete old file before creating.
     *
     * @param filePath The path of file.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean createFileByDeleteOldFile(final String filePath) {
        return createFileByDeleteOldFile(getFileByPath(filePath));
    }

    /**
     * Create a file if it doesn't exist, otherwise delete old file before creating.
     *
     * @param file The file.
     * @return {@code true}: success<br>{@code false}: fail
     */
    public static boolean createFileByDeleteOldFile(final File file) {
        if (file == null) return false;
        // file exists and unsuccessfully delete then return false
        if (file.exists() && !file.delete()) return false;
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Create a directory if it doesn't exist, otherwise do nothing.
     *
     * @param dirPath The path of directory.
     * @return {@code true}: exists or creates successfully<br>{@code false}: otherwise
     */
    public static boolean createOrExistsDir(final String dirPath) {
        return createOrExistsDir(getFileByPath(dirPath));
    }

    /**
     * Create a directory if it doesn't exist, otherwise do nothing.
     *
     * @param file The file.
     * @return {@code true}: exists or creates successfully<br>{@code false}: otherwise
     */
    public static boolean createOrExistsDir(final File file) {
        return file != null && (file.exists() ? file.isDirectory() : file.mkdirs());
    }

    /**
     * Create a file if it doesn't exist, otherwise do nothing.
     *
     * @param filePath The path of file.
     * @return {@code true}: exists or creates successfully<br>{@code false}: otherwise
     */
    public static boolean createOrExistsFile(final String filePath) {
        return createOrExistsFile(getFileByPath(filePath));
    }

    /**
     * Create a file if it doesn't exist, otherwise do nothing.
     *
     * @param file The file.
     * @return {@code true}: exists or creates successfully<br>{@code false}: otherwise
     */
    public static boolean createOrExistsFile(final File file) {
        if (file == null) return false;
        if (file.exists()) return file.isFile();
        if (!createOrExistsDir(file.getParentFile())) return false;
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Notify system to scan the file.
     *
     * @param file The file.
     */
    public static void notifySystemToScan(final File file) {
        if (file == null || !file.exists()) return;
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.parse("file://" + file.getAbsolutePath()));
        Applications.getCurrent().sendBroadcast(intent);
    }


    /**
     * Return the MD5 of file.
     *
     * @param filePath The path of file.
     * @return the md5 of file
     */
    public static String getFileMD5ToString(final String filePath) {
        File file = StringUtils.isSpace(filePath) ? null : new File(filePath);
        return getFileMD5ToString(file);
    }

    /**
     * Return the MD5 of file.
     *
     * @param file The file.
     * @return the md5 of file
     */
    public static String getFileMD5ToString(final File file) {
        return ConvertUtils.bytes2HexString(getFileMD5(file));
    }

    /**
     * Return the MD5 of file.
     *
     * @param filePath The path of file.
     * @return the md5 of file
     */
    public static byte[] getFileMD5(final String filePath) {
        return getFileMD5(getFileByPath(filePath));
    }

    /**
     * Return the MD5 of file.
     *
     * @param file The file.
     * @return the md5 of file
     */
    public static byte[] getFileMD5(final File file) {
        if (file == null) return null;
        DigestInputStream dis = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            MessageDigest md = MessageDigest.getInstance("MD5");
            dis = new DigestInputStream(fis, md);
            byte[] buffer = new byte[1024 * 256];
            while (true) {
                if (!(dis.read(buffer) > 0)) break;
            }
            md = dis.getMessageDigest();
            return md.digest();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (dis != null) {
                    dis.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String getFileName(String path) {
        if (TextUtils.isEmpty(path)) return "";
        if (!path.contains("/")) return path;
        String[] splits = path.split("/");
        return splits[splits.length - 1];
    }

    public static String getFilePathExpectName(String path) {
        if (TextUtils.isEmpty(path)) return "";
        if (!path.contains("/")) return path;
        int position = path.lastIndexOf("/");
        return path.substring(0, position);
    }

    public static String getFileNameNoSuffix(String path) {
        if (TextUtils.isEmpty(path)) return "";
        if (!path.contains("/")) return path;
        String[] splits = path.split("/");
        String[] split = splits[splits.length - 1].split("\\.");

        return split[split.length - 1];
    }

    public static String getFileSuffix(String path) {
        if (TextUtils.isEmpty(path)) return "";
        if (path.contains("/")) {
            String[] tempPath = path.split("/");
            path = tempPath[tempPath.length - 1];
        }
        if (!path.contains(".")) return "";
        String[] splits = path.split("\\.");

        return splits[splits.length - 1];
    }

    public static String getOssFileName(String path) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int data = calendar.get(Calendar.DATE);
        String strMonth ;
        if (month < 10) {
            strMonth = "0" + month;
        } else {
            strMonth = "" + month;
        }
        String strData = year + File.separator + strMonth + File.separator + data + File.separator;
        return strData + System.currentTimeMillis() + "_" + md5(UUID.randomUUID().toString()) + "." + getFileSuffix(path);
    }

    public static String getSpecifyFileName(String fileSuffix) {
        return System.currentTimeMillis() + "_" + md5(UUID.randomUUID().toString()) + "." + fileSuffix;
    }

    public static String getAndroidFileName(String fileSuffix) {
        return "a_" + System.currentTimeMillis() + "." + fileSuffix;
    }

    public static String getAppFilePath() {
        return Applications.getCurrent().getFilesDir()+ "/";
    }

    @SuppressLint("DefaultLocale")
    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;
        if (size < kb) return String.format("%.2f B", (float) size);
        if (size < mb) return String.format("%.2f KB", (float) size / kb);
        if (size < gb) return String.format("%.2f MB", (float) size / mb);

        return String.format("%.2f G", (float) size / gb);
    }

    public static boolean copyFile(Context context, Uri srcUri, File destFile) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(srcUri);
            if (inputStream == null) return false;
            OutputStream outputStream = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                outputStream = Files.newOutputStream(destFile.toPath());
            } else {
                outputStream = new FileOutputStream(destFile);
            }
            copyStream(inputStream, outputStream);
            inputStream.close();
            outputStream.close();
        } catch (Exception e) {
            return false;
        }

        return true;
    }

    public static int copyStream(InputStream input, OutputStream output) throws Exception, IOException {
        final int BUFFER_SIZE = 1024 * 2;
        byte[] buffer = new byte[BUFFER_SIZE];
        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        BufferedOutputStream out = new BufferedOutputStream(output, BUFFER_SIZE);
        int count = 0, n = 0;
        try {
            while ((n = in.read(buffer, 0, BUFFER_SIZE)) != -1) {
                out.write(buffer, 0, n);
                count += n;
            }
            out.flush();
        } finally {
            out.close();
            in.close();
        }
        return count;
    }

    /**
     * 删除文件夹以及文件夹下的所有
     */
    public static void deleteAllFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteAllFile(f);
            }
            file.delete();
        } else if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 获取uri
     */
    public static Uri getUriFromPath(Context context, String filePath) {
        if (TextUtils.isEmpty(filePath)) return null;
        File file = new File(filePath);
        if (file.exists()) {
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                //7.0及更高版本，需要通过FileProvider生成Uri，前缀为content://
                uri = FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName() + ".FileProvider", file);
            } else {
                //低版本可直接通过文件生成Uri，前缀为file://
                uri = Uri.fromFile(file);
            }
            return uri;
        }

        return null;
    }

    private static String md5(String content) {
        byte[] hash = null;
        try {
            hash = MessageDigest.getInstance("MD5").digest(content.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            Log.i("md5", "md5 ex1 = " + e.getMessage());
        } catch (UnsupportedEncodingException e) {
            Log.i("md5", "md5 ex2 = " + e.getMessage());
        }

        if (hash == null) return "";

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10){
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public static Boolean saveQrToPhone(final Activity activity, final Bitmap bitmap) {
        String sdCardDir = Environment.getExternalStorageDirectory() + "/DCIM/sss/";
        File appDir = new File(sdCardDir);
        createOrExistsDir(appDir);
//        if (!appDir.exists()) {//不存在
//            appDir.mkdirs();
//        }
        String fileName = "a_"+ System.currentTimeMillis() + ".JPEG";
        File file = new File(appDir, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        final Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        activity.sendBroadcast(intent);
        return true;
    }


    /**
     * 根据Uri获取文件绝对路径，解决Android4.4以上版本Uri转换 兼容Android 10
     *
     * @param context
     * @param imageUri
     */
    public static String getFileAbsolutePath(Context context, Uri imageUri) {
        if (context == null || imageUri == null) {
            return null;
        }

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.KITKAT) {
            return getRealFilePath(context, imageUri);
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT && android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && DocumentsContract.isDocumentUri(context, imageUri)) {
            if (isExternalStorageDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadsDocument(imageUri)) {
                String id = DocumentsContract.getDocumentId(imageUri);
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (isMediaDocument(imageUri)) {
                String docId = DocumentsContract.getDocumentId(imageUri);
                String[] split = docId.split(":");
                String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                String selection = MediaStore.Images.Media._ID + "=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } // MediaStore (and general)
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
            return uriToFileApiQ(context,imageUri);
        }
        else if ("content".equalsIgnoreCase(imageUri.getScheme())) {
            // Return the remote address
            if (isGooglePhotosUri(imageUri)) {
                return imageUri.getLastPathSegment();
            }
            return getDataColumn(context, imageUri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(imageUri.getScheme())) {
            return imageUri.getPath();
        }
        return null;
    }

    //此方法 只能用于4.4以下的版本
    private static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) {
            return null;
        }
        final String scheme = uri.getScheme();
        String data = null;
        if (scheme == null) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            String[] projection = {MediaStore.Images.ImageColumns.DATA};
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);

//            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        String column = MediaStore.Images.Media.DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }


    /**
     * Android 10 以上适配 另一种写法
     * @param context
     * @param uri
     * @return
     */
    @SuppressLint("Range")
    private static String getFileFromContentUri(Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        String filePath;
        String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, filePathColumn, null,
                null, null);
        if (cursor != null) {
            cursor.moveToFirst();
            try {
                filePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
                return filePath;
            } catch (Exception e) {
            } finally {
                cursor.close();
            }
        }
        return "";
    }

    /**
     * Android 10 以上适配
     * @param context
     * @param uri
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static String uriToFileApiQ(Context context, Uri uri) {
        File file = null;
        //android10以上转换
        if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
            file = new File(uri.getPath());
        } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            //把文件复制到沙盒目录
            ContentResolver contentResolver = context.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") String displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                try {
                    InputStream is = contentResolver.openInputStream(uri);
                    File cache = new File(context.getExternalCacheDir().getAbsolutePath(), Math.round((Math.random() + 1) * 1000) + displayName);
                    FileOutputStream fos = new FileOutputStream(cache);
                    copyStream(is, fos);
                    file = cache;
                    fos.close();
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return file == null ? "" : file.getAbsolutePath();
    }


}
