package com.andb.apps.todo.eventbus;

import com.andb.apps.todo.objects.Tasks;

import java.util.ArrayList;

import androidx.sqlite.db.SupportSQLiteDatabase;

public class MigrateEvent {
    public SupportSQLiteDatabase projectsDatabase;
    public ArrayList<Tasks> taskList;

    public MigrateEvent(SupportSQLiteDatabase db, ArrayList<Tasks> taskList) {
        this.projectsDatabase = db;
        this.taskList = taskList;
    }
}
