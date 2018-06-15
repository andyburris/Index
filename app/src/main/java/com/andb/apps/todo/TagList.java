package com.andb.apps.todo;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.app.Application;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class TagList extends AppCompatActivity{
    public static ArrayList<Tags> tagList = new ArrayList<>();
    public static String savedList = "tagList";

    public static Tags getItem(int position){
        return tagList.get(position);
}
    public static void addTagList(Tags tag){
        tagList.add(tag);
    }
    public static void setTagList(int pos, Tags tag){
        tagList.set(pos, tag);
    }


    public static void loadTags(Context ctxt){
        tagList = getArrayList(savedList, ctxt);
        Log.d("resume", "List Initialized");
    }

    public static void saveTags(Context ctxt){
        saveArrayList(tagList, savedList, ctxt);
        Log.d("pause", "List Saved");
    }


    public static void saveArrayList(ArrayList<Tags> list, String key, Context ctxt){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!
    }

    public static ArrayList<Tags> getArrayList(String key, Context ctxt){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Tags>>() {}.getType();
        return gson.fromJson(json, type);
    }


}
