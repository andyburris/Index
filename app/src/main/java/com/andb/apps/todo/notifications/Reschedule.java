package com.andb.apps.todo.notifications;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.andb.apps.todo.eventbus.UpdateEvent;
import com.andb.apps.todo.objects.Tasks;
import com.andb.apps.todo.objects.reminders.SimpleReminder;
import com.andb.apps.todo.utilities.Current;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class Reschedule extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final int key = getIntent().getIntExtra("key", -1);

        Log.d("reschedule", "activity launched");
        Log.d("reschedule", "got key: " + key);


        DatePickerDialog dialog = new DatePickerDialog(this, (datePicker, i, i1, i2) -> {
            final DateTime taskDateTime = new DateTime().withDate(i, i1 + 1, i2);

            TimePickerDialog timePickerDialog = new TimePickerDialog(Reschedule.this, (timePicker, i3, i11) -> {

                final DateTime finalDateTime = taskDateTime.withTime(i3, i11, 0, 0);
                AsyncTask.execute(() -> {
                    Tasks tasks = Current.database().tasksDao().findTasksById(key);
                    tasks.getTimeReminders().add(new SimpleReminder(finalDateTime));
                    tasks.nextReminder().setNotified(false);
                    Current.database().tasksDao().updateTask(tasks);

                    if (NotificationHandler.Companion.checkActive(Reschedule.this) && Current.project().getKey() == tasks.getProjectId()) {
                        Current.project().setTaskList(new ArrayList<>(Current.database().tasksDao().getAllFromProject(tasks.getProjectId())));
                        EventBus.getDefault().post(new UpdateEvent(true));
                    }
                    NotificationHandler.Companion.resetNotifications();
                    finish();
                });

            }, DateTime.now().getHourOfDay(), DateTime.now().getMinuteOfHour(), false);

            timePickerDialog.show();

        }, DateTime.now().getYear(), DateTime.now().getMonthOfYear() - 1, DateTime.now().getDayOfMonth());
        dialog.show();
    }


}
