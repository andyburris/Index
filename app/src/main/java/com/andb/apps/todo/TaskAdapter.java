package com.andb.apps.todo;

import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.andb.apps.todo.objects.Tasks;
import com.andb.apps.todo.utilities.Utilities;
import com.andb.apps.todo.views.TaskListItem;
import com.jaredrummler.cyanea.Cyanea;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import me.saket.inboxrecyclerview.InboxRecyclerView;

public class TaskAdapter extends InboxRecyclerView.Adapter<TaskAdapter.MyViewHolder> {

    public List<Tasks> taskList;
    public ArrayList<Boolean> expandedList;


    public int selected = -1;

    private int viewType = 0;

    private static final int TASK_VIEW_ITEM = 0;
    private static final int OVERDUE_DIVIDER = 1;
    private static final int TODAY_DIVIDER = 2;
    private static final int THIS_WEEK_DIVIDER = 3;
    private static final int THIS_MONTH_DIVIDER = 4;
    private static final int FUTURE_DIVIDER = 5;

    public static final int FROM_INBOX = 0;
    public static final int FROM_BROWSE = 1;
    public static final int FROM_ARCHIVE = 2;
    //Preferences for which to show

    public int inboxBrowseArchive;

    public class MyViewHolder extends RecyclerView.ViewHolder {



        TextView dividerName;


        public MyViewHolder(View view) {
            super(view);
            dividerName = view.findViewById(R.id.dividerName);
        }


    }


    public TaskAdapter(List<Tasks> tasksList, int inboxBrowseArchive) {
        this.inboxBrowseArchive = inboxBrowseArchive;
        this.taskList = tasksList;

    }


    @Override
    public TaskAdapter.MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        View itemView;


        if (viewType == 0) {
            itemView = new TaskListItem(parent.getContext());
        } else {
            itemView = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.inbox_divider, parent, false);
        }

        return new TaskAdapter.MyViewHolder(itemView);

    }


    @Override
    public void onBindViewHolder(final TaskAdapter.MyViewHolder holder, int position) {
        final int realPosition = holder.getLayoutPosition();

        setUpByViewType(position, holder, realPosition);

        Log.d("onePosUpError", Integer.toString(realPosition));


    }

    private void setUpByViewType(final int position, final TaskAdapter.MyViewHolder holder, final int realPosition) {


        if (viewType == 0) {
            TaskListItem taskListItem = (TaskListItem) holder.itemView;
            taskListItem.setup(taskList.get(position), realPosition, inboxBrowseArchive);
            if(position==selected) {
                taskListItem.setCyaneaBackground(Utilities.desaturate(Utilities.lighterDarker(Cyanea.getInstance().getBackgroundColor(), 0.8f), 0.7));
                //TODO: lighter color on dark theme
            }

        } else { //divider logic
            Log.d("roomViewType", Boolean.toString(holder.itemView instanceof TaskListItem));

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
        if (InboxFragment.Companion.getFilterMode() == 0) {
            if (taskList.get(position).getListName().equals("OVERDUE")) {
                viewType = OVERDUE_DIVIDER;
                return OVERDUE_DIVIDER;
            }
            if (taskList.get(position).getListName().equals("TODAY")) {
                viewType = TODAY_DIVIDER;
                return TODAY_DIVIDER;
            }
            if (taskList.get(position).getListName().equals("WEEK")) {
                viewType = THIS_WEEK_DIVIDER;
                return THIS_WEEK_DIVIDER;
            }
            if (taskList.get(position).getListName().equals("MONTH")) {
                viewType = THIS_MONTH_DIVIDER;
                return THIS_MONTH_DIVIDER;
            }
            if (taskList.get(position).getListName().equals("FUTURE")) {
                viewType = FUTURE_DIVIDER;
                return FUTURE_DIVIDER;
            } else {
                viewType = TASK_VIEW_ITEM;
                return TASK_VIEW_ITEM;
            }


        } else {

            viewType = TASK_VIEW_ITEM;
            return TASK_VIEW_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return taskList.size();
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
