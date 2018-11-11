package com.andb.apps.todo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.MyViewHolder> {

    public static List<Tasks> checkList = new ArrayList<>();

    private RecyclerView iRecyclerView;
    private static ChecklistAdapter iAdapter;
    private RecyclerView.LayoutManager iLayoutManager;
    public View itemView;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CheckBox name;
        public ImageView taskColor;


        public MyViewHolder(View view) {
            super(view);
            name = (CheckBox) view.findViewById(R.id.listTextView);
            Log.d("initialized", /*checkList.get(1).toString()+*/"checklist called");

            //taskColor = (ImageView) view.findViewById(R.id.taskIcon);

        }


    }


    public ChecklistAdapter(List<Tasks> tasksList) {
        this.checkList = tasksList;

    }



    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        /*//Indicates whether each item in the data set can be represented with a unique identifier
        setHasStableIds(true);*/


        itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inbox_checklist_list_item, parent, false);
        Log.d("inflated", "checklist inflated");
        //itemView.startAnimation(AnimationUtils.loadAnimation(parent.getContext(), android.R.anim.slide_in_left));

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.name.setText(checkList.get(position).getListTags(position));
        //holder.taskColor.setColorFilter(task.getListColor());
        //to-do: get tasks
    }

    @Override
    public int getItemViewType(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return checkList.size();
    }



}
