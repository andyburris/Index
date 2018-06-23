package com.andb.apps.todo;

import android.util.Log;

import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;

public class TaskMigrate implements Serializable {
    private String listName;
    private ArrayList<String> listItems;
    private ArrayList<Boolean> listItemsChecked;
    private ArrayList<Integer> listTags;
    private DateTime listDue;
    private boolean notified;
    private int key;
    //private ArrayList<Time> listDue; //to-do: add time due by

    public TaskMigrate() {
    }

    public TaskMigrate(String listName, ArrayList<String> listItems, ArrayList<Boolean> listItemsChecked, ArrayList<Integer> listTags, DateTime time, boolean notified) {
        this.listName = listName;
        this.listItems = listItems;
        this.listItemsChecked = listItemsChecked;
        this.listTags = listTags;
        this.listDue = time;
        this.notified = notified;
    }

    public TaskMigrate(String listName, ArrayList<String> listItems, ArrayList<Boolean> listItemsChecked, ArrayList<Integer> listTags, DateTime time, boolean notified, int key) {
        this.listName = listName;
        this.listItems = listItems;
        this.listItemsChecked = listItemsChecked;
        this.listTags = listTags;
        this.listDue = time;
        this.notified = notified;
        this.key = key;
    }


    public String getListName() {
        return listName;
    }

    public void setListName(String listName) {
        this.listName = listName;
    }


    public boolean doesListContainTag(int tags) {
        if (!listTags.isEmpty()) {
            for (int i = 0; i < listTags.size(); i++) {
                if (listTags.get(i) == tags) {
                    return true;
                }

            }
        }
        return false;

    }


    public String getListItems(int pos) {
        return listItems.get(pos);
    }

    public void setListItems(String item) {
        this.listItems.add(item);
    }

    public ArrayList<String> getAllListItems() {
        return listItems;
    }

    public int getListItemsSize() {
        return listItems.size();
    }

    public boolean isListItems() {
        if (listItems.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public Tasks toTask() {
        Tasks tasks = new Tasks(listName, listItems, listItemsChecked, listTags, listDue, notified, key);
        ;
        return tasks;
    }


    public boolean getListItemsChecked(int pos) {

        Log.d("browseCheckCheckingItem", Integer.toString(pos));

        if (listItemsChecked == null) {
            listItemsChecked = new ArrayList<>();
            for (int i = 1; i <= listItems.size(); i++) {
                listItemsChecked.add(false);
            }
        } else if (listItemsChecked.isEmpty() | listItemsChecked.size() == 0) {
            listItemsChecked = new ArrayList<>();
            for (int i = 1; i <= listItems.size(); i++) {
                listItemsChecked.add(false);
            }
        }
        return listItemsChecked.get(pos);
    }

    public void editListItemsChecked(boolean checked, int pos) {
        this.listItemsChecked.set(pos, checked);
    }

    public ArrayList<Boolean> getAllListItemsChecked() {
        return listItemsChecked;
    }


    public int getListTags(int pos) {
        Log.d("position", Integer.toString(pos));
        Log.d("position", Integer.toString(listTags.size()));
        return listTags.get(pos);
    }

    public void setListTags(int listPos, int tagPos) {
        this.listTags.set(listPos, tagPos);
    }

    public ArrayList<Integer> getAllListTags() {
        return listTags;
    }

    public int getListTagsSize() {
        return listTags.size();
    }

    public boolean isListTags() {
        if (listTags.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }


    public DateTime getDateTime() {
        return listDue;
    }

    public void setDateTime(DateTime datetime) {
        this.listDue = datetime;
    }

    public boolean isListTime() {
        if (new DateTime(listDue).isEqual(new DateTime(3000, 1, 1, 0, 0))) {
            return false;
        } else {
            return true;
        }
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public int getKey() {
        return this.key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
