package com.andb.apps.todo.filtering;

import com.andb.apps.todo.objects.Tags;
import com.andb.apps.todo.settings.SettingsActivity;
import com.andb.apps.todo.utilities.Current;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class Filters {


    public static ArrayList<Tags> backTagFilters = new ArrayList<>();
    public static MutableLiveData<ArrayList<Tags>> filterObserver = new MutableLiveData<ArrayList<Tags>>() {
        @Nullable
        @Override
        public ArrayList<Tags> getValue() {
            return backTagFilters;
        }
    };

    public static ArrayList<Tags> getCurrentFilter() {
        return backTagFilters;
    }

    public static void homeViewAdd() {
        backTagFilters.clear();
        filterObserver.setValue(backTagFilters);
    }


    public static void tagBack() {
        //calling method needs to check for empty!!!

        backTagFilters.remove(backTagFilters.size() - 1);
        filterObserver.setValue(backTagFilters);
    }


    public static void tagForward(Tags tag) {
        backTagFilters.add(tag);//adds new filter to stack
        filterObserver.setValue(backTagFilters);

    }

    public static void tagReset(Tags tag) {
        homeViewAdd();
        backTagFilters.add(tag);
        filterObserver.setValue(backTagFilters);

    }

    public static void updateTag(int position, Tags tag){
        backTagFilters.set(position, tag);
        filterObserver.setValue(backTagFilters);
    }


    public static void updateMostRecent(Tags tag){
        backTagFilters.set(backTagFilters.size()-1, tag);
        filterObserver.setValue(backTagFilters);
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
