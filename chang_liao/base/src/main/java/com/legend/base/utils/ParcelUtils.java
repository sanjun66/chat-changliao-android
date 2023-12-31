package com.legend.base.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParcelUtils {
    public ParcelUtils() {
    }

    public static void writeToParcel(Parcel out, String obj) {
        out.writeString(obj);
    }

    public static void writeToParcel(Parcel out, Long obj) {
        out.writeLong(obj != null ? obj : 0L);
    }

    public static void writeToParcel(Parcel out, Integer obj) {
        out.writeInt(obj != null ? obj : 0);
    }

    public static void writeToParcel(Parcel out, Float obj) {
        out.writeFloat(obj != null ? obj : 0.0F);
    }

    public static void writeToParcel(Parcel out, Double obj) {
        out.writeDouble(obj != null ? obj : 0.0);
    }

    public static void writeToParcel(Parcel out, Map obj) {
        out.writeMap(obj);
    }

    public static void writeToParcel(Parcel out, Date obj) {
        out.writeLong(obj != null ? obj.getTime() : 0L);
    }

    public static Float readFloatFromParcel(Parcel in) {
        return in.readFloat();
    }

    public static Double readDoubleFromParcel(Parcel in) {
        return in.readDouble();
    }

    public static Date readDateFromParcel(Parcel in) {
        long value = in.readLong();
        return value != 0L ? new Date(value) : null;
    }

    public static Integer readIntFromParcel(Parcel in) {
        return in.readInt();
    }

    public static Long readLongFromParcel(Parcel in) {
        return in.readLong();
    }

    public static String readFromParcel(Parcel in) {
        return in.readString();
    }

    public static Map readMapFromParcel(Parcel in) {
        return in.readHashMap(HashMap.class.getClassLoader());
    }

    public static <T extends Parcelable> T readFromParcel(Parcel in, Class<T> cls) {
        T t = in.readParcelable(cls.getClassLoader());
        return t;
    }

    public static <T extends Parcelable> void writeToParcel(Parcel out, T model) {
        out.writeParcelable(model, 0);
    }

    public static <T extends List<?>> void writeToParcel(Parcel out, T model) {
        out.writeList(model);
    }

    public static <T> ArrayList<T> readListFromParcel(Parcel in, Class<T> cls) {
        return in.readArrayList(cls.getClassLoader());
    }

    public static void writeListToParcel(Parcel out, List<?> collection) {
        out.writeList(collection);
    }

    public static <T extends Parcelable> T bytesToParcelable(byte[] data, Class<T> cls) {
        if (data != null && data.length != 0) {
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            T t = readFromParcel(in, cls);
            in.recycle();
            return t;
        } else {
            return null;
        }
    }

    public static byte[] parcelableToByte(Parcelable model) {
        if (model == null) {
            return null;
        } else {
            Parcel parcel = Parcel.obtain();
            writeToParcel(parcel, model);
            return parcel.marshall();
        }
    }

    public static <T extends Parcelable> List<T> bytesToParcelableList(byte[] data, Class<T> cls) {
        if (data != null && data.length != 0) {
            Parcel in = Parcel.obtain();
            in.unmarshall(data, 0, data.length);
            in.setDataPosition(0);
            ArrayList t = readListFromParcel(in, cls);
            in.recycle();
            return t;
        } else {
            return null;
        }
    }

    public static byte[] parcelableListToByte(List<? extends Parcelable> list) {
        if (list == null) {
            return null;
        } else {
            Parcel parcel = Parcel.obtain();
            writeListToParcel(parcel, list);
            return parcel.marshall();
        }
    }
}
