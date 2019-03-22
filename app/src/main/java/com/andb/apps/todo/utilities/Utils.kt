package com.andb.apps.todo.utilities

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