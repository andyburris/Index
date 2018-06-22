package com.andb.apps.todo;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AddTaskTagAdapter extends RecyclerView.Adapter<AddTaskTagAdapter.MyViewHolder> {

    public static List<Integer> tagList = new ArrayList<>();
    public static boolean edit;
    public static int taskPosition;
    ArrayList<Tasks> taskList = new ArrayList<>();

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView tagColor;


        public MyViewHolder(View view) {
            super(view);

            tagColor = (ImageView) view.findViewById(R.id.tagImage);

            tagColor.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getLayoutPosition();

                    Toast.makeText(v.getContext(), position + " removed", Toast.LENGTH_SHORT).show();
                    tagList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, tagList.size());
                    //mRecyclerView.scrollToPosition(position);
                    //mAdapter.notifyDataSetChanged();
                }
            });

        }


    }


    public AddTaskTagAdapter(ArrayList<Tasks> taskList, boolean edit, int taskPosition) {
        this.taskList = taskList;
        this.edit = edit;
        this.taskPosition = taskPosition;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        /*//Indicates whether each item in the data set can be represented with a unique identifier
        setHasStableIds(true);*/


        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inbox_tag_list_item, parent, false);
        //itemView.startAnimation(AnimationUtils.loadAnimation(parent.getContext(), android.R.anim.slide_in_left));

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {

        if (edit) {
            if (position < taskList.get(taskPosition).getListTagsSize()) {
                holder.tagColor.setColorFilter(TagList.getItem(tagList.get(position)).getTagColor());

            }
        } else {
            holder.tagColor.setColorFilter(TagList.getItem(tagList.get(position)).getTagColor());
        }
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
