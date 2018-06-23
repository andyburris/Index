package com.andb.apps.todo;


import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Duration;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import static com.andb.apps.todo.NotifyWorker.workTag;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private TabLayout tabLayout;

    public static Toolbar subTitle;

    public static boolean fabOpen; //for InboxFragment to tell if fabs are visible

    public static String nameFromSettings; //name in drawer
    public static boolean fromSettings; //check if from settings

    public static boolean lightText;
    public static ActionBarDrawerToggle drawerToggle;

    public static int notifKey = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        JodaTimeAndroid.init(this);//initialize joda-time
        loadBeforeSettings();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        subTitle = toolbar;
        subTitle.setSubtitle(Filters.subtitle);
        setSupportActionBar(toolbar);
        pagerInitialize();
        fromSettings = false;
        themeSet(toolbar);

        long startTime = System.nanoTime();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.containsKey("posFromNotif")) {
                notifKey = bundle.getInt("posFromNotif", 0);
                Log.d("notificationKey", Integer.toString(notifKey));
                int index = TaskList.keyList.indexOf(notifKey);
                if (index != -1) {
                    Log.d("alreadyNotifiedNotif", Boolean.toString(TaskList.getItem(index).isNotified()));
                } else {
                    Toast.makeText(this, "Couldn't find that task", Toast.LENGTH_SHORT).show();
                    notifKey = 0;//don't scroll to in InboxFragment

                }
            }
            Log.d("notificationBundle", Integer.toString(TaskList.keyList.indexOf(notifKey)));
        }


        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_theme, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_notifications, false);
        PreferenceManager.setDefaultValues(this, R.xml.pref_folders, false);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration/1000;//to seconds

        Log.d("startupTime", "Get extras and set default values: " + Long.toString(duration));

        fabInitialize();


        drawerInitialize(toolbar);

        loadAfterSettings();

        loadTasks();


        loadTags();

        loadTagLinks();

        loadArchiveTasks();




    }

    @Override
    protected void onResume() {
        super.onResume();

        String label = getResources().getString(R.string.app_name);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);
        int colorPrimary;
        if (SettingsActivity.coloredToolbar) {
            colorPrimary = SettingsActivity.themeColor;
        } else {
            if (SettingsActivity.darkTheme) {
                colorPrimary = getResources().getColor(R.color.colorDarkPrimary);
            } else {
                colorPrimary = getResources().getColor(R.color.colorPrimary);
            }
        }

        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(label, icon, colorPrimary);
        ((Activity) this).setTaskDescription(taskDescription);//set header color in recents

        if (fromSettings)
            settingsReturn();

    }


    public void loadBeforeSettings() {

        long startTime = System.nanoTime();


        SettingsActivity.themeColor = PreferenceManager.getDefaultSharedPreferences(this).getInt("default_color", 0);


        SettingsActivity.folderMode = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("folder_mode", false);
        SettingsActivity.darkTheme = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("dark_theme", false);
        Log.d("darkTheme", Boolean.toString(SettingsActivity.darkTheme));
        if (SettingsActivity.darkTheme) {
            this.setTheme(R.style.AppThemeDarkMain);
        } else {
            this.setTheme(R.style.AppThemeLightMain);
        }

        SettingsActivity.defaultSort = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(this).getString("sort_mode_list", "0"));
        InboxFragment.filterMode = SettingsActivity.defaultSort;

        SettingsActivity.coloredToolbar = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("colored_toolbar", false);
        SettingsActivity.subFilter = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("sub_Filter_pref", false);


        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration;//to seconds

        Log.d("startupTime", "Load Before Settings: " + Long.toString(duration));


    }

    public void loadAfterSettings() {

        long startTime = System.nanoTime();


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Gson gson = Converters.registerDateTime(new GsonBuilder()).create();
        String json = prefs.getString("pref_notify_only_date", null);
        Type type = new TypeToken<DateTime>() {
        }.getType();
        SettingsActivity.timeToNotifyForDateOnly = gson.fromJson(json, type);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration;//to seconds

        Log.d("startupTime", "Load After Settings: " + Long.toString(duration));
    }


    public int getStatusBarHeight() {
        int result = 0;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = getResources().getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }

    public void themeSet(Toolbar toolbar) {

        long startTime = System.nanoTime();

        if (SettingsActivity.coloredToolbar) {//colored toolbar theming
            toolbar.setBackgroundColor(SettingsActivity.themeColor);
            toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

            DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawerLayout.setFitsSystemWindows(false);

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);


            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            int color = (int) Long.parseLong(Integer.toHexString(SettingsActivity.themeColor), 16);
            int r = (color >> 16) & 0xFF;
            int g = (color >> 8) & 0xFF;
            int b = (color >> 0) & 0xFF;

            int textColor;

            if ((r * 0.299 + g * 0.587 + b * 0.114) > 186) {
                lightText = false;
                textColor = 0xFF000000;
                tabLayout.setTabTextColors(0x99000000, textColor);
                tabLayout.setSelectedTabIndicatorColor(textColor);

            } else {
                lightText = true;
                textColor = 0xFFFFFFFF;
                tabLayout.setTabTextColors(0x99FFFFFF, textColor);
                tabLayout.setSelectedTabIndicatorColor(textColor);


            }


            toolbar.setTitleTextColor(textColor);
            toolbar.getOverflowIcon().setColorFilter(textColor, PorterDuff.Mode.SRC_ATOP);

            tabLayout.setBackgroundColor(SettingsActivity.themeColor);
            getWindow().getDecorView().setSystemUiVisibility(0);

            toolbar.setSubtitleTextColor(textColor);

            Log.d("pref_resume", Boolean.toString(fromSettings));
            if (fromSettings) {
                Menu menu = toolbar.getMenu();
                if (lightText) {
                    for (int i = 0; i < menu.size() - 1; i++) {
                        Log.d("darkTheme", "Icon " + Integer.toString(i));
                        menu.getItem(i).getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                    }
                    drawerToggle.getDrawerArrowDrawable().setColor(Color.WHITE);

                } else {
                    drawerToggle.getDrawerArrowDrawable().setColor(Color.BLACK);
                    for (int i = 0; i < menu.size() - 1; i++) {
                        Log.d("darkTheme", "Icon " + Integer.toString(i));
                        menu.getItem(i).getIcon().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);
                    }
                }


            }

        } else if (SettingsActivity.darkTheme) {//dark theme setting

            toolbar.setPadding(0, getStatusBarHeight(), 0, 0);


            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawerLayout.setFitsSystemWindows(false);

            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            tabLayout.setSelectedTabIndicatorColor(SettingsActivity.themeColor);

            lightText = true;
            toolbar.setBackgroundColor(getResources().getColor(R.color.colorDarkPrimary));
            toolbar.setTitleTextColor(Color.WHITE);
            toolbar.getOverflowIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

            tabLayout.setBackgroundColor(getResources().getColor(R.color.colorDarkPrimary));
            tabLayout.setTabTextColors(0x99FFFFFF, Color.WHITE);
            getWindow().getDecorView().setSystemUiVisibility(0);

            toolbar.setSubtitleTextColor(Color.WHITE);

            Menu menu = toolbar.getMenu();
            for (int i = 0; i < menu.size() - 1; i++) {
                Log.d("darkTheme", "Icon " + Integer.toString(i));
                menu.getItem(i).getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }


        } else {//general accent settings
            tabLayout.setSelectedTabIndicatorColor(SettingsActivity.themeColor);
            Menu menu = toolbar.getMenu();
            for (int i = 0; i < menu.size() - 1; i++) {
                Log.d("darkTheme", "Icon " + Integer.toString(i));
                menu.getItem(i).getIcon().setColorFilter(getResources().getColor(R.color.slate_black), PorterDuff.Mode.SRC_ATOP);
            }
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration/1000;//to seconds

        Log.d("startupTime", "Theme Set: " + Long.toString(duration));
    }

    public void settingsReturn() {
        fabInitialize();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        LinearLayout headerColor = headerView.findViewById(R.id.headerImage);
        headerColor.getBackground().setColorFilter(SettingsActivity.themeColor, PorterDuff.Mode.OVERLAY);

        Log.d("pref_resume", Boolean.toString(fromSettings));

        TextView navName = findViewById(R.id.navName);
        setName(navName, false);

        InboxFragment.mAdapter.notifyDataSetChanged();
        BrowseFragment.mAdapter.notifyDataSetChanged();

        Log.d("darkTheme", Boolean.toString(SettingsActivity.darkTheme));


        themeSet((Toolbar) findViewById(R.id.toolbar));

        restartNotificationService();

        fromSettings = false;


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (Filters.backTagFilters != null & Filters.backTagFilters.size() > 1) {
            Filters.tagBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        Log.d("darkTheme", "Menu Size: " + menu.size());

        if (lightText) {
            for (int i = 0; i < menu.size() - 1; i++) {
                Log.d("darkTheme", "Icon " + Integer.toString(i));
                menu.getItem(i).getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;

            case R.id.app_bar_filter:
                PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.app_bar_filter));
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        int id = item.getItemId();
                        Log.d("filterclicked", Integer.toString(id));
                        switch (id) {
                            case R.id.sortDate:
                                InboxFragment.setFilterMode(0);
                                InboxFragment.mAdapter.notifyDataSetChanged();
                                break;
                            case R.id.sortAlpha:
                                InboxFragment.setFilterMode(1);
                                InboxFragment.mAdapter.notifyDataSetChanged();
                                break;

                        }
                        return true;
                    }
                });

                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.filter_menu, popupMenu.getMenu());
                popupMenu.show();
        }


        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Handle the camera action
        } else if (id == R.id.nav_archive) {
            startActivity(new Intent(this, Archive.class));
        } else if (id == R.id.nav_settings) {
            startActivity(new Intent(this, SettingsActivity.class));

        } else if (id == R.id.nav_test) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return InboxFragment.newInstance();

                case 1:
                    return BrowseFragment.newInstance();
                default:
                    return null;
            }
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }
    }



    public void loadTags() {

        long startTime = System.nanoTime();

        TagList.loadTags(this);

        if (TagList.tagList == null) {
            TagList.tagList = TagSelect.blankTagList;
            TagList.saveTags(this);
            TagList.loadTags(this);
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration/1000;//to seconds

        Log.d("startupTime", "Load tags: " + Long.toString(duration));
    }

    public void loadTagLinks() {

        long startTime = System.nanoTime();

        TagLinkList.loadTags(this);

        if (TagLinkList.linkList == null) {
            TagLinkList.linkList = BrowseFragment.blankTagLinkList;
            TagList.saveTags(this);
            TagList.loadTags(this);
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration/1000;//to seconds

        Log.d("startupTime", "Load tag links: " + Long.toString(duration));

    }

    public void loadTasks() {

        long startTime = System.nanoTime();

        TaskList.loadTasks(this);

        if (TaskList.taskList == null) {
            TaskList.taskList = InboxFragment.blankTaskList;
            TaskList.saveTasks(this);
            TaskList.loadTasks(this);
        }


        TaskList.keyList.clear();
        for (int i = 0; i < TaskList.taskList.size(); i++) {
            TaskList.keyList.add(TaskList.getItem(i).getKey());
        }


        Log.d("putKeys", Integer.toString(TaskList.keyList.size()));
        for (int i = 0; i < TaskList.keyList.size(); i++) {
            Log.d("putKeys", Integer.toString(TaskList.keyList.get(i)));
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration;//to seconds

        Log.d("startupTime", "Load tasks (and keys): " + Long.toString(duration));

    }

    public void loadArchiveTasks() {

        long startTime = System.nanoTime();

        ArchiveTaskList.loadTasks(this);

        if (ArchiveTaskList.taskList == null) {
            ArchiveTaskList.taskList = ArchiveFragment.blankTaskList;
            ArchiveTaskList.saveTasks(this);
            ArchiveTaskList.loadTasks(this);
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration/1000;//to seconds

        Log.d("startupTime", "Load archived tasks: " + Long.toString(duration));

    }


    public void pagerInitialize() {

        long startTime = System.nanoTime();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        mViewPager = (ViewPager) findViewById(R.id.container);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration/1000;//to seconds

        Log.d("startupTime", "Initialize pager: " + Long.toString(duration));
    }


    public void fabInitialize() {

        long startTime = System.nanoTime();

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        Log.d("prefLoad", Integer.toHexString(SettingsActivity.themeColor));


        final FloatingActionButton fab_main = (FloatingActionButton) findViewById(R.id.fab_main);

        fab_main.setBackgroundTintList(ColorStateList.valueOf(SettingsActivity.themeColor));

        final FloatingActionButton fab_list = (FloatingActionButton) findViewById(R.id.fab_list);
        fab_list.setBackgroundTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
        final FloatingActionButton fab_tag = (FloatingActionButton) findViewById(R.id.fab_tag);
        fab_tag.setBackgroundTintList(ColorStateList.valueOf(SettingsActivity.themeColor));

        fab_main.setOnClickListener(new View.OnClickListener() {

            float StartRotate = 0;
            float EndRotate = 0;

            ViewGroup fab_layout = (ViewGroup) findViewById(R.id.fab_layout);


            @Override
            public void onClick(View view) {
                if (fab_list.getVisibility() == View.VISIBLE) {
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.sd_fade_and_translate_out);
                    Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.sd_scale_fade_and_translate_out);
                    fab_list.setVisibility(View.GONE);
                    fab_tag.setVisibility(View.GONE);
                    fab_main.animate().rotation(0).setDuration(200);
                    fab_list.startAnimation(animation);
                    fab_list.startAnimation(animation2);
                    fab_tag.startAnimation(animation);
                    fab_tag.startAnimation(animation2);
                    fabOpen = false;


                } else {
                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.sd_fade_and_translate_in);
                    Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.sd_scale_fade_and_translate_in);
                    fab_list.setVisibility(View.VISIBLE);
                    fab_tag.setVisibility(View.VISIBLE);
                    fab_main.animate().rotation(45).setDuration(200);
                    fab_list.startAnimation(animation);
                    fab_list.startAnimation(animation2);
                    fab_tag.startAnimation(animation);
                    fab_tag.startAnimation(animation2);
                    fabOpen = true;


                }

            }


        });


        fab_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, AddTask.class);
                intent.putExtra("edit", false);
                startActivity(intent);

            }
        });
        fab_tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, TagSelect.class);
                intent.putExtra("isTagLink", true);
                startActivity(intent);


            }
        });

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration/1000;//to seconds

        Log.d("startupTime", "Fab initialize: " + Long.toString(duration));
    }


    public void drawerInitialize(Toolbar toolbar) {

        long startTime = System.nanoTime();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        if (lightText) {
            toggle.getDrawerArrowDrawable().setColor(Color.WHITE);
        }
        drawerToggle = toggle;

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        LinearLayout headerColor = headerView.findViewById(R.id.headerImage);
        headerColor.getBackground().setColorFilter(SettingsActivity.themeColor, PorterDuff.Mode.OVERLAY);
        TextView navName = headerView.findViewById(R.id.navName);
        setName(navName, true);

        if (lightText) {
            drawerToggle.getDrawerArrowDrawable().setColor(Color.WHITE);

        } else {
            drawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.slate_black));
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration/1000;//to seconds

        Log.d("startupTime", "Drawer Initialize: " + Long.toString(duration));

    }


    public void setName(TextView navName, boolean start) {

        if (start) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            nameFromSettings = prefs.getString("test_name", "");
            Log.d("prefLoadName", nameFromSettings);
        }


        navName.setText(nameFromSettings);


    }


    @Override
    public void onPause() {
        super.onPause();
        TaskList.saveTasks(this);
        ArchiveTaskList.saveTasks(this);
    }

    private final static String todo_notification_channel = "Task Reminders";


    public static void restartNotificationService() {
        //Here we set the request for the next notification


        if (TaskList.getNextNotificationItem(false) != null) {//if there are any left, restart the service
            Log.d("workManager", TaskList.getNextNotificationItem(false).getListName());
            Duration duration = new Duration(DateTime.now(), TaskList.getNextNotificationItem(false).getDateTime());
            Log.d("workManager", TaskList.getNextNotificationItem(false).getDateTime().toString("MMM d, h:mm:ss a"));
            if (TaskList.getNextNotificationItem(false).getDateTime().get(DateTimeFieldType.secondOfMinute()) == (59)) {
                DateTime onlyDate = TaskList.getNextNotificationItem(false).getDateTime();
                onlyDate = onlyDate.withTime(SettingsActivity.timeToNotifyForDateOnly.toLocalTime());
                duration = new Duration(DateTime.now(), onlyDate);
            }
            long delay = duration.getStandardSeconds();

            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotifyWorker.class)
                    .setInitialDelay(delay, TimeUnit.SECONDS)
                    .addTag(workTag)
                    .build();


            WorkManager.getInstance().enqueue(notificationWork);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Store our shared preference
        SharedPreferences sp = getSharedPreferences("OURINFO", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", true);
        ed.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Store our shared preference
        SharedPreferences sp = getSharedPreferences("OURINFO", MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        ed.putBoolean("active", false);
        ed.apply();

    }
}



