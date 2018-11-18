package com.andb.apps.todo;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;

public class NotifyWorker extends Worker {

    private final static String todo_notification_channel = "Task Reminders";

    public static final String workTag = "notifications";

    public static boolean retry;

    @NonNull
    @Override
    public Worker.Result doWork() {
        // Method to trigger an instant notification
        Log.d("workManager", "Running NotifyWorker");


        NotificationHandler.initializeDatabase(getApplicationContext());
        TaskList.taskList = new ArrayList<>(NotificationHandler.tasksDatabase.tasksDao().getAll());


        if (SettingsActivity.timeToNotifyForDateOnly == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Gson gson = new Gson();

            SettingsActivity.timeToNotifyForDateOnly = new DateTime(prefs.getLong("pref_notify_only_date", 0));
        }

        if (TaskList.getNextNotificationItem(false) != null) {
            Log.d("workManager", "Next isn't null");
            NotificationHandler.createNotification(TaskList.getNextNotificationItem(true), getApplicationContext());
        }

        return Worker.Result.SUCCESS;
        // (Returning RETRY tells WorkManager to try this task again
        // later; FAILURE says not to try again.)
    }

    public static void nextWork() {

        //Here we set the request for the next notification

        if (TaskList.getNextNotificationItem(false) != null) {//if there are any left, restart the service

            Log.d("serviceRestart", "Service Restarting");


            Duration duration = new Duration(DateTime.now().withSecondOfMinute(0), TaskList.getNextNotificationItem(false).getDateTime());
            if (TaskList.getNextNotificationItem(false).getDateTime().get(DateTimeFieldType.secondOfMinute()) == (59)) {
                DateTime onlyDate = TaskList.getNextNotificationItem(false).getDateTime();
                onlyDate = onlyDate.withTime(SettingsActivity.timeToNotifyForDateOnly.toLocalTime());
                duration = new Duration(DateTime.now(), onlyDate);
            }
            long delay = duration.getStandardSeconds();

            OneTimeWorkRequest notificationWork = new OneTimeWorkRequest.Builder(NotifyWorker.class)
                    .setInitialDelay(delay, TimeUnit.SECONDS)
                    .addTag(workTag)
                    .build();


            WorkManager.getInstance().enqueue(notificationWork);

        }

    }
}




