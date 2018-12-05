package com.andb.apps.todo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andb.apps.todo.settings.SettingsActivity;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import me.saket.inboxrecyclerview.InboxRecyclerView;
import me.saket.inboxrecyclerview.page.ExpandablePageLayout;

public class TaskAdapter extends InboxRecyclerView.Adapter<TaskAdapter.MyViewHolder> {

    public List<Tasks> taskList = new ArrayList<>();

    private Context context;

    public boolean isSelected = false;

    private int viewType = 0;

    private static final int TASK_VIEW_ITEM = 0;
    private static final int OVERDUE_DIVIDER = 1;
    private static final int TODAY_DIVIDER = 2;
    private static final int THIS_WEEK_DIVIDER = 3;
    private static final int THIS_MONTH_DIVIDER = 4;
    private static final int FUTURE_DIVIDER = 5;
    //Preferences for which to show


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public CheckBox item1;
        public CheckBox item2;
        public CheckBox item3;
        public Chip chip1;
        public Chip chip2;
        public Chip chip3;
        public LinearLayout encloser;
        public ImageView more;
        public ImageView moreTags;
        public LinearLayout tagEncloser;

        public ConstraintLayout inboxItemBackground;
        public ExpandablePageLayout taskView;

        public TextView dividerName;

        public ImageView toggle;

        //public NestedScrollView scrollView;

        public MyViewHolder(View view) {
            super(view);


            dividerName = (TextView) view.findViewById(R.id.dividerName);

            name = (TextView) view.findViewById(R.id.taskName);
            item1 =  view.findViewById(R.id.item1);
            item2 = view.findViewById(R.id.item2);
            item3 = view.findViewById(R.id.item3);
            chip1 = view.findViewById(R.id.chip1);
            chip2 = view.findViewById(R.id.chip2);
            chip3 = view.findViewById(R.id.chip3);


            inboxItemBackground =  view.findViewById(R.id.inboxCard);

            encloser = view.findViewById(R.id.checklistEncloser);
            tagEncloser = view.findViewById(R.id.tagOnTaskLayout);
            more = view.findViewById(R.id.itemsMore);
            moreTags = view.findViewById(R.id.moreTags);

            taskView = InboxFragment.mRecyclerView.getRootView().findViewById(R.id.expandable_page);

            toggle = view.findViewById(R.id.sublistIcon);

        }


    }


    public TaskAdapter(List<Tasks> tasksList) {
        this.taskList = tasksList;
    }


    @Override
    public TaskAdapter.MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        context = parent.getContext();
        View itemView;


        if (viewType == 0) {
            itemView = LayoutInflater.from(context)
                    .inflate(R.layout.inbox_list_item, parent, false);
            Log.d("viewType", Integer.toString(viewType));
        } else {
            itemView = LayoutInflater.from(context)
                    .inflate(R.layout.inbox_divider, parent, false);
            Log.d("viewType", Integer.toString(viewType));
        }

        return new TaskAdapter.MyViewHolder(itemView);

    }


    @Override
    public void onBindViewHolder(final TaskAdapter.MyViewHolder holder, int position) {
        final int realPosition = holder.getLayoutPosition();

        setUpByViewType(position, holder, realPosition);

        Log.d("onePosUpError", Integer.toString(realPosition));


        if (isSelected) {
            holder.inboxItemBackground.setBackgroundColor(Color.GRAY);
        } else if (viewType == TASK_VIEW_ITEM) {
            if (SettingsActivity.darkTheme) {
                holder.inboxItemBackground.setBackgroundColor(0xFF424242);
            } else {
                holder.inboxItemBackground.setBackgroundColor(Color.WHITE);
            }
        }


    }

    public void setUpByViewType(final int position, final TaskAdapter.MyViewHolder holder, final int realPosition) {


        if (viewType == 0) {
            holder.inboxItemBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = holder.getLayoutPosition();
                    Bundle bundle = new Bundle();
                    bundle.putInt("pos", pos);
                    bundle.putBoolean("inboxOrArchive", true);
                    bundle.putBoolean("browse", false);

                    FragmentActivity activity = (FragmentActivity) context;
                    FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();

                    TaskView taskView = new TaskView();
                    taskView.setArguments(bundle);
                    ft.add(R.id.expandable_page, taskView);
                    ft.commit();

                    InboxFragment.mRecyclerView.expandItem(getItemId(realPosition));

                }
            });

            holder.name.setText(taskList.get(position).getListName());


