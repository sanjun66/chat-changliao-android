package com.legend.basenet.network.util;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SoChatEncryptUtil {
    private static final String CHARSET = "UTF-8";
    private static final String KEY = "hl9NaOeq75FGQB6W";
    private static final String IV  = "hl9NaOeq75FGQB6W";

    /**
     * AES加密操作
     * @param plaintext 明文
     * @return 密文
     */
    public static String encrypt(String plaintext) {
        try {
            SecretKeySpec secret = new SecretKeySpec(KEY.getBytes(CHARSET), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(IV.getBytes(CHARSET));
            cipher.init(Cipher.ENCRYPT_MODE, secret, iv);
            String encrypted = new String(Base64.encode(cipher.doFinal(plaintext.getBytes(CHARSET)),Base64.DEFAULT)).replaceAll("\r\n|\r|\n", "");
            return encrypted;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String encryptByte(byte[] plaintext) {
        try {
            SecretKeySpec secret = new SecretKeySpec(KEY.getBytes(CHARSET), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(IV.getBytes(CHARSET));
            cipher.init(Cipher.ENCRYPT_MODE, secret, iv);
            byte[] encrypted = cipher.doFinal(plaintext);
            return Base64.encodeToString(encrypted,Base64.DEFAULT);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * AES解密操作
     * @param ciphertext 密文
     * @return 明文
     */
    public static String decrypt(String ciphertext) {
        try {
            SecretKeySpec secret = new SecretKeySpec(KEY.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(IV.getBytes(CHARSET));
            cipher.init(Cipher.DECRYPT_MODE, secret, iv);
            byte[] decrypted = cipher.doFinal(Base64.decode(ciphertext, Base64.DEFAULT));
            return new String(decrypted, CHARSET);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
