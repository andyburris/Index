package com.andb.apps.todo;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.andrognito.flashbar.Flashbar;
import com.jaredrummler.android.colorpicker.ColorPreference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
public class SettingsActivity extends /*AppCompatActivityAppCompat*/PreferenceActivity {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */

    public static boolean darkTheme;
    public static boolean coloredToolbar;
    public static int themeColor;
    public static boolean folderMode;
    public static boolean subFilter;
    public static int defaultSort;

    private static Flashbar restartAppFlashbar;
    private static Flashbar restartAppFlashbar2;

    private static ImageView iconForeground;

    SharedPreferences.OnSharedPreferenceChangeListener listener;

    private ArrayList<String> headerList = new ArrayList<>();


    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            Log.d("prefValue", "prefchanged: " + preference.getKey());


            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                if (preference.getKey().equals("sort_mode_list")) {
                    defaultSort = index;

                }
                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);


            } else if (preference instanceof SwitchPreference) {
                Log.d("prefValue", "key = " + preference.getKey());
                Log.d("prefValue", "key = " + R.string.pref_key_filter_folder);
                if (preference.getKey().equals("folder_mode")) {
                    folderMode = ((SwitchPreference) preference).isChecked();
                    folderMode = !folderMode;
                    Log.d("prefValue", "folderMode = " + folderMode);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("folder_mode", folderMode);
                    editor.apply();

                    BrowseFragment.createFilteredTaskList(Filters.getCurrentFilter(), true);


                } else if (preference.getKey().equals("dark_theme")) {
                    darkTheme = ((SwitchPreference) preference).isChecked();
                    darkTheme = !darkTheme;
                    Log.d("darkTheme", Boolean.toString(darkTheme));
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("dark_theme", darkTheme);
                    editor.apply();

                    restartAppFlashbar.show();
                }
            } else if (preference instanceof CheckBoxPreference) {
                if (preference.getKey().equals("colored_toolbar")) {

                    coloredToolbar = ((CheckBoxPreference) preference).isChecked();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("colored_toolbar", coloredToolbar);
                    editor.apply();

                    restartAppFlashbar2.show();

                } else if (preference.getKey().equals("sub_Filter_pref")) {
                    subFilter = ((CheckBoxPreference) preference).isChecked();
                    subFilter = !subFilter;
                    Log.d("subFilterPref", Boolean.toString(subFilter));
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("sub_Filter_pref", subFilter);
                    editor.apply();
                }
            } else if (preference instanceof ColorPreference) {
                if (preference.getKey().equals("default_color")) {
                    themeColor = ((int) value);
                    Log.d("prefValue", Integer.toString(themeColor));
                    Log.d("prefValue", Integer.toHexString(themeColor));
                    MainActivity.fromSettings = true;
                    //iconForeground.setColorFilter(themeColor);

                }
            } else if (preference instanceof EditTextPreference) {
                if (preference.getKey().equals("test_name")) {
                    preference.setSummary(stringValue);
                    MainActivity.nameFromSettings = stringValue;
                    MainActivity.fromSettings = true;
                    Log.d("pref_resume", Boolean.toString(MainActivity.fromSettings));

                }
            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);

            }
            return true;
        }

    };

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


    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.preference_headers);


        //setupActionBar();

        restartAppFlashbar = restartApp();
        restartAppFlashbar2 = restartApp();



    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    /*private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }*/

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */

    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
                || ThemePreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("test_name"));
            bindPreferenceSummaryToValue(findPreference("sort_mode_list"));

            Preference folderMode = findPreference("folder_mode");
            folderMode.setDefaultValue(folderMode);
            folderMode.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            Preference subFilter = findPreference(getResources().getString(R.string.pref_sub_folder_filter_key));
            subFilter.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ThemePreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_theme);
            setHasOptionsMenu(true);


            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.


            Preference darkTheme = findPreference("dark_theme");
            darkTheme.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            Preference themeColor = findPreference("default_color");
            themeColor.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            Preference coloredToolbar = findPreference("colored_toolbar");
            coloredToolbar.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            Preference previewIcon = findPreference("icon_demo");
            View preView = previewIcon.getView(null, null);

            /*ImageView foreground = preView.findViewById(R.id.iconForeground);
            if(SettingsActivity.darkTheme){
                foreground.setColorFilter(getResources().getColor(R.color.slate_black));
            }*/

        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }


    }


}


