package com.andb.apps.todo;

import com.google.common.base.Predicate;

import java.util.ArrayList;
import java.util.Collections;

public class TagFilter implements Predicate<Tasks> {

    ArrayList<Integer> tagList = new ArrayList<>();
    boolean matches;
    ArrayList<Integer> linkList = new ArrayList<>();

    public TagFilter(final ArrayList<Integer> tagList) {
        this.tagList = tagList;
        this.matches = false;
    }

    public TagFilter(final ArrayList<Integer> tagList, final ArrayList<Integer> linkList) {
        this.tagList = tagList;
        this.matches = true;
        this.linkList = linkList;
    }

    @Override
    public boolean apply(Tasks tasks) {

        if (matches) {
            if (tasks.getAllListTags().containsAll(tagList) && !Collections.disjoint(tasks.getAllListTags(), linkList)) {
                return true;
            } else {
                return false;
            }
        } else {
            if (tasks.getAllListTags().containsAll(tagList)) {
                return true;
            } else {
                return false;
            }
        }


    }
}
