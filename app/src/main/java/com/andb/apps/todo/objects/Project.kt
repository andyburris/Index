package com.andb.apps.todo.objects

import java.util.ArrayList

class Project {
    var name = "Project Name"
    var key: Int = 0
    var taskList: ArrayList<Tasks> = ArrayList()
    var tagList: ArrayList<Tags> = ArrayList()
    var tagLinks: ArrayList<TagLinks> = ArrayList()
}