/*package com.andb.apps.todo;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.andrognito.flashbar.Flashbar;
import com.jaredrummler.android.colorpicker.ColorPreference;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

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
 *
public class SettingsActivity extends AppCompatActivity/*AppCompatPreferenceActivity* {

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.


    static Context mContext;


    public static boolean darkTheme;
    public static int themeColor;
    public static boolean folderMode;
    public static int defaultSort;

    private static Flashbar restartAppFlashbar;
    private static ImageView iconForeground;

    SharedPreferences.OnSharedPreferenceChangeListener listener;

    private ArrayList<String> headerList = new ArrayList<>();


    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            Log.d("prefValue", "prefchanged: " + preference.getKey());


            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);
                if(preference.getKey().equals("sort_mode_list")){
                    defaultSort = index;

                }
                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);


            } else if (preference instanceof SwitchPreference) {
                Log.d("prefValue", "key = " + preference.getKey());
                Log.d("prefValue", "key = " + R.string.pref_key_filter_folder);
                if (preference.getKey().equals("folder_mode")) {
                    folderMode = ((SwitchPreference) preference).isChecked();
                    folderMode = !folderMode;
                    Log.d("prefValue", "folderMode = " + folderMode);

                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("folder_mode", folderMode);
                    editor.apply();

                    BrowseFragment.createFilteredTaskList(Filters.getCurrentFilter());


                } else if (preference.getKey().equals("dark_theme")) {
                    darkTheme = ((SwitchPreference) preference).isChecked();
                    darkTheme = !darkTheme;
                    Log.d("darkTheme", Boolean.toString(darkTheme));
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("dark_theme", darkTheme);
                    editor.apply();

                    restartAppFlashbar.show();
                }
            } else if(preference instanceof ColorPreference){
                if(preference.getKey().equals("default_color")) {
                    themeColor = ((int) value);
                    Log.d("prefValue", Integer.toString(themeColor));
                    Log.d("prefValue", Integer.toHexString(themeColor));
                    MainActivity.fromSettings = true;
                    //iconForeground.setColorFilter(themeColor);

                }
            }else if(preference instanceof EditTextPreference){
                if(preference.getKey().equals("test_name")){
                    preference.setSummary(stringValue);
                    MainActivity.nameFromSettings = stringValue;
                    MainActivity.fromSettings = true;
                    Log.d("pref_resume", Boolean.toString(MainActivity.fromSettings));

                }
            }else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);

            }
            return true;
        }

    };

    private Flashbar restartApp() {
        return new Flashbar.Builder(this)
                .gravity(Flashbar.Gravity.BOTTOM)
                .title("Restart App")
                .message("Restart the app for these changes to take place")
                .negativeActionText("Restart later".toUpperCase())
                .positiveActionText("Restart Now".toUpperCase())
                .negativeActionTapListener(new Flashbar.OnActionTapListener(){
                    @Override
                    public void onActionTapped(@NotNull Flashbar flashbar){
                        flashbar.dismiss();
                    }
                })
                .positiveActionTapListener(new Flashbar.OnActionTapListener(){
                    @Override
                    public void onActionTapped(@NotNull Flashbar flashbar){
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


    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.

    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener

    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.preference_headers);

        ConstraintLayout general = (ConstraintLayout) findViewById(R.id.prefGeneralLayout);
        ConstraintLayout theme = (ConstraintLayout) findViewById(R.id.prefThemeLayout);
        ConstraintLayout folders = (ConstraintLayout) findViewById(R.id.prefFolderLayout);

        general.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeneralPreferenceFragment fragment = new GeneralPreferenceFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.pref_header_container, fragment);
                transaction.commit();
            }
        });


        mContext =  getBaseContext();


        setupActionBar();

        restartAppFlashbar = restartApp();


    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     *
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * {@inheritDoc}

     @Override
     public boolean onIsMultiPane() {
     return isXLargeTablet(this);
     }*/

