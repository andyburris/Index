package com.andb.apps.todo.filtering;

import android.util.Log;
import android.view.View;

import com.andb.apps.todo.BrowseFragment;
import com.andb.apps.todo.InboxFragment;
import com.andb.apps.todo.objects.TagFilter;
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

        long startTime = System.nanoTime();

        ArrayList<Tasks> addToInbox = new ArrayList<>();
        ArrayList<Integer> noSubLinkList = new ArrayList<>();

        Log.d("noFilters", Integer.toString(tagsToFilter.size()));


        filteredTagLinks.clear();
        browseTaskList.clear();
        inboxTaskList.clear();

        Log.d("inboxFilterBrowse", Integer.toString(addToInbox.size()));


        if (!tagsToFilter.isEmpty()) {


            if (!Current.project().getTagList().isEmpty()) {
                for (int tag = 0; tag < Current.project().getTagList().size(); tag++) { //check all the tags

                    boolean contains = false;

                    int tagParent = Filters.getCurrentFilter().get(Filters.getCurrentFilter().size() - 1); //for the most recent filter


                    if (Current.tagList().get(tagParent).getChildren().contains(tag)) { //and see if they are linked by the filters
                        Log.d("tagAdding", "Tag " + Integer.toString(tag) + " in.");
                        contains = true;
                    } else {
                        Log.d("tagAdding", "Tag " + Integer.toString(tag) + " in.");
                    }


                    if (contains) { //and if so, add them

                        if (!tagsToFilter.contains(tag)) {//check if tag is part of filters
                            filteredTagLinks.add(tag);
                            if (!SettingsActivity.Companion.getSubFilter() || !Current.project().getTagList().get(tag).isSubFolder())
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


            if (!Current.project().getTaskList().isEmpty()) {

                Log.d("tagPredicate", "filtering");

                ArrayList<Tasks> tempList = new ArrayList<>();

                addToInbox.addAll(Current.project().getTaskList());
                Log.d("tagPredicate", "Size: " + Integer.toString(addToInbox.size()));
                tempList = new ArrayList<>(Collections2.filter(addToInbox, new TagFilter(tagsToFilter) {
                }));


                addToInbox.clear();
                addToInbox.addAll(tempList);

                tempList = new ArrayList<>();

                Log.d("tagPredicate", "Size: " + Integer.toString(addToInbox.size()));

                if (SettingsActivity.Companion.getFolderMode()) {
                    browseTaskList.addAll(Current.project().getTaskList());
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
            for (Tags tag : Current.project().getTagList()) {//if there are no filters, return all tags except subfolders
                if (!tag.isSubFolder()) {
                    filteredTagLinks.add(Current.project().getTagList().indexOf(tag));
                }
            }
            Log.d("noFilters", "TagList size:" + Integer.toString(filteredTagLinks.size()));


            if (SettingsActivity.Companion.getFolderMode()) {//if folders, add all to inbox, only those w/o tags to browse

                ArrayList<Tasks> tempList = new ArrayList<>();

                browseTaskList.addAll(Current.project().getTaskList());

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

                addToInbox.addAll(Current.project().getTaskList());


            } else {//if not, add all to browse& inbox
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

        if (filteredTagLinks.isEmpty()) {
            BrowseFragment.tagCard.setVisibility(View.GONE);
            BrowseFragment.nestedScrollView.scrollTo(0, 0);
        } else {
            BrowseFragment.tagCard.setVisibility(View.VISIBLE);

        }

        if (Filters.getCurrentFilter().size() != 0) {
            InboxFragment.Companion.setTaskCountText(inboxTaskList.size());
        } else {
            InboxFragment.Companion.setTaskCountText(Current.project().getTaskList().size());
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration;//to seconds

        Log.d("startupTime", "Create filtered tasklist: " + Long.toString(duration));

    }
}
