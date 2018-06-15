package com.andb.apps.todo;

import android.graphics.Color;

public class Tags {
    private String tagName;
    private /*static*/ int tagColor;

    public Tags (){
    }

    public Tags (String tagname, int tagColor){
        this.tagName = tagname;
        this.tagColor = tagColor;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public /*static*/ int getTagColor() {
        return tagColor;
    }

    public void setTagColor(int tagColor) {
        this.tagColor = tagColor;
    }

}