/**
 * {@inheritDoc}
 *
 * @Override
 * @TargetApi(Build.VERSION_CODES.HONEYCOMB) public void onBuildHeaders(List<Header> target) {
 * loadHeadersFromResource(R.xml.pref_headers, target);
 * }
 * This method stops fragment injection in malicious applications.
 * Make sure to deny any unknown fragments here.
 * <p>
 * protected boolean isValidFragment(String fragmentName) {
 * return PreferenceFragment.class.getName().equals(fragmentName)
 * || GeneralPreferenceFragment.class.getName().equals(fragmentName)
 * || ThemePreferenceFragment.class.getName().equals(fragmentName);
 * }
 * This fragment shows general preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 * @TargetApi(Build.VERSION_CODES.HONEYCOMB) public static class GeneralPreferenceFragment extends Fragment/* extends PreferenceFragment {
 * @Override public void onCreate(Bundle savedInstanceState) {
 * super.onCreate(savedInstanceState);
 * //addPreferencesFromResource(R.xml.pref_general);
 * setHasOptionsMenu(true);
 * <p>
 * <p>
 * <p>
 * // Bind the summaries of EditText/List/Dialog/Ringtone preferences
 * // to their values. When their values change, their summaries are
 * // updated to reflect the new value, per the Android Design
 * // guidelines.
 * /*bindPreferenceSummaryToValue(("test_name"));
 * bindPreferenceSummaryToValue(findPreference("sort_mode_list"));
 * <p>
 * <p>
 * folderMode.setDefaultValue(folderMode);
 * folderMode.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);*
 * }
 * @Override public boolean onOptionsItemSelected(MenuItem item) {
 * int id = item.getItemId();
 * if (id == android.R.id.home) {
 * startActivity(new Intent(getActivity(), SettingsActivity.class));
 * return true;
 * }
 * return super.onOptionsItemSelected(item);
 * }
 * <p>
 * }
 * <p>
 * /**
 * This fragment shows notification preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 * @TargetApi(Build.VERSION_CODES.HONEYCOMB) public static class ThemePreferenceFragment extends PreferenceFragment {
 * @Override public void onCreate(Bundle savedInstanceState) {
 * super.onCreate(savedInstanceState);
 * addPreferencesFromResource(R.xml.pref_theme);
 * setHasOptionsMenu(true);
 * <p>
 * <p>
 * <p>
 * <p>
 * // Bind the summaries of EditText/List/Dialog/Ringtone preferences
 * // to their values. When their values change, their summaries are
 * // updated to reflect the new value, per the Android Design
 * // guidelines.
 * <p>
 * <p>
 * <p>
 * <p>
 * Preference darkTheme = findPreference("dark_theme");
 * darkTheme.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
 * <p>
 * Preference themeColor = findPreference("default_color");
 * themeColor.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
 * }
 * @Override public boolean onOptionsItemSelected(MenuItem item) {
 * int id = item.getItemId();
 * if (id == android.R.id.home) {
 * startActivity(new Intent(getActivity(), SettingsActivity.class));
 * return true;
 * }
 * return super.onOptionsItemSelected(item);
 * }
 * <p>
 * <p>
 * }
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * }
 * <p>
 * This method stops fragment injection in malicious applications.
 * Make sure to deny any unknown fragments here.
 * <p>
 * protected boolean isValidFragment(String fragmentName) {
 * return PreferenceFragment.class.getName().equals(fragmentName)
 * || GeneralPreferenceFragment.class.getName().equals(fragmentName)
 * || ThemePreferenceFragment.class.getName().equals(fragmentName);
 * }
 * This fragment shows general preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 * @TargetApi(Build.VERSION_CODES.HONEYCOMB) public static class GeneralPreferenceFragment extends Fragment/* extends PreferenceFragment {
 * @Override public void onCreate(Bundle savedInstanceState) {
 * super.onCreate(savedInstanceState);
 * //addPreferencesFromResource(R.xml.pref_general);
 * setHasOptionsMenu(true);
 * <p>
 * <p>
 * <p>
 * // Bind the summaries of EditText/List/Dialog/Ringtone preferences
 * // to their values. When their values change, their summaries are
 * // updated to reflect the new value, per the Android Design
 * // guidelines.
 * /*bindPreferenceSummaryToValue(("test_name"));
 * bindPreferenceSummaryToValue(findPreference("sort_mode_list"));
 * <p>
 * <p>
 * folderMode.setDefaultValue(folderMode);
 * folderMode.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);*
 * }
 * @Override public boolean onOptionsItemSelected(MenuItem item) {
 * int id = item.getItemId();
 * if (id == android.R.id.home) {
 * startActivity(new Intent(getActivity(), SettingsActivity.class));
 * return true;
 * }
 * return super.onOptionsItemSelected(item);
 * }
 * <p>
 * }
 * <p>
 * /**
 * This fragment shows notification preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 * @TargetApi(Build.VERSION_CODES.HONEYCOMB) public static class ThemePreferenceFragment extends PreferenceFragment {
 * @Override public void onCreate(Bundle savedInstanceState) {
 * super.onCreate(savedInstanceState);
 * addPreferencesFromResource(R.xml.pref_theme);
 * setHasOptionsMenu(true);
 * <p>
 * <p>
 * <p>
 * <p>
 * // Bind the summaries of EditText/List/Dialog/Ringtone preferences
 * // to their values. When their values change, their summaries are
 * // updated to reflect the new value, per the Android Design
 * // guidelines.
 * <p>
 * <p>
 * <p>
 * <p>
 * Preference darkTheme = findPreference("dark_theme");
 * darkTheme.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
 * <p>
 * Preference themeColor = findPreference("default_color");
 * themeColor.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
 * }
 * @Override public boolean onOptionsItemSelected(MenuItem item) {
 * int id = item.getItemId();
 * if (id == android.R.id.home) {
 * startActivity(new Intent(getActivity(), SettingsActivity.class));
 * return true;
 * }
 * return super.onOptionsItemSelected(item);
 * }
 * <p>
 * <p>
 * }
 * <p>
 * <p>
 * <p>
 * <p>
 * <p>
 * }
 */

