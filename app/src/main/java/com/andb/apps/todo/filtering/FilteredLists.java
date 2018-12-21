package com.andb.apps.todo.filtering;

import android.util.Log;
import android.view.View;

import com.andb.apps.todo.BrowseFragment;
import com.andb.apps.todo.InboxFragment;
import com.andb.apps.todo.TagFilter;
import com.andb.apps.todo.lists.TagLinkList;
import com.andb.apps.todo.lists.TagList;
import com.andb.apps.todo.lists.TaskList;
import com.andb.apps.todo.objects.Tags;
import com.andb.apps.todo.objects.Tasks;
import com.andb.apps.todo.settings.SettingsActivity;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.util.ArrayList;

public class FilteredLists {
    public static ArrayList<Tasks> inboxTaskList = new ArrayList<>();
    public static ArrayList<Tasks> browseTaskList = new ArrayList<>();
    public static ArrayList<Integer> filteredTagLinks = new ArrayList<>();

    public static void createFilteredTaskList(ArrayList<Integer> tagsToFilter, boolean viewing) {

        long startTime = System.nanoTime();

        ArrayList<Tasks> addToInbox = new ArrayList<>();
        ArrayList<Integer> noSubLinkList = new ArrayList<>();

        Log.d("noFilters", Integer.toString(tagsToFilter.size()));


        filteredTagLinks.clear();
        browseTaskList.clear();
        inboxTaskList.clear();

        Log.d("inboxFilterBrowse", Integer.toString(addToInbox.size()));


        if (!tagsToFilter.isEmpty()) {


            if (!TagList.tagList.isEmpty()) {
                for (int tag = 0; tag < TagList.tagList.size(); tag++) { //check all the tags

                    boolean contains = false;

                    int tagParent = Filters.getCurrentFilter().get(Filters.getCurrentFilter().size() - 1); //for the most recent filter
                    if (TagLinkList.contains(tagParent) != null) { //Catch error


                        if (TagLinkList.contains(tagParent).contains(tag)) { //and see if they are linked by the filters
                            Log.d("tagAdding", "Tag " + Integer.toString(tag) + " in.");

                            contains = true;
                        } else {
                            Log.d("tagAdding", "Tag " + Integer.toString(tag) + " in.");
                        }

                    } else {
                        Log.d("tagAdding", "Tag " + Integer.toString(tag) + " is not there.");
                    }

                    if (contains) { //and if so, add them

                        if (!tagsToFilter.contains(tag)) {//check if tag is part of filters
                            filteredTagLinks.add(tag);
                            if (!SettingsActivity.Companion.getSubFilter() || !TagList.tagList.get(tag).isSubFolder())
                                noSubLinkList.add(tag);
                        }


                    }

                }

            }

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
            //duration = duration;//to seconds

            Log.d("startupTime", "Create filtered tasklist - Tags: " + Long.toString(duration));

            startTime = System.nanoTime();


            if (!TaskList.taskList.isEmpty()) {

                Log.d("tagPredicate", "filtering");

                ArrayList<Tasks> tempList = new ArrayList<>();

                addToInbox.addAll(TaskList.taskList);
                Log.d("tagPredicate", "Size: " + Integer.toString(addToInbox.size()));
                tempList = new ArrayList<>(Collections2.filter(addToInbox, new TagFilter(tagsToFilter) {
                }));


                addToInbox.clear();
                addToInbox.addAll(tempList);

                tempList = new ArrayList<>();

                Log.d("tagPredicate", "Size: " + Integer.toString(addToInbox.size()));

                if (SettingsActivity.Companion.getFolderMode()) {
                    browseTaskList.addAll(TaskList.taskList);
                    tempList = new ArrayList<>(Collections2.filter(browseTaskList, new TagFilter(tagsToFilter, noSubLinkList) {
                    }));

                    browseTaskList.clear();
                    browseTaskList.addAll(tempList);
                } else {
                    browseTaskList.addAll(addToInbox);
                }
            }


            BrowseFragment.refreshWithAnim();

            BrowseFragment.tAdapter.notifyDataSetChanged();

        } else {


            Log.d("noFilters", "no filters");
            for (Tags tag : TagList.tagList) {//if there are no filters, return all tags except subfolders
                if (!tag.isSubFolder()) {
                    filteredTagLinks.add(TagList.tagList.indexOf(tag));
                }
            }
            Log.d("noFilters", "TagList size:" + Integer.toString(filteredTagLinks.size()));


            if (SettingsActivity.Companion.getFolderMode()) {//if folders, add all to inbox, only those w/o tags to browse

                ArrayList<Tasks> tempList = new ArrayList<>();

                browseTaskList.addAll(TaskList.taskList);

                tempList = new ArrayList<>(Collections2.filter(browseTaskList, new Predicate<Tasks>() {
                    @Override
                    public boolean apply(Tasks input) {
                        if (!input.isListTags()) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }));

                browseTaskList.clear();
                browseTaskList.addAll(tempList);

                addToInbox.addAll(TaskList.taskList);


            } else {//if not, add all to browse& inbox
                browseTaskList.addAll(TaskList.taskList);
                addToInbox.addAll(browseTaskList);
            }


            BrowseFragment.refreshWithAnim();

            BrowseFragment.tAdapter.notifyDataSetChanged();
        }


        if (viewing) {

            Log.d("inboxFilterBrowse", Integer.toString(addToInbox.size()));


            inboxTaskList.clear();
            inboxTaskList.addAll(addToInbox);

            InboxFragment.setFilterMode(InboxFragment.filterMode);


            InboxFragment.refreshWithAnim();


        }

        if (filteredTagLinks.isEmpty()) {
            BrowseFragment.tagCard.setVisibility(View.GONE);
            BrowseFragment.nestedScrollView.scrollTo(0, 0);
        } else {
            BrowseFragment.tagCard.setVisibility(View.VISIBLE);

        }

        if (Filters.getCurrentFilter().size() != 0) {
            InboxFragment.setTaskCountText(inboxTaskList.size());
        } else {
            InboxFragment.setTaskCountText(TaskList.taskList.size());
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration;//to seconds

        Log.d("startupTime", "Create filtered tasklist: " + Long.toString(duration));

    }
}
