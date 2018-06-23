package com.andb.apps.todo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.fatboyindustrial.gsonjodatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Duration;

import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

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
            Gson gson = Converters.registerDateTime(new GsonBuilder()).create();
            String json = prefs.getString("pref_notify_only_date", null);
            Type type = new TypeToken<DateTime>() {
            }.getType();
            SettingsActivity.timeToNotifyForDateOnly = gson.fromJson(json, type);
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

        Intent bodyClickIntent = new Intent(getApplicationContext(), MainActivity.class);
        bodyClickIntent.putExtra("posFromNotif", task.getKey());

        //put together the PendingIntent
        PendingIntent pendingClickIntent =
                PendingIntent.getActivity(getApplicationContext(), task.getKey(), bodyClickIntent, FLAG_UPDATE_CURRENT);




        Intent doneClickIntent = new Intent(getApplicationContext(), NotificationHeadless.class);
        doneClickIntent.putExtra("posFromNotifClear", task.getKey());


        PendingIntent pendingDoneClickIntent =
                PendingIntent.getService(getApplicationContext(), task.getKey(), doneClickIntent, FLAG_UPDATE_CURRENT);


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
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //trigger the notification
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());

        //we give each notification the ID of the event it's describing,
        //to ensure they all show up and there are no duplicates


        notificationManager.notify(Integer.toString(task.getKey()), task.getKey(), notificationBuilder.build());

        Log.d("notificationCreate", "Name: " + task.getListName() + ", Key: " + Integer.toString(task.getKey()));


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




