package com.andb.apps.todo;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
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
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import me.saket.inboxrecyclerview.InboxRecyclerView;
import me.saket.inboxrecyclerview.page.ExpandablePageLayout;

public class InboxAdapter extends InboxRecyclerView.Adapter<InboxAdapter.MyViewHolder> {

    public List<Tasks> taskList = new ArrayList<>();

    private Context context;
    private ActionMode actionMode;

    private boolean overdue = true;
    private boolean today = true;
    private boolean thisWeek = true;
    private boolean thisMonth = true;
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
        public ConstraintLayout tag6;
        public LinearLayout tagEncloser;
        public ImageView moreTags;
        public ImageView moreTagsHorizontal;
        public ConstraintLayout timeLayout;
        public TextView dateText;
        public TextView timeText;
        public ImageView timeIcon;
        public CardView inboxCard;
        public ExpandablePageLayout taskView;

        public View divider2;

        public TextView dividerName;

        public ConstraintLayout inboxListItemBackground;
        public NestedScrollView scrollView;

        public MyViewHolder(View view) {
            super(view);

            divider2 = (View) view.findViewById(R.id.timeDivider);

            dividerName = (TextView) view.findViewById(R.id.dividerName);

            name = (TextView) view.findViewById(R.id.listTextView);
            item1 = (ConstraintLayout) view.findViewById(R.id.item1);
            item2 = (ConstraintLayout) view.findViewById(R.id.item2);
            item3 = (ConstraintLayout) view.findViewById(R.id.item3);
            tag1 = (ConstraintLayout) view.findViewById(R.id.tag1);
            tag2 = (ConstraintLayout) view.findViewById(R.id.tag2);
            tag3 = (ConstraintLayout) view.findViewById(R.id.tag3);
            tag4 = (ConstraintLayout) view.findViewById(R.id.tag4);
            tag5 = (ConstraintLayout) view.findViewById(R.id.tag5);
            tag6 = (ConstraintLayout) view.findViewById(R.id.tag6);
            moreTags = (ImageView) view.findViewById(R.id.tagMore);
            moreTagsHorizontal = (ImageView) view.findViewById(R.id.tagMoreHorizontal);

            dateText = (TextView) view.findViewById(R.id.dateInboxText);
            timeText = (TextView) view.findViewById(R.id.timeInboxText);
            inboxCard = (CardView) view.findViewById(R.id.inboxCard);

            encloser = (LinearLayout) view.findViewById(R.id.checklistEncloser);
            tagEncloser = (LinearLayout) view.findViewById(R.id.tagOnTaskLayout);
            timeLayout = (ConstraintLayout) view.findViewById(R.id.dateTimeInboxLayout);
            timeIcon = (ImageView) view.findViewById(R.id.timeIcon);
            more = (ImageView) view.findViewById(R.id.itemsMore);

            inboxListItemBackground = (ConstraintLayout) view.findViewById(R.id.inboxListBackground);
            scrollView = InboxFragment.mRecyclerView.getRootView().findViewById(R.id.inboxScrollView);
            taskView = InboxFragment.mRecyclerView.getRootView().findViewById(R.id.expandable_page);

            Log.d("expandablePageView", Boolean.toString(taskView == null));

            //this.setIsRecyclable(false);


            //taskColor = (ImageView) view.findViewById(R.id.taskIcon);

        }


    }


    public InboxAdapter(List<Tasks> tasksList) {
        this.taskList = tasksList;
    }


    @Override
    public MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        /*//Indicates whether each item in the data set can be represented with a unique identifier
        setHasStableIds(true);*/

        context = parent.getContext();
        View itemView;


        if (viewType == 0) {
            itemView = LayoutInflater.from(context)
                    .inflate(R.layout.inbox_list_item, parent, false);
            Log.d("viewType", Integer.toString(viewType));
            //this.viewType = viewType;
        } else {
            itemView = LayoutInflater.from(context)
                    .inflate(R.layout.inbox_divider, parent, false);
            Log.d("viewType", Integer.toString(viewType));
            //this.viewType = viewType;
        }

        return new MyViewHolder(itemView);

    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final int realPosition = holder.getLayoutPosition();

        setUpByViewType(position, holder, realPosition);

        Log.d("onePosUpError", Integer.toString(realPosition));


        if (isSelected) {
            holder.inboxListItemBackground.setBackgroundColor(Color.GRAY);
        } else if (viewType == TASK_VIEW_ITEM) {
            if (SettingsActivity.darkTheme) {
                holder.inboxListItemBackground.setBackgroundColor(0xFF424242);
            } else {
                holder.inboxListItemBackground.setBackgroundColor(Color.WHITE);
            }
        }


    }

    public void setUpByViewType(final int position, final MyViewHolder holder, final int realPosition) {


        if (viewType == 0) {
            holder.inboxListItemBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    int[] l = new int[2];
                    holder.inboxCard.getLocationOnScreen(l);
                    Rect rect = new Rect(l[0], l[1], holder.inboxListItemBackground.getWidth(), holder.inboxListItemBackground.getHeight());
                    /*
                    ViewGroup.LayoutParams params = holder.inboxCard.getLayoutParams();
                    WindowManager manager = (WindowManager) holder.itemView.getContext().getSystemService(Context.WINDOW_SERVICE);
                    Display display = manager.getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    params.height = size.y;
                    TransitionManager.beginDelayedTransition(holder.inboxCard, new TransitionSet()
                            .addTransition(new ChangeBounds()));
                    holder.scrollView.smoothScrollBy(0, l[1]);
                    holder.inboxCard.setLayoutParams(params);
                    */




                    /*int pos = holder.getLayoutPosition();
                    Intent intent = new Intent(view.getContext(), TaskView.class);
                    intent.putExtra("pos", pos);
                    intent.putExtra("inboxOrArchive", true);
                    intent.putExtra("browse", false);
                    intent.putExtra("rect", rect);
                    Log.d("onePosUpError", Integer.toString(pos));
                    view.getContext().startActivity(intent);*/

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
                    //InboxFragment.mRecyclerView.setExpandedItem(new InboxRecyclerView.ExpandedItem(realPosition, getItemId(realPosition), rect));


                    for (int i = 0; i < InboxFragment.mAdapter.getItemCount(); i++) {
                        if (getItemId(realPosition) == InboxFragment.mAdapter.getItemId(i)) {
                            Log.d("expandLocationTest", "Clicked: " + realPosition + ", Got: " + i);
                            Log.d("expandLocationTest", Integer.toString(InboxFragment.mRecyclerView.getExpandedItem().getViewIndex()));

                        }
                    }

                }
            });



            /*if (taskList.get(realPosition).getListItemsSize() > 3) {
                holder.more.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = holder.getLayoutPosition();
                        Intent intent = new Intent(v.getContext(), TaskView.class);
                        intent.putExtra("pos", pos);
                        intent.putExtra("inboxOrArchive", true);
                        intent.putExtra("browse", false);
                        Log.d("onePosUpError", Integer.toString(pos));
                        v.getContext().startActivity(intent);
                    }
                });
            }
            if (taskList.get(realPosition).getListTagsSize() > 5) {
                holder.moreTags.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
            }*/

            Log.d("errorSetText", taskList.get(realPosition).getListName());

            holder.name.setText(taskList.get(position).getListName());
            if (SettingsActivity.darkTheme)
                holder.name.setTextColor(Color.WHITE);


            if (taskList.get(position).isListTime()) {
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
            }

            if (!taskList.get(position).isListTags()) {
                //holder.divider1.setVisibility(View.GONE);
            }

            final int adapterPosition = holder.getAdapterPosition();
            Log.d("adapterPosition", Integer.toString(adapterPosition));




            setTasks(realPosition, holder.item1, holder.item2, holder.item3, holder.more, holder.encloser);
            setTags(realPosition, holder.tag1, holder.tag2, holder.tag3, holder.tag4, holder.tag5, holder.tag6, holder.moreTags, holder.moreTagsHorizontal, holder.tagEncloser);
        } else {
            Log.d("adapterPosition", Integer.toString(realPosition));

            if (realPosition == 0) {
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


            layout.setVisibility(View.VISIBLE);


        } else {
            Log.d("items", taskList.get(pos).getListName() + ", singleItem");
            check1.setVisibility(View.GONE);
            check2.setVisibility(View.GONE);
            check3.setVisibility(View.GONE);

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

    private void setTags(int pos, ConstraintLayout tag1, ConstraintLayout tag2, ConstraintLayout tag3, ConstraintLayout tag4, ConstraintLayout tag5, ConstraintLayout tag6, ImageView moreTags, ImageView moreTagsHorizontal, LinearLayout layout) {
        ImageView image1 = (ImageView) tag1.findViewById(R.id.tagImage);
        ImageView image2 = (ImageView) tag2.findViewById(R.id.tagImage);
        ImageView image3 = (ImageView) tag3.findViewById(R.id.tagImage);
        ImageView image4 = (ImageView) tag4.findViewById(R.id.tagImage);
        ImageView image5 = (ImageView) tag5.findViewById(R.id.tagImage);
        ImageView image6 = (ImageView) tag6.findViewById(R.id.tagImage);

        TextView text1 = (TextView) tag1.findViewById(R.id.inbox_item_tag_name);
        TextView text2 = (TextView) tag2.findViewById(R.id.inbox_item_tag_name);
        TextView text3 = (TextView) tag3.findViewById(R.id.inbox_item_tag_name);
        TextView text4 = (TextView) tag4.findViewById(R.id.inbox_item_tag_name);
        TextView text5 = (TextView) tag5.findViewById(R.id.inbox_item_tag_name);
        TextView text6 = (TextView) tag6.findViewById(R.id.inbox_item_tag_name);


        if (taskList.get(pos).isListTags()) {
            Log.d("tags", "multipleTags");


            ArrayList<ImageView> tagImages = new ArrayList<>(Arrays.asList(image1, image2, image3, image4, image5, image6));
            ArrayList<TextView> tagNames = new ArrayList<>(Arrays.asList(text1, text2, text3, text4, text5, text6));

            int tagsToDisplay = 2;

            for (int i = 0; i < 2; i++) {
                if (i < taskList.get(pos).getListItemsSize()) {
                    tagsToDisplay = tagsToDisplay + 2;
                }
            }


            /*tagImages = new ArrayList<>(tagImages.subList(0, tagsToDisplay));
            tagNames = new ArrayList<>(tagNames.subList(0, tagsToDisplay));

            Log.d("tagDisplaySize", Integer.toString(tagImages.size()));*/


            for (int i = 0; i < tagImages.size(); i++) {


                if (i < taskList.get(pos).getAllListTags().size() && i < tagsToDisplay) {
                    Tags tagtemp = TagList.getItem(taskList.get(pos).getListTags(i));
                    tagImages.get(i).setColorFilter(tagtemp.getTagColor());
                    tagImages.get(i).setVisibility(View.VISIBLE);
                    tagNames.get(i).setText(TagList.getItem(taskList.get(pos).getListTags(i)).getTagName());
                    tagNames.get(i).setVisibility(View.VISIBLE);
                } else {
                    tagImages.get(i).setVisibility(View.GONE);
                    tagNames.get(i).setVisibility(View.GONE);
                }

            }

            if (taskList.get(pos).getListTagsSize() > tagsToDisplay) {
                if (tagsToDisplay == 2) {
                    moreTagsHorizontal.setVisibility(View.VISIBLE);
                    moreTags.setVisibility(View.GONE);
                } else {
                    moreTagsHorizontal.setVisibility(View.GONE);
                    moreTags.setVisibility(View.VISIBLE);
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
