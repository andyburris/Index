package com.andb.apps.todo.objects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Tags {

    @SerializedName("tagName")
    @Expose
    @PrimaryKey
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

    public Tags(String tagname, int tagColor, boolean subFolder) {
        this.tagName = tagname;
        this.tagColor = tagColor;
        this.subFolder = subFolder;
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
}
