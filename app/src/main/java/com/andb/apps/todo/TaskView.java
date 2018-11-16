package com.andb.apps.todo;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TaskView extends AppCompatActivity {

    int position;
    boolean inboxOrArchive; //true is inbox, false is archive
    ArrayList<Tasks> taskList;

    private RecyclerView mRecyclerView;
    private static TaskViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private RecyclerView tRecyclerView;
    private static TaskViewTagAdapter tAdapter;
    private RecyclerView.LayoutManager tLayoutManager;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*        if (SettingsActivity.darkTheme) {
            this.setTheme(R.style.AppThemeDark);
        } else {
            this.setTheme(R.style.AppThemeLight);
        }*/
        setContentView(R.layout.activity_task_view);




        Bundle bundle=getIntent().getExtras();
        position=bundle.getInt("pos");
        Log.d("onePosUpError", Integer.toString(position));
        inboxOrArchive = bundle.getBoolean("inboxOrArchive");
        if(inboxOrArchive){
            boolean browse = bundle.getBoolean("browse");

            if(browse){
                taskList = BrowseFragment.filteredTaskList;
            }else {
                taskList = InboxFragment.filteredTaskList;
            }
        }else {
            taskList = ArchiveTaskList.taskList;
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(taskList.get(position).getListName());
        Log.d("onePosUpError", taskList.get(position).getListName());

/*        if (SettingsActivity.darkTheme) {
            darkThemeSet(toolbar);
        }*/

        prepareRecyclerView();


    }

    public void darkThemeSet(Toolbar toolbar) {
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorDarkPrimary));
        getWindow().getDecorView().setSystemUiVisibility(0);


    }


    public void prepareRecyclerView(){
        mRecyclerView = (RecyclerView) findViewById(R.id.taskViewRecycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new TaskViewAdapter(taskList.get(position).getAllListItems(), position, taskList);
        mRecyclerView.setAdapter(mAdapter);

        tRecyclerView = (RecyclerView) findViewById(R.id.taskViewTagRecycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        tRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        tLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        tRecyclerView.setLayoutManager(tLayoutManager);

        // specify an adapter (see also next example)
        tAdapter = new TaskViewTagAdapter(taskList.get(position).getAllListTags());
        tRecyclerView.setAdapter(tAdapter);

    }

    @Override
    public void onPause(){
        super.onPause();
        InboxFragment.mAdapter.notifyDataSetChanged();
        BrowseFragment.mAdapter.notifyDataSetChanged();
    }

}
