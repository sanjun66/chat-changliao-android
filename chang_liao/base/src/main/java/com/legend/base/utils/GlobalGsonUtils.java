package com.legend.base.utils;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.ToNumberPolicy;
import com.google.gson.TypeAdapter;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.legend.base.utils.gson.DoubleDefaultAdapter;
import com.legend.base.utils.gson.IntegerDefaultAdapter;
import com.legend.base.utils.gson.JSONArrayAdapter;
import com.legend.base.utils.gson.JSONObjectAdapter;
import com.legend.base.utils.gson.LongDefaultAdapter;
import com.legend.base.utils.gson.StringNullAdapter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class GlobalGsonUtils {
    private static final Gson sGson = buildGson();

    private static final String DATEFORMAT_default = "yyyy-MM-dd HH:mm:ss";

    public static Gson buildGson() {
        return new GsonBuilder()
                .registerTypeAdapter(Integer.class, new IntegerDefaultAdapter())
                .registerTypeAdapter(int.class, new IntegerDefaultAdapter())
                .registerTypeAdapter(Double.class, new DoubleDefaultAdapter())
                .registerTypeAdapter(double.class, new DoubleDefaultAdapter())
                .registerTypeAdapter(Long.class, new LongDefaultAdapter())
                .registerTypeAdapter(long.class, new LongDefaultAdapter())
                .registerTypeAdapter(String.class, new StringNullAdapter())
                .registerTypeAdapter(
                        new TypeToken<TreeMap<String, Object>>(){}.getType(),
                        new JsonDeserializer<TreeMap<String, Object>>() {
                            @Override
                            public TreeMap<String, Object> deserialize(
                                    JsonElement json, Type typeOfT,
                                    JsonDeserializationContext context) throws JsonParseException {

                                TreeMap<String, Object> treeMap = new TreeMap<>();
                                JsonObject jsonObject = json.getAsJsonObject();
                                Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                                for (Map.Entry<String, JsonElement> entry : entrySet) {
                                    Object ot = entry.getValue();
                                    if(ot instanceof JsonPrimitive){
                                        treeMap.put(entry.getKey(), ((JsonPrimitive) ot).getAsString());
                                    }else{
                                        treeMap.put(entry.getKey(), ot);
                                    }
                                }
                                return treeMap;
                            }
                        }
                )
                .registerTypeAdapter(JSONObject.class, new JSONObjectAdapter())
                .registerTypeAdapter(JSONArray.class, new JSONArrayAdapter())
                .setObjectToNumberStrategy(ToNumberPolicy.LAZILY_PARSED_NUMBER)
                .create();
    }
    public static String toJson(final Object object) {
        return sGson.toJson(object);
    }

    public static <T> T fromJson(final String json, @NonNull final Class<T> type) {
        T t = null;
        try {
            t = fromJson(sGson, json, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public static <T> T fromJson(final String json, @NonNull final Type type) {
        T t = null;
        try {
            t = fromJson(sGson, json, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public static <T> T fromJson(final Object object, @NonNull final Type type) {
        T t = null;
        String json = sGson.toJson(object);
        try {
            t = fromJson(sGson, json, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public static <T> T fromJson(@NonNull final Reader reader, @NonNull final Class<T> type) {
        T t = null;
        try {
            t = fromJson(sGson, reader, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public static <T> T fromJson(@NonNull final Reader reader, @NonNull final Type type) {
        T t = null;
        try {
            t = fromJson(sGson, reader, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public static <T> T fromJson(@NonNull final Gson gson, final String json, @NonNull final Class<T> type) {
        T t = null;
        try {
            t = gson.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public static <T> T fromJson(@NonNull final Gson gson, final String json, @NonNull final Type type) {
        T t = null;
        try {
            t = gson.fromJson(json, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public static <T> T fromJson(@NonNull final Gson gson, final Reader reader, @NonNull final Class<T> type) {
        T t = null;
        try {
            t = gson.fromJson(reader, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public static <T> T fromJson(@NonNull final Gson gson, final Reader reader, @NonNull final Type type) {
        T t = null;
        try {
            t = gson.fromJson(reader, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public static <T> List<T> changeGsonToList(String gsonString, Type type) {
        List<T> list = null;
        try {
            list = sGson.fromJson(gsonString, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static <T> List<Map<String, T>> changeGsonToListMaps(String gsonString) {
        List<Map<String, T>> list = null;
        try {
            list = sGson.fromJson(gsonString, new TypeToken<List<Map<String, T>>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static <T> Map<String, T> changeGsonToMaps(String gsonString) {
        Map<String, T> map = null;
        try {
            map = sGson.fromJson(gsonString, new TypeToken<Map<String, T>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static <T> String createArrayToString(List<T> list) {
        String gsonString = null;
        try {
            gsonString = sGson.toJson(list, new TypeToken<List<T>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return gsonString;
    }

    public static class MapTypeAdapter extends TypeAdapter<Object> {

        @Override
        public Object read(JsonReader in) throws IOException {
            JsonToken token = in.peek();
            switch (token) {
                case BEGIN_ARRAY:
                    List<Object> list = new ArrayList<Object>();
                    in.beginArray();
                    while (in.hasNext()) {
                        list.add(read(in));
                    }
                    in.endArray();
                    return list;

                case BEGIN_OBJECT:
                    Map<String, Object> map = new LinkedTreeMap<String, Object>();
                    in.beginObject();
                    while (in.hasNext()) {
                        map.put(in.nextName(), read(in));
                    }
                    in.endObject();
                    return map;

                case STRING:
                    return in.nextString();

                case NUMBER:
                    /**
                     * 改写数字的处理逻辑，将数字值分为整型与浮点型。
                     */
                    double dbNum = in.nextDouble();

                    // 数字超过long的最大值，返回浮点类型
                    if (dbNum > Long.MAX_VALUE) {
                        return dbNum;
                    }

                    // 判断数字是否为整数值
                    long lngNum = (long) dbNum;
                    if (dbNum == lngNum) {
                        return lngNum;
                    } else {
                        return dbNum;
                    }

                case BOOLEAN:
                    return in.nextBoolean();

                case NULL:
                    in.nextNull();
                    return null;

                default:
                    throw new IllegalStateException();
            }
        }

        @Override
        public void write(JsonWriter out, Object value) throws IOException {
            // 序列化无需实现
        }

    }
}
