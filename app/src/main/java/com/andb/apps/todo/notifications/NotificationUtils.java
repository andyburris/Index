package com.andb.apps.todo.notifications;

import com.andb.apps.todo.databases.ProjectsDatabase;
import com.andb.apps.todo.objects.Tasks;
import com.andb.apps.todo.settings.SettingsActivity;
import com.andb.apps.todo.utilities.Current;

import org.joda.time.DateTime;

import java.util.ArrayList;

class NotificationUtils {

    static Tasks nextNotificationAll(ProjectsDatabase projectsDatabase) {

        Tasks toReturn = null;

        ArrayList<Tasks> allProjectsTaskList = new ArrayList<Tasks>(projectsDatabase.tasksDao().getAll().getValue());

        for (Tasks task : allProjectsTaskList) {
            if (!task.getTimeReminders().isEmpty() && !task.nextReminder().getNotified() && !task.isArchived()) {
                if (toReturn != null) {

                    DateTime compareTime = toReturn.nextReminderTime();

                        if (task.nextReminderTime().isBefore(compareTime)) {
                            toReturn = task;
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

