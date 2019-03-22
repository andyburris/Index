package com.andb.apps.todo.databases;

import com.andb.apps.todo.objects.Tags;
import com.andb.apps.todo.objects.Tasks;

import java.util.List;

import androidx.lifecycle.LiveData;
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
    LiveData<List<Tags>> getAllFromProject(int projectKey);

    @Query("SELECT * FROM Tags WHERE `project_id` = :projectKey")
    List<Tags> getAllFromProjectStatic(int projectKey);

    @Query("SELECT * FROM Tags")
    LiveData<List<Tags>> getAll();

    @Query("SELECT * FROM Tags")
    List<Tags> getAllStatic();
}
