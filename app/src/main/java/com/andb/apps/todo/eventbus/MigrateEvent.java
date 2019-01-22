package com.andb.apps.todo.eventbus;

import com.andb.apps.todo.objects.Tasks;

import java.util.ArrayList;

import androidx.sqlite.db.SupportSQLiteDatabase;

public class MigrateEvent {
    public int startVersion;
    public int endVersion;

    public MigrateEvent(int startVersion, int endVersion) {
        this.startVersion = startVersion;
        this.endVersion = endVersion;
    }
}
