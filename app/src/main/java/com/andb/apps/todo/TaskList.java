package com.andb.apps.todo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.joda.time.convert.Converter;
import org.joda.time.convert.ConverterManager;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class TaskList extends AppCompatActivity{
    public static ArrayList<Tasks> taskList = new ArrayList<>();
    public static String savedList = "taskList";



    public static Tasks getItem(int position){
        return taskList.get(position);
}

    public static void setTaskList(int position, Tasks tasks){
        taskList.set(position, tasks);
    }
    public static void addTaskList(Tasks task){
        taskList.add(task);
    }
    public static void addTaskListAtPos(int pos, Tasks task){
        taskList.add(pos, task);
    }





    public static void loadTasks(Context ctxt){
        taskList = getArrayList(savedList, ctxt);
        Log.d("resume", "Tasks Initialized");
    }

    public static void saveTasks(Context ctxt){
        saveArrayList(taskList, savedList, ctxt);
        Log.d("pause", "Tasks Saved");
    }







    public static void saveArrayList(ArrayList<Tasks> task, String key, Context ctxt){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = Converters.registerDateTime(new GsonBuilder()).create();
        String json = gson.toJson(task);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!
    }

    public static ArrayList<Tasks> getArrayList(String key, Context ctxt){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        Gson gson = Converters.registerDateTime(new GsonBuilder()).create();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Tasks>>() {}.getType();
        return gson.fromJson(json, type);
    }


}
