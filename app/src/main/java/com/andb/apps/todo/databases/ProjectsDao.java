package com.andb.apps.todo.databases;

import com.andb.apps.todo.objects.Project;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ProjectsDao {
/*    @Insert
    void insertOnlySingleTask(Tasks tasks);

    @Insert
    void insertMultipleTasks(List<Tasks> taskList);

    @Update
    void updateTask(Tasks tasks);

    @Delete
    void deleteTask(Tasks tasks);

    @Query("SELECT * FROM Tasks WHERE listKey = :listKey")
    Tasks findTaskById(int listKey);

    @Query("SELECT * FROM TASKS")
    List<Tasks> getAll();*/

    @Insert
    void insertOnlySingleProject(Project project);

    @Insert
    void insertMultipleProjects(List<Project> projectList);

    @Update
    void updateProject(Project project);

    @Delete
    void deleteProject(Project project);

    @Query("SELECT * FROM Project WHERE `key` = :key")
    Project findProjectById(int key);

    @Query("SELECT * FROM PROJECT")
    List<Project> getAll();


}