/*            if (taskList.get(position).isListTime()) {
                holder.dateText.setText(taskList.get(position).getDateTime().toString("EEEE, MMMM d"));
                if (taskList.get(position).getDateTime().get(DateTimeFieldType.secondOfMinute()) == 59) {
                    holder.timeText.setVisibility(View.GONE);
                } else {
                    holder.timeText.setVisibility(View.VISIBLE);
                    holder.timeText.setText(taskList.get(position).getDateTime().toString("h:mm a"));
                }
                if (SettingsActivity.darkTheme) {
                    holder.timeIcon.setColorFilter(Color.WHITE);
                }
                holder.divider2.setVisibility(View.VISIBLE);
                holder.timeLayout.setVisibility(View.VISIBLE);
            } else {
                holder.divider2.setVisibility(View.GONE);
                holder.timeLayout.setVisibility(View.GONE);
            }*/



            final int adapterPosition = holder.getAdapterPosition();
            Log.d("adapterPosition", Integer.toString(adapterPosition));


            setTasks(realPosition, holder.item1, holder.item2, holder.item3, holder.more, holder.encloser, holder.toggle);
            setTags(realPosition, holder.chip1, holder.chip2, holder.chip3, holder.moreTags, holder.tagEncloser);


        } else { //divider logic
            Log.d("adapterPosition", Integer.toString(realPosition));

            if (realPosition == 0) { //resize if at top
                final float scale = Resources.getSystem().getDisplayMetrics().density;
                int padding16Dp = (int) (16 * scale);
                int padding20Dp = (int) (20 * scale);
                holder.dividerName.setPadding(padding16Dp, padding20Dp, 0, padding16Dp);
            }

            if (viewType == 1) {

                holder.dividerName.setText("OVERDUE");

            } else if (viewType == 2) {

                holder.dividerName.setText("TODAY");

            } else if (viewType == 3) {

                holder.dividerName.setText("THIS WEEK");

            } else if (viewType == 4) {

                holder.dividerName.setText("THIS MONTH");

            } else if (viewType == 5) {

                holder.dividerName.setText("FUTURE");

            }
        }
    }


    @Override
    public int getItemViewType(int position) {
        if (InboxFragment.filterMode == 0) {
            if (taskList.get(position).getListName().equals("OVERDUE")) {
                Log.d("getItemViewType", "OVERDUE");
                viewType = OVERDUE_DIVIDER;
                return OVERDUE_DIVIDER;
            }
            if (taskList.get(position).getListName().equals("TODAY")) {
                Log.d("getItemViewType", "TODAY");
                viewType = TODAY_DIVIDER;
                return TODAY_DIVIDER;
            }
            if (taskList.get(position).getListName().equals("WEEK")) {
                Log.d("getItemViewType", "WEEK");
                viewType = THIS_WEEK_DIVIDER;
                return THIS_WEEK_DIVIDER;
            }
            if (taskList.get(position).getListName().equals("MONTH")) {
                Log.d("getItemViewType", "MONTH");
                viewType = THIS_MONTH_DIVIDER;
                return THIS_MONTH_DIVIDER;
            }
            if (taskList.get(position).getListName().equals("FUTURE")) {
                Log.d("getItemViewType", "FUTURE");
                viewType = FUTURE_DIVIDER;
                return FUTURE_DIVIDER;
            } else {
                Log.d("getItemViewType", "TASK");
                viewType = TASK_VIEW_ITEM;
                return TASK_VIEW_ITEM;
            }


        } else {
            Log.d("getItemViewType", "ALPHA");

            viewType = TASK_VIEW_ITEM;
            return TASK_VIEW_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }


    private static final int[] STATE_ZERO = {R.attr.state_on, -R.attr.state_off};
    private static final int[] STATE_ONE = {-R.attr.state_on, R.attr.state_off};
    private boolean toggled;
    private void setTasks(final int pos, CheckBox check1, CheckBox check2, CheckBox check3, ImageView more, final LinearLayout layout, final ImageView toggle) {

        ArrayList<CheckBox> checkBoxes = new ArrayList<>(Arrays.asList(check1, check2, check3));

        if (taskList.get(pos).isListItems()) {
            Log.d("items", taskList.get(pos).getListName() + ", multipleItems: " + taskList.get(pos).getListItemsSize());

            for (int i = 0; i < 4; i++) {
                final int toSet = i;
                if (i < taskList.get(pos).getListItemsSize()) {
                    if (i == 3) {
                        more.setVisibility(View.VISIBLE);
                    } else {
                        checkBoxes.get(i).setText(taskList.get(pos).getListItems(i));
                        checkBoxes.get(i).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                taskList.get(pos).editListItemsChecked(isChecked, toSet);
                            }
                        });
                        checkBoxes.get(i).setChecked(taskList.get(pos).getListItemsChecked(i));
                        checkBoxes.get(i).setVisibility(View.VISIBLE);
                    }


                } else {
                    if (i == 3) {
                        more.setVisibility(View.GONE);
                    } else {
                        checkBoxes.get(i).setVisibility(View.GONE);
                    }
                }
            }

            if(SettingsActivity.subtaskDefaultShow){
                ViewGroup.LayoutParams layoutParams = layout.getLayoutParams();
                layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                layout.setLayoutParams(layoutParams);
                layout.setVisibility(View.VISIBLE);
                toggle.setImageState(STATE_ZERO, true);
                toggled = true;
            }else {
                ViewGroup.LayoutParams layoutParams = layout.getLayoutParams();
                layoutParams.height = 0;
                layout.setLayoutParams(layoutParams);
                //layout.setVisibility(View.GONE);
                toggle.setImageState(STATE_ONE, true);
                toggled = false;
            }

            toggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggled = !toggled;
                    if(toggled){
                        toggle.setImageState(STATE_ZERO, true);
                        //layout.setVisibility(View.VISIBLE);

                        TransitionManager.beginDelayedTransition(layout, new TransitionSet().addTransition(new ChangeBounds()));
                        ViewGroup.LayoutParams layoutParams = layout.getLayoutParams();
                        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        layout.setLayoutParams(layoutParams);

                    }else {
                        toggle.setImageState(STATE_ONE, true);

                        TransitionManager.beginDelayedTransition(layout, new TransitionSet().addTransition(new ChangeBounds()));
                        ViewGroup.LayoutParams layoutParams = layout.getLayoutParams();
                        layoutParams.height = 1;
                        layout.setLayoutParams(layoutParams);
                        //layout.setVisibility(View.GONE);
                    }
                }
            });


        } else {
            Log.d("items", taskList.get(pos).getListName() + ", singleItem");
            check1.setVisibility(View.GONE);
            check2.setVisibility(View.GONE);
            check3.setVisibility(View.GONE);
            toggle.setVisibility(View.GONE);
            layout.setPadding(4, 0, 0, 0);

        }

        boolean c1 = false;
        boolean c2 = false;
        boolean c3 = false;
        boolean lay = false;
        if (check1.getVisibility() == View.VISIBLE) {
            c1 = true;
        }
        if (check2.getVisibility() == View.VISIBLE) {
            c2 = true;
        }
        if (check3.getVisibility() == View.VISIBLE) {
            c3 = true;
        }
        if (layout.getVisibility() == View.VISIBLE) {
            lay = true;
        }
        Log.d("items", "Showing:" + Boolean.toString(c1) + Boolean.toString(c2) + Boolean.toString(c3) + Boolean.toString(lay));







    }

    private void setTags(int pos, Chip chip1, Chip chip2, Chip chip3, ImageView moreTags, LinearLayout layout) {



        if (taskList.get(pos).isListTags()) {
            Log.d("tags", "multipleTags");


            ArrayList<Chip> tagsList = new ArrayList<>(Arrays.asList(chip1, chip2, chip3));

            for (int i = 0; i < tagsList.size(); i++) {


                if (i < taskList.get(pos).getAllListTags().size()) {
                    Tags tagtemp = TagList.getItem(taskList.get(pos).getListTags(i));
                    Chip chiptemp = tagsList.get(i);

                    chiptemp.setText(tagtemp.getTagName());
                    Drawable drawable = chiptemp.getChipIcon().mutate();
                    drawable.setColorFilter(tagtemp.getTagColor(), PorterDuff.Mode.SRC_ATOP);
                    chiptemp.setChipIcon(drawable);

                } else {
                    tagsList.get(i).setVisibility(View.GONE);
                }

            }




        } else {
            Log.d("inboxFilterAdapter", Integer.toString(taskList.size()));
            Log.d("items", "singleItem");
            layout.setVisibility(View.GONE);
        }
    }

    @Override
    public long getItemId(int position) {
        //return super.getItemId(position);
        if (getItemViewType(position) == 0) {
            return taskList.get(position).getListKey();
        } else {
            return (-1 * getItemViewType(position));
        }
    }
}
