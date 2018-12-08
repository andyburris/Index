package com.andb.apps.todo.lists.interfaces;

import android.content.Context;

import com.andb.apps.todo.BrowseFragment;
import com.andb.apps.todo.filtering.FilteredLists;
import com.andb.apps.todo.filtering.Filters;
import com.andb.apps.todo.lists.TagLinkList;
import com.andb.apps.todo.lists.TagList;
import com.andb.apps.todo.objects.Tags;

public class TagListInterface {
    public static void addTag(String name, int color, boolean sub) {
        Tags tags = new Tags(name, color, sub);
        TagList.addTagList(tags);
        FilteredLists.createFilteredTaskList(Filters.getCurrentFilter(), true);
    }

    public static void replaceTag(String name, int color, int pos, boolean sub) {
        Tags tags = new Tags(name, color, sub);
        TagList.setTagList(pos, tags);
        FilteredLists.createFilteredTaskList(Filters.getCurrentFilter(), true);
    }

    public static void deleteTag(int pos, Context ctxt) {
        TagList.tagList.remove(pos);

        TaskListInterface.removeTagFromAll(pos);
        LinkListInterface.removeTagFromAll(pos);

        TagList.saveTags(ctxt);
        TagLinkList.saveTags(ctxt);

        Filters.backTagFilters.clear();
        Filters.homeViewAdd();
        FilteredLists.createFilteredTaskList(Filters.getCurrentFilter(), true);
    }
}
