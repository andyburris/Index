package com.andb.apps.todo.data.repository

import com.andb.apps.todo.data.model.Project

interface ProjectsRepository {
    fun addProject(project: Project)
    fun updateProject(project: Project)
    fun deleteProject(id: Int)
    fun getProject(id: Int): Project
    fun getAllProjects(): List<Project>
}