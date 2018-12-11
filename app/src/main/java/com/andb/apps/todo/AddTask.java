package com.andb.apps.todo;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.andb.apps.todo.filtering.FilteredLists;
import com.andb.apps.todo.filtering.Filters;
import com.andb.apps.todo.lists.TaskList;
import com.andb.apps.todo.lists.interfaces.TaskListInterface;
import com.andb.apps.todo.objects.Tasks;
import com.andb.apps.todo.settings.SettingsActivity;
import com.andrognito.flashbar.Flashbar;
import com.fastaccess.datetimepicker.callback.DatePickerCallback;
import com.fastaccess.datetimepicker.callback.TimePickerCallback;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class AddTask extends CyaneaAppCompatActivity implements DatePickerCallback, TimePickerCallback {


    private int position = 2;
    private boolean full;
    private boolean editing;
    public static boolean taskEditingLoaded = false;
    public static boolean tagEditingLoaded;
    private int tagPosition = 0;
    private int taskPosition = 0;

    public ArrayList<Tasks> taskList = new ArrayList<>();

    public DateTime taskDateTime;
    long timeMin = 00;
    boolean timeHasBeenSet;


    private RecyclerView mRecyclerView;
    private static AddTaskAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private RecyclerView tagRecyclerView;
    private static AddTaskTagAdapter tagAdapter;
    private RecyclerView.LayoutManager tagLayoutManager;

    public ArrayList<String> itemsList = new ArrayList<>();

    static Activity activity;

    ImageView resetTimeButton;

    boolean notified;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_task);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        resetTimeButton = (ImageView) findViewById(R.id.resetTimeButton);
        final TextView timeText = (TextView) findViewById(R.id.dateTimeText);

        resetTimeButton.setVisibility(View.GONE);
        resetTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskDateTime = new DateTime(3000, 1, 1, 0, 0);
                resetTimeButton.setVisibility(View.GONE);
                timeText.setText(getResources().getText(R.string.add_time_text));
                timeHasBeenSet = false;
            }
        });

        Bundle bundle = getIntent().getExtras();
        taskEditingLoaded = false;
        tagEditingLoaded = false;
        if (bundle.containsKey("edit"))
            editing = bundle.getBoolean("edit");


        activity = this;
        timeHasBeenSet = false;


        if (editing) {


            boolean browse = bundle.getBoolean("browse");

            if (browse) {
                taskList = FilteredLists.browseTaskList;
            } else {
                taskList = FilteredLists.inboxTaskList;
            }

            taskPosition = bundle.getInt("editPos");
            Log.d("taskPosition", Integer.toString(taskPosition));

            notified = taskList.get(taskPosition).isNotified();


            //itemsList = taskList.get(position).getAllListItems();
            prepareForEditing(taskPosition);

            taskDateTime = taskList.get(taskPosition).getDateTime();


            if (!taskDateTime.equals(new DateTime(3000, 1, 1, 0, 0))) {
                timeText.setText(taskDateTime.toString("MMM d, h:mm a"));
                resetTimeButton.setVisibility(View.VISIBLE);
            }

        } else {
            taskDateTime = new DateTime(3000, 1, 1, 0, 0);
            prepareItems();
            prepareItemsRecyclerView(false);
            AddTaskTagAdapter.tagList.clear();
            if (!Filters.getCurrentFilter().isEmpty()) {
                AddTaskTagAdapter.tagList.addAll(Filters.getCurrentFilter());
            }
            prepareTagsRecyclerView(false);
            switchList();

        }


        checkAddListItem();

        checkAddTagItem();

        checkAddTime();

        fabAddList();


    }


    public void prepareItems() {
        for (int i = 0; i < position; i++) {
            itemsList.add("");
        }

    }

    public void prepareForEditing(int position) {
        boolean isItems = taskList.get(position).isListItems();
        boolean isTags = taskList.get(position).isListTags();
        EditText taskName = (EditText) findViewById(R.id.taskName);

        taskName.setText(taskList.get(position).getListName());

        //AddTaskAdapter.taskList.clear();
        AddTaskTagAdapter.tagList.clear();


        if (isItems) {
            itemsList = new ArrayList<>(taskList.get(position).getAllListItems());

            prepareItemsRecyclerView(true);

            //copy-pasted from switchList, starts checked
            final ViewGroup task_layout = (ViewGroup) findViewById(R.id.task_layout);

            Switch switch_task = (Switch) findViewById(R.id.switch_task);
            switch_task.setChecked(true);
            task_layout.setVisibility(View.VISIBLE);
            switch_task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {


                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        TransitionManager.beginDelayedTransition(task_layout);
                        task_layout.setVisibility(View.VISIBLE);

                    } else {
                        TransitionManager.beginDelayedTransition(task_layout);
                        task_layout.setVisibility(View.INVISIBLE);
                    }
                }
            });


        } else {
            prepareItemsRecyclerView(false);
            switchList();
        }


        if (isTags) {

            for (int i = 0; i < taskList.get(position).getListTagsSize(); i++) {
                AddTaskTagAdapter.tagList.add(taskList.get(position).getListTags(i));

            }

            prepareTagsRecyclerView(true);


        } else {
            prepareTagsRecyclerView(false);
            switchList();
        }


    }


    public void prepareItemsRecyclerView(boolean edit) {
        mRecyclerView = (RecyclerView) findViewById(R.id.itemRecyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new AddTaskAdapter(itemsList, edit, taskPosition);
        mRecyclerView.setAdapter(mAdapter);

        // Create an `ItemTouchHelper` and attach it to the `RecyclerView`
        ItemTouchHelper ith = new ItemTouchHelper(_ithCallback);
        ith.attachToRecyclerView(mRecyclerView);


    }

    public void prepareTagsRecyclerView(boolean edit) {


        tagRecyclerView = (RecyclerView) findViewById(R.id.tagRecyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //tagRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        tagLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        tagRecyclerView.setLayoutManager(tagLayoutManager);

        // specify an adapter (see also next example)
        tagAdapter = new AddTaskTagAdapter(taskList, edit, taskPosition);
        tagRecyclerView.setAdapter(tagAdapter);
    }


    public void switchList() {

        final ViewGroup task_layout = (ViewGroup) findViewById(R.id.task_layout);

        final Switch switch_task = (Switch) findViewById(R.id.switch_task);

        switch_task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {


            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    TransitionManager.beginDelayedTransition(task_layout);
                    task_layout.setVisibility(View.VISIBLE);

                } else {
                    TransitionManager.beginDelayedTransition(task_layout);
                    task_layout.setVisibility(View.GONE);

                }
            }
        });
    }


    public void checkAddListItem() {
        ConstraintLayout addTask = (ConstraintLayout) findViewById(R.id.addButton);

        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                position++;
                ;
                itemsList.add("");
                Log.d("addingValue", "adding #" + position);
                mRecyclerView.setItemViewCacheSize(itemsList.size());
                mAdapter.focused = true;
                mAdapter.notifyItemInserted(itemsList.size() - 1);
                mRecyclerView.smoothScrollToPosition(position);

                //mAdapter.notifyDataSetChanged();
            }
        });
    }

    public void checkAddTagItem() {
        ImageView addTag = (ImageView) findViewById(R.id.tagAddButton);
        addTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tagPosition++;
                Intent selectTag = new Intent(AddTask.this, TagSelect.class);
                selectTag.putExtra("isTaskCreate", true);
                startActivity(selectTag);
            }
        });
    }

    public void checkAddTime() {
        ImageView timeButton = (ImageView) findViewById(R.id.timeButton);
        final ImageView dateButton = (ImageView) findViewById(R.id.dateButton);
        final TextView timeText = (TextView) findViewById(R.id.dateTimeText);


        final long timeMin = 00;

        dateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(AddTask.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        TextView timeText = (TextView) findViewById(R.id.dateTimeText);

                        taskDateTime = taskDateTime.withDate(i, i1 + 1, i2);
                        if (!timeHasBeenSet) {
                            taskDateTime = taskDateTime.withTime(23, 59, 59, 0);
                            timeText.setText(taskDateTime.toString("MMM d"));

                        } else {
                            timeText.setText(taskDateTime.toString("MMM d, h:mm a"));
                        }
                        Log.d("dateTime", taskDateTime.toString());

                        if (editing) {
                            if (taskDateTime.isAfter(DateTime.now()))
                                notified = false;
                            else if (taskDateTime.isBefore(taskList.get(taskPosition).getDateTime()))
                                notified = taskList.get(taskPosition).isNotified();
                        }

                        resetTimeButton.setVisibility(View.VISIBLE);

                    }
                }, DateTime.now().getYear(), DateTime.now().getMonthOfYear() - 1, DateTime.now().getDayOfMonth());
                dialog.show();


