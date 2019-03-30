package com.andb.apps.todo.views

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.ViewCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.andb.apps.todo.R
import com.andb.apps.todo.TagSelect
import com.andb.apps.todo.filtering.Filters
import com.andb.apps.todo.objects.Tags
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.ProjectsUtils
import com.andb.apps.todo.utilities.Utilities
import com.andb.apps.todo.utilities.bindEmpty
import com.github.rongi.klaster.Klaster
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.browse_tag_list_item.view.*
import kotlinx.android.synthetic.main.folder_picker.view.*

class FolderButtonCardLayout : FrameLayout {


    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    val folderAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder> = tagAdapter()
    val tagList = ArrayList<Tags>()
    lateinit var tagClickCallback: (tag: Tags, longClick: Boolean) -> Unit
    var expandCollapseListeners = ArrayList<((Boolean) -> Unit)>()
    var editListeners = ArrayList<((Boolean) -> Unit)>()

    var expanded = false
    var editing: Boolean = false

    init {
        inflate(context, R.layout.folder_picker, this)

        setOnClickListener {
            if (!expanded) {
                expand()
            } else {
                collapse()
            }
        }

        folderAddButton.setOnClickListener {
            val intent = Intent(context, TagSelect::class.java)
            intent.putExtra("isTagLink", true)
            context.startActivity(intent)
        }
    }


    fun setup(tagList: List<Tags>, expandedEditing: Pair<Boolean, Boolean>, tagClickCallback: (tag: Tags, longClick: Boolean) -> Unit) {
        this.tagList.clear()
        this.tagList.addAll(tagList)
        this.tagClickCallback = tagClickCallback

        folderTagRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = folderAdapter
        }

        if (expandedEditing.first) {
            expand()
        } else {
            collapse()
        }

        if (Filters.getCurrentFilter().isNotEmpty()) {
            folderEditButton.apply {
                setOnClickListener {
                    Log.d("folderEditButton", "clicked")
                    editing = !editing
                    editListeners.forEach { it.invoke(editing) }
                    if (editing) {
                        setImageDrawable(context.getDrawable(R.drawable.ic_clear_black_24dp))
                    } else {
                        setImageDrawable(context.getDrawable(R.drawable.ic_edit_black_24dp))
                    }
                    folderAdapter.notifyDataSetChanged()
                }
            }
            editing = expandedEditing.second
        } else {
            editing = false
        }
    }

    fun tagAdapter() = Klaster.get()
        .itemCount { tagList.size }
        .view(com.andb.apps.todo.R.layout.browse_tag_list_item, LayoutInflater.from(context))
        .bindEmpty {
            itemView.apply {
                val tag = tagList[adapterPosition]
                browseTagName.text = tag.tagName
                browseTagImage.setColorFilter(tag.tagColor)
                setOnClickListener {
                    ViewCompat.postOnAnimationDelayed(itemView, {
                        tagClickCallback.invoke(tag, false)
                    }, 100)
                }
                setOnLongClickListener {
                    tagClickCallback.invoke(tag, true)
                    true
                }
                if (editing) {
                    TransitionManager.beginDelayedTransition(tagCardBrowseLayout, ChangeBounds().setDuration(ANIMATION_DURATION))
                    browseRemoveImage.setOnClickListener {
                        val parentTag = Filters.getMostRecent()
                        parentTag.children.removeAt(adapterPosition)
                        tagList.clear()
                        tagList.addAll(parentTag.children.map { Current.tagListAll()[it] })
                        folderAdapter.notifyItemRemoved(adapterPosition)
                        Filters.updateMostRecent(parentTag)
                        ProjectsUtils.update(parentTag)
                    }
                    browseRemoveImage.setVisibility(true)
                    TransitionManager.endTransitions(tagCardBrowseLayout)
                } else {
                    browseRemoveImage.setVisibility(false)
                }
            }
        }
        .build()


    fun expand() {

        expanded = true
        expandCollapseListeners.forEach { it.invoke(expanded) }

        folderCardHolder.apply {
            animateRadius(12)
            animateBackground(Cyanea.instance.backgroundColor)

            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
        }


        folderHeaderIcon.apply {
            setCyaneaBackground(2, .54f)
            updateLayoutParams<ConstraintLayout.LayoutParams> {
                marginStart = Utilities.pxFromDp(0)
            }
        }
        folderHeaderButtonText.visibility = View.GONE
        folderAddButton.visibility = View.VISIBLE
        folderEditButton.setVisibility(Filters.getCurrentFilter().isNotEmpty())
        folderDivider.visibility = View.VISIBLE
        folderTagRecycler.visibility = View.VISIBLE
    }


    fun collapse() {

        expanded = false
        expandCollapseListeners.forEach { it.invoke(expanded) }

        folderCardHolder.apply {

            animateRadius(36)
            animateBackground(Cyanea.instance.accent)

            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT

        }



        folderHeaderIcon.apply {
            setCyaneaBackground(2, 1f)
            updateLayoutParams<ConstraintLayout.LayoutParams> {
                marginStart = Utilities.pxFromDp(12)
            }
        }
        folderHeaderButtonText.visibility = View.VISIBLE
        folderAddButton.visibility = View.GONE
        folderEditButton.visibility = View.GONE
        folderDivider.visibility = View.GONE
        folderTagRecycler.visibility = View.GONE

    }

    fun addExpandCollapseListener(listener: (Boolean) -> Unit) {
        expandCollapseListeners.add(listener)
    }

    fun addEditListener(listener: (Boolean) -> Unit) {
        editListeners.add(listener)
    }

    fun CardView.animateRadius(to: Int): ObjectAnimator {
        return ObjectAnimator.ofFloat(this, "radius", Utilities.pxFromDp(to).toFloat())
            .setDuration(ANIMATION_DURATION)
            .also {
                it.start()
            }
    }

    fun CardView.animateBackground(to: Int): ValueAnimator {
        return ValueAnimator.ofObject(ArgbEvaluator(), cardBackgroundColor.defaultColor, to)
            .setDuration(ANIMATION_DURATION)
            .also {
                it.addUpdateListener { animator ->
                    setCardBackgroundColor(animator.animatedValue as Int)
                }
                it.start()
            }
    }


    companion object {
        val ANIMATION_DURATION: Long = 200
    }


}