package com.andb.apps.todo.filtering;

import android.util.Log;

import com.andb.apps.todo.BrowseFragment;
import com.andb.apps.todo.InboxFragment;
import com.andb.apps.todo.objects.Tags;
import com.andb.apps.todo.objects.Tasks;
import com.andb.apps.todo.settings.SettingsActivity;
import com.andb.apps.todo.utilities.Current;

import java.util.ArrayList;
import java.util.Collections;

import javax.annotation.Nullable;

public class FilteredLists {
    public static ArrayList<Tasks> inboxTaskList = new ArrayList<>();
    public static ArrayList<Tasks> browseTaskList = new ArrayList<>();
    public static ArrayList<Integer> filteredTagLinks = new ArrayList<>();

    public static void createFilteredTaskList(ArrayList<Integer> tagsToFilter, boolean viewing) {


        Log.d("noFilters", Integer.toString(tagsToFilter.size()));

        /*need to maintain object reference for recyclerview*/
        inboxTaskList.clear();
        browseTaskList.clear();
        filteredTagLinks.clear();

        int parentIndex = Filters.getMostRecent();
        if (parentIndex > -1) {
            Tags tagParent = Current.tagList().get(parentIndex);

            if (tagParent.getChildren() == null) {
                tagParent.setChildren(new ArrayList<>());
            }
            filteredTagLinks.addAll(filterChildren(Current.tagList(), tagParent, Filters.getCurrentFilter()));
        } else {
            filteredTagLinks.addAll(filterChildren(Current.tagList(), null, Filters.getCurrentFilter()));
        }


        inboxTaskList.addAll(filterInbox(Current.taskList(), Filters.getCurrentFilter()));
        browseTaskList.addAll(filterBrowse(inboxTaskList, Filters.getCurrentFilter(), filteredTagLinks, Current.tagList(), SettingsActivity.getSubFilter()));


        BrowseFragment.refreshWithAnim();

        BrowseFragment.tAdapter.notifyDataSetChanged();


        //if (viewing) {
        InboxFragment.Companion.setFilterMode();
        InboxFragment.Companion.refreshWithAnim();
        //InboxFragment.Companion.getMAdapter().notifyDataSetChanged();

        Log.d("inboxTaskList", "inboxTaskList: " + inboxTaskList.size() + ", InboxFragment.mAdapter.taskList: " + InboxFragment.Companion.getMAdapter().getTaskList().size());
        for (Tasks t : InboxFragment.mAdapter.getTaskList()) {
            Log.d("inboxTaskList", t.toString());
        }
        //}


        InboxFragment.Companion.setTaskCountText(inboxTaskList.size());


    }

    public static ArrayList<Integer> filterChildren(ArrayList<Tags> tags, @Nullable Tags parent, ArrayList<Integer> previousFilters) {

        ArrayList<Integer> filteredList = new ArrayList<>();

        if (parent != null) {
            for (int tag : parent.getChildren()) { //check all the tags

                if (!previousFilters.contains(tag)) {//check if tag is part of filters

                    if (!previousFilters.isEmpty()) {
                        filteredList.add(tag);
                    }
                }

            }
        } else {
            int i = 0;
            for (Tags t : tags) {
                if (!t.isSubFolder()) {//show all but subfolders in /All
                    filteredList.add(i);
                }
                i++;
            }
        }

        return filteredList;
    }

    public static ArrayList<Tasks> filterInbox(ArrayList<Tasks> tasks, ArrayList<Integer> parents) {

        ArrayList<Tasks> filteredList = new ArrayList<>();

        for (Tasks t : tasks) {
            if (t.getListTags().containsAll(parents)) {
                filteredList.add(t);
            }
        }

        return filteredList;
    }

    public static ArrayList<Tasks> filterBrowse(ArrayList<Tasks> tasks, ArrayList<Integer> parents, ArrayList<Integer> children, ArrayList<Tags> tags, Boolean subfolderAsFilter) {

        ArrayList<Tasks> filteredList = new ArrayList<>();

        ArrayList<Integer> toRemove = new ArrayList<>();
        for (int c : children) {
            if (subfolderAsFilter && tags.get(c).isSubFolder()) {
                toRemove.add(c);
            }
        }
        children.removeAll(toRemove);

        for (Tasks t : tasks) {
            if (!parents.isEmpty()) {
                if (t.getListTags().containsAll(parents) && Collections.disjoint(t.getListTags(), children)) {
                    filteredList.add(t);
                }
            } else {
                if (!t.isListTags()) {
                    filteredList.add(t);
                }
            }
        }

        return filteredList;
    }
}
