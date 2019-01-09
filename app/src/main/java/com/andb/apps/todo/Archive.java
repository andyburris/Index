package com.andb.apps.todo;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.andb.apps.todo.eventbus.UpdateEvent;
import com.andb.apps.todo.objects.Project;
import com.andb.apps.todo.utilities.Current;
import com.andb.apps.todo.utilities.ProjectsUtils;
import com.jaredrummler.cyanea.Cyanea;

import org.greenrobot.eventbus.EventBus;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import me.saket.inboxrecyclerview.PullCollapsibleActivity;

public class Archive extends PullCollapsibleActivity {

    public static RecyclerView mRecyclerView;
    public static TaskAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setTheme(R.style.AppThemeLightCollapse);

        setContentView(R.layout.activity_archive);
        if(getIntent().hasExtra("expandRect")){
            Rect expandRect = Rect.unflattenFromString(getIntent().getExtras().getString("expandRect"));
            if(expandRect != null) {
                expandFrom(expandRect);
            }else{
                expandFromTop();
            }
        }else {
            expandFromTop();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_clear_black_24dp);
        setSupportActionBar(toolbar);

        (findViewById(R.id.archiveRecycler)).setBackgroundColor(Cyanea.getInstance().getBackgroundColor());
        prepareRecyclerView();
    }


    public void prepareRecyclerView() {
        Log.d("recycler", "preparing archive rView");
        mRecyclerView = (RecyclerView) findViewById(R.id.archiveRecycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new TaskAdapter(Current.archiveTaskList(), TaskAdapter.FROM_ARCHIVE);
        mRecyclerView.setAdapter(mAdapter);

        ItemTouchHelper ith = new ItemTouchHelper(_ithCallback);
        ith.attachToRecyclerView(mRecyclerView);

    }

    // Extend the Callback class
    private ItemTouchHelper.Callback _ithCallback = new ItemTouchHelper.Callback() {
        //and in your implementation of
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            Log.d("swipeAction", "swiped");

            if (direction == ItemTouchHelper.RIGHT) {//restore
                Current.taskList().add(Current.project().getArchiveList().get(viewHolder.getAdapterPosition()));
                Current.project().getArchiveList().remove(viewHolder.getAdapterPosition());
                mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                EventBus.getDefault().post(new UpdateEvent(true));
                ProjectsUtils.update();
            } else if (direction == ItemTouchHelper.LEFT) {//delete permanently
                Current.project().getArchiveList().remove(viewHolder.getAdapterPosition());
                mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                ProjectsUtils.update();
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {


            if (dX > 0) { //restore
                Drawable deleteIcon = getDrawable(R.drawable.ic_move_to_inbox_black_24dp).mutate();
                deleteIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                int intrinsicWidth = deleteIcon.getIntrinsicWidth();
                int intrinsicHeight = deleteIcon.getIntrinsicHeight();
                GradientDrawable background = new GradientDrawable();
                int backgroundColor = Color.parseColor("#1B7D1B");


                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getBottom() - itemView.getTop();

                // Draw the green delete background
                background.setColor(backgroundColor);
                background.setBounds(
                        itemView.getLeft(),
                        itemView.getTop(),
                        itemView.getRight(),
                        itemView.getBottom()
                );

                //background.setCornerRadius(0);


                background.draw(c);

                // Calculate position of delete icon
                int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int deleteIconLeft = itemView.getLeft() + intrinsicWidth;
                int deleteIconRight = itemView.getLeft() + intrinsicWidth * 2;
                int deleteIconBottom = deleteIconTop + intrinsicHeight;
                // Draw the delete icon
                deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                deleteIcon.draw(c);

                float newDx = (dX * 9) / 10;
                if (newDx >= 300f) {
                    newDx = 300f;
                }

                super.onChildDraw(c, recyclerView, viewHolder, newDx, dY, actionState, isCurrentlyActive);
            } else { //delete
                Drawable deleteIcon = getDrawable(R.drawable.ic_delete_black_24dp).mutate();
                deleteIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                int intrinsicWidth = deleteIcon.getIntrinsicWidth();
                int intrinsicHeight = deleteIcon.getIntrinsicHeight();
                GradientDrawable background = new GradientDrawable();
                int backgroundColor = Color.parseColor("#D93025");


                View itemView = viewHolder.itemView;
                int itemHeight = itemView.getBottom() - itemView.getTop();

                // Draw the green delete background
                background.setColor(backgroundColor);
                background.setBounds(
                        itemView.getLeft(),
                        itemView.getTop(),
                        itemView.getRight(),
                        itemView.getBottom()
                );

                //background.setCornerRadius(0);


                background.draw(c);

                // Calculate position of delete icon
                int deleteIconTop = itemView.getTop() + (itemHeight - intrinsicHeight) / 2;
                int deleteIconLeft = itemView.getRight() - intrinsicWidth * 2;
                int deleteIconRight = itemView.getRight() - intrinsicWidth;
                int deleteIconBottom = deleteIconTop + intrinsicHeight;
                // Draw the delete icon
                deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                deleteIcon.draw(c);

                float newDx = (dX * 9) / 10;
                if (newDx >= 300f) {
                    newDx = 300f;
                }

                super.onChildDraw(c, recyclerView, viewHolder, newDx, dY, actionState, isCurrentlyActive);
            }
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        //defines the enabled move directions in each state (idle, swiping, dragging).
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_SWIPE,
                    ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        }
    };



}
