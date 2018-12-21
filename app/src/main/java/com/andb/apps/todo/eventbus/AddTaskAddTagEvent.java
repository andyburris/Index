package com.andb.apps.todo.eventbus;

public class AddTaskAddTagEvent {
    public int tag;
    public AddTaskAddTagEvent(int tag){
        this.tag = tag;
    }
}
