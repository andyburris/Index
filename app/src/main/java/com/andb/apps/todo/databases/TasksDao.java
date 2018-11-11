package com.andb.apps.todo.databases;

import com.andb.apps.todo.Tasks;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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
