package com.andb.apps.todo;

public class Tags {
    private String tagName;
    private int tagColor;
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
