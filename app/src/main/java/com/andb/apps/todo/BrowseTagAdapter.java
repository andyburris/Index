package com.andb.apps.todo;

import android.content.Context;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andb.apps.todo.filtering.FilteredLists;
import com.andb.apps.todo.filtering.Filters;
import com.andb.apps.todo.objects.Tags;
import com.andb.apps.todo.utilities.Current;

import java.util.ArrayList;
import java.util.List;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.Slide;
import androidx.transition.TransitionManager;
import androidx.transition.Visibility;

public class BrowseTagAdapter extends RecyclerView.Adapter<BrowseTagAdapter.MyViewHolder> {
    public static List<Integer> tagLinks = new ArrayList<>();

    private Context context;
    private ActionMode actionMode;


    private int viewType = 0;

    private static final int TAG_LINK_ITEM = 0;
    private static final int TASK_VIEW_ITEM = 1;
    private static final int DIVIDER = 2;

    private int lastPosition = -1;

    public int debugSetTasks = 0;

    //Preferences for which to show


    public class MyViewHolder extends RecyclerView.ViewHolder {


        public TextView tagName;
        public ImageView browseTagImage;
        public ConstraintLayout browseLayout;

        public ImageView removeButton;


        public MyViewHolder(View view) {
            super(view);


            tagName = (TextView) view.findViewById(R.id.browseTagName);
            browseTagImage = (ImageView) view.findViewById(R.id.browseTagImage);
            browseLayout = (ConstraintLayout) view.findViewById(R.id.tagCardBrowseLayout);

            removeButton = (ImageView) view.findViewById(R.id.browseRemoveImage);


            //this.setIsRecyclable(false);


            //taskColor = (ImageView) view.findViewById(R.id.taskIcon);

        }


    }


    public BrowseTagAdapter(List<Integer> tagLinks) {
        this.tagLinks = tagLinks;
    }


    @Override
    public MyViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {

        /*//Indicates whether each item in the data set can be represented with a unique identifier
        setHasStableIds(true);*/

        context = parent.getContext();
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.browse_tag_list_item, parent, false);


        return new MyViewHolder(itemView);


    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        Log.d("taskListError", Integer.toString(position));
        final int realPosition = holder.getAdapterPosition();
        Log.d("taskListError", Integer.toString(realPosition));

        setUpByViewType(position, holder, realPosition);


    }

    public void setUpByViewType(final int position, final MyViewHolder holder, final int realPosition) {

        if (BrowseFragment.removing) {


            Slide slide = new Slide();
            slide.setSlideEdge(Gravity.LEFT);
            slide.setMode(Visibility.MODE_IN);
            slide.setDuration(1000);


            TransitionManager.beginDelayedTransition(holder.browseLayout, new AutoTransition()
            );

            holder.removeButton.setVisibility(View.VISIBLE);
            holder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Tags toRemoveFrom = Current.tagList().get(Filters.getMostRecent());
                    Log.d("removeLinks", "Size: " + tagLinks.size() + ", position:" + realPosition);
                    toRemoveFrom.getChildren().remove(tagLinks.get(realPosition));
                    tagLinks.remove(realPosition);
                    notifyItemRemoved(realPosition);
                    notifyItemRangeChanged(0, tagLinks.size());
                }
            });
        } else {
            holder.removeButton.setVisibility(View.GONE);
        }

        holder.tagName.setText(Current.tagList().get(tagLinks.get(position)).getTagName());
        holder.browseTagImage.setColorFilter(Current.tagList().get(tagLinks.get(position)).getTagColor());
        holder.browseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ViewCompat.postOnAnimationDelayed(holder.itemView, new Runnable() {
                    @Override
                    public void run() {
                        Log.d("noFiltersOnBack", Integer.toString(Filters.backTagFilters.get(0).size())
                                + ", "
                                + Filters.backTagFilters.size());
                        int tagClicked = FilteredLists.filteredTagLinks.get(position);


                        Filters.tagForward(tagClicked);
                        Log.d("noFiltersOnBack", Integer.toString(Filters.backTagFilters.get(0).size()) + ", " + Filters.backTagFilters.size());

                    }
                }, 100);
            }


        });
        holder.browseLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!Current.tagList().get(tagLinks.get(realPosition)).isSubFolder()) {
                    Filters.tagReset(FilteredLists.filteredTagLinks.get(position));
                }
                return true;
            }
        });

    }


    @Override
    public int getItemViewType(int position) {


        Log.d("taskListError", "TAG");
        viewType = TAG_LINK_ITEM;
        return TAG_LINK_ITEM;

    }

    @Override
    public int getItemCount() {
        int size = tagLinks.size();
        return size;
    }


}
