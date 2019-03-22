package com.andb.apps.todo.settings

import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andb.apps.todo.Onboarding
import com.andb.apps.todo.R
import com.andb.apps.todo.SORT_TIME
import com.andb.apps.todo.utilities.Utilities
import com.jaredrummler.cyanea.CyaneaResources
import com.jaredrummler.cyanea.app.BaseCyaneaActivity
import com.jaredrummler.cyanea.delegate.CyaneaDelegate
import com.jaredrummler.cyanea.prefs.CyaneaSettingsActivity
import com.pixplicity.easyprefs.library.Prefs
import de.Maxr1998.modernpreferences.PreferenceScreen
import de.Maxr1998.modernpreferences.PreferencesAdapter
import me.saket.inboxrecyclerview.PullCollapsibleActivity
import org.joda.time.DateTime


class SettingsActivity : PullCollapsibleActivity(), PreferencesAdapter.OnScreenChangeListener,
    BaseCyaneaActivity {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setTheme(R.style.AppThemeLightCollapse)
        setContentView(R.layout.activity_settings)

        val bg: CoordinatorLayout = findViewById(R.id.settingsCoordinator)
        bg.setBackgroundColor(cyanea.backgroundColor)

        if (intent.hasExtra("expandRect")) {
            val expandRect: Rect? = Rect.unflattenFromString(intent.extras.getString("expandRect"))
            if (expandRect != null) {
                expandFrom(expandRect)
            } else {
                expandFromTop()
            }
        } else {
            expandFromTop()
        }

        val toolbar = findViewById<Toolbar>(R.id.settings_toolbar)
        var icon: Drawable = getResources().getDrawable(R.drawable.ic_clear_black_24dp)
        icon.setColorFilter(if (Utilities.lightOnBackground(cyanea.backgroundColor)) Utilities.colorWithAlpha(Color.WHITE, 1f) else Utilities.colorWithAlpha(Color.BLACK, 1f), PorterDuff.Mode.SRC_ATOP)
        toolbar.setNavigationIcon(icon)
        toolbar.inflateMenu(R.menu.toolbar_settings)

        val prefView = findViewById<RecyclerView>(R.id.settings_rv)
        prefView.layoutManager = LinearLayoutManager(this)
        prefView.adapter = preferencesAdapter

        val preferenceScreen = SettingsLayout.createRootScreen(this)
        val themeIntent = Intent(this, CyaneaSettingsActivity::class.java)
        val tutorialIntent = Intent(this, Onboarding::class.java)
        preferencesAdapter.onScreenChangeListener = object :
            PreferencesAdapter.OnScreenChangeListener {
            override fun onScreenChanged(preferenceScreen: PreferenceScreen, b: Boolean) {
                when (preferenceScreen.title) {
                    "Theme" -> {
                        startActivity(themeIntent)
                        preferencesAdapter.goBack()
                    }
                    "Show Tutorial" -> {
                        startActivity(tutorialIntent)
                        preferencesAdapter.goBack()
                    }
                }
            }
        }

        preferencesAdapter.setRootScreen(preferenceScreen)

    }

    override fun onScreenChanged(preferenceScreen: PreferenceScreen, b: Boolean) {}


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


        @JvmStatic
        var coloredToolbar: Boolean = false
        @JvmStatic
        var folderMode: Boolean = false
        @JvmStatic
        var subFilter: Boolean = false
        @JvmStatic
        var subtaskDefaultShow = false
        @JvmStatic
        var defaultSort: Int = SORT_TIME

        @JvmStatic
        var timeToNotifyForDateOnly: DateTime = DateTime().withTime(8, 0, 0, 0)


        private val preferencesAdapter = PreferencesAdapter()

        fun setTimeToNotifyForDateOnly(dateTime: DateTime, context: Context) {
            timeToNotifyForDateOnly = dateTime
            Prefs.putLong("pref_notif_only_date", dateTime.millis)
            preferencesAdapter.currentScreen["pref_notif_only_date"]?.summary = timeToNotifyForDateOnly.toString("h:mm")
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


