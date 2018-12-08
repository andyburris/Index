package com.andb.apps.todo;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.andb.apps.todo.lists.ArchiveTaskList;
import com.andb.apps.todo.settings.SettingsActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import me.saket.inboxrecyclerview.PullCollapsibleActivity;

public class Archive extends PullCollapsibleActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if (SettingsActivity.darkTheme) {
        //    this.setTheme(R.style.AppThemeDark);
        //} else {
            this.setTheme(R.style.AppThemeLightCollapse);
        //}
        setContentView(R.layout.activity_archive);
        expandFromTop();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_clear_black_24dp);
        setSupportActionBar(toolbar);

        if (SettingsActivity.darkTheme) {
            darkThemeSet(toolbar);
        }

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


        Fragment archiveFragment = new ArchiveFragment();
        fragmentTransaction.add(R.id.archiveFragmentContainer, archiveFragment);
        fragmentTransaction.commit();
    }

    public void darkThemeSet(Toolbar toolbar) {
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorDarkPrimary));
        getWindow().getDecorView().setSystemUiVisibility(0);


    }

    public void onPause(){
        super.onPause();
        ArchiveTaskList.saveTasks(this);
        Log.d("save", "saving TaskList");
    }



}
