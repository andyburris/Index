package com.andb.apps.todo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkManager;
import me.saket.inboxrecyclerview.InboxRecyclerView;
import me.saket.inboxrecyclerview.page.ExpandablePageLayout;

import static com.andb.apps.todo.NotifyWorker.workTag;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InboxFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InboxFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class InboxFragment extends Fragment {

    public static ArrayList<Tasks> blankTaskList = new ArrayList<>();
    public static int filterMode = 0; //0=date, 1=alphabetical, more to come

    public static ArrayList<Tasks> filteredTaskList = new ArrayList<>();

    public static InboxRecyclerView mRecyclerView;
    public static InboxAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ActionMode contextualToolbar;
    public boolean selected = false;

    private static TextView taskCountText;
    private static TextView currentPathText;

    private static TextView noTasks;

    public static Button tagButton;

    private OnFragmentInteractionListener mListener;


    public InboxFragment() {
        // Required empty public constructor
    }

    public static InboxFragment newInstance() {
        InboxFragment fragment = new InboxFragment();

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        Log.d("inflating", "inbox inflating");
        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        prepareRecyclerView(view);

        noTasks = view.findViewById(R.id.noTasks);

        taskCountText = view.findViewById(R.id.task_count_text);
        currentPathText = view.findViewById(R.id.task_path_text);

        setPathText(Filters.subtitle);
        setTaskCountText(TaskList.taskList.size());

        tagButton = view.findViewById(R.id.tag_button);
        tagButton.getBackground().setColorFilter(SettingsActivity.themeColor, PorterDuff.Mode.SRC_ATOP);

        Drawable drawable = getResources().getDrawable(R.drawable.ic_label_black_24dp).mutate();
        if (MainActivity.lightOnBackground(SettingsActivity.themeColor)) {
            int color = 0xFFFFFFFF;
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            tagButton.setTextColor(color);
        } else {
            int color = 0xFF000000;
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            tagButton.setTextColor(color);
        }
        tagButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);


        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {

            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {
                if (mAdapter.getItemViewType(position) == 0 & !selected) {
                    contextualToolbar = InboxFragment.this.getActivity().startActionMode(setCallback(position));
                    view.setSelected(true);
                    mAdapter.isSelected = true;
                    mAdapter.notifyItemChanged(position);
                    selected = true;
                }
            }
        }) {
        });



        tagButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), TagSelect.class);
                intent.putExtra("isTagLink", true);
                startActivity(intent);


            }
        });

        return view;


    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    public void prepareRecyclerView(View view) {


        mRecyclerView = (InboxRecyclerView) view.findViewById(R.id.inboxRecycler);
        //ExpandablePageLayout taskView = view.findViewById(R.id.task_view_expandable_layout);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)

        ///filteredTaskList = TaskList.taskList;

        mAdapter = new InboxAdapter(filteredTaskList);
        Log.d("inboxFilterRefresh", Integer.toString(filteredTaskList.size()));
        Log.d("inboxFilterRefresh", Integer.toString(mAdapter.getItemCount()));
        mAdapter.setHasStableIds(true);

        mRecyclerView.setAdapter(mAdapter);

        ExpandablePageLayout taskView = view.findViewById(R.id.expandable_page);
        mRecyclerView.setExpandablePage(taskView);

        ItemTouchHelper ith = new ItemTouchHelper(_ithCallback);
        ith.attachToRecyclerView(mRecyclerView);







    }


    public static void addTask(final String title, final ArrayList<String> items, final ArrayList<Boolean> checked, final ArrayList<Integer> tags, final DateTime time) {


        //TaskList.keyList.add(key);


        //TaskList.addTaskList(tasks);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int key = new Random().nextInt();

                while (TaskList.keyList.contains(key) || key == 0) {
                    key = new Random().nextInt();
                }

                Log.d("putKeys", Integer.toString(key));


                Tasks tasks = new Tasks(title, items, checked, tags, time, false, key);

                MainActivity.tasksDatabase.tasksDao().insertOnlySingleTask(tasks);

                TaskList.taskList = new ArrayList<>(MainActivity.tasksDatabase.tasksDao().getAll());


                EventBus.getDefault().post(new UpdateEvent(true));

            }


        });


    }


    public static void replaceTask(final String title, final ArrayList<String> items, final ArrayList<Boolean> checked, final ArrayList<Integer> tags,
                                   final DateTime time, final boolean notified, final int position, final int key, Context ctxt) {


        //TaskList.setTaskList(position, tasks);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int key = new Random().nextInt();

                while (TaskList.keyList.contains(key) || key == 0) {
                    key = new Random().nextInt();
                }

                Log.d("putKeys", Integer.toString(key));


                Tasks tasks = new Tasks(title, items, checked, tags, time, false, key);

                MainActivity.tasksDatabase.tasksDao().insertOnlySingleTask(tasks);

                TaskList.taskList = new ArrayList<>(MainActivity.tasksDatabase.tasksDao().getAll());

                EventBus.getDefault().post(new UpdateEvent(true));
            }
        });


        Log.d("recyclerCreated", "outer created");
        BrowseFragment.createFilteredTaskList(Filters.getCurrentFilter(), true);
        InboxFragment.setFilterMode(InboxFragment.filterMode);
        mAdapter.isSelected = false;
        mAdapter.notifyDataSetChanged();
        WorkManager.getInstance().cancelAllWorkByTag(workTag);
        NotificationHandler.resetNotifications(ctxt);
    }


    public static void setFilterMode(int mode) {

        filterMode = mode;

        ArrayList<Tasks> tempList = new ArrayList<>(filteredTaskList);

        Log.d("inboxFilterInbox", Integer.toString(filteredTaskList.size()));

        for (int i = 0; i < tempList.size(); i++) {
            Tasks task = tempList.get(i);
            if (task.getListName().equals("OVERDUE") | task.getListName().equals("TODAY") | task.getListName().equals("WEEK") | task.getListName().equals("MONTH") | task.getListName().equals("FUTURE")) {
                Log.d("removing", "removing " + task.getListName());
                filteredTaskList.remove(i);
                tempList.remove(i);
                i--;
            }
        }

        tempList = new ArrayList<>(filteredTaskList);

        Log.d("inboxFilterInbox", Integer.toString(filteredTaskList.size()));


        if (mode == 0) {


            boolean overdue = true;
            boolean today = true;
            boolean thisWeek = true;
            boolean thisMonth = true;
            boolean future = true;


            Log.d("loopStart", "Size: " + Integer.toString(filteredTaskList.size()));

            int i = 0;

            for (Tasks task : tempList) {


                Log.d("loopStart", "loop through " + Integer.toString(i));
                DateTime taskDateTime = new DateTime(task.getDateTime());
                if (taskDateTime.isBefore(DateTime.now())) {
                    if (overdue) {
                        Log.d("addDivider", "adding OVERDUE from " + task.getListName() + ", " + task.getDateTime().toString());
                        Tasks tasks = new Tasks("OVERDUE", new ArrayList(), new ArrayList(), new ArrayList(), new DateTime(1970, 1, 1, 0, 0), false);

                        filteredTaskList.add(i, tasks);


                        overdue = false;
                    }
                } else if (taskDateTime.isBefore(DateTime.now().withTime(23, 59, 59, 999))) {
                    if (today) {
                        Log.d("addDivider", "adding TODAY from " + task.getListName() + ", " + task.getDateTime().toString());
                        Log.d("addDivider", task.getListName());
                        Tasks tasks = new Tasks("TODAY", new ArrayList(), new ArrayList(), new ArrayList(), new DateTime(DateTime.now()), false);//drop one category to show at top

                        filteredTaskList.add(i, tasks);


                        today = false;
                    }
                } else if (taskDateTime.isBefore(DateTime.now().plusWeeks(1).minusDays(1).withTime(23, 59, 59, 999))) {
                    if (thisWeek) {
                        Log.d("addDivider", "adding WEEK from " + task.getListName() + ", " + task.getDateTime().toString() + " at position " + Integer.toString(i));
                        Tasks tasks = new Tasks("WEEK", new ArrayList(), new ArrayList(), new ArrayList(), new DateTime(DateTime.now().plusDays(1)), false);

                        filteredTaskList.add(i, tasks);


                        thisWeek = false;
                    }
                } else if (taskDateTime.isBefore(DateTime.now().plusMonths(1).minusDays(1).withTime(23, 59, 59, 999))) {
                    if (thisMonth) {
                        Log.d("addDivider", "adding MONTH from " + task.getListName() + ", " + task.getDateTime().toString());
                        Tasks tasks = new Tasks("MONTH", new ArrayList(), new ArrayList(), new ArrayList(), new DateTime(DateTime.now().plusWeeks(1)), false);

                        filteredTaskList.add(i, tasks);

                        thisMonth = false;
                    }

                } else if (taskDateTime.isAfter(DateTime.now().plusMonths(1).minusDays(1).withTime(23, 59, 59, 999))) {
                    if (future) {
                        Log.d("addDivider", "adding FUTURE from " + task.getListName() + ", " + task.getDateTime().toString());
                        Tasks tasks = new Tasks("FUTURE", new ArrayList(), new ArrayList(), new ArrayList(), new DateTime(DateTime.now().plusMonths(1)), false);

                        filteredTaskList.add(i, tasks);


                        future = false;
                    }

                }

                i++;
            }

            Log.d("inboxFilterInbox", Integer.toString(filteredTaskList.size()));


            Collections.sort(filteredTaskList, new Comparator<Tasks>() {
                @Override
                public int compare(Tasks o1, Tasks o2) {
                    if (o1.getDateTime() == null) {
                        o1.setDateTime(new DateTime(1970, 1, 1, 0, 0, 0));
                    }
                    if (o2.getDateTime() == null) {
                        o2.setDateTime(new DateTime(1970, 1, 1, 0, 0, 0));
                    }

                    return o1.getDateTime().compareTo(o2.getDateTime());
                }
            });

            Log.d("inboxFilterInbox", Integer.toString(filteredTaskList.size()));


        } else if (mode == 1) {


            Collections.sort(filteredTaskList, new Comparator<Tasks>() {
                @Override
                public int compare(Tasks o1, Tasks o2) {
                    return o1.getListName().compareTo(o2.getListName());
                }
            });

            Log.d("inboxFilterInbox", Integer.toString(filteredTaskList.size()));


        }

        //mAdapter.notifyDataSetChanged();

        if (filteredTaskList.isEmpty()) {
            noTasks.setVisibility(View.VISIBLE);
        } else {
            noTasks.setVisibility(View.GONE);

        }
        Log.d("inboxFilterInboxEnd", Integer.toString(filteredTaskList.size()));


    }

    public static void refreshWithAnim() {
        mAdapter.notifyDataSetChanged();
        Log.d("inboxFilterRefresh", Integer.toString(filteredTaskList.size()));
        Log.d("inboxFilterRefresh", Integer.toString(mAdapter.getItemCount()));
        Log.d("inboxFilterRefresh", Integer.toString(filteredTaskList.size()));
        mRecyclerView.scheduleLayoutAnimation();
    }


    public ActionMode.Callback setCallback(final int position) {
        ActionMode.Callback callback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = new MenuInflater(InboxFragment.this.getContext());
                menuInflater.inflate(R.menu.toolbar_inbox_long_press, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.editTask:
                        Intent editTask = new Intent(InboxFragment.this.getContext(), AddTask.class);
                        editTask.putExtra("edit", true);
                        editTask.putExtra("editPos", position);
                        editTask.putExtra("browse", false);
                        startActivity(editTask);
                        mode.finish();
                        return true;

                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selected = false;
                mAdapter.isSelected = false;
                mAdapter.notifyItemChanged(position);
            }
        };

        return callback;
    }

    // Extend the Callback class
    ItemTouchHelper.Callback _ithCallback = new ItemTouchHelper.Callback() {
        //and in your implementation of
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            Log.d("swipeAction", "swiped");

            TransitionManager.beginDelayedTransition(mRecyclerView, new ChangeBounds());
            ViewGroup.LayoutParams layoutParams = mRecyclerView.getChildAt(viewHolder.getAdapterPosition()).getLayoutParams();
            layoutParams.height = 0;
            mRecyclerView.getChildAt(viewHolder.getAdapterPosition()).setLayoutParams(layoutParams);
            if (InboxFragment.filterMode == 0) {
                Log.d("removing", "removing date sorted " + Integer.toString(viewHolder.getAdapterPosition()));
                removeWithDivider(viewHolder.getAdapterPosition());
            } else {
                Log.d("removing", "removing alphabetical " + Integer.toString(viewHolder.getAdapterPosition()));
                removeTask(viewHolder.getAdapterPosition(), false);
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

            Drawable deleteIcon = ContextCompat.getDrawable(getContext(), R.drawable.ic_done_all_black_24dp).mutate();
            deleteIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            int intrinsicWidth = deleteIcon.getIntrinsicWidth();
            int intrinsicHeight = deleteIcon.getIntrinsicHeight();
            GradientDrawable background = new GradientDrawable();
            int backgroundColor = Color.parseColor("#1B7D1B");


            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            View itemView = viewHolder.itemView;
            int itemHeight = itemView.getBottom() - itemView.getTop();

            // Draw the green delete background
            background.setColor(backgroundColor);
            background.setBounds(
                    itemView.getLeft(),
                    itemView.getTop(),
                    itemView.getRight(),
                    itemView.getBottom()
            );

            background.setCornerRadius(8);


            background.draw(c);

            // Calculate position of delete icon
            int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
            int deleteIconMargin = (itemHeight - intrinsicHeight) / 2;
            int deleteIconLeft = itemView.getLeft() + deleteIconMargin / 2;
            int deleteIconRight = itemView.getLeft() + deleteIconMargin / 2 + intrinsicWidth;
            int deleteIconBottom = deleteIconTop + intrinsicHeight;
            // Draw the delete icon
            deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
            deleteIcon.draw(c);

            super.onChildDraw(c, recyclerView, viewHolder, dX / 2, dY, actionState, isCurrentlyActive);
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        //defines the enabled move directions in each state (idle, swiping, dragging).
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            if (viewHolder.getItemViewType() == 0) {
                return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE,
                        ItemTouchHelper.RIGHT);
            } else {
                return 0;
            }
        }
    };

    private void removeWithDivider(int position) {

        long startTime = System.nanoTime();


        int dividerPosition = position - 1;
        int belowDividerPosition = position + 1;
        Log.d("removing", "Above: " + filteredTaskList.get(dividerPosition).getListName() + "\n");
        Log.d("removing", "Clicked: " + filteredTaskList.get(position).getListName() + "\n");
        //Log.d("removing", "Below: " + filteredTaskList.get(belowDividerPosition).getListName() + "\n");

        final Tasks tasks = filteredTaskList.get(position);
        Tasks dividerTask = filteredTaskList.get(dividerPosition);


        if (dividerTask.getListName().equals("OVERDUE")
                | dividerTask.getListName().equals("TODAY")
                | dividerTask.getListName().equals("WEEK")
                | dividerTask.getListName().equals("MONTH")
                | dividerTask.getListName().equals("FUTURE")) {

            if (belowDividerPosition < filteredTaskList.size()) {
                Log.d("size", Integer.toString(filteredTaskList.size()) + ", " + belowDividerPosition);
                Tasks belowDividerTask = filteredTaskList.get(belowDividerPosition);
                if (belowDividerTask.getListName().equals("OVERDUE")
                        | belowDividerTask.getListName().equals("TODAY")
                        | belowDividerTask.getListName().equals("WEEK")
                        | belowDividerTask.getListName().equals("MONTH")
                        | belowDividerTask.getListName().equals("FUTURE")) {

                    removeTask(position, true);
                    if (dividerPosition == 0) {
                        mAdapter.notifyItemChanged(0); //updates top divider if necessary to redo padding
                    }

                } else {
                    removeTask(position, false);
                }
            } else {
                removeTask(position, true);
            }


        } else {
            removeTask(position, false);

        }

        if (BrowseFragment.filteredTaskList.contains(tasks)) {
            BrowseFragment.filteredTaskList.remove(tasks);
        }

        BrowseFragment.mAdapter.notifyDataSetChanged();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.

        Log.d("removeTaskTime", Long.toString(duration) + " milliseconds");


    }

    private void removeTask(int position, boolean above) {

        Log.d("removeTask", "removing task");


        final Tasks tasks = filteredTaskList.get(position);

        ArchiveTaskList.addTaskList(filteredTaskList.get(position));
        TaskList.keyList.remove((Integer) filteredTaskList.get(position).getListKey());
        TaskList.taskList.remove(filteredTaskList.get(position));
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                MainActivity.tasksDatabase.tasksDao().deleteTask(tasks);
            }
        });
        filteredTaskList.remove(position);

        mAdapter.notifyItemRemoved(position);


        int dividerPosition = position - 1;

        if (above) {


            filteredTaskList.remove(dividerPosition);

            mAdapter.notifyItemRemoved(dividerPosition);
        }

        mAdapter.notifyItemRangeChanged(dividerPosition, mAdapter.getItemCount());


    }

    public static void setTaskCountText(int numTasks) {
        String toApply;
        if (numTasks != 1) {
            toApply = " TASKS";
        } else {
            toApply = " TASK";
        }
        toApply = Integer.toString(numTasks) + toApply;
        taskCountText.setText(toApply);

    }

    public static void setPathText(String text) {
        currentPathText.setText(text);
    }

}
