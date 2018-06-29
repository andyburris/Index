package com.andb.apps.todo.typeconverters;

import android.arch.persistence.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;


public class CheckedConverter {


    private static Gson gson = new Gson();

    @TypeConverter
    public static ArrayList<Boolean> checkedArrayList(String data) {
        if (data == null) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<ArrayList<Boolean>>() {
        }.getType();

        return gson.fromJson(data, listType);

    }

    @TypeConverter
    public static String checkedListToString(ArrayList<Boolean> checkedList) {
        return gson.toJson(checkedList);
    }
}
