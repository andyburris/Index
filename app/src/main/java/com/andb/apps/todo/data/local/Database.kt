package com.andb.apps.todo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.matrix.roomigrant.GenerateRoomMigrations
import com.andb.apps.todo.data.model.Project
import com.andb.apps.todo.data.model.Tag
import com.andb.apps.todo.data.model.Task
import com.andb.apps.todo.databases.MigrationRules

@Database(entities = [Project::class, Task::class, Tag::class], version = 1)
@GenerateRoomMigrations(MigrationRules::class)
abstract class Database : RoomDatabase() {
    abstract fun projectsDao(): ProjectsDao
    abstract fun tasksDao(): TasksDao
    abstract fun tagsDao(): TagsDao
}
