package com.andb.apps.todo.filtering;

import android.util.Log;

import com.andb.apps.todo.BrowseFragment;
import com.andb.apps.todo.InboxFragment;
import com.andb.apps.todo.objects.Tags;
import com.andb.apps.todo.objects.Tasks;
import com.andb.apps.todo.settings.SettingsActivity;
import com.andb.apps.todo.utilities.Current;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.util.ArrayList;

public class FilteredLists {
    public static ArrayList<Tasks> inboxTaskList = new ArrayList<>();
    public static ArrayList<Tasks> browseTaskList = new ArrayList<>();
    public static ArrayList<Integer> filteredTagLinks = new ArrayList<>();

    public static void createFilteredTaskList(ArrayList<Integer> tagsToFilter, boolean viewing) {

        ArrayList<Tasks> addToInbox = new ArrayList<>();
        ArrayList<Integer> noSubLinkList = new ArrayList<>();

        Log.d("noFilters", Integer.toString(tagsToFilter.size()));


        filteredTagLinks.clear();
        browseTaskList.clear();
        inboxTaskList.clear();

        Log.d("inboxFilterBrowse", Integer.toString(addToInbox.size()));


        if (!tagsToFilter.isEmpty()) {


            if (!Current.project().getTagList().isEmpty()) {

                Tags tagParent = Current.tagList().get(Filters.getMostRecentTag());

                if(tagParent.getChildren()==null){
                    tagParent.setChildren(new ArrayList<>());
                }

                for (int tag : tagParent.getChildren()) { //check all the tags

                    if (!tagsToFilter.contains(tag)) {//check if tag is part of filters
                        filteredTagLinks.add(tag);
                        if (!SettingsActivity.Companion.getSubFilter() || !Current.project().getTagList().get(tag).isSubFolder())
                            noSubLinkList.add(tag);
                    }


                }

            }

            if (!Current.project().getTaskList().isEmpty()) {

                Log.d("tagPredicate", "filtering");


                addToInbox = new ArrayList<>(Collections2.filter(Current.taskList(), new TagFilter(tagsToFilter) {
                }));

                Log.d("tagPredicate", "Size: " + Integer.toString(addToInbox.size()));

                if (SettingsActivity.Companion.getFolderMode()) {

                    browseTaskList = new ArrayList<>(Collections2.filter(Current.taskList(), new TagFilter(tagsToFilter, noSubLinkList) {
                    }));

                } else {
                    browseTaskList.addAll(addToInbox);
                }
            }


            BrowseFragment.refreshWithAnim();

            BrowseFragment.tAdapter.notifyDataSetChanged();

        } else {


            Log.d("noFilters", "no filters");
            for (Tags tag : Current.project().getTagList()) {//if there are no filters, return all tags except subfolders
                if (!tag.isSubFolder()) {
                    filteredTagLinks.add(Current.project().getTagList().indexOf(tag));
                }
            }
            Log.d("noFilters", "TagList size:" + Integer.toString(filteredTagLinks.size()));


            if (SettingsActivity.Companion.getFolderMode()) {//if folders, add all to inbox, only those w/o tags to browse

                browseTaskList = new ArrayList<>(Collections2.filter(Current.taskList(), new Predicate<Tasks>() {
                    @Override
                    public boolean apply(Tasks input) {
                        return !input.isListTags();
                    }
                }));

                addToInbox.addAll(Current.project().getTaskList());

            } else {//if not, add all to browse & inbox
                browseTaskList.addAll(Current.project().getTaskList());
                addToInbox.addAll(browseTaskList);
            }


            BrowseFragment.refreshWithAnim();

            BrowseFragment.tAdapter.notifyDataSetChanged();
        }


        if (viewing) {

            Log.d("inboxFilterBrowse", Integer.toString(addToInbox.size()));


            inboxTaskList.clear();
            inboxTaskList.addAll(addToInbox);

            InboxFragment.Companion.setFilterMode(InboxFragment.Companion.getFilterMode());


            InboxFragment.Companion.refreshWithAnim();
        }


        if (Filters.getCurrentFilter().size() != 0) {
            InboxFragment.Companion.setTaskCountText(inboxTaskList.size());
        } else {
            InboxFragment.Companion.setTaskCountText(Current.project().getTaskList().size());
        }


    }
}
