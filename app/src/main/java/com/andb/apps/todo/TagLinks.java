package com.andb.apps.todo;

import java.util.ArrayList;

public class TagLinks {
    private int tagPosition;
    private ArrayList<Integer> links;

    public TagLinks (int parent, ArrayList<Integer> links){
        this.tagPosition = parent;
        this.links = links;
    }

    public int tagParent(){
        return tagPosition;
    }

    public void addLink(int pos){
        links.add(pos);
    }

    public int getTagLink (int linkPos){
        return links.get(linkPos);
    }

    public ArrayList<Integer> getAllTagLinks(){
        return links;
    }

    public boolean isTagLinked(){
        if(links.isEmpty()){
            return false;
        }else {
            return true;
        }
    }

    public int getLinkPosition(int pos){
        return links.indexOf(pos);
    }

    public boolean contains(int pos){
        if(links.contains(pos)){
            return true;
        }else {
            return false;
        }
    }
}
