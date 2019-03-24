package com.andb.apps.todo.views

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
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
import com.andb.apps.todo.objects.Tags
import com.andb.apps.todo.utilities.Utilities
import com.github.rongi.klaster.Klaster
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.browse_tag_list_item.view.*
import kotlinx.android.synthetic.main.folder_picker.view.*
import androidx.core.content.ContextCompat.startActivity
import com.andb.apps.todo.TagSelect
import android.content.Intent
import com.andb.apps.todo.TaskAdapter
import com.andb.apps.todo.filtering.Filters
import com.andb.apps.todo.utilities.bindEmpty


class FolderButtonCardLayout : FrameLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    var expanded = true
    val folderAdapter = tagAdapter()
    val tagList = ArrayList<Tags>()
    lateinit var transitionParent: RecyclerView
    lateinit var tagClickCallback: (tag: Tags, longClick: Boolean)->Unit
    var collapseListener: (()->Unit)? = null
    var expandListener: (()->Unit)? = null
    var editing: Boolean = false

    init {
        inflate(context, R.layout.folder_picker, this)

        setOnClickListener {
            if(expanded){
                expand()
            }else{
                collapse()
            }
            (transitionParent.adapter as TaskAdapter).expanded = expanded
        }

        folderEditButton.apply {
            if(Filters.getCurrentFilter().isNotEmpty()) {
                setVisibility(true)
                setOnClickListener {
                    editing = !editing
                    if (editing) {
                        setImageDrawable(context.getDrawable(R.drawable.ic_clear_black_24dp))
                    } else {
                        setImageDrawable(context.getDrawable(R.drawable.ic_edit_black_24dp))
                    }
                    folderAdapter.notifyDataSetChanged()
                }
            }else{
                setVisibility(false)
            }
        }

        folderAddButton.setOnClickListener {
            val intent = Intent(context, TagSelect::class.java)
            intent.putExtra("isTagLink", true)
            context.startActivity(intent)
        }
    }



    fun setup(tagList: List<Tags>, transitionParent: RecyclerView, expanded: Boolean, tagClickCallback: (tag: Tags, longClick: Boolean)->Unit){
        this.tagList.clear()
        this.tagList.addAll(tagList)
        this.transitionParent = transitionParent
        this.tagClickCallback = tagClickCallback

        folderTagRecycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = folderAdapter
        }

        if(expanded){
            expand()
        }else {
            collapse()
        }
        this.expanded = !expanded
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
                if(editing){
                    TransitionManager.beginDelayedTransition(tagCardBrowseLayout, ChangeBounds())
                    browseRemoveImage.setOnClickListener {

                    }
                    browseRemoveImage.setVisibility(true)
                }else{
                    browseRemoveImage.setVisibility(false)
                }
            }
        }
        .build()


    fun expand(){

        expanded = false
        expandListener?.invoke()

        TransitionManager.endTransitions(transitionParent)
        TransitionManager.beginDelayedTransition(folderCardHolder.rootView as ViewGroup, ChangeBounds().setDuration(ANIMATION_DURATION))
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
        folderEditButton.visibility = View.VISIBLE
        folderDivider.visibility = View.VISIBLE
        folderTagRecycler.visibility = View.VISIBLE
    }

    val ANIMATION_DURATION: Long = 200

    fun collapse(){

        expanded = true
        collapseListener?.invoke()

        TransitionManager.endTransitions(transitionParent)
        TransitionManager.beginDelayedTransition(transitionParent, ChangeBounds().setDuration(ANIMATION_DURATION))
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

    fun CardView.animateRadius(to: Int): ObjectAnimator{
        return ObjectAnimator.ofFloat(this, "radius", Utilities.pxFromDp(to).toFloat())
            .setDuration(ANIMATION_DURATION)
            .also {
                it.start()
            }
    }

    fun CardView.animateBackground(to: Int): ValueAnimator{
        return ValueAnimator.ofObject(ArgbEvaluator(), cardBackgroundColor.defaultColor, to)
            .setDuration(ANIMATION_DURATION)
            .also {
                it.addUpdateListener {animator->
                    setCardBackgroundColor(animator.animatedValue as Int)
                }
                it.start()
            }
    }



}