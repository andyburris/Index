package com.andb.apps.todo.databases

import androidx.room.Database
import androidx.room.RoomDatabase
import com.andb.apps.todo.objects.Project
import dev.matrix.roomigrant.GenerateRoomMigrations
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.migration.Migration



@Database(entities = arrayOf(Project::class), version = 3)
@GenerateRoomMigrations(MigrationRules::class)
abstract class ProjectsDatabase : RoomDatabase() {
    abstract fun projectsDao(): ProjectsDao
}

val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE PROJECT " + " ADD COLUMN project_color INTEGER DEFAULT 0x000000 NOT NULL")
    }
}
