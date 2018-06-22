package com.andb.apps.todo;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

public class NotificationHeadless extends Service {

    public static int notifKey = 0;
    public static boolean deleteFromNotif;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    Bundle bundle;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        bundle = intent.getExtras();

        //just checking
        if (bundle != null)
            Toast.makeText(this, Integer.toString(bundle.getInt("posFromNotifClear")), Toast.LENGTH_SHORT).show();
        else {
            Toast.makeText(this, "NULL", Toast.LENGTH_SHORT).show();

        }


        if (TaskList.taskList == null || TaskList.taskList.isEmpty()) {
            TaskList.loadTasks(getApplicationContext());

        }

        Log.d("workManager", "Size: " + TaskList.taskList.size());

        if (TaskList.keyList == null || TaskList.keyList.isEmpty()) {
            TaskList.keyList.clear();
            for (int i = 0; i < TaskList.taskList.size(); i++) {
                TaskList.keyList.add(TaskList.getItem(i).getKey());
                Log.d("workManager", "Size: " + TaskList.keyList.size());

            }
        }

        Log.d("serviceFromNotif", "service started");




        if (bundle != null)

        {

            if (bundle.containsKey("posFromNotifClear")) {
                notifKey = bundle.getInt("posFromNotifClear", 0);
                deleteFromNotif = true;


                Log.d("notificationBundle", Boolean.toString(deleteFromNotif));
            }
        }


        Log.d("workManager", "Key: " + notifKey + ", delete: " + deleteFromNotif);



        if (deleteFromNotif) {


            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(getApplicationContext());
            notificationManager.cancel(Integer.toString(notifKey), notifKey);
            Log.d("notificationRemove", Integer.toString(notifKey));


            Log.d("notificationRemoveKey", Integer.toString(notifKey));
            int index = TaskList.keyList.indexOf(notifKey);


            Log.d("workManager", "Index: " + index + ", Size: " + TaskList.keyList.size());


            Log.d("notificationRemove", Integer.toString(TaskList.taskList.size()));
            Log.d("notificationRemoveIndex", Integer.toString(index));
            if (index != -1) {
                Log.d("notificationRemove", Integer.toString(ArchiveTaskList.taskList.size()));
                ArchiveTaskList.addTaskList(TaskList.getItem(TaskList.keyList.indexOf(notifKey)));
                Log.d("notificationRemove", Integer.toString(ArchiveTaskList.taskList.size()));

                Log.d("workManager", "Size Before: " + TaskList.taskList.size());

                TaskList.taskList.remove(TaskList.keyList.indexOf(notifKey));
                TaskList.keyList.remove((Integer) notifKey);

                Log.d("workManager", "Size After: " + TaskList.taskList.size());

            } else {
                Toast.makeText(this, "Couldn't find that task", Toast.LENGTH_SHORT).show();
            }
            Log.d("notificationRemove", Integer.toString(TaskList.taskList.size()));

            ArchiveTaskList.saveTasks(getApplicationContext());
            TaskList.saveTasks(getApplicationContext());

            Log.d("workManager", "Size: " + TaskList.taskList.size());


        }

        SharedPreferences sp = getSharedPreferences("OURINFO", MODE_PRIVATE);

        boolean active = sp.getBoolean("active", false);

        Log.d("datasetchanged", Boolean.toString(active));
        if (active) {
            Log.d("datasetchanged", Boolean.toString(active));

            if (Filters.backTagFilters.isEmpty()) {
                Filters.homeViewAdd();
            }
            BrowseFragment.createFilteredTaskList(Filters.getCurrentFilter(), true);
            InboxFragment.mAdapter.notifyDataSetChanged();
        }
        return START_NOT_STICKY;


    }
}
