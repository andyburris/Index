package com.andb.apps.todo.databases;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.andb.apps.todo.Tags;

import java.util.List;

@Dao
public interface TagsDao {
    @Insert
    void insertOnlySingleTag(Tags tags);

    @Insert
    void insertMultipleTags(List<Tags> tagList);

    @Update
    void updateTag(Tags tags);

    @Delete
    void deleteTask(Tags tags);

    @Query("SELECT * FROM Tags WHERE tagName = :tagName")
    Tags findTaskById(String tagName);

    @Query("SELECT * FROM TAGS")
    List<Tags> getAll();


}
