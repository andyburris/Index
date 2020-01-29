package com.andb.apps.todo;


import android.app.ActivityManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
import android.widget.SearchView;

import com.afollestad.materialcab.MaterialCab;
import com.andb.apps.todo.databases.GetDatabase;
import com.andb.apps.todo.databases.MigrationHelper;
import com.andb.apps.todo.filtering.FilteredListsKt;
import com.andb.apps.todo.filtering.Filters;
import com.andb.apps.todo.lists.ProjectList;
import com.andb.apps.todo.data.model.Tag;
import com.andb.apps.todo.data.model.Task;
import com.andb.apps.todo.ui.settings.SettingsActivity;
import com.andb.apps.todo.ui.archive.Archive;
import com.andb.apps.todo.ui.drawer.Drawer;
import com.andb.apps.todo.ui.inbox.InboxFragment;
import com.andb.apps.todo.ui.inbox.TaskAdapter;
import com.andb.apps.todo.ui.inbox.TaskListItemKt;
import com.andb.apps.todo.util.UtilsKt;
import com.andb.apps.todo.utilities.Current;
import com.andb.apps.todo.utilities.OnceHolder;
import com.andb.apps.todo.utilities.ProjectsUtils;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.collect.Collections2;
import com.jaredrummler.android.colorpicker.ColorPanelView;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;
import com.pixplicity.easyprefs.library.Prefs;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;

import java.util.Collections;
import java.util.List;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;
import kotlin.Pair;
import kotlin.collections.CollectionsKt;

import static com.andb.apps.todo.ui.drawer.DrawerKt.DRAWER_DIALOG_ID;
import static com.andb.apps.todo.ui.inbox.InboxFragmentKt.SORT_ALPHA;
import static com.andb.apps.todo.ui.inbox.InboxFragmentKt.SORT_TIME;
import static com.andb.apps.todo.databases.GetDatabaseKt.tagsDao;
import static com.andb.apps.todo.databases.GetDatabaseKt.tasksDao;


public class MainActivity extends CyaneaAppCompatActivity implements ColorPickerDialogListener {

    /**
     * The {@link ViewPager} that will host the section contents.
     */

    public InboxFragment inboxFragment;
    public Drawer drawer;
    public Archive archive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        appStart();

        inboxFragment = new InboxFragment();
        archive = new Archive();

        loadSettings();

        setContentView(R.layout.activity_main);
        FragmentManager fm = getSupportFragmentManager();
        fm.executePendingTransactions();
        drawer = (Drawer) fm.findFragmentById(R.id.drawerFragment);
        drawer.setup();
        drawer.setupArchive(findViewById(R.id.expandable_page_archive));
        fm.beginTransaction().replace(R.id.fragmentHolder, inboxFragment).commit();
        setupProjectSelector();

        Filters.homeViewAdd(); //add current filter to back stack

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_keyboard_arrow_down_black_24dp);
        UtilsKt.getToolbarNavigationButton(toolbar).setRotation(180);
        setSupportActionBar(toolbar);
