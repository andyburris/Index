package com.andb.apps.todo;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class TaskViewAdapter extends RecyclerView.Adapter<TaskViewAdapter.MyViewHolder> {

    private List<String> taskList;
    private int parentTask;
    private List<Tasks> parentList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CheckBox box;
        //public ImageView tagColor;


        public MyViewHolder(View view) {
            super(view);
            box = (CheckBox) view.findViewById(R.id.listTextView);
            //tagColor = (ImageView) view.findViewById(R.id.tagIcon);

        }


    }


    public TaskViewAdapter(List<String> taskList, int pos, List<Tasks> parentTasks) {
        this.taskList = taskList;
        this.parentTask = pos;
        this.parentList = parentTasks;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        /*//Indicates whether each item in the data set can be represented with a unique identifier
        setHasStableIds(true);*/


        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inbox_checklist_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final int realPosition = holder.getAdapterPosition();
        String task = taskList.get(realPosition);
        boolean checked = parentList.get(parentTask).getListItemsChecked(realPosition);
        holder.box.setChecked(checked);
        holder.box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                parentList.get(parentTask).editListItemsChecked(isChecked, realPosition);

                if(holder.box.isChecked())
                    holder.box.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
                else
                    holder.box.setButtonTintList(ColorStateList.valueOf(0xFF757575));

            }
        });
        if(holder.box.isChecked())
            holder.box.setButtonTintList(ColorStateList.valueOf(SettingsActivity.themeColor));
        else
            holder.box.setButtonTintList(ColorStateList.valueOf(0xFF757575));


        holder.box.setText(task);
        //holder.tagColor.setColorFilter(task.getTagColor());
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }



}