package com.andb.apps.todo.eventbus;

public class UpdateEvent {
    public boolean viewing = false;
    public boolean setupProject = false;

    public UpdateEvent(){
    }

    public UpdateEvent(boolean inboxAnimNotNeeded) {
        this.viewing = inboxAnimNotNeeded;
    }
    public UpdateEvent(boolean inboxAnimNotNeeded, boolean setupProject) {
        this.viewing = inboxAnimNotNeeded;
        this.setupProject = setupProject;
    }
}
