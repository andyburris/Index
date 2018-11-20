package com.andb.apps.todo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.andb.apps.todo.databases.TasksDatabase;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.room.Room;
import androidx.work.WorkManager;

import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static com.andb.apps.todo.MainActivity.DATABASE_NAME;

public class NotificationHandler extends Service {


    private final static String todo_notification_channel = "Task Reminders";

    public static final String workTag = "notifications";

    public static TasksDatabase tasksDatabase;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public static void createNotification(Tasks task, Context ctxt) {
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
            NotificationManager notificationManager = (NotificationManager) ctxt.
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        int key = task.getListKey();

        Intent bodyClickIntent = new Intent(ctxt, NotificationHandler.class);
        bodyClickIntent.putExtra("posFromNotif", key);

        //put together the PendingIntent
        PendingIntent pendingClickIntent =
                PendingIntent.getActivity(ctxt, task.getListKey(), bodyClickIntent, FLAG_UPDATE_CURRENT);


        Intent doneClickIntent = new Intent(ctxt, NotificationHandler.class);
        doneClickIntent.putExtra("reschedule", false);
        doneClickIntent.putExtra("posFromNotifClear", key);

        Intent rescheduleClickIntent = new Intent(ctxt, NotificationHandler.class);
        rescheduleClickIntent.putExtra("reschedule", true);
        rescheduleClickIntent.putExtra("posFromNotifClear", key);


        PendingIntent pendingDoneClickIntent =
                PendingIntent.getService(ctxt, key, doneClickIntent, FLAG_UPDATE_CURRENT);

        PendingIntent pendingRescheduleClickIntent =
                PendingIntent.getService(ctxt, key + 1, rescheduleClickIntent, FLAG_UPDATE_CURRENT);


        String notificationTitle = task.getListName();
        String notificationText = "";

        for (int i = 0; i < task.getAllListItems().size(); i++) {
            notificationText = notificationText.concat(task.getListItems(i) + "\n");
        }
        notificationText = notificationText.concat(task.getDateTime().toString("MMM d, h:mm a"));

        //build the notification
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(ctxt, todo_notification_channel)
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
                NotificationManagerCompat.from(ctxt);

        //we give each notification the ID of the event it's describing,
        //to ensure they all show up and there are no duplicates


        notificationManager.notify(Integer.toString(key), key, notificationBuilder.build());

        Log.d("notificationCreate", "Name: " + task.getListName() + ", Key: " + Integer.toString(key));


        Log.d("serviceRestart", "Before Restart");

        NotifyWorker.nextWork();
    }


    public static void resetNotifications(Context ctxt) {
        WorkManager.getInstance().cancelAllWorkByTag(workTag);
        if (TaskList.getNextNotificationItem() != null) {
            Log.d("workManager", "Next isn't null");
            NotifyWorker.nextWork();
        }
    }

    public static void handleNotification(int key, boolean fromAction, boolean reschedule, Context ctxt) {
        if (key != -1) {
            if (fromAction) {

                initializeDatabase(ctxt);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(ctxt);
                notificationManager.cancel(Integer.toString(key), key);


                if (reschedule) {
                    //reschedule
                    rescheduleTask(key, ctxt);
                } else {
                    //delete
                    deleteTask(key, ctxt);
                }
            } else {
                //open task in taskview
                Intent intent = new Intent(ctxt, MainActivity.class);
            }
        } else {
            Toast.makeText(ctxt, "Couldn't find task", Toast.LENGTH_SHORT).show();
        }


    }

    public static void initializeDatabase(Context ctxt) {
        tasksDatabase = Room.databaseBuilder(ctxt,
                TasksDatabase.class, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    public static void rescheduleTask(final int key, final Context ctxt) {
        Log.d("rescheduleNotification", "rescheduling notification");

        //DateTime taskDateTime = new DateTime();


        Intent intent = new Intent(ctxt, Reschedule.class);
        intent.putExtra("key", key);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        ctxt.startActivity(intent);

        Log.d("rescheduleNotification", "activity called");


    }

    public static void deleteTask(final int key, final Context ctxt) {

        Log.d("deleteNotification", "deleting notification");

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                tasksDatabase.tasksDao().deleteTask(tasksDatabase.tasksDao().findTaskById(key));
                if (checkActive(ctxt)) {
                    TaskList.taskList = new ArrayList<>(tasksDatabase.tasksDao().getAll());
                    EventBus.getDefault().post(new UpdateEvent(true));
                }
            }
        });
    }

    public static boolean checkActive(Context ctxt) {
        SharedPreferences sp = ctxt.getSharedPreferences("OURINFO", MODE_PRIVATE);

        return sp.getBoolean("active", false);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        Bundle bundle = intent.getExtras();

        int key = -1;
        boolean fromAction = false;
        boolean reschedule = false;

        if (bundle != null) {
            if (bundle.containsKey("posFromNotifClear")) {
                key = bundle.getInt("posFromNotifClear", -1);
                fromAction = true;
                if (bundle.containsKey("reschedule")) {
                    reschedule = bundle.getBoolean("reschedule");
                }
            } else if (bundle.containsKey("posFromNotif")) {
                key = bundle.getInt("posFromNotif", -1);
            }


        }

        Log.d("workManager", "Key: " + key + ", delete: " + fromAction + ", reschedule: " + reschedule);


        handleNotification(key, fromAction, reschedule, getApplicationContext());


        return START_NOT_STICKY;
    }
}
