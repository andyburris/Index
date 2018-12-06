package com.andb.apps.todo.settings

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.util.AttributeSet
import android.widget.TimePicker
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.andb.apps.todo.R
import com.jaredrummler.android.colorpicker.ColorPreference
import de.Maxr1998.modernpreferences.PreferencesAdapter
import de.Maxr1998.modernpreferences.helpers.*

object SettingsLayout {
    fun createRootScreen(context: Context) = screen(context) {
        subScreen {
            title = "General"
            summary = "Name, Default Sort"
            iconRes = R.drawable.ic_info_black_24dp
            centerIcon = false

            categoryHeader("header_plain") {
                title = "Plain"
            }
            pref("plain") {
                title = "A plain preference…"
            }
            pref("with-summary") {
                title = "…that doesn't have a widget"
                summary = "But a summary this time!"
            }
            pref("with-icon") {
                title = "There's also icon support, yay!"
                iconRes = R.drawable.ic_access_time_black_24dp
            }
            categoryHeader("header_two_state") {
                title = "Two state"
            }
            switch("switch") {
                title = "A simple switch"
            }
            pref("dependent") {
                title = "Toggle the switch above"
                dependency = "switch"
                clickView { _, holder ->
                    Toast.makeText(holder.itemView.context, "Preference was clicked!", Toast.LENGTH_SHORT).show()
                    false
                }
            }
            checkBox("checkbox") {
                title = "A checkbox"
            }
            categoryHeader("header_advanced") {
                title = "Advanced"
            }
            seekBar("seekbar") {
                title = "A seekbar"
                min = 1
                max = 100
            }
            expandText("expand-text") {
                title = "Expandable text"
                text = "This is an example implementation of ModernAndroidPreferences, check out " +
                        "the source on https://github.com/Maxr1998/ModernAndroidPreferences"
            }
            collapse {
                pref("collapsed_one") {
                    title = "Collapsed by default"
                }
                pref("collapsed_two") {
                    title = "Another preference"
                }
                pref("collapsed_three") {
                    title = "A longer title to trigger ellipsize"
                }
            }
        }
        subScreen {
            title = "Theme"
            iconRes = R.drawable.ic_palette_black_24dp

            switch("dark_theme"){
                title = "Dark Theme"
            }
            switch("colored_toolbar"){
                title = "Colored Toolbar"
            }
            addPreferenceItem(ColorPicker(context as FragmentActivity).apply {
                title = "Theme Color"
            })
        }
        subScreen {
            title = "Notifications"
            iconRes = R.drawable.ic_notifications_black_24dp

            addPreferenceItem(TimePreference(context).apply {
                title = "Default Notification Time"
                summary = SettingsActivity.timeToNotifyForDateOnly.toString("h:mm")
                iconRes = R.drawable.ic_access_time_black_24dp
            })
        }
        subScreen {
            title = "Folders"
            summary = "Change how the folder hierarchy behaves"
            iconRes = R.drawable.ic_folder_black_24dp

            switch("folder_mode"){
                title = "Folder Mode"
                summaryRes = R.string.pref_description_filter_folder
            }
            checkBox(context.resources.getString(R.string.pref_sub_folder_filter_key)){
                titleRes = R.string.subfolder_filter_check_title
            }
        }
    }
}
