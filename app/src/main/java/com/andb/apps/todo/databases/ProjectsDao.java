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
