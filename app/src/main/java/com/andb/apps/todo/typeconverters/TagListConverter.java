package com.andb.apps.todo.typeconverters;

import com.andb.apps.todo.objects.Tags;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.room.TypeConverter;

public class TagListConverter {
    private static Gson gson = new Gson();

    @TypeConverter
    public static ArrayList<Tags> tagListArrayList(String data) {
        if (data == null) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<ArrayList<Tags>>() {
        }.getType();

        return gson.fromJson(data, listType);

    }

    @TypeConverter
    public static String tagListToString(ArrayList<Tags> tagList) {
        return gson.toJson(tagList);
    }
}
