package com.andb.apps.todo.eventbus;

public class UpdateEvent {
    public boolean viewing;

    public UpdateEvent(boolean inboxAnimNotNeeded) {
        this.viewing = inboxAnimNotNeeded;
    }
}
