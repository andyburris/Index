package com.andb.apps.todo

import android.graphics.PorterDuff
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.utilities.Utilities
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity
import kotlinx.android.synthetic.main.add_task.*
import org.joda.time.DateTime

class TestActivity : CyaneaAppCompatActivity() {

    val task = Tasks("", ArrayList<String>(), ArrayList<Boolean>(), ArrayList<Int>(), DateTime(3000, 1, 1, 0, 0, 59), false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_task)
        theme()
        setupName()
        setupButtons()
    }

    fun theme(){
        addTaskEditFrame.setBackgroundColor(Utilities.lighterDarker(cyanea.backgroundColor, .95f))
        addTaskName.apply {
            setHintTextColor(Utilities.lighterDarker(cyanea.backgroundColor, .7f))
            setTextColor(Utilities.lighterDarker(cyanea.backgroundColor, .5f))
            setCursorColor(this, Utilities.lighterDarker(cyanea.backgroundColor, .6f))
        }
    }

    fun setupName(){
        addTaskName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                task.listName = s.toString()
            }
        })
    }

    fun setupButtons(){
        addTaskAddListIcon.setOnClickListener {

        }
    }

    fun setCursorColor(editText: EditText, color: Int) {
        try {
            // Get the cursor resource id
            var field = TextView::class.java.getDeclaredField("mCursorDrawableRes")
            field.isAccessible = true
            val drawableResId = field.getInt(editText)

            // Get the drawable and set a color filter
            val drawable = ContextCompat.getDrawable(editText.context, drawableResId)
            drawable?.setColorFilter(color, PorterDuff.Mode.SRC_IN)
            val drawables = arrayOf(drawable, drawable)


            // Get the editor
            field = TextView::class.java.getDeclaredField("mEditor")
            field.isAccessible = true
            val editor = field.get(editText)
            // Set the drawables
            field = editor.javaClass.getDeclaredField("mCursorDrawable")
            field.isAccessible = true
            field.set(editor, drawables)

        } catch (e: Exception) {
            Log.e("cursorSetFailed", "-> ", e)
        }
    }


}