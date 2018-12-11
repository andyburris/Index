package com.andb.apps.todo;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.andb.apps.todo.databases.TasksDatabase;
import com.andb.apps.todo.filtering.FilteredLists;
import com.andb.apps.todo.filtering.Filters;
import com.andb.apps.todo.lists.ArchiveTaskList;
import com.andb.apps.todo.lists.TagLinkList;
import com.andb.apps.todo.lists.TagList;
import com.andb.apps.todo.lists.TaskList;
import com.andb.apps.todo.notifications.NotificationHandler;
import com.andb.apps.todo.objects.Tags;
import com.andb.apps.todo.settings.SettingsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;
import com.jaredrummler.cyanea.prefs.CyaneaSettingsActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;

import java.util.ArrayList;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.room.Room;
import androidx.viewpager.widget.ViewPager;


public class MainActivity extends CyaneaAppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    private TabLayout tabLayout;

    public static TextView subTitle;

    public static boolean fabOpen; //for InboxFragment to tell if fabs are visible

    public static String nameFromSettings; //name in drawer
    public static boolean fromSettings; //check if from settings

    public static boolean lightText;
    public static ActionBarDrawerToggle drawerToggle;

    public static int notifKey = 0;

    private boolean appStart;

    public static final String DATABASE_NAME = "Tasks_db";
    public static TasksDatabase tasksDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Debug.startMethodTracing("startup");

        //long startTime = System.nanoTime();


        super.onCreate(savedInstanceState);
        appStart = true;
        loadBeforeSettings();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pagerInitialize();
        fromSettings = false;
        subTitle = findViewById(R.id.toolbar_text);
        //themeSet(toolbar);


        EventBus.getDefault().register(this);


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

        fabInitialize();


        drawerInitialize(toolbar);

        //reportFullyDrawn();


        //long endTime = System.nanoTime();
        //long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration/1000;//to seconds
        //Log.d("startupTime", "OnCreate: " + Long.toString(duration));


    }

    @Override
    protected void onResume() {
        super.onResume();


        if (appStart) {
            //loadTasks();

            tasksDatabase = Room.databaseBuilder(getApplicationContext(),
                    TasksDatabase.class, DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    TaskList.taskList = new ArrayList<>(tasksDatabase.tasksDao().getAll());

                    //InboxFragment.setTaskCountText(TaskList.taskList.size());

                    loadTags();

                    loadTagLinks();

                    Filters.homeViewAdd(); //add current filter to back stack
                    Log.d("noFiltersOnBack", Integer.toString(Filters.backTagFilters.get(Filters.backTagFilters.size() - 1).size()) + ", " + Filters.backTagFilters.size());

                    EventBus.getDefault().post(new UpdateEvent(true));

                    //InboxFragment.setFilterMode(InboxFragment.filterMode);
                }
            });


            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    loadArchiveTasks();

                    loadAfterSettings();


                }
            });

            appStart = false;
        }


        drawerResume();

        String label = getResources().getString(R.string.app_name);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);
        int colorPrimary;


        colorPrimary = getResources().getColor(R.color.cyanea_primary_reference);


        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(label, icon, colorPrimary);
        ((Activity) this).setTaskDescription(taskDescription);//set header color in recents

        if (fromSettings)
            settingsReturn();

    }


    public void loadBeforeSettings() {

        long startTime = System.nanoTime();

        SharedPreferences defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);




        SettingsActivity.folderMode = defaultSharedPrefs.getBoolean("folder_mode", false);

        this.setTheme(R.style.AppThemeGlobal);


        SettingsActivity.defaultSort = Integer.parseInt(defaultSharedPrefs.getString("sort_mode_list", "0"));
        InboxFragment.filterMode = SettingsActivity.defaultSort;

        SettingsActivity.coloredToolbar = defaultSharedPrefs.getBoolean("colored_toolbar", false);
        SettingsActivity.subFilter = defaultSharedPrefs.getBoolean("sub_Filter_pref", false);


        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration;//to seconds

        Log.d("startupTime", "Load Before Settings: " + Long.toString(duration));


    }

    public void loadAfterSettings() {

        long startTime = System.nanoTime();

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);

                SettingsActivity.timeToNotifyForDateOnly = new DateTime(prefs.getLong("pref_notif_only_date", 0));
            }
        });


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



    /*public void themeSet(Toolbar toolbar) {

        long startTime = System.nanoTime();

        if (SettingsActivity.coloredToolbar) {//colored toolbar theming
            toolbar.setBackgroundTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
            //toolbar.setPadding(0, getStatusBarHeight(), 0, 0);

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

            //toolbar.setPadding(0, getStatusBarHeight(), 0, 0);


            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            drawerLayout.setFitsSystemWindows(false);

            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            tabLayout.setSelectedTabIndicatorColor(SettingsActivity.themeColor);

            lightText = true;
            toolbar.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorDarkPrimary)));
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
    }*/

    public void settingsReturn() {
        fabInitialize();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        LinearLayout headerColor = headerView.findViewById(R.id.headerImage);
        headerColor.getBackground().setColorFilter(Cyanea.getInstance().getAccent(), PorterDuff.Mode.OVERLAY);

        Log.d("pref_resume", Boolean.toString(fromSettings));

        TextView navName = findViewById(R.id.navName);
        setName(navName, false);

        InboxFragment.mAdapter.notifyDataSetChanged();
        BrowseFragment.mAdapter.notifyDataSetChanged();


        //themeSet((Toolbar) findViewById(R.id.toolbar));

        NotificationHandler.resetNotifications(this);

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

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));


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
                                InboxFragment.refreshWithAnim();
                                break;
                            case R.id.sortAlpha:
                                InboxFragment.setFilterMode(1);
                                InboxFragment.refreshWithAnim();
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_import_export) {

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Import or export tasks, tags, and links")
                    .setNegativeButton("Export", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            ImportExport.exportTasks(MainActivity.this);
                        }
                    })
                    .setPositiveButton("Import", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ImportExport.importTasks(MainActivity.this);
                        }
                    });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        } else if (id == R.id.nav_test) {
            startActivity(new Intent(this, CyaneaSettingsActivity.class));
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

        for (Tags tags : TagList.tagList) {
            TagList.keyList.add(tags.getTagName());
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

    public void loadSearch() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

            }
        });
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

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddTask.class);
                intent.putExtra("edit", false);
                startActivity(intent);
            }
        });


        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration/1000;//to seconds

        Log.d("startupTime", "Fab initialize: " + Long.toString(duration));
    }


    public void drawerInitialize(Toolbar toolbar) {

        //long startTime = System.nanoTime();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        drawerToggle = toggle;

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setBackgroundColor(Cyanea.getInstance().getBackgroundColor());
        View headerView = navigationView.getHeaderView(0);
        LinearLayout headerColor = headerView.findViewById(R.id.headerImage);
        headerColor.getBackground().setColorFilter(Cyanea.getInstance().getAccent(), PorterDuff.Mode.OVERLAY);
        TextView navName = headerView.findViewById(R.id.navName);
        setName(navName, true);

        navigationView.setItemTextAppearance(R.style.AppThemeNavDrawer);


        //long endTime = System.nanoTime();
        //long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration/1000;//to seconds

        //Log.d("startupTime", "Drawer Initialize: " + Long.toString(duration));

    }

    public void drawerResume() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(0).setChecked(true);

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
        // TaskList.saveTasks(this);
        ArchiveTaskList.saveTasks(this);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void OnUpdateEvent(UpdateEvent event) {

        Log.d("eventbus", "received updateEvent");

        FilteredLists.createFilteredTaskList(Filters.getCurrentFilter(), event.viewing);


        NotificationHandler.resetNotifications(this);
    }


}



