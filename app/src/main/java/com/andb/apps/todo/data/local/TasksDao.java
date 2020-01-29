package com.andb.apps.todo.data.local;

import com.andb.apps.todo.data.model.Task;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface TasksDao {

    @Insert
    void insertOnlySingleTask(Task task);

    @Insert
    void insertMultipleTasks(List<Task> taskList);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("SELECT * FROM Task WHERE `listKey` = :key")
    Task findTaskById(int key);

    @Query("SELECT * FROM Task WHERE `list_name` = :name")
    List<Task> findTasksByName(String name);

    @Query("SELECT * FROM Task WHERE `project_id` = :projectKey")
    LiveData<List<Task>> getAllFromProject(int projectKey);

    @Query("SELECT * FROM Task")
    LiveData<List<Task>> getAll();

    @Query("SELECT * FROM Task")
    List<Task> getAllStatic();
}
