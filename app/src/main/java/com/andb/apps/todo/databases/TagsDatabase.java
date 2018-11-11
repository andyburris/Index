package com.andb.apps.todo.databases;

import com.andb.apps.todo.Tags;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Tags.class}, version = 1, exportSchema = false)
public abstract class TagsDatabase extends RoomDatabase {
    public abstract TagsDao tagsDao();

}
