package com.andb.apps.todo;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.MyViewHolder> {

    private List<Tags> tagList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public ImageView tagColor;


        public MyViewHolder(View view) {
            super(view);
            name = (TextView) view.findViewById(R.id.tagname);
            tagColor = (ImageView) view.findViewById(R.id.tagIcon);

        }


    }


    public TagAdapter(List<Tags> tagsList) {
        this.tagList = tagsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        /*//Indicates whether each item in the data set can be represented with a unique identifier
        setHasStableIds(true);*/

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.tag_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Tags tag = tagList.get(position);
        holder.name.setText(tag.getTagName());
        holder.tagColor.setColorFilter(tag.getTagColor());
        Log.d("color", Integer.toString(tag.getTagColor()));

    }

    @Override
    public int getItemCount() {
        return tagList.size();
    }

}