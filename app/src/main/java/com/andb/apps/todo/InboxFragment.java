package com.andb.apps.todo;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
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

import com.andb.apps.todo.filtering.FilteredLists;
import com.andb.apps.todo.filtering.Filters;
import com.andb.apps.todo.lists.ArchiveTaskList;
import com.andb.apps.todo.lists.TaskList;
import com.andb.apps.todo.lists.interfaces.TaskListInterface;
import com.andb.apps.todo.notifications.NotificationHandler;
import com.andb.apps.todo.objects.Tasks;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.app.CyaneaFragment;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkManager;
import me.saket.inboxrecyclerview.InboxRecyclerView;
import me.saket.inboxrecyclerview.page.ExpandablePageLayout;
import me.saket.inboxrecyclerview.page.SimplePageStateChangeCallbacks;

import static com.andb.apps.todo.notifications.NotifyWorker.workTag;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InboxFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InboxFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class InboxFragment extends CyaneaFragment {


    public static int filterMode = 0; //0=date, 1=alphabetical, more to come



    public static InboxRecyclerView mRecyclerView;
    public static TaskAdapter mAdapter;

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
        Drawable bgdrawable = getResources().getDrawable(R.drawable.rounded_button_background).mutate();
        bgdrawable.setColorFilter(Cyanea.getInstance().getAccent(), PorterDuff.Mode.SRC_ATOP);
        tagButton.setBackground(bgdrawable);
        tagButton.setBackgroundTintList(ColorStateList.valueOf(Cyanea.getInstance().getAccent()));


        Drawable drawable = getResources().getDrawable(R.drawable.ic_label_black_24dp).mutate();
        if (Utilities.lightOnBackground(Cyanea.getInstance().getAccent())) {
            int color = 0xFFFFFFFF;
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            tagButton.setTextColor(color);
        } else {
            int color = 0xFF000000;
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            tagButton.setTextColor(color);
        }
        tagButton.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);


        mRecyclerView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
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
                intent.putExtra("isTagLink", false);
                startActivity(intent);


            }
        });

        return view;


    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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


    public void prepareRecyclerView(final View view) {


        mRecyclerView = (InboxRecyclerView) view.findViewById(R.id.inboxRecycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)

        ///FilteredLists.inboxTaskList = TaskList.taskList;

        mAdapter = new TaskAdapter(FilteredLists.inboxTaskList, TaskAdapter.FROM_INBOX);

        mAdapter.setHasStableIds(true);

        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setNested(true);

        final ExpandablePageLayout taskView = view.findViewById(R.id.expandable_page_inbox);
        mRecyclerView.setExpandablePage(taskView);

        taskView.addStateChangeCallbacks(new TaskView.PageCollapseCallback(getActivity()));

        ItemTouchHelper ith = new ItemTouchHelper(_ithCallback);
        ith.attachToRecyclerView(mRecyclerView);







    }





    public static void setFilterMode(int mode) {

        filterMode = mode;

        ArrayList<Tasks> tempList = new ArrayList<>(FilteredLists.inboxTaskList);

        Log.d("inboxFilterInbox", Integer.toString(FilteredLists.inboxTaskList.size()));

        for (int i = 0; i < tempList.size(); i++) {
            Tasks task = tempList.get(i);
            if (task.getListName().equals("OVERDUE") | task.getListName().equals("TODAY") | task.getListName().equals("WEEK") | task.getListName().equals("MONTH") | task.getListName().equals("FUTURE")) {
                Log.d("removing", "removing " + task.getListName());
                FilteredLists.inboxTaskList.remove(i);
                tempList.remove(i);
                i--;
            }
        }

        tempList = new ArrayList<>(FilteredLists.inboxTaskList);

        Log.d("inboxFilterInbox", Integer.toString(FilteredLists.inboxTaskList.size()));


        if (mode == 0) {


            boolean overdue = true;
            boolean today = true;
            boolean thisWeek = true;
            boolean thisMonth = true;
            boolean future = true;


            Log.d("loopStart", "Size: " + Integer.toString(FilteredLists.inboxTaskList.size()));

            int i = 0;

            for (Tasks task : tempList) {


                Log.d("loopStart", "loop through " + Integer.toString(i));
                DateTime taskDateTime = new DateTime(task.getDateTime());
                if (taskDateTime.isBefore(DateTime.now())) {
                    if (overdue) {
                        Log.d("addDivider", "adding OVERDUE from " + task.getListName() + ", " + task.getDateTime().toString());
                        Tasks tasks = new Tasks("OVERDUE", new ArrayList(), new ArrayList(), new ArrayList(), new DateTime(1970, 1, 1, 0, 0), false);

                        FilteredLists.inboxTaskList.add(i, tasks);


                        overdue = false;
                    }
                } else if (taskDateTime.isBefore(DateTime.now().withTime(23, 59, 59, 999))) {
                    if (today) {
                        Log.d("addDivider", "adding TODAY from " + task.getListName() + ", " + task.getDateTime().toString());
                        Log.d("addDivider", task.getListName());
                        Tasks tasks = new Tasks("TODAY", new ArrayList(), new ArrayList(), new ArrayList(), new DateTime(DateTime.now()), false);//drop one category to show at top

                        FilteredLists.inboxTaskList.add(i, tasks);


                        today = false;
                    }
                } else if (taskDateTime.isBefore(DateTime.now().plusWeeks(1).minusDays(1).withTime(23, 59, 59, 999))) {
                    if (thisWeek) {
                        Log.d("addDivider", "adding WEEK from " + task.getListName() + ", " + task.getDateTime().toString() + " at position " + Integer.toString(i));
                        Tasks tasks = new Tasks("WEEK", new ArrayList(), new ArrayList(), new ArrayList(), new DateTime(DateTime.now().withTime(23, 59, 59, 999)), false);

                        FilteredLists.inboxTaskList.add(i, tasks);


                        thisWeek = false;
                    }
                } else if (taskDateTime.isBefore(DateTime.now().plusMonths(1).minusDays(1).withTime(23, 59, 59, 999))) {
                    if (thisMonth) {
                        Log.d("addDivider", "adding MONTH from " + task.getListName() + ", " + task.getDateTime().toString());
                        Tasks tasks = new Tasks("MONTH", new ArrayList(), new ArrayList(), new ArrayList(), new DateTime(DateTime.now().plusWeeks(1).minusDays(1).withTime(23, 59, 59, 999)), false);

                        FilteredLists.inboxTaskList.add(i, tasks);

                        thisMonth = false;
                    }

                } else if (taskDateTime.isAfter(DateTime.now().plusMonths(1).minusDays(1).withTime(23, 59, 59, 999))) {
                    if (future) {
                        Log.d("addDivider", "adding FUTURE from " + task.getListName() + ", " + task.getDateTime().toString());
                        Tasks tasks = new Tasks("FUTURE", new ArrayList(), new ArrayList(), new ArrayList(), new DateTime(DateTime.now().plusMonths(1).minusDays(1).withTime(23, 59, 59, 999)), false);

                        FilteredLists.inboxTaskList.add(i, tasks);


                        future = false;
                    }

                }

                i++;
            }

            Log.d("inboxFilterInbox", Integer.toString(FilteredLists.inboxTaskList.size()));


            Collections.sort(FilteredLists.inboxTaskList, new Comparator<Tasks>() {
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



        } else if (mode == 1) {


            Collections.sort(FilteredLists.inboxTaskList, new Comparator<Tasks>() {
                @Override
                public int compare(Tasks o1, Tasks o2) {
                    return o1.getListName().compareTo(o2.getListName());
                }
            });



        }



    }

    public static void refreshWithAnim() {
        mAdapter.notifyDataSetChanged();
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
    private ItemTouchHelper.Callback _ithCallback = new ItemTouchHelper.Callback() {
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

            //background.setCornerRadius(0);


            background.draw(c);

            // Calculate position of delete icon
            int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
            int deleteIconLeft = itemView.getLeft() + intrinsicWidth;
            int deleteIconRight = itemView.getLeft() + intrinsicWidth*2;
            int deleteIconBottom = deleteIconTop + intrinsicHeight;
            // Draw the delete icon
            deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
            deleteIcon.draw(c);

            float newDx = (dX*9)/10;
            if (newDx >= 300f) {
                newDx = 300f;
            }

            super.onChildDraw(c, recyclerView, viewHolder, newDx, dY, actionState, isCurrentlyActive);
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
        Log.d("removing", "Above: " + FilteredLists.inboxTaskList.get(dividerPosition).getListName() + "\n");
        Log.d("removing", "Clicked: " + FilteredLists.inboxTaskList.get(position).getListName() + "\n");
        //Log.d("removing", "Below: " + FilteredLists.inboxTaskList.get(belowDividerPosition).getListName() + "\n");

        final Tasks tasks = FilteredLists.inboxTaskList.get(position);
        Tasks dividerTask = FilteredLists.inboxTaskList.get(dividerPosition);


        if (dividerTask.getListName().equals("OVERDUE")
                | dividerTask.getListName().equals("TODAY")
                | dividerTask.getListName().equals("WEEK")
                | dividerTask.getListName().equals("MONTH")
                | dividerTask.getListName().equals("FUTURE")) {

            if (belowDividerPosition < FilteredLists.inboxTaskList.size()) {
                Log.d("size", Integer.toString(FilteredLists.inboxTaskList.size()) + ", " + belowDividerPosition);
                Tasks belowDividerTask = FilteredLists.inboxTaskList.get(belowDividerPosition);
                if (belowDividerTask.getListName().equals("OVERDUE")
                        | belowDividerTask.getListName().equals("TODAY")
                        | belowDividerTask.getListName().equals("WEEK")
                        | belowDividerTask.getListName().equals("MONTH")
                        | belowDividerTask.getListName().equals("FUTURE")) {

                    removeTask(position, true); //sandwiched by dividers, remove top one
                    if (dividerPosition == 0) {
                        mAdapter.notifyItemChanged(0); //updates top divider if necessary to redo padding
                    }

                } else { //more tasks below this
                    removeTask(position, false);
                }
            } else { //last in list w/ divider above this
                removeTask(position, true);
            }


        } else {
            removeTask(position, false);
        }

        if (FilteredLists.browseTaskList.contains(tasks)) {
            FilteredLists.browseTaskList.remove(tasks);
        }

        BrowseFragment.mAdapter.notifyDataSetChanged();

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.

        Log.d("removeTaskTime", Long.toString(duration) + " milliseconds");


    }

    private void removeTask(int position, boolean above) {

        Log.d("removeTask", "removing task");


        final Tasks tasks = FilteredLists.inboxTaskList.get(position);

        TaskListInterface.removeTask(tasks);

        FilteredLists.inboxTaskList.remove(position);

        mAdapter.notifyItemRemoved(position);

        int dividerPosition = position - 1;

        if (above) {


            FilteredLists.inboxTaskList.remove(dividerPosition);

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
