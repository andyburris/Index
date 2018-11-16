package com.andb.apps.todo;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.joda.time.DateTimeFieldType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.WorkManager;

import static android.util.Log.d;
import static com.andb.apps.todo.NotifyWorker.workTag;

public class BrowseTaskAdapter extends RecyclerView.Adapter<BrowseTaskAdapter.MyViewHolder> {
    public static List<Tasks> taskList = new ArrayList<>();

    private Context context;
    private ActionMode actionMode;


    public boolean isSelected = false;

    private int viewType = 0;

    private static final int TAG_LINK_ITEM = 0;
    private static final int TASK_VIEW_ITEM = 1;
    private static final int DIVIDER = 2;

    private int lastPosition = -1;

    public int debugSetTasks = 0;

    //Preferences for which to show


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView clearList;
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

        public ConstraintLayout inboxListItemBackground;

        public TextView tagName;
        public ImageView browseTagImage;
        public ConstraintLayout browseLayout;

        public CardView browseCard;

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

            inboxListItemBackground = (ConstraintLayout) view.findViewById(R.id.inboxListBackground);

            tagName = (TextView) view.findViewById(R.id.browseTagName);
            browseTagImage = (ImageView) view.findViewById(R.id.browseTagImage);
            browseLayout = (ConstraintLayout) view.findViewById(R.id.tagCardBrowseLayout);


            //this.setIsRecyclable(false);


