package com.andb.apps.todo.lists;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.andb.apps.todo.objects.Tasks;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class ArchiveTaskList extends AppCompatActivity{
    public static ArrayList<Tasks> taskList = new ArrayList<>();
    public static String savedList = "archiveTaskList";

    public static Tasks getItem(int position){
        return taskList.get(position);
}
    public static void addTaskList(Tasks task){
        taskList.add(task);
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
        //Gson gson = Converters.registerDateTime(new GsonBuilder()).create();
        Gson gson = new Gson();
        String json = gson.toJson(task);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!
    }

    public static ArrayList<Tasks> getArrayList(String key, Context ctxt){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        //Gson gson = Converters.registerDateTime(new GsonBuilder()).create();
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Tasks>>() {}.getType();
        return gson.fromJson(json, type);
        /*ArrayList<Tasks> tasksArrayList = new ArrayList<>();
        for(int i = 0; i<taskMigrates.size(); i++){
            tasksArrayList.add(taskMigrates.get(i).toTask());
        }
        return tasksArrayList;*/
    }


}
