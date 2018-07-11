package com.andb.apps.todo.databases;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.andb.apps.todo.Tasks;

@Database(entities = {Tasks.class}, version = 1, exportSchema = false)
public abstract class TasksDatabase extends RoomDatabase {
    public abstract TasksDao tasksDao();

}
