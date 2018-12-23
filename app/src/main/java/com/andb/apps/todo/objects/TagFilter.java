package com.andb.apps.todo.objects;

import android.util.Log;

import com.andb.apps.todo.objects.Tasks;
import com.google.common.base.Predicate;

import java.util.ArrayList;
import java.util.Collections;

public class TagFilter implements Predicate<Tasks> {

    private ArrayList<Integer> tagList = new ArrayList<>();
    private boolean matches;
    private ArrayList<Integer> linkList = new ArrayList<>();


    public TagFilter(final ArrayList<Integer> tagList) {
        this.tagList = tagList;
        this.matches = false;
    }

    public TagFilter(final ArrayList<Integer> tagList, final ArrayList<Integer> linkList) {
        this.tagList = tagList;
        this.matches = !linkList.isEmpty();
        this.linkList = linkList;
    }

    @Override
    public boolean apply(Tasks tasks) {

        if (matches) {
            return tasks.getAllListTags().containsAll(tagList) && Collections.disjoint(tasks.getAllListTags(), linkList);

        } else {
            Log.d("tagPredicate", Boolean.toString(tasks.getAllListTags().containsAll(tagList)));
            return tasks.getAllListTags().containsAll(tagList);
        }


    }
}
