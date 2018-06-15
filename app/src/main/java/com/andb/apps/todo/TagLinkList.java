package com.andb.apps.todo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class TagLinkList {
    public static ArrayList<TagLinks> linkList;
    public static String savedList = "linkList";

    public static int contains(int tag){
        for (int i = 0; i<linkList.size(); i++){
            if(linkList.get(i).tagParent()==tag){
                Log.d("tagAdding", "Tag: " + Integer.toString(tag) + " At:" + Integer.toString(i));
                return i;
            }
        }
        int returnInt = -1;
        Log.d("tagAdding", "Returning " + Integer.toString(returnInt));
        return returnInt;
    }

    public static TagLinks getLinkListItem(int pos){
        return linkList.get(pos);
    }

    public static void addLinkListItem(TagLinks tagLinks){
        linkList.add(tagLinks);
    }

    public static void replaceLinkListItem(int pos, TagLinks tagLinks){
        linkList.set(pos, tagLinks);
    }

    public static void loadTags(Context ctxt){
        linkList = getArrayList(savedList, ctxt);
        Log.d("resume", "List Initialized");
    }

    public static void saveTags(Context ctxt){
        saveArrayList(linkList, savedList, ctxt);
        Log.d("pause", "List Saved");
    }


    public static void saveArrayList(ArrayList<TagLinks> list, String key, Context ctxt){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(list);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!
    }

    public static ArrayList<TagLinks> getArrayList(String key, Context ctxt){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<TagLinks>>() {}.getType();
        return gson.fromJson(json, type);
    }
}
