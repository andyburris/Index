package com.andb.apps.todo.eventbus;

public class UpdateEvent {
    public boolean setupProject = false;

    public UpdateEvent(){
    }

    public UpdateEvent(boolean setupProject) {
        this.setupProject = setupProject;
    }
}
