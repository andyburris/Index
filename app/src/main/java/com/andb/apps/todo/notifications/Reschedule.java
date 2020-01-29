package com.andb.apps.todo.notifications;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.andb.apps.todo.data.model.Task;
import com.andb.apps.todo.data.model.reminders.SimpleReminder;
import com.andb.apps.todo.utilities.Current;

import org.joda.time.DateTime;

import androidx.appcompat.app.AppCompatActivity;

public class Reschedule extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final int key = getIntent().getIntExtra("id", -1);

        Log.d("reschedule", "activity launched");
        Log.d("reschedule", "got id: " + key);


        DatePickerDialog dialog = new DatePickerDialog(this, (datePicker, i, i1, i2) -> {
            final DateTime taskDateTime = new DateTime().withDate(i, i1 + 1, i2);

            TimePickerDialog timePickerDialog = new TimePickerDialog(Reschedule.this, (timePicker, i3, i11) -> {

                final DateTime finalDateTime = taskDateTime.withTime(i3, i11, 0, 0);
                AsyncTask.execute(() -> {
                    Task task = Current.database().tasksDao().findTaskById(key);
                    task.getTimeReminders().add(new SimpleReminder(finalDateTime));
                    task.nextReminder().setNotified(false);
                    Current.database().tasksDao().updateTask(task);
/*
                    if (NotificationHandler.Companion.checkActive(Reschedule.this) && Current.project().getId() == task.getProjectId()) {
                        Current.project().setTaskList(new ArrayList<>(Current.database().tasksDao().getAllFromProject(task.getProjectId())));
                        EventBus.getDefault().post(new UpdateEvent(true));
                    }*/
                    NotificationHandler.Companion.resetNotifications();
                    finish();
                });

            }, DateTime.now().getHourOfDay(), DateTime.now().getMinuteOfHour(), false);

            timePickerDialog.show();

        }, DateTime.now().getYear(), DateTime.now().getMonthOfYear() - 1, DateTime.now().getDayOfMonth());
        dialog.show();
    }


}
