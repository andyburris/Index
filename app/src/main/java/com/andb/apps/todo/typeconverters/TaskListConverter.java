package com.andb.apps.todo.typeconverters;

import com.andb.apps.todo.data.model.Task;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.room.TypeConverter;

public class TaskListConverter {
    private static Gson gson = new Gson();

    @TypeConverter
    public static ArrayList<Task> tasksArrayList(String data) {
        if (data == null) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<ArrayList<Task>>() {
        }.getType();

        return gson.fromJson(data, listType);

    }

    @TypeConverter
    public static String taskListToString(ArrayList<Task> tagList) {
        return gson.toJson(tagList);
    }
}
