package com.andb.apps.todo.objects

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.andb.apps.todo.utilities.ProjectsUtils
import java.util.*

@Entity
open class Project(
    @PrimaryKey
    val key: Int,

    @ColumnInfo(name = "project_name")
    var name: String,

    @ColumnInfo(name = "project_color")
    var color: Int = 0x00000000, //black

    @ColumnInfo(name = "project_index")
    var index: Int

)
