package com.andb.apps.todo.lists.interfaces;

import android.app.Activity;
import android.util.Log;

import com.andb.apps.todo.BrowseFragment;
import com.andb.apps.todo.filtering.FilteredLists;
import com.andb.apps.todo.filtering.Filters;
import com.andb.apps.todo.TagSelect;
import com.andb.apps.todo.lists.TagLinkList;
import com.andb.apps.todo.objects.TagLinks;

import java.util.ArrayList;

public class LinkListInterface {
    public static boolean addLinkToCurrentDirectory(int position, Activity flashbarActivity){
        int tagParent = Filters.getCurrentFilter().get(Filters.getCurrentFilter().size() - 1);

        if (TagLinkList.contains(tagParent) != null) {

            if (TagLinkList.contains(tagParent).contains(position)) {
                TagSelect.tagExists(flashbarActivity).show();
            } else if (position == tagParent) {
                TagSelect.sameTag(flashbarActivity).show();
            } else {
                TagLinkList.contains(tagParent).addLink(position);

                FilteredLists.createFilteredTaskList(Filters.getCurrentFilter(), true);
                BrowseFragment.mAdapter.notifyDataSetChanged();
                return true;
            }
            return false;
        } else {
            Log.d("tagAdding", "Starting links for tag " + Integer.toString(tagParent) + ".");
            ArrayList<Integer> arrayList = new ArrayList<>();
            arrayList.add(position);
            TagLinkList.addLinkListItem(new TagLinks(tagParent, arrayList));



            FilteredLists.createFilteredTaskList(Filters.getCurrentFilter(), true);
            BrowseFragment.mAdapter.notifyDataSetChanged();
            return true;

        }


    }

    public static void removeTagFromAll(int pos){
        if (!TagLinkList.linkList.isEmpty()) {
            ArrayList<TagLinks> toRemoveParent = new ArrayList<>();
            for (TagLinks tagLinks : TagLinkList.linkList) {
                if (tagLinks.tagParent() == pos) {
                    toRemoveParent.add(tagLinks);
                } else {
                    ArrayList<Integer> toRemoveChild = new ArrayList<>();
                    for (int linkPos : tagLinks.getAllTagLinks()) {
                        if (linkPos == pos) {
                            toRemoveChild.add(linkPos);
                        } else if (linkPos > pos) {
                            tagLinks.getAllTagLinks().set(tagLinks.getAllTagLinks().indexOf(linkPos), linkPos - 1);
                        }
                    }
                    tagLinks.getAllTagLinks().removeAll(toRemoveChild);
                }
            }
            TagLinkList.linkList.removeAll(toRemoveParent);
        }

    }
}
