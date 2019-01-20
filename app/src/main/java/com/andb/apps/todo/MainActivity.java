package com.andb.apps.todo;


import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.andb.apps.todo.databases.GetDatabase;
import com.andb.apps.todo.databases.MigrationHelper;
import com.andb.apps.todo.databases.ProjectsDatabase;
import com.andb.apps.todo.eventbus.MigrateEvent;
import com.andb.apps.todo.eventbus.UpdateEvent;
import com.andb.apps.todo.filtering.FilteredLists;
import com.andb.apps.todo.filtering.Filters;
import com.andb.apps.todo.lists.ProjectList;
import com.andb.apps.todo.notifications.NotificationHandler;
import com.andb.apps.todo.settings.SettingsActivity;
import com.andb.apps.todo.utilities.Current;
import com.andb.apps.todo.views.InboxRVViewPager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.jaredrummler.android.colorpicker.ColorPanelView;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;


public class MainActivity extends CyaneaAppCompatActivity implements ColorPickerDialogListener {

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    public static InboxRVViewPager mViewPager;



    public static boolean fabOpen; //for InboxFragment to tell if fabs are visible

    public static boolean fromSettings; //check if from settings

    public static boolean lightText;

    public static int notifKey = 0;

    private boolean appStart;

    public static ProjectsDatabase projectsDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        appStart = true;
        fromSettings = false;

        loadSettings();

        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Drawable navIcon = toolbar.getNavigationIcon().mutate();
        navIcon.setColorFilter(App.Companion.colorAlpha(Cyanea.getInstance().getPrimary(), .8f, .54f), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationIcon(navIcon);

        pagerInitialize();
        getWindow().setStatusBarColor(0x33333333);

        fabInitialize();


    }

    @Override
    protected void onResume() {
        super.onResume();


        if (appStart) {
            //loadTasks();

            projectsDatabase = GetDatabase.getDatabase(getApplicationContext());


            AsyncTask.execute(() -> {
                ProjectList.INSTANCE.appStart(this, projectsDatabase);
                Filters.homeViewAdd(false); //add current filter to back stack


                Log.d("eventBusTrace", "onResume/appstart");
                EventBus.getDefault().post(new UpdateEvent(false, true));

            });


            appStart = false;
        }

        String label = getResources().getString(R.string.app_name);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);
        int colorPrimary;


        colorPrimary = getResources().getColor(R.color.cyanea_primary_reference);


        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(label, icon, colorPrimary);
        this.setTaskDescription(taskDescription);//set header color in recents

