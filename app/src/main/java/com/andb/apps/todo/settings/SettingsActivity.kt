package com.andb.apps.todo.settings

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.view.View
import android.widget.ImageView

import com.andb.apps.todo.R
import com.andrognito.flashbar.Flashbar
import com.jaredrummler.android.colorpicker.ColorPanelView
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener
import com.jaredrummler.cyanea.Cyanea
import com.jaredrummler.cyanea.app.BaseCyaneaActivity
import com.jaredrummler.cyanea.prefs.CyaneaSettingsActivity
import org.joda.time.DateTime
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.todo.Utilities
import com.jaredrummler.cyanea.CyaneaResources
import com.jaredrummler.cyanea.delegate.CyaneaDelegate
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter
import me.saket.inboxrecyclerview.PullCollapsibleActivity


class SettingsActivity : PullCollapsibleActivity(), PreferencesAdapter.OnScreenChangeListener, BaseCyaneaActivity {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setTheme(R.style.AppThemeLightCollapse)
        setContentView(R.layout.activity_settings)
        val bg: CoordinatorLayout = findViewById(R.id.settingsCoordinator)
        bg.setBackgroundColor(cyanea.backgroundColor)
        expandFromTop()
        val toolbar = findViewById<Toolbar>(R.id.settings_toolbar)
        var icon: Drawable = getResources().getDrawable(R.drawable.ic_clear_black_24dp)
        icon.setColorFilter(if(Utilities.lightOnBackground(cyanea.backgroundColor)) Utilities.colorWithAlpha(Color.WHITE, 1f) else Utilities.colorWithAlpha(Color.BLACK, 1f), PorterDuff.Mode.SRC_ATOP)
        toolbar.setNavigationIcon(icon)
        toolbar.inflateMenu(R.menu.toolbar_settings)
        val prefView = findViewById<RecyclerView>(R.id.settings_rv)
        prefView.layoutManager = LinearLayoutManager(this)
        prefView.adapter = preferencesAdapter

        val preferenceScreen = SettingsLayout.createRootScreen(this)
        val themeIntent = Intent(this, CyaneaSettingsActivity::class.java)
        preferencesAdapter.onScreenChangeListener = object : PreferencesAdapter.OnScreenChangeListener {
            override fun onScreenChanged(preferenceScreen: PreferenceScreen, b: Boolean) {
                if (preferenceScreen.title == "Theme") {
                    startActivity(themeIntent)
                    preferencesAdapter.goBack()
                }
            }
        }

        preferencesAdapter.setRootScreen(preferenceScreen)


        restartAppFlashbar = restartApp()
        restartAppFlashbar2 = restartApp()
    }

    override fun onScreenChanged(preferenceScreen: PreferenceScreen, b: Boolean) {

    }

    private fun restartApp(): Flashbar {
        return Flashbar.Builder(this)
                .gravity(Flashbar.Gravity.BOTTOM)
                .title("Restart App")
                .message("Restart the app for these changes to take place")
                .negativeActionText("Restart later".toUpperCase())
                .positiveActionText("Restart Now".toUpperCase())
                .negativeActionTapListener(object : Flashbar.OnActionTapListener {
                    override fun onActionTapped(flashbar: Flashbar) {
                        flashbar.dismiss()
                    }
                })
                .positiveActionTapListener(object : Flashbar.OnActionTapListener {
                    override fun onActionTapped(flashbar: Flashbar) {
                        val i = baseContext.packageManager
                                .getLaunchIntentForPackage(baseContext.packageName)
                        i!!.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        startActivity(i)
                    }
                })
                .dismissOnTapOutside()
                .backgroundColor(Cyanea.instance.accent)
                .build()

    }



    override fun onBackPressed() {
        if (preferencesAdapter.isInSubScreen()) {
            preferencesAdapter.goBack()
        } else {
            super.onBackPressed()
        }
    }

    companion object {

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */


        var coloredToolbar: Boolean = false
        var folderMode: Boolean = false
        var subFilter: Boolean = false
        var subtaskDefaultShow = true
        var defaultSort: Int = 0
        var timeToNotifyForDateOnly: DateTime = DateTime().withTime(8, 0, 0, 0)

        private var restartAppFlashbar: Flashbar? = null
        private var restartAppFlashbar2: Flashbar? = null

        private val iconForeground: ImageView? = null

        private val preferencesAdapter = PreferencesAdapter()

        fun setTimeToNotifyForDateOnly(dateTime: DateTime, context: Context) {
            timeToNotifyForDateOnly = dateTime
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putLong("pref_notif_only_date", dateTime.millis)
                    .apply()
            preferencesAdapter.currentScreen["pref_notif_only_date"]!!.summary = timeToNotifyForDateOnly.toString("h:mm")
            preferencesAdapter.notifyDataSetChanged()
        }
    }

    private val delegate: CyaneaDelegate by lazy {
        CyaneaDelegate.create(this, cyanea, getThemeResId())
    }

    private val resources: CyaneaResources by lazy {
        CyaneaResources(super.getResources(), cyanea)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(delegate.wrap(newBase))
    }

    override fun getResources(): Resources = resources


}


