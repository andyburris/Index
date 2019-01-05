package com.andb.apps.todo.typeconverters;

import com.andb.apps.todo.objects.Tasks;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.room.TypeConverter;

public class TaskListConverter {
    private static Gson gson = new Gson();

    @TypeConverter
    public static ArrayList<Tasks> tasksArrayList(String data) {
        if (data == null) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<ArrayList<Tasks>>() {
        }.getType();

        return gson.fromJson(data, listType);

    }

    @TypeConverter
    public static String taskListToString(ArrayList<Tasks> tagList) {
        return gson.toJson(tagList);
    }
}
