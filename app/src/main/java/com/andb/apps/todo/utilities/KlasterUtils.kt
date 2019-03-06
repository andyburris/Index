package com.andb.apps.todo.utilities

import com.github.rongi.klaster.KlasterBuilder
import com.github.rongi.klaster.KlasterViewHolder

fun <T> KlasterBuilder.bind(binder: KlasterViewHolder.(position: Int)->Unit): KlasterBuilder =
        this.bind { position: Int ->
            binder(adapterPosition)
        }

fun KlasterBuilder.bindEmpty(binder: KlasterViewHolder.() -> Unit): KlasterBuilder =
        this.bind { position ->
            binder()
        }