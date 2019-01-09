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

public class NotificationUtils {

    public static Tasks nextNotificationInProject(Project project){
        Tasks toReturn = null;
        for (Tasks tasks : project.getTaskList()){

            if(!tasks.isNotified()) {
                if (toReturn != null) {
                    DateTime compareTime;
                    if (toReturn.getDateTime().getSecondOfMinute() != 59) {
                        compareTime = toReturn.getDateTime();
                    } else {
                        compareTime = toReturn.getDateTime().withTime(SettingsActivity.getTimeToNotifyForDateOnly().toLocalTime());
                    }

                    if (tasks.getDateTime().getSecondOfMinute() != 59) {
                        if (tasks.getDateTime().isBefore(compareTime)) {
                            toReturn = tasks;
                        }
                    } else {//untimed tasks
                        if (tasks.getDateTime().withTime(SettingsActivity.getTimeToNotifyForDateOnly().toLocalTime()).isBefore(compareTime)) {
                            toReturn = tasks;
                        }
                    }
                } else {
                    if (!tasks.getDateTime().equals(new DateTime(3000, 1, 1, 0, 0))) {
                        toReturn = tasks;
                    }
                }
            }
        }
        return toReturn;
    }

    public static NotificationObject nextNotificationAll(){

        int project = -1;

        Tasks tasks = null;
        for (Project p : ProjectList.INSTANCE.getProjectList()){
            if (tasks!=null) {
                if (nextNotificationInProject(p)!=null && nextNotificationInProject(p).getDateTime().isBefore(tasks.getDateTime())) {
                    tasks = nextNotificationInProject(p);
                    project = p.getKey();
                }
            }else {
                if (nextNotificationInProject(p)!=null){
                    tasks = nextNotificationInProject(p);
                    project = p.getKey();
                }
            }
        }

        if(tasks!=null) {
            Log.d("nextNotif", tasks.toString());
        }else {
            Log.d("nextNotif", "none");
        }

        return new NotificationObject(project, tasks);

    }
    public static NotificationObject nextNotificationAll(ProjectsDatabase projectsDatabase){
        NotificationObject toReturn = nextNotificationAll();

        Project project = ProjectsUtils.projectFromKey(toReturn.projectKey);
        project.getTaskList().get(project.getTaskList().indexOf(toReturn.task)).setNotified(true);
        AsyncTask.execute(()-> projectsDatabase.projectsDao().updateProject(project));
        return toReturn;
    }

    public static boolean isNextNotification(){
        return nextNotificationAll().task != null;
    }
}

class NotificationObject{
    public int projectKey;
    public Tasks task;

    public NotificationObject(int projectKey, Tasks task){
        this.projectKey = projectKey;
        this.task = task;
    }
}
