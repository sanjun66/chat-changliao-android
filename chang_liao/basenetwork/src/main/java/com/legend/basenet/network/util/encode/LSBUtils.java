package com.legend.basenet.network.util.encode;

import android.graphics.Bitmap;
import android.text.TextUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class LSBUtils {

    public static final String TAG = "LSB";

    public static String decode(Bitmap encryptBitmap, String key) {

        // 补齐128位
        key = convertKeyTo128bit(key);

        //splitting images
        List<Bitmap> srcEncodedList = Utility.splitImage(encryptBitmap);

        //decoding encrypted zipped message
        String decoded_message = EncodeDecode.decodeMessage(srcEncodedList);

        //Log.d("LSB", "Decoded_Message : " + decoded_message);

        //decrypting the encoded message
        String decrypted_message = decryptMessage(decoded_message, key);
        //Log.d("LSB", "Decrypted message : " + decrypted_message);

        return decrypted_message;
    }


    public static Bitmap encode(Bitmap bitmap, String key, String message) {

        if (bitmap == null || TextUtils.isEmpty(key) || TextUtils.isEmpty(message)) {
            return null;
        }

        int originalHeight = bitmap.getHeight();

        int originalWidth = bitmap.getWidth();

        List<Bitmap> src_list = Utility.splitImage(bitmap);

        key = convertKeyTo128bit(key);

        String encryptedMessage = encryptMessage(message, key);

        List<Bitmap> encoded_list = EncodeDecode.encodeMessage(src_list, encryptedMessage, null);

        //free Memory
        for (Bitmap bitm : src_list)
            bitm.recycle();

        //Java Garbage collector
        System.gc();

        //merging the split encoded image
        return Utility.mergeImage(encoded_list, originalHeight, originalWidth);
    }

    public static void encodeAndSave(Bitmap bitmap, String key, String message, String outputFile) {
        Bitmap srcEncoded = encode(bitmap, key, message);
        if (outputFile == null) {
            throw new IllegalArgumentException("output file cannot be null");
        }
        File file = new File(outputFile);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            srcEncoded.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
            //Log.e(TAG, "encodeAndSave: ", e);
        }
    }

    public static String decryptMessage(String message, String secret_key) {
        String decrypted_message = "";

        if (message != null) {
            if (!Utility.isStringEmpty(secret_key)) {
                try {
                    decrypted_message = Crypto.decryptMessage(message, secret_key);
                } catch (Exception e) {
                    //Log.d(TAG, "Error : " + e.getMessage() + " , may be due to wrong key.");
                }
            } else {
                decrypted_message = message;
            }
        }

        return decrypted_message;
    }

    private static String encryptMessage(String message, String secret_key) {
        //Log.d(TAG, "Message : " + message);

        String encrypted_message = "";
        if (message != null) {
            if (!Utility.isStringEmpty(secret_key)) {
                try {
                    encrypted_message = Crypto.encryptMessage(message, secret_key);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                encrypted_message = message;
            }
        }

        //Log.d(TAG, "Encrypted_message : " + encrypted_message);

        return encrypted_message;
    }

    private static String convertKeyTo128bit(String secret_key) {

        StringBuilder result = new StringBuilder(secret_key);

        if (secret_key.length() <= 16) {
            for (int i = 0; i < (16 - secret_key.length()); i++) {
                result.append("#");
            }
        } else {
            result = new StringBuilder(result.substring(0, 15));
        }

        //Log.d(TAG, "Secret Key Length : " + result.toString().getBytes().length);

        return result.toString();
    }
}
