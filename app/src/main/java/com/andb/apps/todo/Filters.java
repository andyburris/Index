package com.andb.apps.todo;

import android.util.Log;

import java.util.ArrayList;

public class Filters {

    public static String subtitle = "All";

    public static ArrayList<ArrayList<Integer>> backTagFilters = new ArrayList<>();


    public static ArrayList<Integer> getCurrentFilter() {
        Log.d("backStack", "Size: " + Integer.toString(backTagFilters.get(backTagFilters.size()-1).size()));

        return backTagFilters.get(backTagFilters.size() - 1);
    }

    public static void homeViewAdd() {
        backTagFilters.add(new ArrayList<Integer>());
    }


    public static void tagBack() {
        //calling method needs to check for empty!!!

        backTagFilters.remove(backTagFilters.size() - 1);


        BrowseFragment.createFilteredTaskList(getCurrentFilter(),true);

        subtitle = "All";


        Log.d("subtitle", subtitle);

        for(int i = 0; i<getCurrentFilter().size(); i++){
            subtitle +=  "/"+ TagList.getItem(getCurrentFilter().get(i)).getTagName();
        }

        MainActivity.subTitle.setSubtitle(subtitle);
    }


    public static void tagForward(int tag) {
        ArrayList<Integer> newFilter = new ArrayList<>(backTagFilters.get(backTagFilters.size() - 1));//copies old filter
        newFilter.add(tag);//adds tag that is sent to it
        backTagFilters.add(newFilter);//adds new filter to stack

        Log.d("backStack", Integer.toString(backTagFilters.get(backTagFilters.size()-2).size()) + ", " + Integer.toString(backTagFilters.get(backTagFilters.size()-1).size()));

        BrowseFragment.createFilteredTaskList(getCurrentFilter(), true);//filters tasklist with new filter
        BrowseFragment.mAdapter.notifyDataSetChanged();//updates recyclerviews


        subtitle = "All";

        Log.d("subtitle", subtitle);


        for(int i = 0; i<getCurrentFilter().size(); i++){
            subtitle += "/"+ TagList.getItem(getCurrentFilter().get(i)).getTagName();
        }


        MainActivity.subTitle.setSubtitle(subtitle);
    }

    public static void tagReset(int tag) {

        if(SettingsActivity.folderMode) {
            backTagFilters.clear();        //if folders back to home, if filter back to last multi-tag filter; right now folder behavior
        }
        homeViewAdd();
        ArrayList<Integer> newFilter = new ArrayList<Integer>(backTagFilters.get(backTagFilters.size() - 1));
        newFilter.add(tag);
        backTagFilters.add(newFilter);
        BrowseFragment.createFilteredTaskList(getCurrentFilter(),true);
        BrowseFragment.mAdapter.notifyDataSetChanged();

        subtitle = "All";


        Log.d("subtitle", subtitle);

        for(int i = 0; i<getCurrentFilter().size(); i++){
            subtitle +=  "/"+ TagList.getItem(getCurrentFilter().get(i)).getTagName();
        }

        MainActivity.subTitle.setSubtitle(subtitle);
    }

    public static int getMostRecent() {
        return getCurrentFilter().get(getCurrentFilter().size() - 1);
    }
}
