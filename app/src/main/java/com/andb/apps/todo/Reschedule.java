package com.andb.apps.todo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.andb.apps.todo.lists.TaskList;
import com.andb.apps.todo.notifications.NotificationHandler;
import com.andb.apps.todo.objects.Tasks;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

import static com.andb.apps.todo.notifications.NotificationHandler.tasksDatabase;

public class Reschedule extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int tempKey = -1;

        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("key")) {
            tempKey = bundle.getInt("key");
        }

        final int key = tempKey;

        Log.d("reschedule", "activity launched");
        Log.d("reschedule", "got key: " + key);


        DatePickerDialog dialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                final DateTime taskDateTime = new DateTime().withDate(i, i1 + 1, i2);
                TimePickerDialog timePickerDialog = new TimePickerDialog(Reschedule.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        final DateTime finalDateTime = taskDateTime.withTime(i, i1, 0, 0);
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                Tasks tasks = tasksDatabase.tasksDao().findTaskById(key);
                                tasks.setDateTime(finalDateTime);
                                tasks.setNotified(false);
                                tasksDatabase.tasksDao().updateTask(tasks);
                                if (NotificationHandler.checkActive(Reschedule.this)) {
                                    TaskList.taskList = new ArrayList<>(tasksDatabase.tasksDao().getAll());
                                    EventBus.getDefault().post(new UpdateEvent(true));
                                }
                            }
                        });
                    }
                }, DateTime.now().getHourOfDay(), DateTime.now().getMinuteOfHour(), false);
                timePickerDialog.show();
            }

        }, DateTime.now().getYear(), DateTime.now().getMonthOfYear() - 1, DateTime.now().getDayOfMonth());
        dialog.show();
    }


}
