package com.andb.apps.todo.objects

import android.graphics.Color
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.andb.apps.todo.typeconverters.KeyListConverter
import com.andb.apps.todo.typeconverters.TagListConverter
import com.andb.apps.todo.typeconverters.TaskListConverter
import java.util.*

@Entity
open class Project {

    constructor(key: Int, name: String, taskList: ArrayList<Tasks>, archiveList: ArrayList<Tasks>, tagList: ArrayList<Tags>, color: Int, index: Int) {
        this.key = key
        this.name = name
        this.taskList = taskList
        this.archiveList = archiveList
        this.tagList = tagList
        this.color = color
        this.index = index
        for (t: Tasks in taskList) {
            this.keyList.add(t.listKey)
        }
    }

    @PrimaryKey
    var key: Int = 0

    @ColumnInfo(name = "project_name")
    var name = "Project Name"

    @ColumnInfo(name = "task_list")
    @TypeConverters(TaskListConverter::class)
    var taskList: ArrayList<Tasks> = ArrayList()

    @ColumnInfo(name = "key_list")
    @TypeConverters(KeyListConverter::class)
    var keyList: ArrayList<Int> = ArrayList()

    @ColumnInfo(name = "archive_list")
    @TypeConverters(TaskListConverter::class)
    var archiveList: ArrayList<Tasks> = ArrayList()

    @ColumnInfo(name = "tag_list")
    @TypeConverters(TagListConverter::class)
    var tagList: ArrayList<Tags> = ArrayList()

    @ColumnInfo(name = "project_color")
    var color: Int = 0x00000000//black

    @ColumnInfo(name = "project_index")
    var index: Int = -1


}
