package com.andb.apps.todo;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTimeFieldType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ArchiveAdapter extends RecyclerView.Adapter<ArchiveAdapter.MyViewHolder> {

    public static List<Tasks> taskList = new ArrayList<>();

    private Context context;
    /*private RecyclerView iRecyclerView;
    private static ChecklistAdapter iAdapter;
    private RecyclerView.LayoutManager iLayoutManager;
    public View itemView;*/

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView clearList;
        public ImageView backToInbox;
        public ConstraintLayout item1;
        public ConstraintLayout item2;
        public ConstraintLayout item3;
        public LinearLayout encloser;
        public ImageView more;
        public ConstraintLayout tag1;
        public ConstraintLayout tag2;
        public ConstraintLayout tag3;
        public ConstraintLayout tag4;
        public ConstraintLayout tag5;
        public LinearLayout tagEncloser;
        public ImageView moreTags;
        public ConstraintLayout timeLayout;
        public TextView dateText;
        public TextView timeText;
        public ImageView timeIcon;


        public View divider1;
        public View divider2;

        public TextView dividerName;

        public MyViewHolder(View view) {
            super(view);

            divider1 = (View) view.findViewById(R.id.tagDivider);
            divider2 = (View) view.findViewById(R.id.timeDivider);

            dividerName = (TextView) view.findViewById(R.id.dividerName);

            name = (TextView) view.findViewById(R.id.listTextView);
            clearList = (ImageView) view.findViewById(R.id.clearList);
            item1 = (ConstraintLayout) view.findViewById(R.id.item1);
            item2 = (ConstraintLayout) view.findViewById(R.id.item2);
            item3 = (ConstraintLayout) view.findViewById(R.id.item3);
            tag1 = (ConstraintLayout) view.findViewById(R.id.tag1);
            tag2 = (ConstraintLayout) view.findViewById(R.id.tag2);
            tag3 = (ConstraintLayout) view.findViewById(R.id.tag3);
            tag4 = (ConstraintLayout) view.findViewById(R.id.tag4);
            tag5 = (ConstraintLayout) view.findViewById(R.id.tag5);
            moreTags = (ImageView) view.findViewById(R.id.tagMore);
            dateText = (TextView) view.findViewById(R.id.dateInboxText);
            timeText = (TextView) view.findViewById(R.id.timeInboxText);
            timeIcon = (ImageView) view.findViewById(R.id.timeIcon);


            encloser = (LinearLayout) view.findViewById(R.id.checklistEncloser);
            tagEncloser = (LinearLayout) view.findViewById(R.id.tagOnTaskLayout);
            timeLayout = (ConstraintLayout) view.findViewById(R.id.dateTimeInboxLayout);
            more = (ImageView) view.findViewById(R.id.itemsMore);
            backToInbox = (ImageView) view.findViewById(R.id.backToInbox);

            more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    Intent intent = new Intent(v.getContext(), TaskView.class);
                    intent.putExtra("pos", position);
                    intent.putExtra("inboxOrArchive", false);
                    v.getContext().startActivity(intent);
                }
            });
            moreTags.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int realPosition = getAdapterPosition();
                    Intent intent = new Intent(v.getContext(), TaskView.class);
                    intent.putExtra("pos", realPosition);
                    intent.putExtra("inboxOrArchive", false);
                    Log.d("onePosUpError", Integer.toString(realPosition));
                    v.getContext().startActivity(intent);
                }
            });

            backToInbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getLayoutPosition();
                    final Tasks tasks = taskList.get(position);
                    taskList.remove(position);
                    notifyItemRemoved(position);
                    Log.d("backToInbox", "backToInbox");


                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {

                            MainActivity.tasksDatabase.tasksDao().insertOnlySingleTask(tasks);

                            TaskList.taskList = new ArrayList<>(MainActivity.tasksDatabase.tasksDao().getAll());


                            EventBus.getDefault().post(new UpdateEvent(true));

                        }


                    });


                }
            });

            //taskColor = (ImageView) view.findViewById(R.id.taskIcon);

        }


    }


    public ArchiveAdapter(List<Tasks> tasksList) {
        this.taskList = tasksList;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        /*//Indicates whether each item in the data set can be represented with a unique identifier
        setHasStableIds(true);*/

        context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.inbox_list_item, parent, false);


        Log.d("inflated", "inbox inflated");
        //itemView.startAnimation(AnimationUtils.loadAnimation(parent.getContext(), android.R.anim.slide_in_left));

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        holder.name.setText(taskList.get(position).getListName());

        if (taskList.get(position).isListTime()) {
            holder.dateText.setText(taskList.get(position).getDateTime().toString("EEEE, MMMM d"));
            if (taskList.get(position).getDateTime().get(DateTimeFieldType.secondOfMinute()) == 59) {
                holder.timeText.setVisibility(View.GONE);
            } else {
                holder.timeText.setVisibility(View.VISIBLE);
                holder.timeText.setText(taskList.get(position).getDateTime().toString("h:mm a"));
            }
/*            if (SettingsActivity.darkTheme) {
                holder.timeIcon.setColorFilter(Color.WHITE);
            }*/
        } else {
            holder.divider2.setVisibility(View.GONE);
            holder.timeLayout.setVisibility(View.GONE);
        }

        if (!taskList.get(position).isListTags()) {
            holder.divider1.setVisibility(View.GONE);
        }


        holder.clearList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                taskList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, taskList.size());
            }
        });


        setTasks(position, holder.item1, holder.item2, holder.item3, holder.more, holder.encloser);
        setTags(position, holder.tag1, holder.tag2, holder.tag3, holder.tag4, holder.tag5, holder.moreTags, holder.tagEncloser);

        holder.backToInbox.setVisibility(View.VISIBLE);
