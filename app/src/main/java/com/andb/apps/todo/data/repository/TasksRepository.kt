package com.andb.apps.todo.data.repository

import com.andb.apps.todo.data.model.Task

interface TasksRepository {
    fun addTask(task: Task)
    fun updateTask(task: Task)
    fun deleteTask(id: Int)
    fun getTask(id: Int): Task
    fun getTasksByProject(id: Int): List<Task>
}