//                DatePickerFragmentDialog.newInstance(DateTimeBuilder.newInstance()
//                        .withMinDate(Calendar.getInstance().getTimeInMillis()))
//                        .show(getSupportFragmentManager(), "DatePickerFragmentDialog");

            }
        });

        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TimePickerDialog timePickerDialog = new TimePickerDialog(AddTask.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        TextView timeText = (TextView) findViewById(R.id.dateTimeText);
                        timeHasBeenSet = true;
                        LocalTime localTime = new LocalTime().withHourOfDay(i).withMinuteOfHour(i1);
                        localTime = localTime.secondOfMinute().setCopy(0);
                        if (taskDateTime.isEqual(new DateTime(3000, 1, 1, 0, 0))) {
                            taskDateTime = new DateTime(DateTime.now().getYear(), DateTime.now().getMonthOfYear(), DateTime.now().getDayOfMonth(), 23, 0);
                            Log.d("dateTime", taskDateTime.toString());
                            taskDateTime = taskDateTime.withTime(localTime);
                            timeText.setText(taskDateTime.toString("h:mm a"));


                        } else {
                            taskDateTime = taskDateTime.withTime(localTime);
                            timeText.setText(taskDateTime.toString("MMM d, h:mm a"));

                        }

                        if (editing) {
                            if (taskDateTime.isAfter(DateTime.now()))
                                notified = false;
                            else if (taskDateTime.isBefore(taskList.get(taskPosition).getDateTime()))
                                notified = taskList.get(taskPosition).isNotified();
                        }

                        Log.d("dateTime", taskDateTime.toString("h:mm:ss"));

                        resetTimeButton.setVisibility(View.VISIBLE);


                    }
                }, DateTime.now().getHourOfDay(), DateTime.now().getMinuteOfHour(), false);
                timePickerDialog.show();

