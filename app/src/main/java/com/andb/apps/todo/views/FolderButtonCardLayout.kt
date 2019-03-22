package com.andb.apps.todo.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import com.andb.apps.todo.R
import com.andb.apps.todo.objects.Tags
import com.andb.apps.todo.utilities.Utilities
import com.jaredrummler.cyanea.Cyanea
import kotlinx.android.synthetic.main.folder_picker.view.*

class FolderButtonCardLayout : CardView {

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    init {
        inflate(context, R.layout.folder_picker, this)
        collapse()
    }

    fun expand(){
        folderCardHolder.apply {
            radius = Utilities.pxFromDp(8).toFloat()
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            setCardBackgroundColor(Cyanea.instance.backgroundColor)
        }

        folderHeaderIcon.setCyaneaBackground(0, folderHeaderIcon.alpha)
        folderHeaderButtonText.visibility = View.GONE
        folderAddButton.visibility = View.VISIBLE
        folderCollapseButton.visibility = View.VISIBLE
        folderDivider.visibility = View.VISIBLE
        folderTagRecycler.visibility = View.VISIBLE
    }

    fun collapse(){
        folderCardHolder.apply {
            radius = Utilities.pxFromDp(24).toFloat()
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
            setCardBackgroundColor(Cyanea.instance.accent)
        }

        folderHeaderIcon.setCyaneaBackground(2, folderHeaderIcon.alpha)
        folderHeaderButtonText.visibility = View.VISIBLE
        folderAddButton.visibility = View.GONE
        folderCollapseButton.visibility = View.GONE
        folderDivider.visibility = View.GONE
        folderTagRecycler.visibility = View.GONE
    }

    fun setup(tagList: List<Tags>, clickCallback: (tag: Tags)->Unit){

    }


}