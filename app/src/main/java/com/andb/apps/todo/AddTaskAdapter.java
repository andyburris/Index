package com.andb.apps.todo;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.andb.apps.todo.settings.SettingsActivity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class AddTaskAdapter extends RecyclerView.Adapter<AddTaskAdapter.MyViewHolder>{

    public List<String> itemList;


    public boolean focused;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView remove;
        EditText editText;
        RVEditTextListener rvEditTextListener;


        public MyViewHolder(View view, RVEditTextListener rvEditTextListener) {
            super(view);
            remove = view.findViewById(R.id.removeListItem);
            editText = view.findViewById(R.id.taskItemEditText);
            this.rvEditTextListener = rvEditTextListener;

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    itemList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, itemList.size());
                }
            });

            editText.addTextChangedListener(rvEditTextListener);

        }


    }


    public AddTaskAdapter(List<String> tasksList) {
        this.itemList = tasksList;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {


        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_list_item, parent, false);

        return new MyViewHolder(itemView, new RVEditTextListener());
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        /* final int realPosition = holder.getAdapterPosition();

         if(!holder.editText.getText().equals(itemList.get(realPosition))){
                holder.editText.setText(itemList.get(realPosition));
            }

            holder.editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d("textWatcherChanged", Integer.toString(realPosition));
                    if(realPosition<itemList.size()) {//fixes bug with trying to update on kill
                        itemList.set(realPosition, s.toString());
                        Log.d("textWatcherChanged", itemList.get(realPosition));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });*/

        if (focused) {
            holder.editText.requestFocus();
            focused = false;
        }

        holder.rvEditTextListener.setPosition(position);
        holder.editText.setText(itemList.get(position));

    }

    private class RVEditTextListener implements TextWatcher{

        int position = 0;

        public void setPosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            itemList.set(position, s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }




    @Override
    public int getItemViewType(final int position) {
        return 0;

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

}
