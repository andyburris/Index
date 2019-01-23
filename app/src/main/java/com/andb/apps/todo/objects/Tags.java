package com.andb.apps.todo.objects;

import android.util.Log;

import com.andb.apps.todo.typeconverters.KeyListConverter;
import com.andb.apps.todo.utilities.Current;
import com.andb.apps.todo.utilities.ProjectsUtils;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity(foreignKeys = @ForeignKey(entity = BaseProject.class, parentColumns = "key", childColumns = "project_id", onDelete = ForeignKey.CASCADE))
public class Tags {

    public Tags(int key, @NonNull String tagName, int tagColor, boolean subFolder, ArrayList<Integer> children, int projectId, int index) {
        this.key = key;
        this.tagName = tagName;
        this.tagColor = tagColor;
        this.subFolder = subFolder;
        this.children = children;
        this.projectId = projectId;
        this.index = index;
    }

    @Ignore
    public Tags(@NonNull String tagname, int tagColor, boolean subFolder, int index) {
        key = ProjectsUtils.keyGenerator();
        this.tagName = tagname;
        this.tagColor = tagColor;
        this.subFolder = subFolder;
        this.index = index;
        this.projectId = Current.project().getKey();
    }

    @PrimaryKey
    @Expose
    private
    int key;

    @SerializedName("tagName")
    @Expose
    @NonNull
    private String tagName;

    @SerializedName("tagColor")
    @Expose
    @ColumnInfo(name = "tag_color")
    private int tagColor;

    @SerializedName("subfolder")
    @Expose
    @ColumnInfo(name = "subfolder")
    private boolean subFolder;

    @SerializedName("tag_Links")
    @Expose
    @TypeConverters(KeyListConverter.class)
    @ColumnInfo(name = "tag_children")
    private ArrayList<Integer> children = new ArrayList<>();

    @SerializedName("project_id")
    @Expose
    @ColumnInfo(name = "project_id")
    private int projectId;

    @SerializedName("tag_index")
    @Expose
    @ColumnInfo(name = "tag_index")
    private int index;

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public int getTagColor() {
        return tagColor;
    }

    public void setTagColor(int tagColor) {
        this.tagColor = tagColor;
    }

    public boolean isSubFolder() {
        return subFolder;
    }

    public void setSubFolder(boolean subFolder) {
        this.subFolder = subFolder;
    }

    public void setChildren(ArrayList<Integer> children) {
        this.children = children;
    }

    public ArrayList<Integer> getChildren() {
        return children;
    }

    @NonNull
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(tagName).append(": \n");
        if (children != null) {
            for (int c : children) {
                sb.append("- ").append(Current.tagList().get(c).tagName).append("\n");
            }
        } else {
            Log.d("getMostRecentTag", "null children");
        }


        return sb.toString();
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
