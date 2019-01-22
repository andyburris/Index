package com.andb.apps.todo.objects

import com.andb.apps.todo.utilities.ProjectsUtils
import java.util.*

open class Project(key: Int, name: String, var taskList: ArrayList<Tasks>, var archiveList: ArrayList<Tasks>, var tagList: ArrayList<Tags>, color: Int, index: Int) : BaseProject() {


    var keyList: ArrayList<Int> = ArrayList()


    //TODO: Run updateKeyList on add task/tag
    fun updateKeyList() {
        for (t: Tasks in taskList) {
            this.keyList.add(t.listKey)
        }
        for (t: Tasks in archiveList) {
            this.keyList.add(t.listKey)
        }
        for (t: Tags in tagList) {
            while (this.keyList.contains(t.key)) {
                t.key = ProjectsUtils.keyGenerator()
            }
            this.keyList.add(t.key)
        }
    }

    init {
        super.key = key
        super.name = name
        super.color = color
        super.index = index
        updateKeyList()
    }

}
