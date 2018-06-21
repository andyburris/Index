package com.andb.apps.todo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import java.util.Random;

import androidx.work.Worker;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;

public class NotifyWorker extends Worker {

    private final static String todo_notification_channel = "Task Reminders";
    public static int lastItemPos; // TODO: 6/20/2018 check if higher and if so decrease by one on any remove

    @NonNull
    @Override
    public Worker.Result doWork() {
        // Method to trigger an instant notification
        triggerNotification(TaskList.getNextNotificationItem(lastItemPos));

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
        bodyClickIntent.putExtra("posFromNotif", TaskList.taskList.indexOf(task));

        //put together the PendingIntent
        PendingIntent pendingClickIntent =
                PendingIntent.getActivity(getApplicationContext(), 1, bodyClickIntent, FLAG_UPDATE_CURRENT);


        Intent doneClickIntent = new Intent(getApplicationContext(), MainActivity.class);
        doneClickIntent.putExtra("posFromNotifClear", TaskList.taskList.indexOf(task));

        PendingIntent pendingDoneClickIntent =
                PendingIntent.getActivity(getApplicationContext(), 2, doneClickIntent, FLAG_UPDATE_CURRENT);


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
                        .addAction(R.drawable.ic_todo_small, "DONE", pendingDoneClickIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        //trigger the notification
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(getApplicationContext());

        //we give each notification the ID of the event it's describing,
        //to ensure they all show up and there are no duplicates
        notificationManager.notify(new Random().nextInt(), notificationBuilder.build());

        lastItemPos = TaskList.taskList.lastIndexOf(task);
    }
}


