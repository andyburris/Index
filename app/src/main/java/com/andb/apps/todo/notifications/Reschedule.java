package com.andb.apps.todo.notifications;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.andb.apps.todo.eventbus.UpdateEvent;
import com.andb.apps.todo.lists.ProjectList;
import com.andb.apps.todo.objects.Project;
import com.andb.apps.todo.objects.Tasks;
import com.andb.apps.todo.utilities.ProjectsUtils;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

import static com.andb.apps.todo.notifications.NotificationHandler.projectsDatabase;

public class Reschedule extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int tempKey = -1;
        int tempIndex = -1;

        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("key")) {
            tempKey = bundle.getInt("key");
        }
        if (bundle.containsKey("projectKey")) {
            tempIndex = bundle.getInt("projectKey");
        }

        final int key = tempKey;
        final int projectKey = tempIndex;

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
                                ProjectList.INSTANCE.setProjectList(new ArrayList<>(projectsDatabase.projectsDao().getAll()));
                                Project project = ProjectsUtils.projectFromKey(projectKey);
                                Tasks tasks = null;
                                for (Tasks t : project.getTaskList()) {
                                    if (t.getListKey() == key) {
                                        tasks = t;
                                    }
                                }
                                if (tasks != null) {
                                    tasks.setDateTime(finalDateTime);
                                    tasks.setNotified(false);
                                    project.getTaskList().set(project.getTaskList().indexOf(tasks), tasks);
                                    projectsDatabase.projectsDao().updateProject(project);
                                    if (NotificationHandler.checkActive(Reschedule.this)) {
                                        ProjectList.INSTANCE.setProjectList(new ArrayList<>(projectsDatabase.projectsDao().getAll()));
                                        EventBus.getDefault().post(new UpdateEvent(true));
                                    }
                                }
                                finish();
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
