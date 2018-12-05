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

    public List<String> taskList = new ArrayList<>();
    public boolean edit;
    public int taskPosition;

    public boolean focused;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView remove;
        public EditText editText;


        public MyViewHolder(View view) {
            super(view);

            remove = (ImageView) view.findViewById(R.id.removeListItem);
            //taskColor = (ImageView) view.findViewById(R.id.taskIcon);
            editText = (EditText) view.findViewById(R.id.taskItemEditText);

            remove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();

                    android.widget.Toast.makeText(v.getContext(), position + " removed", Toast.LENGTH_SHORT).show();
                    editText.getText().clear();
                    taskList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, taskList.size());
                    //mRecyclerView.scrollToPosition(position);
                    //mAdapter.notifyDataSetChanged();
                }
            });

        }


    }


    public AddTaskAdapter(List<String> tasksList, boolean edit, int taskPosition) {
        this.taskList = tasksList;
        this.edit = edit;
        this.taskPosition = taskPosition;
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        /*//Indicates whether each item in the data set can be represented with a unique identifier
        setHasStableIds(true);*/

        Log.d("footer", Integer.toString(viewType));

        View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.task_list_item, parent, false);

        //itemView.startAnimation(AnimationUtils.loadAnimation(parent.getContext(), android.R.anim.slide_in_left));

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final int realPosition = holder.getAdapterPosition();

            if(!holder.editText.getText().equals(taskList.get(realPosition))){
                holder.editText.setText(taskList.get(realPosition));
            }

            setInputTextLayoutColor(SettingsActivity.themeColor, holder.editText);

            holder.editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d("textWatcherChanged", Integer.toString(realPosition));
                    if(realPosition<taskList.size()) {//fixes bug with trying to update on kill
                        taskList.set(realPosition, s.toString());
                        Log.d("textWatcherChanged", taskList.get(realPosition));
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

        if (focused) {
            holder.editText.requestFocus();
            focused = false;
        }

        //holder.taskColor.setColorFilter(task.getListColor());
        //to-do: get tasks
    }


    private void setInputTextLayoutColor(final int color, final EditText editText) {

        editText.getBackground().setColorFilter(0xFF757575, PorterDuff.Mode.SRC_IN);


        try {
            // Get the cursor resource id
            Field field = TextView.class.getDeclaredField("mCursorDrawableRes");
            field.setAccessible(true);
            int drawableResId = field.getInt(editText);

            // Get the editor
            field = TextView.class.getDeclaredField("mEditor");
            field.setAccessible(true);
            Object editor = field.get(editText);

            // Get the drawable and set a color filter
            Drawable drawable = ContextCompat.getDrawable(editText.getContext(), drawableResId);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            Drawable[] drawables = {drawable, drawable};

            // Set the drawables
            field = editor.getClass().getDeclaredField("mCursorDrawable");
            field.setAccessible(true);
            field.set(editor, drawables);
        } catch (Exception ignored) {
        }



        editText.setHighlightColor(color);



        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    editText.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                }
                else {
                    editText.getBackground().setColorFilter(0xFF757575, PorterDuff.Mode.SRC_IN);
                }
            }
        });



    }


    @Override
    public int getItemViewType(final int position) {
        //return position;
        return 0;

    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

}
