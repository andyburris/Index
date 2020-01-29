package com.andb.apps.todo.util

import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


inline fun <VM : ViewModel> viewModelFactory(crossinline f: () -> VM) =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(aClass: Class<T>): T = f() as T
    }

fun <E> MutableCollection<E>.clearWith(replaceWith: Collection<E>){
    this.clear()
    this.addAll(replaceWith)
}

fun Toolbar.getToolbarNavigationButton(): ImageButton? {
    val size = childCount
    for (i in 0 until size) {
        val child = getChildAt(i)
        if (child is ImageButton) {
            val btn = child as ImageButton
            if (btn.drawable === navigationIcon) {
                return btn
            }
        }
    }
    return null
}

fun d(tag: String, message: ()->String){
    Log.d(tag.take(23), message.invoke())
}

fun d(message: ()->String){
    Log.d(Thread.currentThread().stackTrace[1].methodName.take(23), message.invoke())
}

fun <T> MutableCollection<T>.dropBy(amount: Int = 1) {
    for (i in 0 until amount) {
        val element = this.last()
        remove(element)
    }
}

fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    val tmp = this[index1] // 'this' corresponds to the list
    this[index1] = this[index2]
    this[index2] = tmp
}