/**
 * This method stops fragment injection in malicious applications.
 * Make sure to deny any unknown fragments here.

 protected boolean isValidFragment(String fragmentName) {
 return PreferenceFragment.class.getName().equals(fragmentName)
 || GeneralPreferenceFragment.class.getName().equals(fragmentName)
 || ThemePreferenceFragment.class.getName().equals(fragmentName);
 }*/

/**
 * This fragment shows general preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 *
 @TargetApi(Build.VERSION_CODES.HONEYCOMB) public static class GeneralPreferenceFragment extends Fragment/* extends PreferenceFragment {
 @Override public void onCreate(Bundle savedInstanceState) {
 super.onCreate(savedInstanceState);
 //addPreferencesFromResource(R.xml.pref_general);
 setHasOptionsMenu(true);



 // Bind the summaries of EditText/List/Dialog/Ringtone preferences
 // to their values. When their values change, their summaries are
 // updated to reflect the new value, per the Android Design
 // guidelines.
 /*bindPreferenceSummaryToValue(("test_name"));
 bindPreferenceSummaryToValue(findPreference("sort_mode_list"));


 folderMode.setDefaultValue(folderMode);
 folderMode.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);*
 }

 @Override public boolean onOptionsItemSelected(MenuItem item) {
 int id = item.getItemId();
 if (id == android.R.id.home) {
 startActivity(new Intent(getActivity(), SettingsActivity.class));
 return true;
 }
 return super.onOptionsItemSelected(item);
 }

 }

 /**
  * This fragment shows notification preferences only. It is used when the
  * activity is showing a two-pane settings UI.
 *
 @TargetApi(Build.VERSION_CODES.HONEYCOMB) public static class ThemePreferenceFragment extends PreferenceFragment {
 @Override public void onCreate(Bundle savedInstanceState) {
 super.onCreate(savedInstanceState);
 addPreferencesFromResource(R.xml.pref_theme);
 setHasOptionsMenu(true);




 // Bind the summaries of EditText/List/Dialog/Ringtone preferences
 // to their values. When their values change, their summaries are
 // updated to reflect the new value, per the Android Design
 // guidelines.




 Preference darkTheme = findPreference("dark_theme");
 darkTheme.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

 Preference themeColor = findPreference("default_color");
 themeColor.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);
 }

 @Override public boolean onOptionsItemSelected(MenuItem item) {
 int id = item.getItemId();
 if (id == android.R.id.home) {
 startActivity(new Intent(getActivity(), SettingsActivity.class));
 return true;
 }
 return super.onOptionsItemSelected(item);
 }


 }





 }
 */