/*                TimePickerFragmentDialog.newInstance(DateTimeBuilder.newInstance()
                        .withMinDate(timeMin))
                        .show(getSupportFragmentManager(), "TimePickerFragmentDialog");*/
            }
        });

        timeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskDateTime = new DateTime(3000, 1, 1, 0, 0);
                timeText.setText("Add time");
            }
        });

    }


    public void fabAddList() {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkFull();
                if (full) {
                    saveTask();
                    finish();
                } else {
                    Flashbar flashbar = blankText();
                    flashbar.show();
                }
            }
        });
    }


    public void saveTask() {
        EditText taskName = (EditText) findViewById(R.id.taskName);
        ArrayList<String> items = new ArrayList<>();
        ArrayList<Boolean> checked = new ArrayList<>();
        ArrayList<Integer> tags = new ArrayList<>(AddTaskTagAdapter.tagList);
        Switch switch_task = (Switch) findViewById(R.id.switch_task);
        if (switch_task.isChecked()) {
            Log.d("Adding Items", "Adding Items");
            items = new ArrayList<>(itemsList);
        } else {
            //AddTaskAdapter.taskList.clear();
        }


        if (editing) {
            TaskListInterface.replaceTask(taskName.getText().toString(), items, checked, tags, taskDateTime, notified, TaskList.taskList.indexOf(taskList.get(taskPosition)), taskList.get(taskPosition).getListKey(), getApplicationContext());
        } else {
            TaskListInterface.addTask(taskName.getText().toString(), items, checked, tags, taskDateTime);
        }

    }


    public static void addTag(int tagPosition) {
        boolean isAlreadyAdded = false;
        for (int i = 0; i < AddTaskTagAdapter.tagList.size(); i++) {
            if (AddTaskTagAdapter.tagList.get(i) == tagPosition) {
                isAlreadyAdded = true;
            }
        }
        if (!isAlreadyAdded) {
            AddTaskTagAdapter.tagList.add(tagPosition);
            Log.d("addingValue", "adding #" + tagPosition);
            tagAdapter.notifyDataSetChanged();
        } else {
            Flashbar flashbar = tagExists(activity);
            flashbar.show();
        }
    }


    public void checkFull() {
        Log.d("taskName", "gets here");

        EditText taskName = (EditText) findViewById(R.id.taskName);
        Switch switch_task = (Switch) findViewById(R.id.switch_task);
        if (TextUtils.isEmpty(taskName.getText())) {
            full = false;
            Log.d("taskName", "full = false");
        } else if (switch_task.isChecked()) {


            for (int i = 0; i < itemsList.size(); i++) {
                if (itemsList.get(i).isEmpty()) {
                    full = false;
                    break;
                } else {
                    full = true;
                }
            }
        } else if (taskDateTime == null) {

        } else {
            full = true;
        }
    }

    private Flashbar blankText() {
        return new Flashbar.Builder(this)
                .gravity(Flashbar.Gravity.BOTTOM)
                .title("Blank field")
                .message("Please fill in or remove any blank fields")
                .dismissOnTapOutside()
                .backgroundColor(Cyanea.getInstance().getAccent())
                .build();

    }

    private static Flashbar tagExists(Activity activity) {
        return new Flashbar.Builder(activity)
                .gravity(Flashbar.Gravity.BOTTOM)
                .title("Tag Exists")
                .message("The tag you selected has already been added to this task")
                .dismissOnTapOutside()
                .backgroundColor(Cyanea.getInstance().getAccent())
                .build();

    }


    @Override
    public void onDateSet(long date) {





    }

    @Override
    public void onTimeSet(long time, long date) {

    }


    // Extend the Callback class
    ItemTouchHelper.Callback _ithCallback = new ItemTouchHelper.Callback() {
        //and in your implementation of
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Log.d("dragDropStart", itemsList.get(fromPosition));
                    Log.d("dragDropStart", itemsList.get(toPosition));
                    Collections.swap(itemsList, i, i + 1);
                    Log.d("dragDropEnd", itemsList.get(fromPosition));
                    Log.d("dragDropEnd", itemsList.get(toPosition));
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Log.d("dragDropStart", itemsList.get(fromPosition));
                    Log.d("dragDropStart", itemsList.get(toPosition));
                    Collections.swap(itemsList, i, i - 1);
                    Log.d("dragDropEnd", itemsList.get(fromPosition));
                    Log.d("dragDropEnd", itemsList.get(toPosition));
                }
            }
            Log.d("dragDropStart", itemsList.get(fromPosition));
            Log.d("dragDropStart", itemsList.get(toPosition));
            mAdapter.notifyItemMoved(fromPosition, toPosition);
            Log.d("dragDropEnd", itemsList.get(fromPosition));
            Log.d("dragDropEnd", itemsList.get(toPosition));

            return false;


        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        }

        @Override
        public boolean isLongPressDragEnabled() {

            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        //defines the enabled move directions in each state (idle, swiping, dragging).
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN | ItemTouchHelper.UP);
        }
    };


    @Override
    public void onPause() {
        super.onPause();
    }


}

