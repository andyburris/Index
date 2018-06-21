package com.andb.apps.todo;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class NotificationHolder {
    private static String savedList = "notificationsList";


    public static ArrayList<Integer> lastPositionList = new ArrayList<>();

    public static int getLastPosition() {
        return lastPositionList.get(lastPositionList.size() - 1);
    }

    public static int getPreviousPosition(int pos) {
        return lastPositionList.get(pos);
    }

    public static void addPosition(int position) {
        lastPositionList.add(position);
    }

    public static ArrayList<Integer> getLastPositionList() {
        return lastPositionList;
    }

    public static void onDelete(int threshold) {
        for (int i = 0; i < lastPositionList.size(); i++) {
            if (lastPositionList.get(i) > threshold) {
                int temp = lastPositionList.get(i) - 1;
                lastPositionList.set(i, temp);
            }
        }
    }

    public static void loadTasks(Context ctxt) {
        lastPositionList = getArrayList(savedList, ctxt);
        Log.d("resume", "Tasks Initialized");
    }

    public static void saveTasks(Context ctxt) {
        saveArrayList(lastPositionList, savedList, ctxt);
        Log.d("pause", "Tasks Saved");
    }

    public static void saveArrayList(ArrayList<Integer> list, String key, Context ctxt) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!
    }

    public static ArrayList<Integer> getArrayList(String key, Context ctxt) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Integer>>() {
        }.getType();
        return gson.fromJson(json, type);
    }
}
