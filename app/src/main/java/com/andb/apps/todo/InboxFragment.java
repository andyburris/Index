package com.andb.apps.todo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import androidx.work.WorkManager;

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

    private static RecyclerView mRecyclerView;
    public static InboxAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ActionMode contextualToolbar;
    public boolean selected = false;

    private static TextView noTasks;

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

        //hide fab on scroll
        final FloatingActionButton fabMain = (FloatingActionButton) getActivity().findViewById(R.id.fab_main);
        final FloatingActionButton fabList = (FloatingActionButton) getActivity().findViewById(R.id.fab_list);
        final FloatingActionButton fabTag = (FloatingActionButton) getActivity().findViewById(R.id.fab_tag);
        final int scrollSensitivity = 5;
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > scrollSensitivity) {
                    fabMain.hide();
                    fabList.hide();
                    fabTag.hide();

                } else if (dy < scrollSensitivity) {
                    fabMain.show();
                    if (MainActivity.fabOpen) { //checks if extra fabs are open, if yes then return them to view
                        fabList.show();
                        fabTag.show();
                    }
                }
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    public void prepareRecyclerView(View view) {


        mRecyclerView = (RecyclerView) view.findViewById(R.id.inboxRecycler);

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

        mRecyclerView.setAdapter(mAdapter);


        String taskName;

        if (MainActivity.notifKey != 0) {

            int index = TaskList.keyList.indexOf(MainActivity.notifKey);
            int finalPos = -1;
            for (int i = 0; i < filteredTaskList.size(); i++) {
                if (filteredTaskList.get(i).getListKey() == MainActivity.notifKey) {
                    finalPos = i;
                    break;
                }


            }

            Log.d("scrollBug", Integer.toString(finalPos) + ", Size: " + Integer.toString(filteredTaskList.size()));
            if (finalPos != -1) {
                taskName = filteredTaskList.get(finalPos).getListName();


                if (taskName.equals("OVERDUE") | taskName.equals("TODAY") | taskName.equals("WEEK") | taskName.equals("MONTH") | taskName.equals("FUTURE"))
                    mRecyclerView.scrollToPosition(finalPos - 1);
                else
                    mRecyclerView.scrollToPosition(finalPos);
            }
        }


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


    public static void replaceTask(final String title, final ArrayList<String> items, final ArrayList<Boolean> checked, final ArrayList<Integer> tags, final DateTime time, final boolean notified, final int position, final int key) {


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
        MainActivity.restartNotificationService();
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

    public void loading(boolean state) {

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


}
