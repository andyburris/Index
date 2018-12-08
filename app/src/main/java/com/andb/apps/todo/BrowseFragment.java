package com.andb.apps.todo;

import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.andb.apps.todo.settings.SettingsActivity;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

import java.util.ArrayList;

import androidx.cardview.widget.CardView;
import androidx.core.view.ViewCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.ChangeBounds;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;
import me.saket.inboxrecyclerview.InboxRecyclerView;
import me.saket.inboxrecyclerview.page.ExpandablePageLayout;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link BrowseFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link BrowseFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BrowseFragment extends Fragment {
    public static ArrayList<TagLinks> blankTagLinkList = new ArrayList<>();

    public static ArrayList<Integer> filteredTagLinks = new ArrayList<>();
    public static ArrayList<Tasks> filteredTaskList = new ArrayList<>();


    public static InboxRecyclerView mRecyclerView;
    public static TaskAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private RecyclerView tRecyclerView;
    public static BrowseTagAdapter tAdapter;
    private RecyclerView.LayoutManager tLayoutManager;

    private ActionMode contextualToolbar;
    public boolean selected = false;

    public boolean tagCollapsed = false;
    public static CardView tagCard;

    public static NestedScrollView nestedScrollView;

    public static boolean removing;

    public static boolean fromAdapter = false;


    private BrowseFragment.OnFragmentInteractionListener mListener;

    public BrowseFragment() {
        // Required empty public constructor
    }

    public static BrowseFragment newInstance() {
        BrowseFragment fragment = new BrowseFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        Log.d("inflating", "inbox inflating");
        View view = inflater.inflate(R.layout.fragment_browse, container, false);
        prepareRecyclerView(view);


        //EventBus.getDefault().post(new UpdateEvent(true));//all things needed for this to run have been loaded

        tagCard = (CardView) view.findViewById(R.id.browseTagCardHolder);
        prepareTagCollapse(view);
        prepareTagAdd(view);

        nestedScrollView = (NestedScrollView) view.findViewById(R.id.browseScrollView);





        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {

            @Override
            public void onClick(View view, int position) {

            }

            @Override
            public void onLongClick(View view, int position) {
                if (mAdapter.getItemViewType(position) == 1 & !selected) {
                    contextualToolbar = BrowseFragment.this.getActivity().startActionMode(setCallback(position));
                    view.setSelected(true);
                    mAdapter.isSelected = true;
                    mAdapter.notifyItemChanged(position);
                    selected = true;
                }
            }
        }) {
        });


        return view;


    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

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


    public void prepareRecyclerView(View view) {


        mRecyclerView = view.findViewById(R.id.browseTaskRecycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new TaskAdapter(filteredTaskList, TaskAdapter.FROM_BROWSE);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);


        final ExpandablePageLayout taskView = view.findViewById(R.id.expandable_page_browse);
        mRecyclerView.setExpandablePage(taskView);

        ViewCompat.setNestedScrollingEnabled(mRecyclerView, false);


        tRecyclerView = (RecyclerView) view.findViewById(R.id.browseTagRecycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        tLayoutManager = new LinearLayoutManager(view.getContext());
        tRecyclerView.setLayoutManager(tLayoutManager);

        // specify an adapter (see also next example)
        tAdapter = new BrowseTagAdapter(filteredTagLinks);

        tRecyclerView.setAdapter(tAdapter);

        ViewCompat.setNestedScrollingEnabled(tRecyclerView, false);




    }


    private static final int[] STATE_ZERO = {R.attr.state_on, -R.attr.state_off};
    private static final int[] STATE_ONE = {-R.attr.state_on, R.attr.state_off};

    @SuppressLint("ClickableViewAccessibility")
//accessibility not needed as warning only concerns visual workaround.
    public void prepareTagCollapse(final View view) {


        final ImageView collapseButton = (ImageView) view.findViewById(R.id.tagCollapseButton);
        final View dividerItemDecoration = (View) view.findViewById(R.id.browseDivider);
        final NestedScrollView nestedScrollView = (NestedScrollView) view.findViewById(R.id.browseScrollView);
        final CardView browseCard = (CardView) view.findViewById(R.id.browseTagCardHolder);
        final RecyclerView tagCardLayout = (RecyclerView) view.findViewById(R.id.browseTagRecycler);
        final RecyclerView taskRecycler = (RecyclerView) view.findViewById(R.id.browseTaskRecycler);

        final float scale = getContext().getResources().getDisplayMetrics().density;
        final int pixels = (int) (56 * scale + 0.5f);


        //collapseButton.setImageResource(R.drawable.ic_expand_more_black_24dp);
        collapseButton.setImageState(STATE_ONE, true);


        Log.d("wontCollapse", "got here");

        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!removing) {
                    Log.d("wontCollapse", "Won't collapse");
                    if (!tagCollapsed) {

                        taskRecycler.setPadding(0, browseCard.getHeight() - pixels, 0, 0);//no jump


                        TransitionManager.beginDelayedTransition(browseCard, new TransitionSet()
                                .addTransition(new ChangeBounds()));
                        ViewGroup.LayoutParams params = browseCard.getLayoutParams();
                        params.height = pixels;
                        browseCard.setLayoutParams(params);

                        TransitionManager.beginDelayedTransition(taskRecycler, new TransitionSet()
                                .addTransition(new ChangeBounds()));
                        taskRecycler.setPadding(0, 0, 0, 0);//animate with card collapse, after it if need be


                        dividerItemDecoration.setVisibility(View.GONE);
                        collapseButton.animate().setDuration(100).rotation(0).setListener(new AnimatorListenerAdapter() {
                        });


                        tagCardLayout.setVisibility(View.GONE);


                        tagCollapsed = true;

                    } else {

                        taskRecycler.setPadding(0, 0, 0, 0);//same as last but vice-versa

                        TransitionManager.beginDelayedTransition(browseCard, new TransitionSet()
                                .addTransition(new ChangeBounds()));
                        ViewGroup.LayoutParams params = browseCard.getLayoutParams();
                        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                        browseCard.setLayoutParams(params);

                        TransitionManager.beginDelayedTransition(taskRecycler, new TransitionSet()
                                .addTransition(new ChangeBounds()));
                        taskRecycler.setPadding(0, browseCard.getHeight() - pixels, 0, 0);//no jump


                        tagCardLayout.setVisibility(View.VISIBLE);


                        dividerItemDecoration.setVisibility(View.VISIBLE);
                        collapseButton.animate().setDuration(100).rotation(180).setListener(null);

                        tagCollapsed = false;

                    }
                } else {
                    removing = false;
                    tAdapter.notifyItemRangeChanged(0, filteredTagLinks.size());
                    //collapseButton.setImageResource(R.drawable.ic_expand_more_black_24dp);
                    collapseButton.setImageState(STATE_ONE, true);

                }
            }


        };

        collapseButton.setOnClickListener(listener);

        collapseButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                Log.d("wontCollapse", "longClick");


                if (!tagCollapsed && Filters.backTagFilters.size() > 1) {
                    if (!removing) {
                        removing = true;

                        //make arrow into x, start remove items
                        //collapseButton.setImageResource(R.drawable.ic_clear_black_24dp);
                        collapseButton.setImageState(STATE_ZERO, true);


                        tAdapter.notifyItemRangeChanged(0, filteredTagLinks.size());

                        collapseButton.setOnClickListener(null);
                        collapseButton.setOnTouchListener(new View.OnTouchListener() {


                            @Override
                            public boolean onTouch(View v, MotionEvent event) {


                                if (event.getAction() == MotionEvent.ACTION_UP) {

                                    collapseButton.postDelayed(new Runnable() {//debouncing, otherwise inner onclick would call if finger was on top of button on release, making it ineffective
                                        @Override
                                        public void run() {

                                            collapseButton.setOnClickListener(listener);
                                            collapseButton.setOnTouchListener(null);
                                        }
                                    }, 100);

                                    return false;
                                }
                                return false;
                            }
                        });


                    }
                }

                return false;
            }
        });

        if (SettingsActivity.darkTheme) {
            collapseButton.setColorFilter(Color.WHITE);
        }
    }
    private static void prepareTagAdd(View view){
        ImageView addButton = view.findViewById(R.id.tagAddButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), TagSelect.class);
                intent.putExtra("isTagLink", true);
                view.getContext().startActivity(intent);
            }
        });
    }


    public static void createFilteredTaskList(ArrayList<Integer> tagsToFilter, boolean viewing) {

        long startTime = System.nanoTime();

        ArrayList<Tasks> addToInbox = new ArrayList<>();
        ArrayList<Integer> noSubLinkList = new ArrayList<>();

        Log.d("noFilters", Integer.toString(tagsToFilter.size()));


        filteredTagLinks.clear();
        filteredTaskList.clear();

        Log.d("inboxFilterBrowse", Integer.toString(addToInbox.size()));


        if (!tagsToFilter.isEmpty()) {



            if (!TagList.tagList.isEmpty()) {
                for (int tag = 0; tag < TagList.tagList.size(); tag++) { //check all the tags

                    boolean contains = false;

                    int tagParent = Filters.getCurrentFilter().get(Filters.getCurrentFilter().size() - 1); //for the most recent filter
                    if (TagLinkList.contains(tagParent) != null) { //Catch error


                        if (TagLinkList.contains(tagParent).contains(tag)) { //and see if they are linked by the filters
                            Log.d("tagAdding", "Tag " + Integer.toString(tag) + " in.");

                            contains = true;
                        } else {
                            Log.d("tagAdding", "Tag " + Integer.toString(tag) + " in.");
                        }

                    } else {
                        Log.d("tagAdding", "Tag " + Integer.toString(tag) + " is not there.");
                    }

                    if (contains) { //and if so, add them

                        if (!tagsToFilter.contains(tag)) {//check if tag is part of filters
                            filteredTagLinks.add(tag);
                            if (!SettingsActivity.subFilter || !TagList.tagList.get(tag).isSubFolder())
                                noSubLinkList.add(tag);
                        }


                    }

                }

            }

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
            //duration = duration;//to seconds

            Log.d("startupTime", "Create filtered tasklist - Tags: " + Long.toString(duration));

            startTime = System.nanoTime();


            if (!TaskList.taskList.isEmpty()) {

                Log.d("tagPredicate", "filtering");

                ArrayList<Tasks> tempList = new ArrayList<>();

                addToInbox.addAll(TaskList.taskList);
                Log.d("tagPredicate", "Size: " + Integer.toString(addToInbox.size()));
                tempList = new ArrayList<>(Collections2.filter(addToInbox, new TagFilter(tagsToFilter) {
                }));


                addToInbox.clear();
                addToInbox.addAll(tempList);

                tempList = new ArrayList<>();

                Log.d("tagPredicate", "Size: " + Integer.toString(addToInbox.size()));

                if (SettingsActivity.folderMode) {
                    filteredTaskList.addAll(TaskList.taskList);
                    tempList = new ArrayList<>(Collections2.filter(filteredTaskList, new TagFilter(tagsToFilter, noSubLinkList) {
                    }));

                    filteredTaskList.clear();
                    filteredTaskList.addAll(tempList);
                } else {
                    filteredTaskList.addAll(addToInbox);
                }
            }


            if (!fromAdapter) {
                refreshWithAnim();
            } else {
                fromAdapter = false;
            }
            tAdapter.notifyDataSetChanged();

        } else {


            Log.d("noFilters", "no filters");
            for (Tags tag : TagList.tagList) {//if there are no filters, return all tags except subfolders
                if (!tag.isSubFolder()) {
                    filteredTagLinks.add(TagList.tagList.indexOf(tag));
                }
            }
            Log.d("noFilters", "TagList size:" + Integer.toString(filteredTagLinks.size()));


            if (SettingsActivity.folderMode) {//if folders, add all to inbox, only those w/o tags to browse

                ArrayList<Tasks> tempList = new ArrayList<>();

                filteredTaskList.addAll(TaskList.taskList);

                tempList = new ArrayList<>(Collections2.filter(filteredTaskList, new Predicate<Tasks>() {
                    @Override
                    public boolean apply(Tasks input) {
                        if (!input.isListTags()) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                }));

                filteredTaskList.clear();
                filteredTaskList.addAll(tempList);

                addToInbox.addAll(TaskList.taskList);


            } else {//if not, add all to browse& inbox
                filteredTaskList.addAll(TaskList.taskList);
                addToInbox.addAll(filteredTaskList);
            }

            Log.d("noFilters", "TaskList size:" + Integer.toString(filteredTaskList.size()));


            if (!fromAdapter) {
                refreshWithAnim();
            } else {
                fromAdapter = false;
            }
            tAdapter.notifyDataSetChanged();
        }

        Log.d("inboxFilterBrowse", Integer.toString(addToInbox.size()));
        Log.d("inboxFilterBrowse", Integer.toString(filteredTaskList.size()));


        if (viewing) {

            Log.d("inboxFilterBrowse", Integer.toString(addToInbox.size()));


            InboxFragment.filteredTaskList.clear();
            InboxFragment.filteredTaskList.addAll(addToInbox);

            InboxFragment.setFilterMode(InboxFragment.filterMode);

            Log.d("inboxFilter", Integer.toString(InboxFragment.filteredTaskList.size()));

            InboxFragment.refreshWithAnim();

            Log.d("inboxFilterBrowse", Integer.toString(InboxFragment.filteredTaskList.size()));
            Log.d("inboxFilterBrowse", Integer.toString(InboxFragment.mAdapter.getItemCount()));
            Log.d("inboxFilterBrowseBrowse", Integer.toString(filteredTaskList.size()));
            Log.d("inboxFilterBrowseBrowse", Integer.toString(mAdapter.getItemCount()));

            Log.d("inboxFilter", Integer.toString(InboxFragment.filteredTaskList.size()));


        }

        if (filteredTagLinks.isEmpty()) {
            tagCard.setVisibility(View.GONE);
            nestedScrollView.scrollTo(0, 0);
        } else {
            tagCard.setVisibility(View.VISIBLE);

        }

        if (Filters.getCurrentFilter().size() != 0) {
            InboxFragment.setTaskCountText(filteredTaskList.size());
        } else {
            InboxFragment.setTaskCountText(TaskList.taskList.size());
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1000000;  //divide by 1000000 to get milliseconds.
        //duration = duration;//to seconds

        Log.d("startupTime", "Create filtered tasklist: " + Long.toString(duration));

    }

    public static void refreshWithAnim() {
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scheduleLayoutAnimation();
    }


    public ActionMode.Callback setCallback(final int position) {
        ActionMode.Callback callback = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = new MenuInflater(BrowseFragment.this.getContext());
                menuInflater.inflate(R.menu.toolbar_inbox_long_press, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.editTask:
                        selected = false;
                        mAdapter.isSelected = false;
                        mAdapter.notifyItemChanged(position);
                        Intent editTask = new Intent(BrowseFragment.this.getContext(), AddTask.class);
                        editTask.putExtra("edit", true);
                        editTask.putExtra("editPos", (position));
                        editTask.putExtra("browse", true);
                        startActivity(editTask);
                        mode.finish();
                        return true;

                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selected = false;
                mAdapter.isSelected = false;
                mAdapter.notifyItemChanged(position);
            }
        };

        return callback;
    }
}
