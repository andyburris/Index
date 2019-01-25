package com.andb.apps.todo.notifications;

import android.os.AsyncTask;
import android.util.Log;

import com.andb.apps.todo.databases.ProjectsDatabase;
import com.andb.apps.todo.lists.ProjectList;
import com.andb.apps.todo.objects.Project;
import com.andb.apps.todo.objects.Tasks;
import com.andb.apps.todo.settings.SettingsActivity;
import com.andb.apps.todo.utilities.ProjectsUtils;

import org.joda.time.DateTime;

import java.util.ArrayList;

public class NotificationUtils {

    public static Tasks nextNotificationAll(ProjectsDatabase projectsDatabase, boolean update){
        ArrayList<Tasks> allProjectsTaskList = new ArrayList<>(projectsDatabase.tasksDao().getAll());

        Tasks toReturn = null;

        for(Tasks task : allProjectsTaskList){
            if(!task.isNotified()) {
                if (toReturn != null) {
                    DateTime compareTime;
                    if (toReturn.getDateTime().getSecondOfMinute() != 59) {
                        compareTime = toReturn.getDateTime();
                    } else {
                        compareTime = toReturn.getDateTime().withTime(SettingsActivity.getTimeToNotifyForDateOnly().toLocalTime());
                    }

                    if (task.getDateTime().getSecondOfMinute() != 59) {
                        if (task.getDateTime().isBefore(compareTime)) {
                            toReturn = task;
                        }
                    } else {//untimed tasks
                        if (task.getDateTime().withTime(SettingsActivity.getTimeToNotifyForDateOnly().toLocalTime()).isBefore(compareTime)) {
                            toReturn = task;
                        }
                    }
                } else {
                    if (!task.getDateTime().equals(new DateTime(3000, 1, 1, 0, 0))) {
                        toReturn = task;
                    }
                }
            }
        }

        if(update && toReturn!=null){
            toReturn.setNotified(true);
        }

        return toReturn;
    }

    public static Tasks nextNotificationAll(ProjectsDatabase projectsDatabase){
        return nextNotificationAll(projectsDatabase, false);
    }

    public static Tasks nextNotificationAll(){
        return nextNotificationAll(NotificationHandler.projectsDatabase, false);
    }

    public static boolean isNextNotification(ProjectsDatabase db){
        return nextNotificationAll(db) != null;
    }

    public static boolean isNextNotification(){
        return nextNotificationAll(NotificationHandler.projectsDatabase) != null;
    }
}

