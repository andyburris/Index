package com.andb.apps.todo.ui.settings

import android.app.TimePickerDialog
import android.content.Context
import android.widget.TimePicker
import de.Maxr1998.modernpreferences.Preference
import de.Maxr1998.modernpreferences.PreferencesAdapter
import org.joda.time.DateTime

class TimePreference (val ctxt: Context) : Preference("pref_notif_only_date"){

    override fun onClick(holder: PreferencesAdapter.ViewHolder) {
        super.onClick(holder)
        TimePickerDialog(ctxt, object : TimePickerDialog.OnTimeSetListener {
            override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
                var dateTime = DateTime(1970, 1, 1, hourOfDay, minute, 58)
                SettingsActivity.setTimeToNotifyForDateOnly(dateTime, ctxt)
            }
        }, SettingsActivity.timeToNotifyForDateOnly.hourOfDay, SettingsActivity.timeToNotifyForDateOnly.minuteOfHour, false).show()
    }





}