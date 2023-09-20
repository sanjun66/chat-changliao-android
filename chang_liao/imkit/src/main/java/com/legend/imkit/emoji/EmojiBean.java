package com.legend.imkit.emoji;

/**
 * Describe: 表情的实体类
  */

public class EmojiBean {
    private int id;
    private int unicodeInt;

    private String strRes;

    public String getEmojiString() {
        return  getEmojiStringByUnicode(unicodeInt);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUnicodeInt() {
        return getEmojiStringByUnicode(unicodeInt);
    }

    public void setUnicodeInt(int unicodeInt) {
        this.unicodeInt = unicodeInt;
    }

    public String getStrRes() {
        return strRes;
    }

    public void setStrRes(String strRes) {
        this.strRes = strRes;
    }

    public static String getEmojiStringByUnicode(int unicode){
        char[] chars = Character.toChars(unicode);
        StringBuilder key =
                new StringBuilder(Character.toString(chars[0]));
        for (int i = 1; i < chars.length; i++) {
            key.append(chars[i]);
        }
        return key.toString();
    }

    @Override
    public String toString() {
        return "EmojiBean{" +
                "id=" + id +
                ", unicodeInt=" + unicodeInt +
                '}';
    }
}
