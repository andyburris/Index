package com.andb.apps.todo.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.andb.apps.todo.objects.Project
import dev.matrix.roomigrant.GenerateRoomMigrations

@Database(entities = arrayOf(Project::class), version = 2)
@GenerateRoomMigrations(MigrationRules::class)
abstract class ProjectsDatabase : RoomDatabase() {
    abstract fun projectsDao(): ProjectsDao
}
