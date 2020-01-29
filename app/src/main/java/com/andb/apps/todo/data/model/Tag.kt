package com.andb.apps.todo.data.model

import java.util.ArrayList
import androidx.room.Entity

@Entity
data class Tag (
        val id: Int = 0,
        val name: String,
        val color: Int = 0,
        val isSubFolder: Boolean = false,
        val children: ArrayList<Int> = ArrayList(),
        val projectId: Int = 0
){

    override fun toString(): String {

        val sb = StringBuilder()
        sb.append(name).append(": \n")
        for (c in children) {
            sb.append("- ").append("Tag ").append(c).append("\n")
        }


        return sb.toString()
    }

    fun toString(tagList: ArrayList<Tag>): String {
        val sb = StringBuilder()
        sb.append(name).append(": \n")
        for (c in children) {
            sb.append("- ").append(tagList[c].name).append("\n")
        }
        return sb.toString()
    }
}
