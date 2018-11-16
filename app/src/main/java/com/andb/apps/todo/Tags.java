package com.andb.apps.todo;

import com.google.gson.annotations.SerializedName;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Tags {

    @PrimaryKey
    @NonNull
    @SerializedName("tagName")
    private String tagName;

    @ColumnInfo(name = "tag_color")
    @SerializedName("tagColor")
    private int tagColor;

    @ColumnInfo(name = "subfolder")
    @SerializedName("subFolder")
    private boolean subFolder;

    public Tags (){
    }

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
