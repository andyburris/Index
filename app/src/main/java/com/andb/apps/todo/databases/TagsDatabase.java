package com.andb.apps.todo.databases;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.andb.apps.todo.Tags;

@Database(entities = {Tags.class}, version = 1, exportSchema = false)
public abstract class TagsDatabase extends RoomDatabase {
    public abstract TagsDao tagsDao();

}
