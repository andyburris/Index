package com.andb.apps.todo.notifications;

import com.andb.apps.todo.databases.ProjectsDatabase;
import com.andb.apps.todo.objects.Tasks;
import com.andb.apps.todo.settings.SettingsActivity;
import com.andb.apps.todo.utilities.Current;

import org.joda.time.DateTime;

import java.util.ArrayList;

class NotificationUtils {

    public static Tasks nextNotificationAll(ProjectsDatabase projectsDatabase) {

        Tasks toReturn = null;

        ArrayList<Tasks> allProjectsTaskList = new ArrayList<>(projectsDatabase.tasksDao().getAll());

        for (Tasks task : allProjectsTaskList) {
            if (task.hasDate() && !task.isNotified() && !task.isArchived()) {
                if (toReturn != null) {

                    DateTime compareTime;
                    if (toReturn.hasTime()) {
                        compareTime = toReturn.getDateTime();
                    } else {
                        compareTime = toReturn.getDateTime().withTime(SettingsActivity.getTimeToNotifyForDateOnly().toLocalTime());
                    }

                    if (task.hasTime()) {
                        if (task.getDateTime().isBefore(compareTime)) {
                            toReturn = task;
                        }
                    } else {//untimed tasks
                        if (task.getDateTime().withTime(SettingsActivity.getTimeToNotifyForDateOnly().toLocalTime()).isBefore(compareTime)) {
                            toReturn = task;
                        }
                    }
                } else {
                    toReturn = task;
                }
            }
        }


        return toReturn;
    }


    static Tasks nextNotificationAll() {
        return nextNotificationAll(Current.database());
    }

    static boolean isNextNotification(ProjectsDatabase db) {
        return nextNotificationAll(db) != null;
    }

    static boolean isNextNotification() {
        return nextNotificationAll(Current.database()) != null;
    }
}

