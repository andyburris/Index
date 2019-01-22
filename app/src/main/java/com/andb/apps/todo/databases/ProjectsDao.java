package com.andb.apps.todo.databases;

import com.andb.apps.todo.objects.BaseProject;
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
    void insertOnlySingleProject(BaseProject project);

    @Insert
    void insertMultipleProjects(List<BaseProject> projectList);

    @Update
    void updateProject(BaseProject project);

    @Delete
    void deleteProject(BaseProject project);

    @Query("SELECT * FROM BaseProject WHERE `key` = :key")
    BaseProject findProjectById(int key);

    @Query("SELECT * FROM BaseProject")
    List<BaseProject> getAll();


}
