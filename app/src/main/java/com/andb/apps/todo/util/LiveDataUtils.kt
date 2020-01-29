package com.andb.apps.todo.util

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.snakydesign.livedataextensions.map

/**MediatorLiveData of List<T> with better sync to backing list and better modification methods**/
open class ListLiveData<T>(initialList: List<T> = emptyList()) : MediatorLiveData<List<T>>(),
        List<T> {

    private val backingList: MutableList<T> = initialList.toMutableList()

    fun add(item: T) {
        backingList.add(item)
        postValue(backingList)
    }

    fun add(item: T, index: Int = backingList.size) {
        backingList.add(index, item)
        postValue(backingList)
    }

    fun addAll(items: Collection<T>) {
        backingList.addAll(items)
        postValue(backingList)
    }

    fun remove(item: T) {
        backingList.remove(item)
        postValue(backingList)
    }

    fun removeAt(index: Int) {
        backingList.removeAt(index)
        postValue(backingList)
    }

    fun drop(by: Int) {
        backingList.dropBy(by)
        postValue(backingList)
    }

    fun clear() {
        backingList.clear()
        postValue(backingList)
    }

    fun last(): T {
        return backingList.last()
    }


    fun lastOrNull(): T? {
        return backingList.lastOrNull()
    }

    override fun postValue(value: List<T>?) {
        if (value !== backingList) {
            backingList.clear()
            backingList.addAll(value.orEmpty())
        }
        super.postValue(backingList)
    }

    override fun setValue(value: List<T>?) {
        if (value !== backingList) {
            backingList.clear()
            backingList.addAll(value.orEmpty())
        }
        super.setValue(backingList)
    }

    override fun getValue(): List<T> {
        return backingList
    }

    override val size: Int
        get() = backingList.size

    override fun contains(element: T): Boolean = backingList.contains(element)

    override fun containsAll(elements: Collection<T>): Boolean = backingList.containsAll(elements)

    override fun get(index: Int): T = backingList[index]

    override fun indexOf(element: T): Int = backingList.indexOf(element)

    override fun isEmpty(): Boolean = backingList.isEmpty()

    override fun iterator(): Iterator<T> = backingList.iterator()

    override fun lastIndexOf(element: T): Int = backingList.lastIndexOf(element)

    override fun listIterator(): ListIterator<T> = backingList.listIterator()

    override fun listIterator(index: Int): ListIterator<T> = backingList.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int): List<T> =
            backingList.subList(fromIndex, toIndex)

}

fun <T, R> LiveData<List<T>>.mapEach(block: (T)->R): LiveData<List<R>>{
    return this.map { list -> list.map(block) }
}

fun <T> LiveData<List<T>>.filterEach(block: (T)->Boolean): LiveData<List<T>>{
    return this.map { list -> list.filter(block) }
}

class InitialLiveData<T>(private val initialValue: T, emitInitial: Boolean = true) :
        MediatorLiveData<T>() {
    init {
        if (emitInitial) {
            postValue(initialValue) // emit starting value
        }
    }
    override fun getValue(): T {
        return super.getValue() ?: initialValue
    }
}