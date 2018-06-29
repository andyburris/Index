package com.andb.apps.todo;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface TasksDao {
    @Insert
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
    List<Tasks> getAll();


}
