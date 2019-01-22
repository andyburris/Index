package com.andb.apps.todo.databases;

import com.andb.apps.todo.objects.Tags;
import com.andb.apps.todo.objects.Tasks;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface TagsDao {

    @Insert
    void insertOnlySingleTag(Tags tag);

    @Insert
    void insertMultipleTags(List<Tags> tagList);

    @Update
    void updateTag(Tags tag);

    @Delete
    void deleteTag(Tags tag);

    @Query("SELECT * FROM Tags WHERE `key` = :key")
    Tags findTagsById(int key);

    @Query("SELECT * FROM Tags WHERE `project_id` = :projectKey")
    List<Tags> getAllFromProject(int projectKey);
}
