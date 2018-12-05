package com.andb.apps.todo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.andb.apps.todo.settings.SettingsActivity;
import com.andrognito.flashbar.Flashbar;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


public class TagSelect extends AppCompatActivity {

    public static ArrayList<Tags> blankTagList = new ArrayList<>();


    private RecyclerView mRecyclerView;
    private static TagAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private boolean isTaskCreate = false;

    private boolean isTagLink = false;


    private ActionMode contextualToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //if (SettingsActivity.darkTheme) {
        //    this.setTheme(R.style.AppThemeDark);
        //} else {
            this.setTheme(R.style.AppThemeLight);
        //}
        setContentView(R.layout.activity_tag_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (SettingsActivity.darkTheme)
            darkThemeSet(toolbar);

        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("isTaskCreate")) {
            isTaskCreate = bundle.getBoolean("isTaskCreate");
        }

        if (getIntent().hasExtra("isTagLink")) {
            isTagLink = bundle.getBoolean("isTagLink");

        }

        mRecyclerView = (RecyclerView) findViewById(R.id.tagrecycler);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));


        // specify an adapter (see also next example)
        mAdapter = new TagAdapter(TagList.tagList);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());


        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), mRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                Tags tags = TagList.getItem(position);
                Toast.makeText(getApplicationContext(), tags.getTagName() + " is selected!", Toast.LENGTH_SHORT).show();


                if (isTaskCreate) {

                    AddTask.addTag(position);
                    finish();
                } else if (isTagLink && Filters.getCurrentFilter().size() > 0) {
                    int tagParent = Filters.getCurrentFilter().get(Filters.getCurrentFilter().size() - 1);

                    if (TagLinkList.contains(tagParent) != null) {

                        if (TagLinkList.contains(tagParent).contains(position)) {
                            //Log.d("tagAdding", "Already Added" + TagLinkList.contains(tagParent).getTagLink(TagLinkList.contains(tagParent).getLinkPosition(position)) + " to " + Integer.toString(tagParent));
                            tagExists(TagSelect.this).show();
                        } else if (position == tagParent) {
                            sameTag(TagSelect.this).show();
                        } else {
                            //Log.d("tagAdding", "Adding " + Integer.toString(position) + " to " + Integer.toString(positionInLinkList));
                            TagLinkList.contains(tagParent).addLink(position);

                            BrowseFragment.createFilteredTaskList(Filters.getCurrentFilter(), true);
                            BrowseFragment.mAdapter.notifyDataSetChanged();
                            finish();
                        }
                    } else {
                        Log.d("tagAdding", "Starting links for tag " + Integer.toString(tagParent) + ".");
                        ArrayList<Integer> arrayList = new ArrayList<>();
                        arrayList.add(position);
                        TagLinkList.addLinkListItem(new TagLinks(tagParent, arrayList));

                        boolean debug = false;
                        if (TagLinkList.contains(tagParent) != null)
                            debug = true;


                        //Log.d("tagAdding","Now added " + TagLinkList.getLinkListItem(TagLinkList.contains(tagParent)).getTagLink(0) + " to " + Integer.toString(tagParent) + ", " + debug + ", " + TagLinkList.getLinkListItem(TagLinkList.contains(tagParent)).contains(position));

                        BrowseFragment.createFilteredTaskList(Filters.getCurrentFilter(), true);
                        BrowseFragment.mAdapter.notifyDataSetChanged();
                        finish();

                    }

                } else {
                    Filters.tagForward(position);
                    finish();
                }

            }

            @Override
            public void onLongClick(View view, int position) {

                contextualToolbar = TagSelect.this.startActionMode(setCallback(position));
                view.setSelected(true);
            }


        }));


    }

    public void darkThemeSet(Toolbar toolbar) {
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorDarkPrimary));
        getWindow().getDecorView().setSystemUiVisibility(0);


    }

    private static Flashbar tagExists(Activity activity) {
        return new Flashbar.Builder(activity)
                .gravity(Flashbar.Gravity.BOTTOM)
                .title("Tag Exists")
                .message("The tag you selected has already been linked")
                .dismissOnTapOutside()
                .backgroundColorRes(R.color.colorAccent)
                .build();

    }

    private static Flashbar sameTag(Activity activity) {
        return new Flashbar.Builder(activity)
                .gravity(Flashbar.Gravity.BOTTOM)
                .title("Same Tag")
                .message("The tag you selected is the current tag. Why would you do that?")
                .dismissOnTapOutside()
                .backgroundColor(SettingsActivity.themeColor)
                .build();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tag_select, menu);
        if (SettingsActivity.darkTheme)
            menu.getItem(0).getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.app_bar_add:
                Intent editTask = new Intent(this, CreateTag.class);
                editTask.putExtra("edit", false);
                startActivity(editTask);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);
        }
    }


    public static void addTag(String name, int color, boolean sub) {
        Tags tags = new Tags(name, color, sub);
        TagList.addTagList(tags);
        mAdapter.notifyDataSetChanged();
        BrowseFragment.createFilteredTaskList(Filters.getCurrentFilter(), true);


    }

    public static void replaceTag(String name, int color, int pos, boolean sub) {
        Tags tags = new Tags(name, color, sub);
        TagList.setTagList(pos, tags);
        mAdapter.notifyItemChanged(pos);
        BrowseFragment.createFilteredTaskList(Filters.getCurrentFilter(), true);


    }


    public ActionMode.Callback setCallback(final int position) {
        return new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = new MenuInflater(TagSelect.this.getApplicationContext());
                menuInflater.inflate(R.menu.toolbar_tag_select_long_press, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.editTag:
                        Intent editTask = new Intent(TagSelect.this.getApplicationContext(), CreateTag.class);
                        editTask.putExtra("edit", true);
                        editTask.putExtra("editPos", position);
                        startActivity(editTask);
                        mode.finish();
                        return true;
                    case R.id.deleteTag:
                        deleteTag(position);
                        mode.finish();


                }

                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        };

    }

    public void deleteTag(int pos) {
        TagList.tagList.remove(pos);
        mAdapter.notifyItemRemoved(pos);
        if (!TaskList.taskList.isEmpty()) {
            for (final Tasks tasks : TaskList.taskList) {
                if (tasks.isListTags()) {
                    ArrayList<Integer> toRemove = new ArrayList<>();
                    for (int tag : tasks.getAllListTags()) {
                        if (tag == pos) {
                            toRemove.add(tag);
                        } else if (tag > pos) {
                            tasks.setListTags(tasks.getAllListTags().indexOf(tag), tag - 1);
                        }
                    }
                    tasks.getAllListTags().removeAll(toRemove);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.tasksDatabase.tasksDao().updateTask(tasks);
                        }
                    });
                }
            }
        }
        if (!TagLinkList.linkList.isEmpty()) {
            ArrayList<TagLinks> toRemoveParent = new ArrayList<>();
            for (TagLinks tagLinks : TagLinkList.linkList) {
                if (tagLinks.tagParent() == pos) {
                    toRemoveParent.add(tagLinks);
                } else {
                    ArrayList<Integer> toRemoveChild = new ArrayList<>();
                    for (int linkPos : tagLinks.getAllTagLinks()) {
                        if (linkPos == pos) {
                            toRemoveChild.add(linkPos);
                        } else if (linkPos > pos) {
                            tagLinks.getAllTagLinks().set(tagLinks.getAllTagLinks().indexOf(linkPos), linkPos - 1);
                        }
                    }
                    tagLinks.getAllTagLinks().removeAll(toRemoveChild);
                }
            }
            TagLinkList.linkList.removeAll(toRemoveParent);
        }

        TagList.saveTags(this);
        TagLinkList.saveTags(this);

        BrowseFragment.createFilteredTaskList(Filters.getCurrentFilter(), true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isTagLink) {
            TagLinkList.saveTags(this);
        }
    }


}
