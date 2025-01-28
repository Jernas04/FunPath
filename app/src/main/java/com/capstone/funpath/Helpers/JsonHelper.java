package com.capstone.funpath.Helpers;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class JsonHelper {
    private final Context context;

    public JsonHelper(Context context) {
        this.context = context;
    }

    public List<Item> loadItemsFromJson(String key, int jsonId) {
        try {
            InputStream is = context.getResources().openRawResource(jsonId);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            Type type = new TypeToken<Map<String, List<Item>>>() {}.getType();
            Map<String, List<Item>> data = new Gson().fromJson(json, type);
            return data.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class Item {
        public String name;
        public String image;
        public List<String> choices;
    }
}