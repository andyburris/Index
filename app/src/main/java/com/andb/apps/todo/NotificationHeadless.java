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

    public static int posFromNotif = -1;
    public static boolean deleteFromNotif;

    public static int notifID;


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

        //return START_REDELIVER_INTENT;

    /*}

    @Override
    public void onCreate(){*/


        Log.d("serviceFromNotif", "service started");




        if (bundle != null)

        {

            if (bundle.containsKey("posFromNotifClear")) {
                posFromNotif = bundle.getInt("posFromNotifClear", 0);
                deleteFromNotif = true;
                Log.d("notificationBundle", Boolean.toString(deleteFromNotif));
            }
        }

        notifID = bundle.getInt("notifID");
        Log.d("serviceFromNotif", Integer.toString(notifID));


        if (deleteFromNotif) {


            NotificationManagerCompat notificationManager =
                    NotificationManagerCompat.from(getApplicationContext());
            notificationManager.cancel(notifID);
            Log.d("notificationRemove", Integer.toString(notifID));


            Log.d("notificationRemove", Integer.toString(ArchiveTaskList.taskList.size()));
            ArchiveTaskList.addTaskList(TaskList.getItem(posFromNotif));
            Log.d("notificationRemove", Integer.toString(ArchiveTaskList.taskList.size()));

            TaskList.taskList.remove(posFromNotif);
        }

        SharedPreferences sp = getSharedPreferences("OURINFO", MODE_PRIVATE);

        boolean active = sp.getBoolean("active", false);

        Log.d("datasetchanged", Boolean.toString(active));
        if (active) {
            Log.d("datasetchanged", Boolean.toString(active));

            BrowseFragment.createFilteredTaskList(Filters.getCurrentFilter(), true);
            InboxFragment.mAdapter.notifyDataSetChanged();
        }

        //schedule next work

        return START_REDELIVER_INTENT;


    }
}
