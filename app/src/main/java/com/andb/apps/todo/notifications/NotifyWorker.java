package com.andb.apps.todo.notifications;

import android.content.Context;
import android.util.Log;

import com.andb.apps.todo.lists.ProjectList;
import com.andb.apps.todo.objects.Tasks;
import com.andb.apps.todo.settings.SettingsActivity;
import com.andb.apps.todo.utilities.ProjectsUtils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Duration;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class NotifyWorker extends Worker {

    public NotifyWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    public static final String workTag = "notifications";

    @NonNull
    @Override
    public Worker.Result doWork() {
        // Method to trigger an instant notification
        Log.d("workManager", "Running NotifyWorker");


        NotificationHandler.initializeDatabase(getApplicationContext());
        ProjectsUtils.setupProjectList(NotificationHandler.projectsDatabase);


        if (NotificationUtils.isNextNotification()) {
            Log.d("workManager", "Next isn't null");
            NotificationObject nextNotif = NotificationUtils.nextNotificationAll(NotificationHandler.projectsDatabase);
            NotificationHandler.createNotification(nextNotif.task, nextNotif.projectKey, getApplicationContext());
        }

        return Result.success();
        // (Returning RETRY tells WorkManager to try this task again
        // later; FAILURE says not to try again.)
    }

    public static void nextWork() {

        //Here we set the request for the next notification

        if (NotificationUtils.isNextNotification()) {//if there are any left, restart the service

            Log.d("serviceRestart", "Service Restarting");


            Duration duration = new Duration(DateTime.now().withSecondOfMinute(0), (NotificationUtils.nextNotificationAll().task).getDateTime());

            if ((NotificationUtils.nextNotificationAll().task).getDateTime().get(DateTimeFieldType.secondOfMinute()) == (59)) {//check for no-time reminders
                DateTime onlyDate = (NotificationUtils.nextNotificationAll().task).getDateTime();
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




