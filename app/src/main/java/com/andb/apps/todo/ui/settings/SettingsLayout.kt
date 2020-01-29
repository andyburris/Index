package com.andb.apps.todo.ui.settings

import android.content.Context
import com.andb.apps.todo.R
import de.Maxr1998.modernpreferences.helpers.categoryHeader
import de.Maxr1998.modernpreferences.helpers.screen
import de.Maxr1998.modernpreferences.helpers.subScreen
import de.Maxr1998.modernpreferences.helpers.switch

object SettingsLayout {
    fun createRootScreen(context: Context) = screen(context) {
        subScreen {
            title = "General"
            summary = "Name, Default Sort"
            iconRes = R.drawable.ic_info_black_24dp
            centerIcon = false

            categoryHeader("header_defaults") {
                title = "Defaults"
            }
            switch("expand_lists") {
                title = "Expand lists to start"
                summary = "If off, you can still expand by clicking the icon"
            }
            switch("sort_mode_list") {
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
                summary = SettingsActivity.timeToNotifyForDateOnly.toString("h:mm a")
                iconRes = R.drawable.ic_access_time_black_24dp
            })
        }
        subScreen {
            title = "Show Tutorial"
            iconRes = R.drawable.ic_info_black_24dp
        }
    }
}
