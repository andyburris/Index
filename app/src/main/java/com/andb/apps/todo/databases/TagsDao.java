package com.andb.apps.todo.databases;

import com.andb.apps.todo.Tags;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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
