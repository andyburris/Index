package com.andb.apps.todo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.transition.ChangeBounds;
import android.support.transition.TransitionManager;
import android.support.transition.TransitionSet;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import java.util.ArrayList;


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


    private RecyclerView mRecyclerView;
    public static BrowseTaskAdapter mAdapter;
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
        tagCard = (CardView) view.findViewById(R.id.browseTagCardHolder);
        prepareTagCollapse(view);

        nestedScrollView = (NestedScrollView) view.findViewById(R.id.browseScrollView);


        Filters.homeViewAdd(); //add current filter to back stack
        Log.d("noFiltersOnBack", Integer.toString(Filters.backTagFilters.get(Filters.backTagFilters.size() - 1).size()) + ", " + Filters.backTagFilters.size());
        createFilteredTaskList(Filters.getCurrentFilter(), true);

        if (filteredTaskList.isEmpty() & filteredTagLinks.isEmpty()) {
            view.findViewById(R.id.noTasks).setVisibility(View.VISIBLE);
        }

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

        //hide fab on scroll
        final FloatingActionButton fabMain = (FloatingActionButton) getActivity().findViewById(R.id.fab_main);
        final FloatingActionButton fabList = (FloatingActionButton) getActivity().findViewById(R.id.fab_list);
        final FloatingActionButton fabTag = (FloatingActionButton) getActivity().findViewById(R.id.fab_tag);
        final int scrollSensitivity = 5;
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > scrollSensitivity) {
                    fabMain.hide();
                    fabList.hide();
                    fabTag.hide();

                } else if (dy < scrollSensitivity) {
                    fabMain.show();
                    if (MainActivity.fabOpen) { //checks if extra fabs are open, if yes then return them to view
                        fabList.show();
                        fabTag.show();
                    }
                }
            }
        });


        return view;


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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }


    public void prepareRecyclerView(View view) {


        mRecyclerView = (RecyclerView) view.findViewById(R.id.browseTaskRecycler);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        //mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new BrowseTaskAdapter(filteredTaskList);
        mRecyclerView.setAdapter(mAdapter);

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

    @SuppressLint("ClickableViewAccessibility")
//accessibility not needed as warning only concerns visual workaround.
    public void prepareTagCollapse(final View view) {
        final ImageView collapseButton = (ImageView) view.findViewById(R.id.tagCollapseButton);
        final ConstraintLayout tagView = (ConstraintLayout) view.findViewById(R.id.tagRecyclerViewHolder);
        final View dividerItemDecoration = (View) view.findViewById(R.id.browseDivider);
        final NestedScrollView nestedScrollView = (NestedScrollView) view.findViewById(R.id.browseScrollView);
        final CardView browseCard = (CardView) view.findViewById(R.id.browseTagCardHolder);


        final float scale = getContext().getResources().getDisplayMetrics().density;
        final int pixels = (int) (56 * scale + 0.5f);

        collapseButton.setImageResource(R.drawable.ic_expand_more_black_24dp);

        Log.d("wontCollapse", "got here");
        collapseButton.setOnClickListener(null);
        collapseButton.setOnTouchListener(null);
        collapseButton.setOnLongClickListener(null);
        collapseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("wontCollapse", "Won't collapse");
                if (!tagCollapsed) {


                    TransitionManager.beginDelayedTransition(browseCard, new TransitionSet()
                            .addTransition(new ChangeBounds()));
                    ViewGroup.LayoutParams params = browseCard.getLayoutParams();
                    params.height = pixels;
                    browseCard.setLayoutParams(params);

                    dividerItemDecoration.setVisibility(View.GONE);
                    collapseButton.animate().setDuration(100).rotation(0).setListener(null);

                    tagView.setVisibility(View.GONE);


                    tagCollapsed = true;

                } else {
                    TransitionManager.beginDelayedTransition(browseCard, new TransitionSet()
                            .addTransition(new ChangeBounds()));
                    ViewGroup.LayoutParams params = browseCard.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    browseCard.setLayoutParams(params);

                    tagView.setVisibility(View.VISIBLE);


                    dividerItemDecoration.setVisibility(View.VISIBLE);
                    collapseButton.animate().setDuration(100).rotation(180).setListener(null);


                    tagCollapsed = false;

                }
            }


        });

        collapseButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                if (!tagCollapsed) {
                    //make arrow into x, start remove items
                    collapseButton.setImageResource(R.drawable.ic_clear_black_24dp);
                    removing = true;
                    tAdapter.notifyItemRangeChanged(0, filteredTagLinks.size());

                    collapseButton.setOnClickListener(null);

                    collapseButton.setOnTouchListener(new View.OnTouchListener() {


                        @Override
                        public boolean onTouch(View v, MotionEvent event) {

                            if (event.getAction() == MotionEvent.ACTION_UP) {

                                collapseButton.setOnLongClickListener(null);

                                collapseButton.postDelayed(new Runnable() {//debouncing, otherwise inner onclick would call if finger was on top of button on release, making it ineffective
                                    @Override
                                    public void run() {
                                        collapseButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                removing = false;
                                                tAdapter.notifyItemRangeChanged(0, filteredTagLinks.size());

                                                prepareTagCollapse(view);
                                            }
                                        });
                                    }
                                }, 100);

                                return false;
                            }
                            return false;
                        }
                    });


                }

                return false;
            }
        });

        if (SettingsActivity.darkTheme) {
            collapseButton.setColorFilter(Color.WHITE);
        }
    }


    public static void createFilteredTaskList(ArrayList<Integer> tagsToFilter, boolean viewing) {

        ArrayList<Tasks> addToInbox = new ArrayList<>();

        Log.d("noFilters", Integer.toString(tagsToFilter.size()));


        filteredTagLinks.clear();
        filteredTaskList.clear();
        addToInbox.clear();

        Log.d("inboxFilterBrowse", Integer.toString(addToInbox.size()));


        if (!tagsToFilter.isEmpty()) {

            Log.d("noFilters", Integer.toString(tagsToFilter.size()));


            if (!TagList.tagList.isEmpty()) {
                for (int k = 0; k < TagList.tagList.size(); k++) { //check all the tags

                    boolean contains = false;
                    int tag = k;
                    int tagParent = Filters.getCurrentFilter().get(Filters.getCurrentFilter().size() - 1); //for the most recent filter
                    if (TagLinkList.contains(tagParent) >= 0) { //Catch error

                        int positionInLinkList = TagLinkList.contains(tagParent);
                        Log.d("tagAdding", "Returned " + Integer.toString(positionInLinkList));

                        if (TagLinkList.getLinkListItem(positionInLinkList).contains(tag)) { //and see if they are linked by the filters
                            Log.d("tagAdding", "Tag " + Integer.toString(tag) + " in " + Integer.toString(positionInLinkList) + " is not there.");

                            contains = true;
                        } else {
                            Log.d("tagAdding", "Tag " + Integer.toString(tag) + " in " + Integer.toString(positionInLinkList) + " is there.");
                        }

                    } else {
                        Log.d("tagAdding", "Tag " + Integer.toString(tag) + " is not there.");
                    }

                    if (contains) { //and if so, add them
                        boolean tagIsFilter = false;
                        for (int i = 0; i < tagsToFilter.size(); i++) { ///check if tag is part of filters
                            if (tagsToFilter.get(i) == tag) {
                                tagIsFilter = true;
                            }
                        }
                        if (!tagIsFilter) {
                            filteredTagLinks.add(tag);
                        }
                    }

                }

            }


            if (!TaskList.taskList.isEmpty()) {
                for (int k = 0; k < TaskList.taskList.size(); k++) { //check all the tasks
                    boolean contains = true;
                    boolean isFoldered = false;
                    int task = k;
                    for (int i = 0; i < tagsToFilter.size(); i++) { //for all the filters
                        boolean filterFolder = SettingsActivity.folderMode; //turn to preferences var later,, rn is folder behavior
                        if (!TaskList.getItem(k).doesListContainTag(tagsToFilter.get(i))) { //and see if they are linked by the filters
                            contains = false;
                        } else if (filterFolder) {
                            //Log.d("folderFreeze", "running ");
                            for (int j = 0; j < filteredTagLinks.size(); j++) {
                                //Log.d("folderFreeze", "running j");
                                if (TaskList.getItem(k).doesListContainTag(filteredTagLinks.get(j))) {
                                    if (SettingsActivity.subFilter) {
                                        if (!TagList.getItem(filteredTagLinks.get(j)).isSubFolder()) {


                                            contains = false;
                                            isFoldered = true;
                                        }

                                    } else {
                                        contains = false;
                                        isFoldered = true;
                                    }
                                }
                            }

                        }
                    }
                    if (isFoldered) {
                        Log.d("inboxFilterBrowse", "Adding");
                        addToInbox.add(TaskList.getItem(task));
                        Log.d("inboxFilterBrowse", Integer.toString(addToInbox.size()) + ", " + k + ", " + TaskList.taskList.size());

                    }
                    if (contains) { //and if so, add them
                        filteredTaskList.add(TaskList.getItem(task));
                    }
                }

            }
            mAdapter.notifyDataSetChanged();
            tAdapter.notifyDataSetChanged();

        } else

        {

            Log.d("noFilters", "no filters");
            for (int i = 0; i < TagList.tagList.size(); i++) {//if there are no filters, return all tags except subfolders
                if (!TagList.getItem(i).isSubFolder()) {
                    filteredTagLinks.add(i);
                }
            }
            Log.d("noFilters", "TagList size:" + Integer.toString(filteredTagLinks.size()));

            for (int j = 0; j < TaskList.taskList.size(); j++) { //for all the tasklist items
                if (SettingsActivity.folderMode) {//if folders, add all to inbox, only those w/o tags to browse
                    if (!TaskList.getItem(j).isListTags()) {
                        filteredTaskList.add(TaskList.getItem(j));
                    } else {
                        addToInbox.add(TaskList.getItem(j));
                    }
                } else {//if not, add all to browse& inbox
                    filteredTaskList.add(TaskList.getItem(j));
                }
            }
            Log.d("noFilters", "TaskList size:" + Integer.toString(filteredTaskList.size()));


            mAdapter.notifyDataSetChanged();
            tAdapter.notifyDataSetChanged();
        }

        Log.d("inboxFilterBrowse", Integer.toString(addToInbox.size()));
        Log.d("inboxFilterBrowse", Integer.toString(filteredTaskList.size()));


        if (viewing) {
            addToInbox.addAll(filteredTaskList);//add everything from browse

            Log.d("inboxFilterBrowse", Integer.toString(addToInbox.size()));


            InboxFragment.filteredTaskList.clear();
            InboxFragment.filteredTaskList.addAll(addToInbox);

            Log.d("inboxFilter", Integer.toString(InboxFragment.filteredTaskList.size()));

            InboxFragment.setFilterMode(InboxFragment.filterMode);
            InboxFragment.mAdapter.notifyDataSetChanged();

            Log.d("inboxFilterBrowse", Integer.toString(InboxAdapter.taskList.size()));
        }

        if (filteredTagLinks.isEmpty()) {
            tagCard.setVisibility(View.GONE);
            nestedScrollView.scrollTo(0, 0);
        } else {
            tagCard.setVisibility(View.VISIBLE);

        }


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
