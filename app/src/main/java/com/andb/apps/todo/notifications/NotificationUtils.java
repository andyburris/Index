package com.andb.apps.todo.notifications;

import com.andb.apps.todo.data.local.Database;
import com.andb.apps.todo.data.model.Task;
import com.andb.apps.todo.utilities.Current;

import org.joda.time.DateTime;

import java.util.ArrayList;

class NotificationUtils {

    static Task nextNotificationAll(Database database) {

        Task toReturn = null;

        ArrayList<Task> allProjectsTaskList = new ArrayList<Task>(database.tasksDao().getAllStatic());

        for (Task task : allProjectsTaskList) {
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


    static Task nextNotificationAll() {
        return nextNotificationAll(Current.database());
    }

    static boolean isNextNotification(Database db) {
        return nextNotificationAll(db) != null;
    }

    static boolean isNextNotification() {
        return nextNotificationAll(Current.database()) != null;
    }
}

