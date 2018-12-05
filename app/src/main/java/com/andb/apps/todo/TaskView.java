package com.andb.apps.todo;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andb.apps.todo.settings.SettingsActivity;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.saket.inboxrecyclerview.page.ExpandablePageLayout;

public class TaskView extends Fragment {

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);

    /*if (SettingsActivity.darkTheme) {
            this.setTheme(R.style.AppThemeDark);
        } else {*/
        //this.setTheme(R.style.AppThemeLightCollapse);
        //}

        return inflater.inflate(R.layout.activity_task_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        Bundle bundle = getArguments();
        /*ArrayList<Integer> rectList = new ArrayList<>(bundle.getIntegerArrayList("rect"));

        Rect expand_from;
        expand_from = new Rect(rectList.get(0), rectList.get(1), rectList.get(0)+rectList.get(2), rectList.get(1)+rectList.get(3));
        expandFrom(expand_from);*/

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
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("");
        toolbar.setNavigationIcon(R.drawable.ic_clear_black_24dp);
        Log.d("onePosUpError", taskList.get(position).getListName());

        if (SettingsActivity.darkTheme) {
            darkThemeSet(toolbar);
        }

        TextView task_title = view.findViewById(R.id.task_view_task_name);
        task_title.setText(taskList.get(position).getListName().toUpperCase());

        prepareRecyclerView(view);


    }

    public void darkThemeSet(Toolbar toolbar) {
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorDarkPrimary));
        //getWindow().getDecorView().setSystemUiVisibility(0);


    }


    public void prepareRecyclerView(View view) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.taskViewRecycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new TaskViewAdapter(taskList.get(position).getAllListItems(), position, taskList);
        mRecyclerView.setAdapter(mAdapter);

        tRecyclerView = (RecyclerView) view.findViewById(R.id.taskViewTagRecycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        tRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        tLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        tRecyclerView.setLayoutManager(tLayoutManager);

        // specify an adapter (see also next example)
        tAdapter = new TaskViewTagAdapter(taskList.get(position).getAllListTags());
        tRecyclerView.setAdapter(tAdapter);

    }

    public static ExpandablePageLayout returnViewForExpandable(View view) {
        return view.findViewById(R.id.task_view_parent);
    }

    @Override
    public void onPause(){
        super.onPause();
        InboxFragment.mAdapter.notifyDataSetChanged();
        BrowseFragment.mAdapter.notifyDataSetChanged();
    }


}
