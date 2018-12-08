package com.andb.apps.todo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.andb.apps.todo.filtering.Filters;
import com.andb.apps.todo.lists.TagLinkList;
import com.andb.apps.todo.lists.TagList;
import com.andb.apps.todo.lists.interfaces.LinkListInterface;
import com.andb.apps.todo.lists.interfaces.TagListInterface;
import com.andb.apps.todo.objects.Tags;
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
    public static TagAdapter mAdapter;
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
                    boolean finish = LinkListInterface.addLinkToCurrentDirectory(position, TagSelect.this);
                    if(finish){
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

    public static Flashbar tagExists(Activity activity) {
        return new Flashbar.Builder(activity)
                .gravity(Flashbar.Gravity.BOTTOM)
                .title("Tag Exists")
                .message("The tag you selected has already been linked")
                .dismissOnTapOutside()
                .backgroundColorRes(R.color.colorAccent)
                .build();

    }

    public static Flashbar sameTag(Activity activity) {
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
                        TagListInterface.deleteTag(position, getApplicationContext());
                        mAdapter.notifyItemRemoved(position);
                        mode.finish();


                }

                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        };

    }



    @Override
    public void onPause() {
        super.onPause();
        if (isTagLink) {
            TagLinkList.saveTags(this);
        }
    }


}
