package com.andb.apps.todo.databases;

import com.andb.apps.todo.objects.Project;
import com.andb.apps.todo.objects.Tasks;

import java.util.List;

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
    Tasks findTasksById(int key);

    @Query("SELECT * FROM Tasks WHERE `project_id` = :projectKey")
    List<Tasks> getAllFromProject(int projectKey);

    @Query("SELECT * FROM Tasks")
    List<Tasks> getAll(); //only use for notifications/search all
}
