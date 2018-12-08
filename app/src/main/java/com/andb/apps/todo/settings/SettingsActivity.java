package com.andb.apps.todo.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;
import androidx.appcompat.widget.Toolbar;

import com.andb.apps.todo.R;
import com.andrognito.flashbar.Flashbar;
import com.jaredrummler.android.colorpicker.ColorPanelView;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.Maxr1998.modernpreferences.PreferenceScreen;
import de.Maxr1998.modernpreferences.PreferencesAdapter;
import me.saket.inboxrecyclerview.PullCollapsibleActivity;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends PullCollapsibleActivity implements PreferencesAdapter.OnScreenChangeListener, ColorPickerDialogListener {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */

    public static boolean darkTheme;
    public static boolean coloredToolbar;
    public static int themeColor;
    public static boolean folderMode;
    public static boolean subFilter;
    public static boolean subtaskDefaultShow = true;
    public static int defaultSort;
    public static DateTime timeToNotifyForDateOnly;

    private static Flashbar restartAppFlashbar;
    private static Flashbar restartAppFlashbar2;

    private static ImageView iconForeground;

    private static PreferencesAdapter preferencesAdapter = new PreferencesAdapter();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTheme(R.style.AppThemeLightCollapse);


        setContentView(R.layout.activity_settings);
        expandFromTop();
        Toolbar toolbar = findViewById(R.id.settings_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_clear_black_24dp);
        toolbar.inflateMenu(R.menu.toolbar_settings);
        RecyclerView prefView = findViewById(R.id.settings_rv);
        prefView.setLayoutManager(new LinearLayoutManager(this));
        prefView.setAdapter(preferencesAdapter);

        PreferenceScreen preferenceScreen = SettingsLayout.INSTANCE.createRootScreen(this);

        preferencesAdapter.setRootScreen(preferenceScreen);


        restartAppFlashbar = restartApp();
        restartAppFlashbar2 = restartApp();
    }

    @Override
    public void onScreenChanged(@NotNull PreferenceScreen preferenceScreen, boolean b) {

    }

    private Flashbar restartApp() {
        return new Flashbar.Builder(this)
                .gravity(Flashbar.Gravity.BOTTOM)
                .title("Restart App")
                .message("Restart the app for these changes to take place")
                .negativeActionText("Restart later".toUpperCase())
                .positiveActionText("Restart Now".toUpperCase())
                .negativeActionTapListener(new Flashbar.OnActionTapListener() {
                    @Override
                    public void onActionTapped(@NotNull Flashbar flashbar) {
                        flashbar.dismiss();
                    }
                })
                .positiveActionTapListener(new Flashbar.OnActionTapListener() {
                    @Override
                    public void onActionTapped(@NotNull Flashbar flashbar) {
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                })
                .dismissOnTapOutside()
                .backgroundColor(SettingsActivity.themeColor)
                .build();

    }

    public static void setTimeToNotifyForDateOnly(DateTime dateTime, Context context) {
        timeToNotifyForDateOnly = dateTime;
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putLong("pref_notif_only_date", dateTime.getMillis())
                .apply();
        preferencesAdapter.getCurrentScreen().get("pref_notif_only_date").setSummary(timeToNotifyForDateOnly.toString("h:mm"));
        preferencesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        SettingsActivity.themeColor = color;
        preferencesAdapter.getCurrentScreen().get("theme_color").commitInt(color);
        ((ColorPanelView)findViewById(R.id.colorPanel)).setColor(SettingsActivity.themeColor);
        preferencesAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed() {
        if(preferencesAdapter.isInSubScreen()){
            preferencesAdapter.goBack();
        }else {
            super.onBackPressed();
        }
    }
}


