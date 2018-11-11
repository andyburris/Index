package com.andb.apps.todo.typeconverters;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.room.TypeConverter;


public class ItemsConverter {


    private static Gson gson = new Gson();

    @TypeConverter
    public static ArrayList<String> itemsArrayList(String data) {
        if (data == null) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<ArrayList<String>>() {
        }.getType();

        return gson.fromJson(data, listType);

    }

    @TypeConverter
    public static String itemsListToString(ArrayList<String> tagList) {
        return gson.toJson(tagList);
    }
}
