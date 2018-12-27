package com.andb.apps.todo.settings

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.res.Resources
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.view.View
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

            categoryHeader("info") {
                title = "Info"
            }
            pref("user_name") {
                title = "Name"
                val prefs = PreferenceManager.getDefaultSharedPreferences(context)
                summary =  prefs.getString("user_name", "John Doe")
                requestRebind()
                clickView { _, holder ->
                    var view = EditText(context)
                    view.setText(getString("John Doe"))
                    AlertDialog.Builder(context)
                            .setView(view)
                            .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                                commitString(view.text.toString())
                                summary = view.text.toString()
                                requestRebind()
                            })
                            .show()
                    true
                }
            }
            categoryHeader("header_defaults") {
                title = "Defaults"
            }
            switch("expand_lists"){
                title = "Expand lists to start"
                summary = "If off, icon will display to open"
            }
            switch("sort_mode_list"){
                title = "Default sort"
                summary = "On: Date Sort, Off: Alphabetical Sort"
            }
        }
        subScreen {
            title = "Theme"
            iconRes = R.drawable.ic_palette_black_24dp
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
                iconRes = R.drawable.ic_filter_list_black_24dp
            }
            checkBox(context.resources.getString(R.string.pref_sub_folder_filter_key)){
                titleRes = R.string.subfolder_filter_check_title
                iconRes = R.drawable.ic_subfolder_black_24dp
            }
        }
    }
}
