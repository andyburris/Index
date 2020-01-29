package com.andb.apps.todo.data.local;

import com.andb.apps.todo.data.model.Tag;

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
    void insertOnlySingleTag(Tag tag);

    @Insert
    void insertMultipleTags(List<Tag> tagList);

    @Update
    void updateTag(Tag tag);

    @Delete
    void deleteTag(Tag tag);

    @Query("SELECT * FROM Tag WHERE id = :key")
    LiveData<Tag> findTagsById(int key);

    @Query("SELECT * FROM Tag WHERE `project_id` = :projectKey")
    LiveData<List<Tag>> getAllFromProject(int projectKey);

    @Query("SELECT * FROM Tag WHERE `project_id` = :projectKey")
    List<Tag> getAllFromProjectStatic(int projectKey);

    @Query("SELECT * FROM Tag")
    LiveData<List<Tag>> getAll();

    @Query("SELECT * FROM Tag")
    List<Tag> getAllStatic();
}
