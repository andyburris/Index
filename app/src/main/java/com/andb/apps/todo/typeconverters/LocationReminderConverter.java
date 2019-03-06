package com.andb.apps.todo.typeconverters;

import com.andb.apps.todo.objects.reminders.LocationReminder;
import com.andb.apps.todo.objects.reminders.SimpleReminder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.room.TypeConverter;

public class LocationReminderConverter {
    private static Gson gson = new Gson();

    @TypeConverter
    public static ArrayList<LocationReminder> locationRemindersArrayList(String data){
        if (data == null) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<ArrayList<LocationReminder>>() {
        }.getType();

        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String locationRemindersToString(ArrayList<LocationReminder> tagList) {
        return gson.toJson(tagList);
    }
}
