package com.andb.apps.todo.databases;

import com.andb.apps.todo.objects.Project;
import com.andb.apps.todo.objects.Tasks;

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
    void insertOnlySingleTask(Tasks task);

    @Insert
    void insertMultipleTasks(List<Tasks> taskList);

    @Update
    void updateTask(Tasks task);

    @Delete
    void deleteTask(Tasks task);

    @Query("SELECT * FROM Tasks WHERE `listKey` = :key")
    Tasks findTaskById(int key);

    @Query("SELECT * FROM Tasks WHERE `list_name` = :name")
    List<Tasks> findTasksByName(String name);

    @Query("SELECT * FROM Tasks WHERE `project_id` = :projectKey")
    LiveData<List<Tasks>> getAllFromProject(int projectKey);

    @Query("SELECT * FROM Tasks")
    LiveData<List<Tasks>> getAll(); //only use for notifications/search all
}
