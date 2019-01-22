package com.andb.apps.todo.objects

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity
open class BaseProject {

    /*    constructor(key: Int, name: String, color: Int, index: Int) {
        this.key = key
        this.name = name

        this.color = color
        this.index = index
    }*/


    @PrimaryKey
    var key: Int = 0

    @ColumnInfo(name = "project_name")
    var name = "Project Name"

    @ColumnInfo(name = "project_color")
    var color: Int = 0x00000000//black

    @ColumnInfo(name = "project_index")
    var index: Int = -1
}