/*        if (SettingsActivity.darkTheme) {
            holder.backToInbox.setColorFilter(Color.WHITE);
            holder.clearList.setColorFilter(Color.WHITE);
            holder.name.setTextColor(Color.WHITE);
        }*/


    }

    @Override
    public int getItemViewType(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }


    private void setTasks(final int pos, ConstraintLayout box1, ConstraintLayout box2, ConstraintLayout box3, ImageView more, LinearLayout layout) {
        final CheckBox check1 = (CheckBox) box1.findViewById(R.id.listTextView);
        final CheckBox check2 = (CheckBox) box2.findViewById(R.id.listTextView);
        final CheckBox check3 = (CheckBox) box3.findViewById(R.id.listTextView);


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
            /*
                    if (taskList.get(pos).isListItems()) {
            Log.d("items", "multipleItems");
            if (taskList.get(pos).getListItemsSize() == 1) {

                check1.setChecked(taskList.get(pos).getListItemsChecked(0));
                check1.setText(taskList.get(pos).getListItems(0));
                check1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        ArchiveTaskList.getItem(pos).editListItemsChecked(isChecked, 0);
                        *//*if (check1.isChecked())
                            check1.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                        else
                            check1.setButtonTintList(ColorStateList.valueOf(0xFF757575));*//*
                    }
                });

                *//*if (check1.isChecked())
                    check1.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                else
                    check1.setButtonTintList(ColorStateList.valueOf(0xFF757575));*//*

                check2.setVisibility(View.GONE);

                check3.setVisibility(View.GONE);


            } else if (taskList.get(pos).getListItemsSize() == 2) {

                check1.setChecked(taskList.get(pos).getListItemsChecked(0));
                check1.setText(taskList.get(pos).getListItems(0));
                check1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                        ArchiveTaskList.getItem(pos).editListItemsChecked(isChecked, 0);
                        *//*if (check1.isChecked())
                            check1.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                        else
                            check1.setButtonTintList(ColorStateList.valueOf(0xFF757575));*//*
                    }
                });
                *//*if (check1.isChecked())
                    check1.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                else
                    check1.setButtonTintList(ColorStateList.valueOf(0xFF757575));*//*


                check2.setChecked(taskList.get(pos).getListItemsChecked(1));
                check2.setText(taskList.get(pos).getListItems(1));
                check2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        ArchiveTaskList.getItem(pos).editListItemsChecked(isChecked, 1);
                        *//*if (check2.isChecked())
                            check2.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                        else
                            check2.setButtonTintList(ColorStateList.valueOf(0xFF757575));*//*
                    }
                });

                *//*if (check2.isChecked())
                    check2.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                else
                    check2.setButtonTintList(ColorStateList.valueOf(0xFF757575));*//*


                check3.setVisibility(View.GONE);


            } else if (taskList.get(pos).getListItemsSize() == 3) {
                check1.setChecked(taskList.get(pos).getListItemsChecked(0));
                check1.setText(taskList.get(pos).getListItems(0));
                check1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        ArchiveTaskList.getItem(pos).editListItemsChecked(isChecked, 0);
                        *//*if (check1.isChecked())
                            check1.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                        else
                            check1.setButtonTintList(ColorStateList.valueOf(0xFF757575));*//*
                    }
                });

                *//*if (check1.isChecked())
                    check1.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                else
                    check1.setButtonTintList(ColorStateList.valueOf(0xFF757575));*//*


                check2.setChecked(taskList.get(pos).getListItemsChecked(1));
                check2.setText(taskList.get(pos).getListItems(1));
                check2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        ArchiveTaskList.getItem(pos).editListItemsChecked(isChecked, 1);
                        *//*if (check2.isChecked())
                            check2.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                        else
                            check2.setButtonTintList(ColorStateList.valueOf(0xFF757575));*//*
                    }
                });

                *//*if (check2.isChecked())
                    check2.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                else
                    check2.setButtonTintList(ColorStateList.valueOf(0xFF757575));*//*


                check3.setChecked(taskList.get(pos).getListItemsChecked(2));
                check3.setText(taskList.get(pos).getListItems(2));

                check3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        ArchiveTaskList.getItem(pos).editListItemsChecked(isChecked, 2);
                        *//*if (check3.isChecked())
                            check3.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                        else
                            check3.setButtonTintList(ColorStateList.valueOf(0xFF757575));*//*
                    }
                });

                *//*if (check3.isChecked())
                    check3.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                else
                    check3.setButtonTintList(ColorStateList.valueOf(0xFF757575));*//*


            } else if (taskList.get(pos).getListItemsSize() >= 4) {

                check1.setChecked(taskList.get(pos).getListItemsChecked(0));
                check1.setText(taskList.get(pos).getListItems(0));
                check1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        ArchiveTaskList.getItem(pos).editListItemsChecked(isChecked, 0);
                        *//*if (check1.isChecked())
                            check1.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                        else
                            check1.setButtonTintList(ColorStateList.valueOf(0xFF757575));*//*
                    }
                });

                if (check1.isChecked())
                    check1.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                else
                    check1.setButtonTintList(ColorStateList.valueOf(0xFF757575));


                check2.setChecked(taskList.get(pos).getListItemsChecked(1));
                check2.setText(taskList.get(pos).getListItems(1));
                check2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        ArchiveTaskList.getItem(pos).editListItemsChecked(isChecked, 1);
                        if (check2.isChecked())
                            check2.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                        else
                            check2.setButtonTintList(ColorStateList.valueOf(0xFF757575));
                    }
                });

                if (check2.isChecked())
                    check2.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                else
                    check2.setButtonTintList(ColorStateList.valueOf(0xFF757575));


                check3.setChecked(taskList.get(pos).getListItemsChecked(2));
                check3.setText(taskList.get(pos).getListItems(2));

                check3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        ArchiveTaskList.getItem(pos).editListItemsChecked(isChecked, 2);
                        if (check3.isChecked())
                            check3.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                        else
                            check3.setButtonTintList(ColorStateList.valueOf(0xFF757575));
                    }
                });

                if (check3.isChecked())
                    check3.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                else
                    check3.setButtonTintList(ColorStateList.valueOf(0xFF757575));


                more.setVisibility(View.VISIBLE);
                if (SettingsActivity.darkTheme)
                    more.setColorFilter(Color.WHITE);


            }*/
        } else {
            Log.d("items", "singleItem");
            layout.setVisibility(View.GONE);
        }
    }

    private void setTags(int pos, ConstraintLayout tag1, ConstraintLayout tag2, ConstraintLayout tag3, ConstraintLayout tag4, ConstraintLayout tag5, ImageView moreTags, LinearLayout layout) {
        ImageView image1 = (ImageView) tag1.findViewById(R.id.tagImage);
        ImageView image2 = (ImageView) tag2.findViewById(R.id.tagImage);
        ImageView image3 = (ImageView) tag3.findViewById(R.id.tagImage);
        ImageView image4 = (ImageView) tag4.findViewById(R.id.tagImage);
        ImageView image5 = (ImageView) tag5.findViewById(R.id.tagImage);

        ArrayList<ImageView> tagPointers = new ArrayList<>(Arrays.asList(image1, image2, image3, image4, image5));

        for (int i = 0; i < tagPointers.size(); i++) {
            if (i < taskList.get(pos).getAllListTags().size()) {
                Tags tagtemp = TagList.getItem(taskList.get(pos).getListTags(i));
                tagPointers.get(i).setColorFilter(tagtemp.getTagColor());
                tagPointers.get(i).setVisibility(View.VISIBLE);
            } else {
                tagPointers.get(i).setVisibility(View.GONE);
            }
        }

        if (taskList.get(pos).getAllListTags().size() >= 6) {


            moreTags.setVisibility(View.VISIBLE);
/*            if (SettingsActivity.darkTheme)
                moreTags.setColorFilter(Color.WHITE);*/


        }
        /*if (taskList.get(pos).isListTags()) {
            Log.d("tags", "multipleTags");

            if (taskList.get(pos).getAllListTags().size() == 1) {

                Tags tag1temp = TagList.getItem(taskList.get(pos).getListTags(0));
                image1.setColorFilter(tag1temp.getTagColor());
                Log.d("color", Integer.toString(tag1temp.getTagColor()));
                image2.setVisibility(View.GONE);
                image3.setVisibility(View.GONE);
                image4.setVisibility(View.GONE);
                image5.setVisibility(View.GONE);

            } else if (taskList.get(pos).getAllListTags().size() == 2) {

                Tags tag1temp = TagList.getItem(taskList.get(pos).getListTags(0));
                image1.setColorFilter(tag1temp.getTagColor());
                Tags tag2temp = TagList.getItem(taskList.get(pos).getListTags(1));
                image2.setColorFilter(tag2temp.getTagColor());
                image3.setVisibility(View.GONE);
                image4.setVisibility(View.GONE);
                image5.setVisibility(View.GONE);
            } else if (taskList.get(pos).getAllListTags().size() == 3) {
                Tags tag1temp = TagList.getItem(taskList.get(pos).getListTags(0));
                image1.setColorFilter(tag1temp.getTagColor());
                Tags tag2temp = TagList.getItem(taskList.get(pos).getListTags(1));
                image2.setColorFilter(tag2temp.getTagColor());
                Tags tag3temp = TagList.getItem(taskList.get(pos).getListTags(2));
                image3.setColorFilter(tag3temp.getTagColor());

                image4.setVisibility(View.GONE);
                image5.setVisibility(View.GONE);
            } else if (taskList.get(pos).getAllListTags().size() == 4) {
                Tags tag1temp = TagList.getItem(taskList.get(pos).getListTags(0));
                image1.setColorFilter(tag1temp.getTagColor());
                Tags tag2temp = TagList.getItem(taskList.get(pos).getListTags(1));
                image2.setColorFilter(tag2temp.getTagColor());
                Tags tag3temp = TagList.getItem(taskList.get(pos).getListTags(2));
                image3.setColorFilter(tag3temp.getTagColor());
                Tags tag4temp = TagList.getItem(taskList.get(pos).getListTags(3));
                image4.setColorFilter(tag4temp.getTagColor());
                image5.setVisibility(View.GONE);
            } else if (taskList.get(pos).getAllListTags().size() == 5) {
                Tags tag1temp = TagList.getItem(taskList.get(pos).getListTags(0));
                image1.setColorFilter(tag1temp.getTagColor());
                Tags tag2temp = TagList.getItem(taskList.get(pos).getListTags(1));
                image2.setColorFilter(tag2temp.getTagColor());
                Tags tag3temp = TagList.getItem(taskList.get(pos).getListTags(2));
                image3.setColorFilter(tag3temp.getTagColor());
                Tags tag4temp = TagList.getItem(taskList.get(pos).getListTags(3));
                image4.setColorFilter(tag4temp.getTagColor());
                Tags tag5temp = TagList.getItem(taskList.get(pos).getListTags(4));
                image5.setColorFilter(tag5temp.getTagColor());
            } else if (taskList.get(pos).getAllListTags().size() >= 6) {

                Tags tag1temp = TagList.getItem(taskList.get(pos).getListTags(0));
                image1.setColorFilter(tag1temp.getTagColor());
                Tags tag2temp = TagList.getItem(taskList.get(pos).getListTags(1));
                image2.setColorFilter(tag2temp.getTagColor());
                Tags tag3temp = TagList.getItem(taskList.get(pos).getListTags(2));
                image3.setColorFilter(tag3temp.getTagColor());
                Tags tag4temp = TagList.getItem(taskList.get(pos).getListTags(3));
                image4.setColorFilter(tag4temp.getTagColor());
                Tags tag5temp = TagList.getItem(taskList.get(pos).getListTags(4));
                image5.setColorFilter(tag5temp.getTagColor());

                moreTags.setVisibility(View.VISIBLE);
                if (SettingsActivity.darkTheme)
                    moreTags.setColorFilter(Color.WHITE);


            }
        } else {
            Log.d("items", "singleItem");
            layout.setVisibility(View.GONE);
        }*/
    }


}
