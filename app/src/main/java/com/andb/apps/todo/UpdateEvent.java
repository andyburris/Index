package com.andb.apps.todo;

public class UpdateEvent {
    public boolean viewing;

    public UpdateEvent(boolean inboxAnimNotNeeded) {
        this.viewing = inboxAnimNotNeeded;
    }
}
