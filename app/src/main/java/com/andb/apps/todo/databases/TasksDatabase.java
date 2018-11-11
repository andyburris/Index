package com.andb.apps.todo.databases;

import com.andb.apps.todo.Tasks;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Tasks.class}, version = 1, exportSchema = false)
public abstract class TasksDatabase extends RoomDatabase {
    public abstract TasksDao tasksDao();

}
