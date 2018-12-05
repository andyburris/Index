package com.andb.apps.todo.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;

import org.joda.time.DateTime;

public class TimePreference extends DialogPreference {
    private int lastHour = 0;
    private int lastMinute = 0;
    private TimePicker picker = null;

    public static int getHour(String time) {
        String[] pieces = time.split(":");

        return (Integer.parseInt(pieces[0]));
    }

    public static int getMinute(String time) {
        String[] pieces = time.split(":");

        return (Integer.parseInt(pieces[1]));
    }

    public TimePreference(Context ctxt, AttributeSet attrs) {
        super(ctxt, attrs);

        setPositiveButtonText("Set");
        setNegativeButtonText("Cancel");
    }

    @Override
    protected View onCreateDialogView() {
        picker = new TimePicker(getContext());

        return (picker);
    }

    @Override
    protected void onBindDialogView(View v) {
        super.onBindDialogView(v);

        picker.setCurrentHour(lastHour);
        picker.setCurrentMinute(lastMinute);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            lastHour = picker.getCurrentHour();
            lastMinute = picker.getCurrentMinute();

            DateTime dateTime = new DateTime().withHourOfDay(lastHour).withMinuteOfHour(lastMinute).withSecondOfMinute(0);
            String time = String.valueOf(lastHour) + ":" + String.valueOf(lastMinute);

            if (callChangeListener(dateTime)) {
                persistLong(dateTime.getMillis());
            }
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return (a.getString(index));
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        String time = SettingsActivity.timeToNotifyForDateOnly.toString("h:m");

        Log.d("loadedTime", Boolean.toString(restoreValue));

        /*if (restoreValue) {
            if (defaultValue==null) {
                time=getPersistedLong(0);
            }
            else {
                Log.d("loadedTime", defaultValue.toString());
                time=getPersistedLong((Long) defaultValue);
            }
        }
        else {
            time=((Long) defaultValue);
        }*/

        lastMinute = getMinute(time);
        lastHour = getHour(time);


    }
}
