package com.andb.apps.todo.lists.interfaces;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.andb.apps.todo.MainActivity;
import com.andb.apps.todo.eventbus.UpdateEvent;
import com.andb.apps.todo.lists.ArchiveTaskList;
import com.andb.apps.todo.lists.TaskList;
import com.andb.apps.todo.objects.Tasks;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Random;

public class TaskListInterface {

    public static void addTask(final String title, final ArrayList<String> items, final ArrayList<Boolean> checked, final ArrayList<Integer> tags, final DateTime time) {

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int key = new Random().nextInt();
                while (TaskList.keyList.contains(key) || key == 0) {
                    key = new Random().nextInt();
                }

                Tasks tasks = new Tasks(title, items, checked, tags, time, false, key);

                MainActivity.tasksDatabase.tasksDao().insertOnlySingleTask(tasks);
                TaskList.taskList = new ArrayList<>(MainActivity.tasksDatabase.tasksDao().getAll());

                EventBus.getDefault().post(new UpdateEvent(true));
            }


        });

    }

    public static void replaceTask(final String title, final ArrayList<String> items, final ArrayList<Boolean> checked, final ArrayList<Integer> tags, final DateTime time, final boolean notified, final int position, final int key, Context ctxt) {


        //TaskList.setTaskList(position, tasks);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Tasks tasks = new Tasks(title, items, checked, tags, time, notified, key);

                MainActivity.tasksDatabase.tasksDao().updateTask(tasks);

                TaskList.taskList = new ArrayList<>(MainActivity.tasksDatabase.tasksDao().getAll());

                EventBus.getDefault().post(new UpdateEvent(true));
            }
        });

    }

    public static void removeTask(final Tasks tasks) {

        Log.d("removeTask", "removing task");



        ArchiveTaskList.addTaskList(tasks);
        TaskList.keyList.remove((Integer) tasks.getListKey());
        TaskList.taskList.remove(tasks);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                MainActivity.tasksDatabase.tasksDao().deleteTask(tasks);
            }
        });




    }

    public static void removeTagFromAll(int pos){
        if (!TaskList.taskList.isEmpty()) {
            for (final Tasks tasks : TaskList.taskList) {
                if (tasks.isListTags()) {
                    ArrayList<Integer> toRemove = new ArrayList<>();
                    for (int tag : tasks.getAllListTags()) {
                        if (tag == pos) {
                            toRemove.add(tag);
                        } else if (tag > pos) {
                            tasks.setListTags(tasks.getAllListTags().indexOf(tag), tag - 1);
                        }
                    }
                    tasks.getAllListTags().removeAll(toRemove);
                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.tasksDatabase.tasksDao().updateTask(tasks);
                        }
                    });
                }
            }
        }
    }
}
