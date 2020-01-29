package com.andb.apps.todo.data.repository

import com.andb.apps.todo.data.model.Tag

interface TagsRepository {
    fun addTag(tag: Tag)
    fun updateTag(tag: Tag)
    fun deleteTag(id: Int)
    fun getTag(id: Int)
    fun getTagsByProject(id: Int): List<Tag>

}