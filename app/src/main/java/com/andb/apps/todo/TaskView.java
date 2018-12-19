package com.andb.apps.todo;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andb.apps.todo.filtering.FilteredLists;
import com.andb.apps.todo.lists.ArchiveTaskList;
import com.andb.apps.todo.objects.Tasks;
import com.andb.apps.todo.views.CyaneaTextView;
import com.andb.apps.todo.views.Icon;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.app.CyaneaFragment;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskView extends CyaneaFragment {

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
                taskList = FilteredLists.browseTaskList;
                break;
            case TaskAdapter.FROM_ARCHIVE:
                taskList = ArchiveTaskList.taskList;
                break;
            default: //inbox
                taskList = FilteredLists.inboxTaskList;
                break;
        }


    }

    @BindView(R.id.task_view_task_name) TextView task_title;
    @BindView(R.id.taskViewTimeText) TextView time_text;
    @BindView(R.id.taskViewTimeIcon) Icon time_icon;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_task_view, (ViewGroup) container.getParent(), false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        CoordinatorLayout bg = view.findViewById(R.id.task_view_parent);
        bg.setBackgroundColor(Utilities.lighterDarker(Cyanea.getInstance().getBackgroundColor(), 1.2f));
        collapseAndChangeAppBar(getActivity().findViewById(R.id.toolbar), getActivity().findViewById(R.id.fab), getActivity().findViewById(R.id.tabs));

        Tasks task = taskList.get(position);

        Log.d("onePosUpError", task.getListName());


        task_title.setText(task.getListName().toUpperCase());

        if(!task.isListTime()){ //no time
            time_text.setVisibility(View.GONE);
            time_icon.setVisibility(View.GONE);
        }else if (task.getDateTime().getSecondOfMinute() == 59){ //date only
            time_text.setText(task.getDateTime().toString("EEEE, MMMM d"));
            time_icon.setImageDrawable(getResources().getDrawable(R.drawable.ic_event_black_24dp));
        }else {
            time_text.setText(task.getDateTime().toString("hh:mm | EEEE, MMMM d"));
        }

        prepareRecyclerView(view, task);


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


    public void prepareRecyclerView(View view, Tasks task) {
        mRecyclerView = (RecyclerView) view.findViewById(R.id.taskViewRecycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new TaskViewAdapter(task.getAllListItems(), position, taskList);
        mRecyclerView.setAdapter(mAdapter);

        tRecyclerView = (RecyclerView) view.findViewById(R.id.taskViewTagRecycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        tRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        tLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        tRecyclerView.setLayoutManager(tLayoutManager);

        // specify an adapter (see also next example)
        tAdapter = new TaskViewTagAdapter(task.getAllListTags());
        tRecyclerView.setAdapter(tAdapter);
    }


    @Override
    public void onPause() {
        super.onPause();
        InboxFragment.mAdapter.notifyDataSetChanged();
        BrowseFragment.mAdapter.notifyDataSetChanged();
    }


}
