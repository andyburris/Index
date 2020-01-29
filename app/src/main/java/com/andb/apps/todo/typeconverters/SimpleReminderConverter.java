package com.andb.apps.todo.typeconverters;

import com.andb.apps.todo.data.model.reminders.SimpleReminder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.room.TypeConverter;

public class SimpleReminderConverter {
    private static Gson gson = new Gson();

    @TypeConverter
    public static ArrayList<SimpleReminder> timeRemindersArrayList(String data){
        if (data == null) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<ArrayList<SimpleReminder>>() {
        }.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String timeRemindersToString(ArrayList<SimpleReminder> tagList) {
        return gson.toJson(tagList);
    }
}
