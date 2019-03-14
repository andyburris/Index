package com.andb.apps.todo.filtering;

import android.util.Log;

import com.andb.apps.todo.BrowseFragment;
import com.andb.apps.todo.InboxFragment;
import com.andb.apps.todo.MainActivity;
import com.andb.apps.todo.settings.SettingsActivity;
import com.andb.apps.todo.utilities.Current;

import java.util.ArrayList;

public class Filters {


    public static ArrayList<Integer> backTagFilters = new ArrayList<>();


    public static ArrayList<Integer> getCurrentFilter() {
        //Log.d("backStack", "Size: " + Integer.toString(backTagFilters.get(backTagFilters.size() - 1).size()));
        return backTagFilters;
    }

    public static void homeViewAdd(){
        backTagFilters.clear();
    }


    public static void tagBack() {
        //calling method needs to check for empty!!!

        backTagFilters.remove(backTagFilters.size() - 1);


        FilteredLists.INSTANCE.createFilteredTaskList(getCurrentFilter(), true);


    }


    public static void tagForward(int tag) {
        backTagFilters.add(tag);//adds new filter to stack

        FilteredLists.INSTANCE.createFilteredTaskList(getCurrentFilter(), true);//filters tasklist with new filter

    }

    public static void tagReset(int tag) {

        if (SettingsActivity.Companion.getFolderMode()) {
            backTagFilters.clear();        //if folders back to home, if filter back to last multi-tag filter; right now folder behavior
        }
        homeViewAdd();
        backTagFilters.add(tag);
        FilteredLists.INSTANCE.createFilteredTaskList(getCurrentFilter(), true);



    }

    public static int getMostRecent() {
        if (!getCurrentFilter().isEmpty()) {
            return getCurrentFilter().get(getCurrentFilter().size() - 1);
        } else {
            return -1;
        }
    }

    public static String path(){

        StringBuilder subtitle = new StringBuilder("All");

        if(backTagFilters.size()>0 && Current.hasProjects()) {
            for (int f : getCurrentFilter()) {
                subtitle.append("/").append(Current.tagList().get(f).getTagName());
            }
        }

        return subtitle.toString();
    }
}
