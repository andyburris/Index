package com.andb.apps.todo.objects.reminders

import org.joda.time.DateTime

class SimpleReminder(val due: Long, var notified: Boolean = false){
    @JvmOverloads
    constructor(dateTime: DateTime, notified: Boolean = false) : this(dateTime.millis, notified)

    fun hasTime(): Boolean {
        return asDateTime().secondOfMinute ==0
    }


    fun asDateTime(): DateTime{
        return DateTime(due)
    }

    override fun toString(): String {
        val pattern = if(hasTime()) "hh:mm a | EEEE, MMMM d" else "EEEE, MMMM d"
        return asDateTime().toString(pattern)
    }
}