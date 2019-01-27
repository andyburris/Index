package com.andb.apps.todo.objects;

import android.util.Log;

import com.andb.apps.todo.InboxFragment;
import com.andb.apps.todo.settings.SettingsActivity;
import com.andb.apps.todo.typeconverters.CheckedConverter;
import com.andb.apps.todo.typeconverters.ItemsConverter;
import com.andb.apps.todo.typeconverters.TagConverter;
import com.andb.apps.todo.utilities.Current;
import com.andb.apps.todo.utilities.ProjectsUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.util.ArrayList;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import static com.andb.apps.todo.utilities.Values.TIME_SORT;

@Entity(foreignKeys = @ForeignKey(entity = BaseProject.class, parentColumns = "key", childColumns = "project_id", onDelete = ForeignKey.CASCADE))
public class Tasks implements Cloneable {
    @SerializedName("key")
    @Expose
    @PrimaryKey
    private int listKey;

    @SerializedName("list_name")
    @Expose
    @ColumnInfo(name = "list_name")
    private String listName;

    @SerializedName("list_items")
    @Expose
    @TypeConverters(ItemsConverter.class)
    @ColumnInfo(name = "list_items")
    private ArrayList<String> listItems;

    @SerializedName("list_items_checked")
    @Expose
    @TypeConverters(CheckedConverter.class)
    @ColumnInfo(name = "list_items_checked")
    private ArrayList<Boolean> listItemsChecked;

    @SerializedName("list_tags")
    @Expose
    @TypeConverters(TagConverter.class)
    @ColumnInfo(name = "list_tags")
    private ArrayList<Integer> listTags;

    @SerializedName("list_due")
    @Expose
    @ColumnInfo(name = "list_due")
    private long listDue;

    @SerializedName("list_notified")
    @Expose
    @ColumnInfo(name = "list_notified")
    private boolean notified;

    @SerializedName("project_id")
    @Expose
    @ColumnInfo(name = "project_id")
    private int projectId;

    @SerializedName("archived")
    @Expose
    @ColumnInfo(name = "archived")
    private boolean archived;

    @Ignore
    private boolean editing = false;

    @Ignore
    public Tasks(String listName, ArrayList<String> listItems, ArrayList<Boolean> listItemsChecked, ArrayList<Integer> listTags, DateTime time, boolean notified) {
        this.listName = listName;
        this.listItems = listItems;
        this.listItemsChecked = listItemsChecked;
        this.listTags = listTags;
        this.listDue = time.getMillis();
        this.notified = notified;
        this.listKey = ProjectsUtils.keyGenerator();
        this.archived = false;
        this.projectId = Current.project().getKey();
    }

    @Ignore
    public Tasks(String listName, ArrayList<String> listItems, ArrayList<Boolean> listItemsChecked, ArrayList<Integer> listTags, DateTime time, boolean notified, int listKey, int projectId, boolean archived) {
        this.listName = listName;
        this.listItems = listItems;
        this.listItemsChecked = listItemsChecked;
        this.listTags = listTags;
        this.listDue = time.getMillis();
        this.notified = notified;
        this.listKey = listKey;
        this.projectId = projectId;
        this.archived = archived;
    }

    public Tasks(int listKey, String listName, ArrayList<String> listItems, ArrayList<Boolean> listItemsChecked, ArrayList<Integer> listTags, long listDue, boolean notified, int projectId, boolean archived) {
        this.listKey = listKey;
        this.listName = listName;
        this.listItems = listItems;
        this.listItemsChecked = listItemsChecked;
        this.listTags = listTags;
        this.listDue = listDue;
        this.notified = notified;
        this.projectId = projectId;
        this.archived = archived;
    }


    public ArrayList<String> getListItems() {
        return listItems;
    }

    public void setListItems(ArrayList<String> listItems) {
        this.listItems = listItems;
    }

    public ArrayList<Boolean> getListItemsChecked() {
        return listItemsChecked;
    }

    public void setListItemsChecked(ArrayList<Boolean> listItemsChecked) {
        this.listItemsChecked = listItemsChecked;
    }

    public ArrayList<Integer> getListTags() {
        return listTags;
    }

    public long getListDue() {
        return listDue;
    }

    public void setListDue(long listDue) {
        this.listDue = listDue;
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

    public ArrayList<String> getAllListItems() {
        return listItems;
    }

    public int getListItemsSize() {
        return listItems.size();
    }

    public boolean isListItems() {
        return !listItems.isEmpty();
    }

    public void setListItems(String item) {
        this.listItems.add(item);
    }

    public boolean getListItemsChecked(int pos) {

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
        return !listTags.isEmpty();
    }

    public void setListTags(ArrayList<Integer> listTags) {
        this.listTags = listTags;
    }

    public DateTime getDateTime() {
        return new DateTime(listDue);
    }

    public void setDateTime(DateTime datetime) {
        this.listDue = datetime.getMillis();
    }

    public boolean hasTime() {
        return getDateTime().getSecondOfMinute()==0;
    }

    public boolean hasDate() {
        if(getDateTime().equals(new DateTime(3000, 1, 1, 0, 0))){//fixes migration of old dates
            setDateTime(new DateTime(3000, 1, 1, 0, 59, 30));
        }
        return getDateTime().getSecondOfMinute()!=30;
    }


    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public int getListKey() {
        return this.listKey;
    }

    public void setListKey(int listKey) {
        this.listKey = listKey;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public void normalizeAfterImport() {
        if (listTags == null) {
            listTags = new ArrayList<>();

        }
        if (listItems == null) {
            listItems = new ArrayList<>();

        }
        if (listName == null) {
            listName = "";
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getListName());
        builder.append(", " + getDateTime().toString("MMMM D, h:mm") + "\n");
        for (String s : getListItems()) {
            builder.append("- " + s + "\n");
        }
        builder.append("Tags: \n");
        for (int i : getListTags()) {
            builder.append("- Tag " + i + "\n");
        }
        return builder.toString();
    }

    public int compareTo(Tasks o, int filterMode) {
        Log.d("compareTasks", "filterMode: " + filterMode);
        if (filterMode == TIME_SORT) {
            Log.d("compareTasks", Integer.toString(compareTimes(o)));
            if (compareTimes(o) != 0) {
                return compareTimes(o);
            } else if(compareEditing(o)!=0){
                return compareEditing(o);
            }else if (compareAlphabetical(o) != 0) {
                return compareAlphabetical(o);
            } else {
                return compareLists(o);
            }

        } else {//Alphabetical sort
            if(compareEditing(o)!=0){
                return compareEditing(o);
            }else if (compareAlphabetical(o) != 0) {
                return compareAlphabetical(o);
            } else if (compareTimes(o) != 0) {
                return compareTimes(o);
            } else {
                return compareLists(o);
            }
        }
    }

    public int compareTimes(Tasks o) {
        return this.getDateTime().compareTo(o.getDateTime());
    }

    public int compareEditing(Tasks o) {
        if(this.isEditing()!=o.isEditing()){
            if(this.isEditing()){
                return 1;
            }else {
                return -1;
            }
        }else {
            return 0;
        }


    }

    public int compareAlphabetical(Tasks o) {
        return this.listName.compareTo(o.listName);
    }

    public int compareLists(Tasks o){
        return Integer.compare(this.getListItemsSize(), o.getListItemsSize());
    }

    public boolean isEditing() {
        return editing;
    }

    public void setEditing(boolean editing) {
        this.editing = editing;
    }


}
