package com.andb.apps.todo.filtering;

import com.andb.apps.todo.objects.Tags;
import com.andb.apps.todo.settings.SettingsActivity;
import com.andb.apps.todo.utilities.Current;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

public class Filters {


    public static ArrayList<Tags> backTagFilters = new ArrayList<>();
    public static LiveData<ArrayList<Tags>> filterObserver = new LiveData<ArrayList<Tags>>() {
        @Nullable
        @Override
        public ArrayList<Tags> getValue() {
            return backTagFilters;
        }
    };

    public static ArrayList<Tags> getCurrentFilter() {
        //Log.d("backStack", "Size: " + Integer.toString(backTagFilters.get(backTagFilters.size() - 1).size()));
        return backTagFilters;
    }

    public static void homeViewAdd() {
        backTagFilters.clear();
    }


    public static void tagBack() {
        //calling method needs to check for empty!!!

        backTagFilters.remove(backTagFilters.size() - 1);

    }


    public static void tagForward(Tags tag) {
        backTagFilters.add(tag);//adds new filter to stack
    }

    public static void tagReset(Tags tag) {

        if (SettingsActivity.Companion.getFolderMode()) {
            backTagFilters.clear();        //if folders back to home, if filter back to last multi-tag filter; right now folder behavior
        }
        homeViewAdd();
        backTagFilters.add(tag);

    }

    public static Tags getMostRecent() {
        if (!getCurrentFilter().isEmpty()) {
            return getCurrentFilter().get(getCurrentFilter().size() - 1);
        } else {
            return null;
        }
    }

    public static String path() {

        StringBuilder subtitle = new StringBuilder("All");

        if (backTagFilters.size() > 0 && Current.hasProjects()) {
            for (Tags f : getCurrentFilter()) {
                subtitle.append("/").append(f.getTagName());
            }
        }

        return subtitle.toString();
    }
}
