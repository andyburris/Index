package com.andb.apps.todo;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.transition.ChangeBounds;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andb.apps.todo.lists.ArchiveTaskList;
import com.andb.apps.todo.lists.TaskList;
import com.andb.apps.todo.lists.interfaces.TaskListInterface;
import com.andb.apps.todo.objects.Tasks;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link InboxFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link InboxFragment#newInstance} factory method to
 * create an instance of this fragment.
 */


public class ArchiveFragment extends Fragment {


    public static ArrayList<Tasks> blankTaskList = new ArrayList<>();


    private RecyclerView mRecyclerView;
    private static TaskAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private OnFragmentInteractionListener mListener;

    public ArchiveFragment() {
        // Required empty public constructor
    }


    //public static InboxFragment newInstance(String param1, String param2) {
    public static ArchiveFragment newInstance() {
        ArchiveFragment fragment = new ArchiveFragment();
        Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if (getArguments() != null) {
        //mParam1 = getArguments().getString(ARG_PARAM1);
        //mParam2 = getArguments().getString(ARG_PARAM2);
        //}

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        Log.d("inflating", "inbox inflating");
        View view = inflater.inflate(R.layout.fragment_archive, container, false);
        prepareRecyclerView(view);
        //testEntries();
        return view;


    }





    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }


    public void prepareRecyclerView(View view){
        Log.d("recycler", "preparing archive rView");
        mRecyclerView = (RecyclerView) view.findViewById(R.id.archiveRecycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new TaskAdapter(ArchiveTaskList.taskList, TaskAdapter.FROM_ARCHIVE);
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

            if(direction == ItemTouchHelper.RIGHT){//restore
                TaskList.addTaskList(ArchiveTaskList.taskList.get(viewHolder.getAdapterPosition()));
                ArchiveTaskList.taskList.remove(viewHolder.getAdapterPosition());
                mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                EventBus.getDefault().post(new UpdateEvent(true));
            }else if (direction == ItemTouchHelper.LEFT){//delete permanently
                ArchiveTaskList.taskList.remove(viewHolder.getAdapterPosition());
                mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {


            if(dX>0) { //restore
                Drawable deleteIcon = ContextCompat.getDrawable(getActivity(), R.drawable.ic_move_to_inbox_black_24dp).mutate();
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
            }else { //delete
                Drawable deleteIcon = ContextCompat.getDrawable(getActivity(), R.drawable.ic_delete_black_24dp).mutate();
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