        if (fromSettings)
            settingsReturn();

    }

    public void loadSettings() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        SettingsActivity.Companion.setFolderMode(prefs.getBoolean("folder_mode", false));

        this.setTheme(R.style.AppThemeGlobal);

        try {
            prefs.getBoolean("sort_mode_list", true);
        } catch (Exception e) {
            prefs.edit().putBoolean("sort_mode_list", true).apply();
        }
        if (prefs.getBoolean("sort_mode_list", true)) {
            SettingsActivity.Companion.setDefaultSort(0);
        } else {
            SettingsActivity.Companion.setDefaultSort(1);
        }
        InboxFragment.Companion.setFilterMode(SettingsActivity.Companion.getDefaultSort());

        SettingsActivity.Companion.setColoredToolbar(prefs.getBoolean("colored_toolbar", false));
        SettingsActivity.Companion.setSubFilter(prefs.getBoolean("sub_Filter_pref", false));
        SettingsActivity.Companion.setSubtaskDefaultShow(prefs.getBoolean("expand_lists", true));

        AsyncTask.execute(() -> {
            SettingsActivity.Companion.setTimeToNotifyForDateOnly(new DateTime(prefs.getLong("pref_notif_only_date", 0)));
        });
    }


    public void settingsReturn() {
        fabInitialize();



        Log.d("pref_resume", Boolean.toString(fromSettings));

        InboxFragment.Companion.getMAdapter().notifyDataSetChanged();
        BrowseFragment.mAdapter.notifyDataSetChanged();


        NotificationHandler.resetNotifications(this);

        fromSettings = false;


    }

    @Override
    public void onBackPressed() {

        if (Drawer.bottomSheetBehavior.getState()==BottomSheetBehavior.STATE_EXPANDED) {
            Drawer.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (TaskView.Companion.getPageState() != 0) {
            InboxFragment.mRecyclerView.collapse();
            BrowseFragment.mRecyclerView.collapse();
        } else if (MaterialCab.Companion.isActive()) {
            MaterialCab.Companion.destroy();
        } else if (Filters.backTagFilters != null & Filters.backTagFilters.size() > 1) {
            Filters.tagBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(@NotNull Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        menu.setGroupVisible(R.id.toolbar_task_view, false);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));

        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            try {
                Drawable drawable = menuItem.getIcon().mutate();
                drawable.setColorFilter(App.Companion.colorAlpha(getCyanea().getPrimary(), .8f, .54f), PorterDuff.Mode.SRC_ATOP);
                menuItem.setIcon(drawable);
            } catch (Exception e) {
                Log.d("menuIconColor", "not an icon (collapsed in overflow)");
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
            /*MAINACTIVITY ITEMS*/
            case R.id.action_settings:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                return true;

            case R.id.app_bar_filter:
                PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.app_bar_filter));
                popupMenu.setOnMenuItemClickListener(item1 -> {
                    int id1 = item1.getItemId();
                    Log.d("filterclicked", Integer.toString(id1));
                    switch (id1) {
                        case R.id.sortDate:
                            InboxFragment.Companion.setFilterMode(0);
                            InboxFragment.Companion.refreshWithAnim();
                            break;
                        case R.id.sortAlpha:
                            InboxFragment.Companion.setFilterMode(1);
                            InboxFragment.Companion.refreshWithAnim();
                            break;

                    }
                    return true;
                });

                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.filter_menu, popupMenu.getMenu());
                popupMenu.show();

                break;
            case R.id.action_test:
                Intent intent = new Intent(this, Onboarding.class);
                startActivity(intent);
                return true;

                /*TASKVIEW ITEMS*/
            case R.id.app_bar_edit:
                TaskView.Companion.editFromToolbar(this);
                return true;
            //TODO: Reschedule
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return InboxFragment.Companion.newInstance();

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


    public void pagerInitialize() {

        long startTime = System.nanoTime();

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration/1000;//to seconds

        Log.d("startupTime", "Initialize pager: " + Long.toString(duration));
    }


    public void fabInitialize() {

        long startTime = System.nanoTime();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            if(TaskView.Companion.getPageState()==0) {
                Intent intent = new Intent(MainActivity.this, AddTask.class);
                intent.putExtra("edit", false);
                startActivity(intent);
            }else {
                TaskView.Companion.taskDone();
            }
        });


        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration/1000;//to seconds

        Log.d("startupTime", "Fab initialize: " + Long.toString(duration));
    }


    static boolean expanded = false;

    public void setupProjectSelector() {

        FrameLayout bottomSheet = findViewById(R.id.bottom_sheet_container);

        TextView toolbarSubtitle = findViewById(R.id.toolbar_project_name);
        toolbarSubtitle.setText(Current.project().getName());

        Drawer.bottomSheetBehavior = (BottomSheetBehavior.from(bottomSheet));
        Drawer.bottomSheetBehavior.setBottomSheetCallback(Drawer.getNormalSheetCallback());

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> {
            if(TaskView.Companion.getPageState()==0) {
                expanded = !expanded;
                Log.d("expanded", Boolean.toString(expanded));
                if (expanded) {
                    Drawer.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    Drawer.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }else {
                InboxFragment.mRecyclerView.collapse();
                BrowseFragment.mRecyclerView.collapse();
            }
        });

    }



    public void setName(TextView navName) {

        if (Current.allProjects().size() > 0) {
            navName.setText(Current.project().getName());
        }
    }


    @Override
    public void onPause() {
        super.onPause();
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

        if(event.setupProject){
            setupProjectSelector();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMigrateEvent(MigrateEvent event) {
        MigrationHelper.migrate_1_2_with_context(this, projectsDatabase, event.taskList);
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case Drawer.DIALOG_ID: {
                ColorPanelView colorPanelView = findViewById(R.id.projectColorPreview);
                if(colorPanelView!=null){
                    colorPanelView.setColor(color);
                }
                Drawer.setSelectedColor(color);
            }
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }


}



