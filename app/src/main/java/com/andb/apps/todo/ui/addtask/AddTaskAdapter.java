package com.andb.apps.todo.ui.addtask;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.andb.apps.todo.R;
import com.andb.apps.todo.utilities.Utilities;
import com.jaredrummler.cyanea.Cyanea;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class AddTaskAdapter extends RecyclerView.Adapter<AddTaskAdapter.MyViewHolder>{

    public List<String> itemList;


    public boolean focused;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView remove;
        EditText editText;
        LinearLayout bg;
        RVEditTextListener rvEditTextListener;


        public MyViewHolder(View view, RVEditTextListener rvEditTextListener) {
            super(view);
            remove = view.findViewById(R.id.removeListItem);
            editText = view.findViewById(R.id.taskItemEditText);
            bg = view.findViewById(R.id.addTaskListItem);
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


        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.add_task_list_item, parent, false);

        return new MyViewHolder(itemView, new RVEditTextListener());
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        if (focused) {
            holder.editText.requestFocus();
            focused = false;
        }

        holder.rvEditTextListener.setPosition(position);
        holder.editText.setText(itemList.get(position));
        holder.bg.setBackgroundColor(Utilities.lighterDarker(Cyanea.getInstance().getBackgroundColor(), 1.2f));
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