            //taskColor = (ImageView) view.findViewById(R.id.taskIcon);

        }


    }


    public BrowseTaskAdapter(List<Tasks> tasksList) {
        this.taskList = tasksList;
    }


    @Override
    public MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        /*//Indicates whether each item in the data set can be represented with a unique identifier
        setHasStableIds(true);*/

        context = parent.getContext();
        View itemView;
        if (viewType == TASK_VIEW_ITEM) {
            itemView = LayoutInflater.from(context)
                    .inflate(R.layout.inbox_list_item, parent, false);
            d("viewType", Integer.toString(viewType));
        } else {
            itemView = LayoutInflater.from(context)
                    .inflate(R.layout.browse_blank_list_item, parent, false);
            d("viewType", Integer.toString(viewType));
        }

        return new MyViewHolder(itemView);


    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Log.d("taskListError", Integer.toString(position));
        final int realPosition = holder.getAdapterPosition();
        Log.d("taskListError", Integer.toString(realPosition));

        setUpByViewType(position, holder, realPosition);

        if (isSelected) {
            holder.inboxListItemBackground.setBackgroundColor(Color.GRAY);
        } else {
            if (SettingsActivity.darkTheme) {
                holder.inboxListItemBackground.setBackgroundColor(0xFF424242);
            } else {
                holder.inboxListItemBackground.setBackgroundColor(Color.WHITE);
            }
        }


    }

    public void setUpByViewType(final int position, final MyViewHolder holder, final int realPosition) {

        if (viewType == TASK_VIEW_ITEM) {
            if (taskList.get(realPosition).getListItemsSize() > 3) {
                holder.more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), TaskView.class);
                        intent.putExtra("pos", realPosition);
                        intent.putExtra("inboxOrArchive", true);
                        intent.putExtra("browse", true);
                        v.getContext().startActivity(intent);
                    }
                });
            }
            if (taskList.get(realPosition).getListTagsSize() > 5) {
                holder.moreTags.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(v.getContext(), TaskView.class);
                        intent.putExtra("pos", realPosition);
                        intent.putExtra("inboxOrArchive", true);
                        intent.putExtra("browse", true);
                        Log.d("onePosUpError", Integer.toString(realPosition));
                        v.getContext().startActivity(intent);
                    }
                });
            }

            d("errorSetText", taskList.get(realPosition).getListName());

            holder.name.setText(taskList.get(realPosition).getListName());
            if (SettingsActivity.darkTheme)
                holder.name.setTextColor(Color.WHITE);

            if (taskList.get(realPosition).isListTime()) {
                holder.dateText.setText(taskList.get(realPosition).getDateTime().toString("EEEE, MMMM d"));
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
            }

            if (!taskList.get(realPosition).isListTags()) {
                holder.divider1.setVisibility(View.GONE);
            }

            holder.clearList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    ArchiveTaskList.addTaskList(taskList.get(realPosition));
                    TaskList.keyList.remove((Integer) taskList.get(realPosition).getListKey());
                    TaskList.taskList.remove(taskList.get(realPosition));
                    taskList.remove(realPosition);
                    notifyItemRemoved(realPosition);
                    notifyItemRangeChanged(realPosition, taskList.size() - 1);
                    BrowseFragment.fromAdapter = true;
                    BrowseFragment.createFilteredTaskList(Filters.getCurrentFilter(), true);
                    WorkManager.getInstance().cancelAllWorkByTag(workTag);
                    MainActivity.restartNotificationService();

                }
            });
            if (SettingsActivity.darkTheme)
                holder.clearList.setColorFilter(Color.WHITE);


            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {


                    d("longclicked", "long clicked");


                    return false;
                }
            });


            Log.d("browseCheckSetTasks", Integer.toString(debugSetTasks));
            debugSetTasks++;

            setTasks(realPosition, holder.item1, holder.item2, holder.item3, holder.more, holder.encloser);
            setTags(realPosition, holder.tag1, holder.tag2, holder.tag3, holder.tag4, holder.tag5, holder.moreTags, holder.tagEncloser);


        }
    }


    @Override
    public int getItemViewType(int position) {

        if (InboxFragment.filterMode == 0) {
            if (taskList.get(position).getListName().equals("OVERDUE")
                    | taskList.get(position).getListName().equals("TODAY")
                    | taskList.get(position).getListName().equals("WEEK")
                    | taskList.get(position).getListName().equals("MONTH")
                    | taskList.get(position).getListName().equals("FUTURE")) {
                viewType = DIVIDER;
                Log.d("taskListError", "DIV");
                return DIVIDER;
            } else {
                Log.d("taskListError", "TASK");
                viewType = TASK_VIEW_ITEM;
                return TASK_VIEW_ITEM;
            }


        } else {
            d("getItemViewType", "ALPHA");

            viewType = TASK_VIEW_ITEM;
            return TASK_VIEW_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        int size = taskList.size();
        return size;
    }


    private void setTasks(final int pos, ConstraintLayout box1, ConstraintLayout box2, ConstraintLayout box3, ImageView more, LinearLayout layout) {
        final CheckBox check1 = (CheckBox) box1.findViewById(R.id.listTextView);
        final CheckBox check2 = (CheckBox) box2.findViewById(R.id.listTextView);
        final CheckBox check3 = (CheckBox) box3.findViewById(R.id.listTextView);

        if (taskList.get(pos).isListItems()) {
            Log.d("items", taskList.get(pos).getListName() + ", multipleItems: " + taskList.get(pos).getListItemsSize());

            switch (taskList.get(pos).getListItemsSize()) {
                case 1:
                    Log.d("items", taskList.get(pos).getListName() + ", 1 item: " + taskList.get(pos).getListItemsSize());
                    check1.setText(taskList.get(pos).getListItems(0));
                    check1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            taskList.get(pos).editListItemsChecked(isChecked, 0);
                            if (check1.isChecked())
                                check1.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                            else
                                check1.setButtonTintList(ColorStateList.valueOf(0xFF757575));
                        }
                    });
                    check1.setChecked(taskList.get(pos).getListItemsChecked(0));

                    if (check1.isChecked())
                        check1.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                    else
                        check1.setButtonTintList(ColorStateList.valueOf(0xFF757575));

                    check1.setVisibility(View.VISIBLE);
                    check2.setVisibility(View.GONE);
                    check3.setVisibility(View.GONE);

                    break;


                case 2:
                    Log.d("items", taskList.get(pos).getListName() + ", 2 items: " + taskList.get(pos).getListItemsSize());

                    check1.setText(taskList.get(pos).getListItems(0));
                    check1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            taskList.get(pos).editListItemsChecked(isChecked, 0);
                            if (check1.isChecked())
                                check1.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                            else
                                check1.setButtonTintList(ColorStateList.valueOf(0xFF757575));
                        }
                    });
                    check1.setChecked(taskList.get(pos).getListItemsChecked(0));

                    if (check1.isChecked())
                        check1.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                    else
                        check1.setButtonTintList(ColorStateList.valueOf(0xFF757575));

                    check2.setText(taskList.get(pos).getListItems(1));
                    check2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            taskList.get(pos).editListItemsChecked(isChecked, 1);
                            if (check2.isChecked())
                                check2.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                            else
                                check2.setButtonTintList(ColorStateList.valueOf(0xFF757575));
                        }
                    });
                    check2.setChecked(taskList.get(pos).getListItemsChecked(1));

                    if (check2.isChecked())
                        check2.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                    else
                        check2.setButtonTintList(ColorStateList.valueOf(0xFF757575));

                    check1.setVisibility(View.VISIBLE);
                    check2.setVisibility(View.VISIBLE);
                    check3.setVisibility(View.GONE);
                    break;


                case 3:
                    check1.setText(taskList.get(pos).getListItems(0));
                    check1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            taskList.get(pos).editListItemsChecked(isChecked, 0);
                            if (check1.isChecked())
                                check1.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                            else
                                check1.setButtonTintList(ColorStateList.valueOf(0xFF757575));
                        }
                    });
                    check1.setChecked(taskList.get(pos).getListItemsChecked(0));
                    if (check1.isChecked())
                        check1.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                    else
                        check1.setButtonTintList(ColorStateList.valueOf(0xFF757575));

                    check2.setText(taskList.get(pos).getListItems(1));
                    check2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            taskList.get(pos).editListItemsChecked(isChecked, 1);
                            if (check2.isChecked())
                                check2.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                            else
                                check2.setButtonTintList(ColorStateList.valueOf(0xFF757575));
                        }
                    });
                    check2.setChecked(taskList.get(pos).getListItemsChecked(1));
                    if (check2.isChecked())
                        check2.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                    else
                        check2.setButtonTintList(ColorStateList.valueOf(0xFF757575));

                    check3.setText(taskList.get(pos).getListItems(2));

                    check3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            taskList.get(pos).editListItemsChecked(isChecked, 2);
                            if (check3.isChecked())
                                check3.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                            else
                                check3.setButtonTintList(ColorStateList.valueOf(0xFF757575));
                        }
                    });
                    check3.setChecked(taskList.get(pos).getListItemsChecked(2));
                    if (check3.isChecked())
                        check3.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                    else
                        check3.setButtonTintList(ColorStateList.valueOf(0xFF757575));

                    check1.setVisibility(View.VISIBLE);
                    check2.setVisibility(View.VISIBLE);
                    check3.setVisibility(View.VISIBLE);

                    break;


                default:

                    Log.d("items", taskList.get(pos).getListName() + ", more than 3 items: " + taskList.get(pos).getListItemsSize());

                    check1.setText(taskList.get(pos).getListItems(0));
                    check1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            taskList.get(pos).editListItemsChecked(isChecked, 0);
                            if (check1.isChecked())
                                check1.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                            else
                                check1.setButtonTintList(ColorStateList.valueOf(0xFF757575));
                        }
                    });
                    check1.setChecked(taskList.get(pos).getListItemsChecked(0));

                    if (check1.isChecked())
                        check1.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                    else
                        check1.setButtonTintList(ColorStateList.valueOf(0xFF757575));


                    check2.setText(taskList.get(pos).getListItems(1));
                    check2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            taskList.get(pos).editListItemsChecked(isChecked, 1);
                            if (check2.isChecked())
                                check2.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                            else
                                check2.setButtonTintList(ColorStateList.valueOf(0xFF757575));
                        }
                    });
                    check2.setChecked(taskList.get(pos).getListItemsChecked(1));

                    if (check2.isChecked())
                        check2.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                    else
                        check2.setButtonTintList(ColorStateList.valueOf(0xFF757575));

                    check3.setText(taskList.get(pos).getListItems(2));

                    check3.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            taskList.get(pos).editListItemsChecked(isChecked, 2);
                            if (check3.isChecked())
                                check3.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                            else
                                check3.setButtonTintList(ColorStateList.valueOf(0xFF757575));
                        }
                    });
                    check3.setChecked(taskList.get(pos).getListItemsChecked(2));

                    if (check3.isChecked())
                        check3.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                    else
                        check3.setButtonTintList(ColorStateList.valueOf(0xFF757575));

                    more.setVisibility(View.VISIBLE);
                    if (SettingsActivity.darkTheme)
                        more.setColorFilter(Color.WHITE);

                    check1.setVisibility(View.VISIBLE);
                    check2.setVisibility(View.VISIBLE);
                    check3.setVisibility(View.VISIBLE);


                    break;
            }

            layout.setVisibility(View.VISIBLE);


        } else {
            Log.d("items", taskList.get(pos).getListName() + ", singleItem");
            layout.setVisibility(View.GONE);
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

    private void setTags(int pos, ConstraintLayout tag1, ConstraintLayout tag2, ConstraintLayout tag3, ConstraintLayout tag4, ConstraintLayout tag5, ImageView moreTags, LinearLayout layout) {
        ImageView image1 = (ImageView) tag1.findViewById(R.id.tagImage);
        ImageView image2 = (ImageView) tag2.findViewById(R.id.tagImage);
        ImageView image3 = (ImageView) tag3.findViewById(R.id.tagImage);
        ImageView image4 = (ImageView) tag4.findViewById(R.id.tagImage);
        ImageView image5 = (ImageView) tag5.findViewById(R.id.tagImage);


        if (taskList.get(pos).isListTags()) {
            Log.d("tags", "multipleTags");

            /*if (taskList.get(pos).getAllListTags().size() == 1) {

                Log.d("errorLoadingTags", Integer.toString(taskList.get(pos).getListTags(0)));

                Tags tag1temp = TagList.getItem(taskList.get(pos).getListTags(0));
                image1.setColorFilter(tag1temp.getTagColor());
                Log.d("color", Integer.toString(tag1temp.getTagColor()));
                image1.setVisibility(View.VISIBLE);
                image2.setVisibility(View.GONE);
                image3.setVisibility(View.GONE);
                image4.setVisibility(View.GONE);
                image5.setVisibility(View.GONE);

            } else if (taskList.get(pos).getAllListTags().size() == 2) {

                Tags tag1temp = TagList.getItem(taskList.get(pos).getListTags(0));
                image1.setColorFilter(tag1temp.getTagColor());
                image1.setVisibility(View.VISIBLE);
                Tags tag2temp = TagList.getItem(taskList.get(pos).getListTags(1));
                image2.setColorFilter(tag2temp.getTagColor());
                image2.setVisibility(View.VISIBLE);
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

                image1.setVisibility(View.VISIBLE);
                image2.setVisibility(View.VISIBLE);
                image3.setVisibility(View.VISIBLE);
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
                image1.setVisibility(View.VISIBLE);
                image2.setVisibility(View.VISIBLE);
                image3.setVisibility(View.VISIBLE);
                image4.setVisibility(View.VISIBLE);
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

                image1.setVisibility(View.VISIBLE);
                image2.setVisibility(View.VISIBLE);
                image3.setVisibility(View.VISIBLE);
                image4.setVisibility(View.VISIBLE);
                image5.setVisibility(View.VISIBLE);
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

                image1.setVisibility(View.VISIBLE);
                image2.setVisibility(View.VISIBLE);
                image3.setVisibility(View.VISIBLE);
                image4.setVisibility(View.VISIBLE);
                image5.setVisibility(View.VISIBLE);

                moreTags.setVisibility(View.VISIBLE);
                if (SettingsActivity.darkTheme)
                    moreTags.setColorFilter(Color.WHITE);


            }*/

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
/*                if (SettingsActivity.darkTheme)
                    moreTags.setColorFilter(Color.WHITE);*/


            }

        } else {
            Log.d("inboxFilterAdapter", Integer.toString(taskList.size()));
            Log.d("items", "singleItem");
            layout.setVisibility(View.GONE);
        }
    }

}
