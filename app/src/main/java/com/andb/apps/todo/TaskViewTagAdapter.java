package com.andb.apps.todo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andb.apps.todo.lists.TagList;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class TaskViewTagAdapter extends RecyclerView.Adapter<TaskViewTagAdapter.MyViewHolder> {

    public static List<Integer> tagList = new ArrayList<>();



    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView tagColor;
        private TextView tagName;


        public MyViewHolder(View view) {
            super(view);

            tagColor = (ImageView) view.findViewById(R.id.tagImage);
            tagName = view.findViewById(R.id.task_view_item_tag_name);
        }


    }


    public TaskViewTagAdapter(List<Integer> tasksList) {
        this.tagList = tasksList;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        /*//Indicates whether each item in the data set can be represented with a unique identifier
        setHasStableIds(true);*/


        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_view_tag_list_item, parent, false);
        //itemView.startAnimation(AnimationUtils.loadAnimation(parent.getContext(), android.R.anim.slide_in_left));

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

            holder.tagColor.setColorFilter(TagList.getItem(tagList.get(position)).getTagColor());
        holder.tagName.setText(TagList.getItem(tagList.get(position)).getTagName());

    }


    @Override
    public int getItemViewType(final int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

}
