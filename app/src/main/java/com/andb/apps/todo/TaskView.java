package com.andb.apps.todo;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TaskView extends Fragment {

    int position;
    int inboxBrowseArchive; //true is inbox, false is archive
    int viewPos = 0;
    ArrayList<Tasks> taskList;

    private RecyclerView mRecyclerView;
    private static TaskViewAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private RecyclerView tRecyclerView;
    private static TaskViewTagAdapter tAdapter;
    private RecyclerView.LayoutManager tLayoutManager;

    public static Drawable oldNavIcon;
    public static int oldMargin;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();

        position = bundle.getInt("pos");
        Log.d("onePosUpError", Integer.toString(position));
        inboxBrowseArchive = bundle.getInt("inboxBrowseArchive");

        switch (inboxBrowseArchive) {
            case TaskAdapter.FROM_BROWSE:
                taskList = BrowseFragment.filteredTaskList;
                break;
            case TaskAdapter.FROM_ARCHIVE:
                taskList = ArchiveTaskList.taskList;
                break;
            default: //inbox
                taskList = InboxFragment.filteredTaskList;
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_view, (ViewGroup) container.getParent(), false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        collapseAndChangeAppBar((BottomAppBar) getActivity().findViewById(R.id.toolbar), (FloatingActionButton) getActivity().findViewById(R.id.fab), (TabLayout) getActivity().findViewById(R.id.tabs));


        Log.d("onePosUpError", taskList.get(position).getListName());


        TextView task_title = view.findViewById(R.id.task_view_task_name);
        task_title.setText(taskList.get(position).getListName().toUpperCase());

        prepareRecyclerView(view);


    }

    public void darkThemeSet(Toolbar toolbar) {
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorDarkPrimary));
        //getWindow().getDecorView().setSystemUiVisibility(0);


    }

    public void collapseAndChangeAppBar(Toolbar toolbar, FloatingActionButton fab, TabLayout tabLayout) {
        oldNavIcon = toolbar.getNavigationIcon().mutate();
        oldMargin = ((CoordinatorLayout.LayoutParams) toolbar.getLayoutParams()).bottomMargin;
        toolbar.setNavigationIcon(R.drawable.ic_clear_black_24dp);

        //TransitionManager.beginDelayedTransition(tabLayout, new ChangeBounds());
        CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) tabLayout.getLayoutParams();
        layoutParams.height = 1;
        tabLayout.setLayoutParams(layoutParams);

        //TransitionManager.beginDelayedTransition((ViewGroup) toolbar.getRootView(), new ChangeBounds());
        layoutParams = (CoordinatorLayout.LayoutParams) toolbar.getLayoutParams();
        layoutParams.bottomMargin = 0;
        toolbar.setLayoutParams(layoutParams);

        fab.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_all_black_24dp).mutate());


    }

    public void expandAndChangeAppBar(Toolbar toolbar, FloatingActionButton fab, TabLayout tabLayout) {

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


    @Override
    public void onPause() {
        super.onPause();
        InboxFragment.mAdapter.notifyDataSetChanged();
        BrowseFragment.mAdapter.notifyDataSetChanged();
    }


}
