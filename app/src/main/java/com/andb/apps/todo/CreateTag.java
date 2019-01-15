package com.andb.apps.todo;

import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.andb.apps.todo.objects.Tags;
import com.andb.apps.todo.utilities.Current;
import com.andb.apps.todo.utilities.ProjectsUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.android.colorpicker.ColorPanelView;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.jaredrummler.cyanea.Cyanea;
import com.jaredrummler.cyanea.app.CyaneaAppCompatActivity;

import java.lang.reflect.Field;

import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;


public class CreateTag extends CyaneaAppCompatActivity implements ColorPickerDialogListener {

    public EditText tagNameEdit;
    public Switch subFolderSwitch;
    public boolean editing;
    public int tagPosition;

    boolean started = false;

    public int tagColor = Cyanea.getInstance().getAccent();
    public boolean subFolder;

    private static final int DIALOG_ID = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_create_tag);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        Bundle bundle = getIntent().getExtras();
        editing = bundle.getBoolean("edit");


        tagNameEdit = findViewById(R.id.createTagName);
        subFolderSwitch = findViewById(R.id.subfolderCreateSwitch);

        setInputTextLayoutColor(tagColor, tagNameEdit);


        if (editing) {

            tagPosition = bundle.getInt("editPos");
            Log.d("taskPosition", Integer.toString(tagPosition));
            tagNameEdit.setText(Current.tagList().get(tagPosition).getTagName());
            tagColor = Current.tagList().get(tagPosition).getTagColor();
            ColorPanelView colorPanelView = (ColorPanelView) findViewById(R.id.tagColorPreview);
            colorPanelView.setColor(tagColor);

            subFolderSwitch.setChecked(Current.tagList().get(tagPosition).isSubFolder());
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

        boolean nameTaken = false;
        for (Tags tags : Current.tagList()) {
            if (tags.getTagName().equals(tagNameEdit.getText().toString())) {
                nameTaken = true;
            }
        }

        if (TextUtils.isEmpty(tagNameEdit.getText())) {
            Snackbar.make(tagNameEdit.getRootView().getRootView(), "Please fill in the tag name", Snackbar.LENGTH_LONG).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show();
        } else if (nameTaken&&!editing) {
            Snackbar.make(tagNameEdit.getRootView().getRootView(), "A tag with this name already exists, please choose another one", Snackbar.LENGTH_LONG).setAnimationMode(Snackbar.ANIMATION_MODE_SLIDE).show();
        } else {
            if (editing) {
                Current.tagList().set(tagPosition, new Tags(tagName, tagColor, subFolder));
                TagSelect.mAdapter.notifyItemChanged(tagPosition);
                ProjectsUtils.update();
            } else {
                Current.tagList().add(new Tags(tagName, tagColor, subFolder));
                TagSelect.mAdapter.notifyDataSetChanged();
                ProjectsUtils.update();
            }
            finish();

        }


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
}


