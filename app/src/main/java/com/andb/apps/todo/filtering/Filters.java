package com.andb.apps.todo.filtering;

import android.util.Log;

import com.andb.apps.todo.BrowseFragment;
import com.andb.apps.todo.InboxFragment;
import com.andb.apps.todo.settings.SettingsActivity;
import com.andb.apps.todo.utilities.Current;

import java.util.ArrayList;

public class Filters {


    public static ArrayList<ArrayList<Integer>> backTagFilters = new ArrayList<>();


    public static ArrayList<Integer> getCurrentFilter() {
        //Log.d("backStack", "Size: " + Integer.toString(backTagFilters.get(backTagFilters.size() - 1).size()));

        return backTagFilters.get(backTagFilters.size() - 1);
    }


    public static void homeViewAdd() {
        homeViewAdd(true);
    }
    public static void homeViewAdd(boolean updatePath){
        backTagFilters.add(new ArrayList<Integer>());
        if (updatePath) {
            setPath();
        }
    }


    public static void tagBack() {
        //calling method needs to check for empty!!!

        backTagFilters.remove(backTagFilters.size() - 1);


        FilteredLists.createFilteredTaskList(getCurrentFilter(), true);

        setPath();


/*        if (getCurrentFilter().size() > 0) {
            MainActivity.toolbarTitle.setText(Current.tagList().get(getMostRecent()).getTagName());
        } else {
            MainActivity.toolbarTitle.setText(R.string.app_name);
        }*/
    }


    public static void tagForward(int tag) {
        ArrayList<Integer> newFilter = new ArrayList<>(backTagFilters.get(backTagFilters.size() - 1));//copies old filter
        newFilter.add(tag);//adds tag that is sent to it
        backTagFilters.add(newFilter);//adds new filter to stack

        Log.d("backStack", Integer.toString(backTagFilters.get(backTagFilters.size() - 2).size()) + ", " + Integer.toString(backTagFilters.get(backTagFilters.size() - 1).size()));

        FilteredLists.createFilteredTaskList(getCurrentFilter(), true);//filters tasklist with new filter
        BrowseFragment.mAdapter.notifyDataSetChanged();//updates recyclerviews

        setPath();
    }

    public static void tagReset(int tag) {

        if (SettingsActivity.Companion.getFolderMode()) {
            backTagFilters.clear();        //if folders back to home, if filter back to last multi-tag filter; right now folder behavior
        }
        homeViewAdd();
        ArrayList<Integer> newFilter = new ArrayList<Integer>(backTagFilters.get(backTagFilters.size() - 1));
        newFilter.add(tag);
        backTagFilters.add(newFilter);
        FilteredLists.createFilteredTaskList(getCurrentFilter(), true);
        BrowseFragment.mAdapter.notifyDataSetChanged();

        setPath();


    }

    public static int getMostRecent() {
        if (!getCurrentFilter().isEmpty()) {
            return getCurrentFilter().get(getCurrentFilter().size() - 1);
        } else {
            return -1;
        }
    }

    public static void setPath(){

        StringBuilder subtitle = new StringBuilder("All");

        if(backTagFilters.size()>0 && Current.hasProjects()) {
            for (int f : getCurrentFilter()) {
                subtitle.append("/").append(Current.tagList().get(f).getTagName());
            }
        }

        InboxFragment.Companion.setPathText(subtitle.toString());
    }
}
