package com.andb.apps.todo.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.andb.apps.todo.utilities.ProjectsUtils
import java.util.*

@Entity
data class Project(
        @PrimaryKey
        val id: Int,

        @ColumnInfo(name = "project_name")
        var name: String,

        @ColumnInfo(name = "project_color")
        var color: Int = 0x00000000, //black

        @ColumnInfo(name = "project_index")
        var index: Int

)
