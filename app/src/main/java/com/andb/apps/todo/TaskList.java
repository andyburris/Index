package com.andb.apps.todo;

import android.os.AsyncTask;
import android.util.Log;

import com.andb.apps.todo.databases.TasksDatabase;
import com.andb.apps.todo.settings.SettingsActivity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;

public class TaskList extends AppCompatActivity {
    public static ArrayList<Tasks> taskList = new ArrayList<>();
    public static String savedList = "taskList";

    public static ArrayList<Integer> keyList = new ArrayList<>();


    public static Tasks getItem(int position) {
        return taskList.get(position);
    }

    public static void setTaskList(int position, Tasks tasks) {
        taskList.set(position, tasks);
    }

    public static void addTaskList(Tasks task) {
        taskList.add(task);
    }

    public static void addTaskListAtPos(int pos, Tasks task) {
        taskList.add(pos, task);
    }


    public static Tasks getNextNotificationItem() {

        //Log.d("notScheduled", "getting item, beingUsed = " + Boolean.toString(beingUsed));

        Tasks finalTask = new Tasks();


        DateTime returnDateTime = new DateTime(3000, 1, 1, 0, 0);


        boolean notificationsLeft = false;
        for (int i = 0; i < taskList.size(); i++) {
            //Log.d("notScheduled", "starting loop"); potentially not finding all
            if (taskList.get(i).isListTime()) {
                Log.d("notScheduled", taskList.get(i).getListName() + " is Time");
                Tasks returnTask = taskList.get(i);

                if (returnTask.getDateTime().get(DateTimeFieldType.secondOfMinute()) == 59) {
                    Log.d("notScheduled", taskList.get(i).getListName() + " is Date only");

                    DateTime beforeTime = new DateTime(returnDateTime);

                    returnDateTime = returnTask.getDateTime().withTime(SettingsActivity.timeToNotifyForDateOnly.toLocalTime());

                    if ((returnDateTime.isBefore(beforeTime) || returnDateTime.isEqual(beforeTime)) && !returnTask.isNotified()) {


                        finalTask = returnTask;
                        notificationsLeft = true;
                        Log.d("notificationLastPosName", returnTask.getListName());
                        Log.d("alreadyNotifiedFilter", Boolean.toString(taskList.get(i).isNotified()));
                    } else {
                        returnDateTime = beforeTime;
                    }

                } else {
                    Log.d("notScheduled", taskList.get(i).getListName() + " has time");
                    if (taskList.get(i).getDateTime().isBefore(returnDateTime) && !taskList.get(i).isNotified()) {


                        returnDateTime = returnTask.getDateTime();

                        finalTask = returnTask;

                        notificationsLeft = true;
                        Log.d("notificationLastPosName", returnTask.getListName());
                        Log.d("alreadyNotifiedFilter", Boolean.toString(taskList.get(i).isNotified()));

                    }
                }

            }
        }
        if (notificationsLeft) {
            return finalTask;
        }

        return null;
    }

    public static Tasks getNextNotificationItem(final TasksDatabase toUpdate) {
        Tasks finalTask = getNextNotificationItem();

        taskList.get(taskList.indexOf(finalTask)).setNotified(true);
        Log.d("alreadyNotifiedFilterNm", finalTask.getListName());
        Log.d("alreadyNotifiedFilterNt", Boolean.toString(finalTask.isNotified()));
        Log.d("returnedTime", finalTask.getDateTime().toString("MMM d h:mm a"));
        final Tasks taskToUpdate = finalTask;
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                toUpdate.tasksDao().updateTask(taskToUpdate);
            }
        });

        return finalTask;
    }


    /*public static void loadTasks(Context ctxt) {
        taskList = getArrayList(savedList, ctxt);
        Log.d("resume", "Tasks Initialized");
    }

    public static void saveTasks(Context ctxt) {
        saveArrayList(taskList, savedList, ctxt);
        Log.d("pause", "Tasks Saved");
    }

    public static void saveArrayList(ArrayList<Tasks> task, String key, Context ctxt) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        SharedPreferences.Editor editor = prefs.edit();
        //Gson gson = Converters.registerDateTime(new GsonBuilder()).create();
        Gson gson = new Gson();
        String json = gson.toJson(task);
        editor.putString(key, json);
        editor.apply();     // This line is IMPORTANT !!!
    }

    public static ArrayList<Tasks> getArrayList(String key, Context ctxt) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctxt);
        //Gson gson = Converters.registerDateTime(new GsonBuilder()).create();
        Gson gson = new Gson();
        String json = prefs.getString(key, null);
        Type type = new TypeToken<ArrayList<Tasks>>() {
        }.getType();
        return gson.fromJson(json, type);
        //ArrayList<TaskMigrate> taskMigrates =  gson.fromJson(json, type);

        /*ArrayList<Tasks> tasksArrayList = new ArrayList<>();

        for(int i = 0; i<taskMigrates.size(); i++){

            tasksArrayList.add(taskMigrates.get(i).toTask());
        }
        return tasksArrayList;
    */
    //}


}
