package com.andb.apps.todo;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.andb.apps.todo.lists.TagList;
import com.andb.apps.todo.lists.interfaces.TagListInterface;
import com.andb.apps.todo.settings.SettingsActivity;
import com.andrognito.flashbar.Flashbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jaredrummler.android.colorpicker.ColorPanelView;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.jaredrummler.cyanea.Cyanea;

import java.lang.reflect.Field;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;


public class CreateTag extends AppCompatActivity implements ColorPickerDialogListener {

    public EditText tagNameEdit;
    public Switch subFolderSwitch;
    public boolean editing;
    public int tagPosition;

    boolean started = false;

    public int tagColor = Cyanea.getInstance().getAccent();
    public boolean subFolder;

    private static final int DIALOG_ID = 0;

    Flashbar flashbar;
    Flashbar usedFlashbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_tag);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        flashbar = blankText();
        usedFlashbar = keyUsed();


        Bundle bundle = getIntent().getExtras();
        editing = bundle.getBoolean("edit");


        tagNameEdit = findViewById(R.id.createTagName);
        subFolderSwitch = findViewById(R.id.subfolderCreateSwitch);

        setInputTextLayoutColor(tagColor, tagNameEdit);


        if (editing) {

            tagPosition = bundle.getInt("editPos");
            Log.d("taskPosition", Integer.toString(tagPosition));
            tagNameEdit.setText(TagList.getItem(tagPosition).getTagName());
            tagColor = TagList.getItem(tagPosition).getTagColor();
            ColorPanelView colorPanelView = (ColorPanelView) findViewById(R.id.tagColorPreview);
            colorPanelView.setColor(tagColor);

            subFolderSwitch.setChecked(TagList.getItem(tagPosition).isSubFolder());
        }

        ConstraintLayout colorPreview = (ConstraintLayout) findViewById(R.id.colorPreviewLayout);

        colorPreview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("gotToCode", "Color dialog should appear");
                ColorPickerDialog.newBuilder()
                        .setColor(tagColor)
                        .setShowAlphaSlider(false)
                        .setDialogId(DIALOG_ID)
                        .show(CreateTag.this);
            }
        });

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setBackgroundTintList(ColorStateList.valueOf(tagColor));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                fabAddTag();


            }
        });



    }




    private void setInputTextLayoutColor(final int color, final EditText editText) {


        try {
            // Get the cursor resource id
            Field field = TextView.class.getDeclaredField("mCursorDrawableRes");
            field.setAccessible(true);
            int drawableResId = field.getInt(editText);

            // Get the editor
            field = TextView.class.getDeclaredField("mEditor");
            field.setAccessible(true);
            Object editor = field.get(editText);

            // Get the drawable and set a color filter
            Drawable drawable = ContextCompat.getDrawable(editText.getContext(), drawableResId);
            drawable.setColorFilter(color, PorterDuff.Mode.SRC_IN);
            Drawable[] drawables = {drawable, drawable};

            // Set the drawables
            field = editor.getClass().getDeclaredField("mCursorDrawable");
            field.setAccessible(true);
            field.set(editor, drawables);
        } catch (Exception ignored) {
        }


        editText.setHighlightColor(color);


        if (!started) {

            editText.getBackground().setColorFilter(0xFF757575, PorterDuff.Mode.SRC_IN);

            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    Log.d("focusError", "focus change");

                    if (hasFocus) {
                        editText.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
                    } else {
                        editText.getBackground().setColorFilter(0xFF757575, PorterDuff.Mode.SRC_IN);
                    }
                }
            });
            started = true;
        } else {
            editText.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_IN);
        }

    }


    public void fabAddTag() {
        Log.d("gotToCode", "Clicked FAB");

        String tagName = tagNameEdit.getText().toString();
        subFolder = subFolderSwitch.isChecked();

        if (TextUtils.isEmpty(tagNameEdit.getText())) {
            flashbar.show();
        } else if (TagList.keyList.contains(tagNameEdit.getText().toString())) {
            usedFlashbar.show();
        } else {

            if (editing) {
                TagListInterface.replaceTag(tagName, tagColor, tagPosition, subFolder);
                TagSelect.mAdapter.notifyItemChanged(tagPosition);
            } else {
                TagListInterface.addTag(tagName, tagColor, subFolder);
                TagSelect.mAdapter.notifyDataSetChanged();
            }
            finish();

        }


    }

    private Flashbar blankText() {
        return new Flashbar.Builder(this)
                .gravity(Flashbar.Gravity.BOTTOM)
                .title("Blank")
                .message("Please fill in the tag name")
                .dismissOnTapOutside()
                .backgroundColor(Cyanea.getInstance().getAccent())
                .build();

    }

    private Flashbar keyUsed() {
        return new Flashbar.Builder(this)
                .gravity(Flashbar.Gravity.BOTTOM)
                .title("Tag Exists")
                .message("A tag with this name already exists, please choose another one")
                .dismissOnTapOutside()
                .backgroundColor(Cyanea.getInstance().getAccent())
                .build();

    }


    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case DIALOG_ID:
                // We got result from the dialog that is shown when clicking on the icon in the action bar.
                tagColor = color;
                ColorPanelView colorPanelView = (ColorPanelView) findViewById(R.id.tagColorPreview);
                colorPanelView.setColor(color);
                Toast.makeText(CreateTag.this, "Selected Color: #" + Integer.toHexString(color), Toast.LENGTH_SHORT).show();
                final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                fab.setBackgroundTintList(ColorStateList.valueOf(tagColor));
                tagNameEdit.clearFocus();
                setInputTextLayoutColor(tagColor, tagNameEdit);

                if (subFolderSwitch.isChecked()) {
                    subFolderSwitch.getThumbDrawable().setColorFilter(tagColor, PorterDuff.Mode.MULTIPLY);
                    subFolderSwitch.getTrackDrawable().setColorFilter(tagColor, PorterDuff.Mode.MULTIPLY);
                }

                break;
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    @Override
    public void onPause() {
        super.onPause();
        TagList.saveTags(this);
    }
}


