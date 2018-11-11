package com.andb.apps.todo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Duration;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class NotifyWorker extends Worker {

    private final static String todo_notification_channel = "Task Reminders";

    public static final String workTag = "notifications";

    public static boolean retry;

    @NonNull
    @Override
    public Worker.Result doWork() {
        // Method to trigger an instant notification
        Log.d("workManager", "Running NotifyWorker");

        if (TaskList.taskList == null || TaskList.taskList.isEmpty()) {
            TaskList.loadTasks(getApplicationContext());
        }

        if (SettingsActivity.timeToNotifyForDateOnly == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Gson gson = new Gson();

            SettingsActivity.timeToNotifyForDateOnly = new DateTime(prefs.getLong("pref_notify_only_date", 0));
        }

        if (TaskList.getNextNotificationItem(false) != null) {
            Log.d("workManager", "Next isn't null");
            triggerNotification(TaskList.getNextNotificationItem(true));
        }

        return Worker.Result.SUCCESS;
        // (Returning RETRY tells WorkManager to try this task again
        // later; FAILURE says not to try again.)
    }

    public void triggerNotification(Tasks task) {

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //define the importance level of the notification
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            //build the actual notification channel, giving it a unique ID and name
            NotificationChannel channel =
                    new NotificationChannel(todo_notification_channel, todo_notification_channel, importance);

            //we can optionally add a description for the channel
            String description = "Shows notifications for tasks when they are due";
            channel.setDescription(description);

            //we can optionally set notification LED colour
            channel.setLightColor(SettingsActivity.themeColor);

            // Register the channel with the system
            NotificationManager notificationManager = (NotificationManager) getApplicationContext().
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        int key = task.getListKey();

        Intent bodyClickIntent = new Intent(getApplicationContext(), MainActivity.class);
        bodyClickIntent.putExtra("posFromNotif", key);

        //put together the PendingIntent
        PendingIntent pendingClickIntent =
                PendingIntent.getActivity(getApplicationContext(), task.getListKey(), bodyClickIntent, FLAG_UPDATE_CURRENT);




        Intent doneClickIntent = new Intent(getApplicationContext(), NotificationHeadless.class);
        doneClickIntent.putExtra("reschedule", false);
        doneClickIntent.putExtra("posFromNotifClear", key);

        Intent rescheduleClickIntent = new Intent(getApplicationContext(), NotificationHeadless.class);
        rescheduleClickIntent.putExtra("reschedule", true);
        rescheduleClickIntent.putExtra("posFromNotifClear", key);


        PendingIntent pendingDoneClickIntent =
                PendingIntent.getService(getApplicationContext(), key, doneClickIntent, FLAG_UPDATE_CURRENT);

        PendingIntent pendingRescheduleClickIntent =
                PendingIntent.getService(getApplicationContext(), key, rescheduleClickIntent, FLAG_UPDATE_CURRENT);


        String notificationTitle = task.getListName();
        String notificationText = "";

        for (int i = 0; i < task.getAllListItems().size(); i++) {
            notificationText = notificationText.concat(task.getListItems(i) + "\n");
        }
        notificationText = notificationText.concat(task.getDateTime().toString("MMM d, h:mm a"));

        //build the notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(getApplicationContext(), todo_notification_channel)
                        .setSmallIcon(R.drawable.ic_todo_small)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationText)
                        .setContentIntent(pendingClickIntent)
                        .setAutoCancel(true)
                        .addAction(R.drawable.ic_check_white_24dp, "DONE", pendingDoneClickIntent)
                        .addAction(R.drawable.ic_access_time_black_24dp, "RESCHEDULE", pendingRescheduleClickIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //trigger the notification
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());

        //we give each notification the ID of the event it's describing,
        //to ensure they all show up and there are no duplicates


        notificationManager.notify(Integer.toString(key), key, notificationBuilder.build());

        Log.d("notificationCreate", "Name: " + task.getListName() + ", Key: " + Integer.toString(key));


        Log.d("serviceRestart", "Before Restart");




        //Here we set the request for the next notification

        if (TaskList.getNextNotificationItem(false) != null) {//if there are any left, restart the service

            Log.d("serviceRestart", "Service Restarting");


            Duration duration = new Duration(DateTime.now().withSecondOfMinute(0), TaskList.getNextNotificationItem(false).getDateTime());
            if (TaskList.getNextNotificationItem(false).getDateTime().get(DateTimeFieldType.secondOfMinute()) == (59)) {
                notificationTitle = TaskList.getNextNotificationItem(false).getDateTime().toString("MMM d");
                notificationBuilder.setContentTitle(notificationTitle);
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

        TaskList.saveTasks(getApplicationContext());
    }
}




