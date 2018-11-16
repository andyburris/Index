package com.andb.apps.todo;

import android.content.Context;
import android.util.Log;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
        public TextView name;
        public ImageView clearList;
        public ConstraintLayout item1;
        public ConstraintLayout item2;
        public ConstraintLayout item3;
        public LinearLayout encloser;
        public ImageView more;
        public ConstraintLayout tag1;
        public ConstraintLayout tag2;
        public ConstraintLayout tag3;
        public ConstraintLayout tag4;
        public ConstraintLayout tag5;
        public LinearLayout tagEncloser;
        public ImageView moreTags;
        public ConstraintLayout timeLayout;
        public TextView timeText;

        public View divider1;
        public View divider2;

        public TextView dividerName;

        public ConstraintLayout inboxListItemBackground;

        public TextView tagName;
        public ImageView browseTagImage;
        public ConstraintLayout browseLayout;

        public ImageView removeButton;


        public MyViewHolder(View view) {
            super(view);

            divider1 = (View) view.findViewById(R.id.tagDivider);
            divider2 = (View) view.findViewById(R.id.timeDivider);

            dividerName = (TextView) view.findViewById(R.id.dividerName);

            name = (TextView) view.findViewById(R.id.listTextView);
            clearList = (ImageView) view.findViewById(R.id.clearList);
            item1 = (ConstraintLayout) view.findViewById(R.id.item1);
            item2 = (ConstraintLayout) view.findViewById(R.id.item2);
            item3 = (ConstraintLayout) view.findViewById(R.id.item3);
            tag1 = (ConstraintLayout) view.findViewById(R.id.tag1);
            tag2 = (ConstraintLayout) view.findViewById(R.id.tag2);
            tag3 = (ConstraintLayout) view.findViewById(R.id.tag3);
            tag4 = (ConstraintLayout) view.findViewById(R.id.tag4);
            tag5 = (ConstraintLayout) view.findViewById(R.id.tag5);
            moreTags = (ImageView) view.findViewById(R.id.tagMore);
            timeText = (TextView) view.findViewById(R.id.dateInboxText);

            encloser = (LinearLayout) view.findViewById(R.id.checklistEncloser);
            tagEncloser = (LinearLayout) view.findViewById(R.id.tagOnTaskLayout);
            timeLayout = (ConstraintLayout) view.findViewById(R.id.dateTimeInboxLayout);
            more = (ImageView) view.findViewById(R.id.itemsMore);

            inboxListItemBackground = (ConstraintLayout) view.findViewById(R.id.inboxListBackground);

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

/*            if (SettingsActivity.darkTheme)
                holder.removeButton.setColorFilter(Color.WHITE);*/
            TransitionManager.beginDelayedTransition(holder.browseLayout, new AutoTransition()
            );

            holder.removeButton.setVisibility(View.VISIBLE);
            holder.removeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TagLinks toRemove = TagLinkList.contains(Filters.getMostRecent());
                    Log.d("removeLinks", "Size: " + tagLinks.size() + ", position:" + realPosition);
                    Log.d("removeLinks", "TagLink Size: " + toRemove.getAllTagLinks().size() + ", tag parent:" + TagList.getItem(toRemove.tagParent()).getTagName());
                    toRemove.removeTagLink(tagLinks.get(realPosition));
                    Log.d("removeLinks", "TagLink Size: " + toRemove.getAllTagLinks().size() + ", tag parent:" + TagList.getItem(toRemove.tagParent()).getTagName());
                    tagLinks.remove(realPosition);
                    notifyItemRemoved(realPosition);
                    notifyItemRangeChanged(0, tagLinks.size());
                }
            });
        } else {
            holder.removeButton.setVisibility(View.GONE);
        }

        holder.tagName.setText(TagList.getItem(tagLinks.get(position)).getTagName());
        holder.browseTagImage.setColorFilter(TagList.getItem(tagLinks.get(position)).getTagColor());
        holder.browseLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ViewCompat.postOnAnimationDelayed(holder.itemView, new Runnable() {
                    @Override
                    public void run() {
                        Log.d("noFiltersOnBack", Integer.toString(Filters.backTagFilters.get(0).size())
                                + ", "
                                + Filters.backTagFilters.size());
                        int tagClicked;
                        if (Filters.backTagFilters.size() > 1) {
                            tagClicked = BrowseFragment.filteredTagLinks.get(position);

                        } else {
                            tagClicked = position;
                        }

                        Filters.tagForward(tagClicked);
                        Log.d("noFiltersOnBack", Integer.toString(Filters.backTagFilters.get(0).size()) + ", " + Filters.backTagFilters.size());

                    }
                }, 100);
            }


        });
        holder.browseLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!TagList.getItem(tagLinks.get(realPosition)).isSubFolder()) {
                    Filters.tagReset(BrowseFragment.filteredTagLinks.get(position));
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
