package com.andb.apps.todo.data.local;

import com.andb.apps.todo.data.model.Project;

import java.util.List;

import androidx.lifecycle.LiveData;
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

    @Query("SELECT * FROM Project WHERE id = :key")
    Project findProjectById(int key);

    @Query("SELECT * FROM Project")
    List<Project> getAllStatic();

    @Query("SELECT * FROM Project")
    LiveData<List<Project>> getAll();


}
