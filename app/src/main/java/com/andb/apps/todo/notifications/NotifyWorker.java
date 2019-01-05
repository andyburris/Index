package com.andb.apps.todo.notifications;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.andb.apps.todo.lists.ProjectList;
import com.andb.apps.todo.objects.Tasks;
import com.andb.apps.todo.settings.SettingsActivity;
import com.andb.apps.todo.utilities.ProjectsUtils;
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
        ProjectList.INSTANCE.setProjectList(new ArrayList<>(NotificationHandler.projectsDatabase.projectsDao().getAll()));


        if (SettingsActivity.Companion.getTimeToNotifyForDateOnly() == null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            Gson gson = new Gson();

            SettingsActivity.Companion.setTimeToNotifyForDateOnly(new DateTime(prefs.getLong("pref_notify_only_date", 0)));
        }

        if ((int)ProjectsUtils.nextNotificationAll().get(1)>0) {
            Log.d("workManager", "Next isn't null");
            ArrayList<Object> nextNotif = ProjectsUtils.nextNotificationAll(NotificationHandler.projectsDatabase);
            NotificationHandler.createNotification((Tasks) nextNotif.get(0), (int)nextNotif.get(1), getApplicationContext());
        }

        return Worker.Result.SUCCESS;
        // (Returning RETRY tells WorkManager to try this task again
        // later; FAILURE says not to try again.)
    }

    public static void nextWork() {

        //Here we set the request for the next notification

        if ((int)ProjectsUtils.nextNotificationAll().get(1) > 0) {//if there are any left, restart the service

            Log.d("serviceRestart", "Service Restarting");


            Duration duration = new Duration(DateTime.now().withSecondOfMinute(0), ((Tasks)ProjectsUtils.nextNotificationAll().get(0)).getDateTime());
            if (((Tasks)ProjectsUtils.nextNotificationAll().get(0)).getDateTime().get(DateTimeFieldType.secondOfMinute()) == (59)) {
                DateTime onlyDate = ((Tasks)ProjectsUtils.nextNotificationAll().get(0)).getDateTime();
                onlyDate = onlyDate.withTime(SettingsActivity.Companion.getTimeToNotifyForDateOnly().toLocalTime());
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