/*        Drawable navIcon = toolbar.getNavigationIcon().mutate();
        navIcon.setColorFilter(App.Companion.colorAlpha(Cyanea.getInstance().getPrimary(), .8f, .54f), PorterDuff.Mode.SRC_ATOP);
        toolbar.setNavigationIcon(navIcon);*/


        getWindow().setStatusBarColor(0x33333333);

        fabInitialize();


    }

    @Override
    protected void onResume() {
        super.onResume();


        String label = getResources().getString(R.string.app_name);
        Bitmap icon = BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher);
        int colorPrimary;


        colorPrimary = getResources().getColor(R.color.cyanea_primary_reference);


        ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(label, icon, colorPrimary);
        this.setTaskDescription(taskDescription);//set header color in recents


    }

    public void appStart() {

        GetDatabase.database = GetDatabase.getDatabase(getApplicationContext());
        Log.d("databaseInit", java.lang.Boolean.toString(GetDatabase.isInit()));

        OnceHolder.checkAppSetup(this);

        Current.initProjects(this);
        ProjectList.setKey(Prefs.getInt("project_viewing", 0));
        Current.initViewing(this);

        Current.initTags(this);
        Current.initTasks(this);

        AsyncTask.execute(() -> {//cleanse blank names from add & exit app
            List<Task> list = tasksDao().findTasksByName("");
            for (Task t : list) {
                tasksDao().deleteTask(t);
            }

            List<Tag> tagList = tagsDao().getAllStatic();
            Log.d("tagIndexCleanse", "starting cleanse with size of " + tagList.size());

            CollectionsKt.filter(tagList, tags -> tags.getProjectId() == Current.projectKey());
            Collections.sort(tagList, (o1, o2) -> Integer.compare(o1.getIndex(), o2.getIndex()));

            Log.d("tagIndexCleanse", "sorted + filtered cleanse with size of " + tagList.size());

            for (int i = 0; i < tagList.size(); i++) {
                Log.d("tagIndexCleanse", "setting index to " + i);
                tagList.get(i).setIndex(i);
                Log.d("tagIndexCleanse", "index is " + tagList.get(i).getIndex());
                ProjectsUtils.update(tagList.get(i));
            }
        });
    }

    public void loadSettings() {

        if (Prefs.getBoolean("sort_mode_list", true)) {
            SettingsActivity.Companion.setDefaultSort(SORT_TIME);
        } else {
            SettingsActivity.Companion.setDefaultSort(SORT_ALPHA);
        }

        SettingsActivity.Companion.setSubtaskDefaultShow(Prefs.getBoolean("expand_lists", true));

        SettingsActivity.Companion.setTimeToNotifyForDateOnly(new DateTime(Prefs.getLong("pref_notif_only_date", 0)));
    }


    @Override
    public void onBackPressed() {

        if (drawer.bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            drawer.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        } else if (TaskView.Companion.getAnyExpanded()) {
            inboxFragment.mRecyclerView.collapse();
        } else if (MaterialCab.Companion.isActive()) {
            MaterialCab.Companion.destroy();
        } else if (Filters.getCurrentFilter().size() > 0) {
            Filters.tagBack();
            inboxFragment.mAdapter.setHeaderPair(new Pair<>(inboxFragment.mAdapter.getHeaderPair().getFirst(), false));//remove editing on tag back
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

/*        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            try {
                Drawable drawable = menuItem.getIcon().mutate();
                drawable.setColorFilter(App.Companion.colorAlpha(getCyanea().getPrimary(), .8f, .54f), PorterDuff.Mode.SRC_ATOP);
                menuItem.setIcon(drawable);
            } catch (Exception e) {
                Log.d("menuIconColor", "not an icon (collapsed in overflow)");
            }

        }*/

        Cyanea.getInstance().tint(menu, this, true);


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            /*NAVIGATION ICON*/
            case android.R.id.home:
                Log.d("navClicked", "navigation icon clicked");
                if (!TaskView.Companion.getAnyExpanded()) {
                    if (drawer.getExpanded()) {
                        drawer.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    } else {
                        drawer.bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                    }
                } else {
                    inboxFragment.mRecyclerView.collapse();
                }
                break;
            /*MAINACTIVITY ITEMS*/
            case R.id.app_bar_filter:
                PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.app_bar_filter));
                popupMenu.setOnMenuItemClickListener(item1 -> {
                    int id1 = item1.getItemId();
                    Log.d("filterclicked", Integer.toString(id1));
                    switch (id1) {
                        case R.id.sortDate:
                            inboxFragment.setFilterMode(SORT_TIME);
                            break;
                        case R.id.sortAlpha:
                            inboxFragment.setFilterMode(SORT_ALPHA);
                            break;
                    }
                    inboxFragment.mAdapter.update(FilteredListsKt.filterInbox(Current.taskListAll(), inboxFragment.getFilterMode()));
                    return true;
                });

                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.filter_menu, popupMenu.getMenu());
                popupMenu.show();

                break;
            case R.id.action_test:
                Intent intent = new Intent(this, TestActivity.class);
                startActivity(intent);
                return true;

            /*TASKVIEW ITEMS*/
            case R.id.app_bar_edit:
                inboxFragment.mRecyclerView.collapse();
                int taskId = (int) inboxFragment.mRecyclerView.getExpandedItem().getItemId();
                Task task = Collections2.filter(Current.taskListAll(), t->t.getListKey()==taskId).iterator().next();
                task.setEditing(true);
                ProjectsUtils.update(task);
                return true;
            //TODO: Reschedule
        }


        return super.onOptionsItemSelected(item);
    }

    public void fabInitialize() {

        long startTime = System.nanoTime();

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setImageState(TaskListItemKt.getSTATE_ONE(), true);
        fab.setOnClickListener(view -> {
            if (!TaskView.Companion.getAnyExpanded()) {
                if (inboxFragment.isEditing()) {
                    Snackbar.make(fab.getRootView(), R.string.already_adding_task, Snackbar.LENGTH_SHORT).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show();
                    return;
                }
                Task task = TaskAdapter.newAddTask();
                inboxFragment.setEditingId(task.getListKey());
                inboxFragment.setAdding(true);

                AsyncTask.execute(() -> tasksDao().insertOnlySingleTask(task));
            } else {
                inboxFragment.mRecyclerView.collapse();
                int taskId = (int) inboxFragment.mRecyclerView.getExpandedItem().getItemId();
                Task task = Collections2.filter(Current.taskListAll(), t->t.getListKey()==taskId).iterator().next();
                task.setArchived(true);
                ProjectsUtils.update(task);
            }
        });


        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration/1000;//to seconds

        Log.d("startupTime", "Fab initialize: " + Long.toString(duration));
    }

    public void setupProjectSelector() {

        FrameLayout bottomSheet = findViewById(R.id.bottom_sheet_container);

        drawer.bottomSheetBehavior = (BottomSheetBehavior.from(bottomSheet));
        drawer.bottomSheetBehavior.setBottomSheetCallback(drawer.getNormalSheetCallback());

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

    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case DRAWER_DIALOG_ID: {
                ColorPanelView colorPanelView = drawer.addEditLayout.findViewById(R.id.projectColorPreview);
                if (colorPanelView != null) {
                    colorPanelView.setColor(color);
                }
                drawer.setSelectedColor(color);
            }
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }


}



