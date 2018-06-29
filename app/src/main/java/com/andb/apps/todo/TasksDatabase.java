package com.andb.apps.todo;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Tasks.class}, version = 1, exportSchema = false)
public abstract class TasksDatabase extends RoomDatabase {
    public abstract TasksDao tasksDao();

}
