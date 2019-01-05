package com.andb.apps.todo.typeconverters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.room.TypeConverter;

public class KeyListConverter {
    private static Gson gson = new Gson();

    @TypeConverter
    public static ArrayList<Integer> keyArrayList(String data) {
        if (data == null) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<ArrayList<Integer>>() {
        }.getType();

        return gson.fromJson(data, listType);

    }

    @TypeConverter
    public static String keyListToString(ArrayList<Integer> tagList) {
        return gson.toJson(tagList);
    }
}
