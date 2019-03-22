package com.andb.apps.todo.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.WorkManager
import com.andb.apps.todo.MainActivity
import com.andb.apps.todo.R
import com.andb.apps.todo.databases.GetDatabase
import com.andb.apps.todo.eventbus.UpdateEvent
import com.andb.apps.todo.objects.Tasks
import com.andb.apps.todo.objects.reminders.LocationFence
import com.andb.apps.todo.utilities.Current
import com.andb.apps.todo.utilities.ProjectsUtils
import com.jaredrummler.cyanea.Cyanea
import org.greenrobot.eventbus.EventBus
import java.util.*

class NotificationHandler : Service() {


    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val bundle = intent.extras

        var key = -1
        var fromAction = false
        var reschedule = false
        var locationKey = ""

        if (bundle != null) {
            if (bundle.containsKey("taskKeyClear")) {
                key = bundle.getInt("taskKeyClear", -1)
                fromAction = true
                if (bundle.containsKey("reschedule")) {
                    reschedule = bundle.getBoolean("reschedule")
                    if (bundle.containsKey("locationKey")) {
                        locationKey = bundle.getString("locationKey")
                    }
                }
            } else if (bundle.containsKey("taskKey")) {
                key = bundle.getInt("taskKey", -1)
            }


        }

        Log.d("workManager", "Key: $key, delete: $fromAction, reschedule: $reschedule")


        handleNotification(key, fromAction, reschedule, locationKey, applicationContext)


        return Service.START_NOT_STICKY
    }

    companion object {


        private val todo_notification_channel = "Task Reminders"
        val workTag = "notifications"


        @JvmOverloads
        fun createNotification(task: Tasks, ctxt: Context, locationKey: String = "") {

            createNotificationChannel(ctxt, todo_notification_channel)

            val key = task.listKey
            val bodyClickIntent = Intent(ctxt, NotificationHandler::class.java)
            bodyClickIntent.putExtra("taskKey", key)

            val doneClickIntent = Intent(ctxt, NotificationHandler::class.java)
            doneClickIntent.putExtra("reschedule", false)
            doneClickIntent.putExtra("taskKeyClear", key)

            val rescheduleClickIntent = Intent(ctxt, NotificationHandler::class.java)
            rescheduleClickIntent.putExtra("reschedule", true)
            rescheduleClickIntent.putExtra("locationKey", locationKey)
            rescheduleClickIntent.putExtra("taskKeyClear", key)

            val pendingBodyClickIntent = PendingIntent.getActivity(ctxt, key, bodyClickIntent, FLAG_UPDATE_CURRENT)

            val pendingDoneClickIntent = PendingIntent.getService(ctxt, key, doneClickIntent, FLAG_UPDATE_CURRENT)

            val pendingRescheduleClickIntent = PendingIntent.getService(ctxt, key + 1, rescheduleClickIntent, FLAG_UPDATE_CURRENT)


            val notificationTitle = task.listName
            val notificationText = buildString {
                for (i in task.listItems) {
                    append("- $i \n")
                }

                if (locationKey.isEmpty()) {
                    val pattern = "MMM d, h:mm a"
                    append(task.nextReminderTime().toString(pattern))
                } else {
                    append(task.locationReminders.first { it.key == locationKey.toInt() }.name)
                }
            }


            val reschedule = ctxt.getString(if (locationKey.isNotEmpty()) R.string.notif_action_next_location else R.string.notif_action_reschedule)

            //build the notification
            val notificationBuilder = NotificationCompat.Builder(ctxt, todo_notification_channel)
                .setSmallIcon(R.drawable.ic_todo_small)
                .setContentTitle(notificationTitle)
                //.setContentText(notificationText)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(notificationText)
                )
                .setContentIntent(pendingBodyClickIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_check_white_24dp, ctxt.getString(R.string.notif_action_done), pendingDoneClickIntent)
                .addAction(R.drawable.ic_access_time_black_24dp, reschedule, pendingRescheduleClickIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            //trigger the notification
            val notificationManager = NotificationManagerCompat.from(ctxt)

            //we give each notification the ID of the event it's describing,
            //to ensure they all show up and there are no duplicates
            notificationManager.notify(Integer.toString(key), key, notificationBuilder.build())


            Log.d("notificationCreate", "Name: " + task.listName + ", Key: " + Integer.toString(key))


            Log.d("serviceRestart", "Before Restart")

            if (locationKey.isEmpty()) {
                task.nextReminder()?.notified = true
            }
            ProjectsUtils.update(task)

            NotifyWorker.nextWork()
        }


        fun resetNotifications() {
            AsyncTask.execute {
                WorkManager.getInstance().cancelAllWorkByTag(workTag)
                if (NotificationUtils.isNextNotification(Current.database())) {
                    Log.d("workManager", "Next isn't null")
                    NotifyWorker.nextWork()
                }
            }

        }

        fun handleNotification(key: Int, fromAction: Boolean, reschedule: Boolean, locationKey: String, ctxt: Context) {
            if (key != -1) {
                if (fromAction) {

                    initializeDatabase(ctxt)

                    val notificationManager = NotificationManagerCompat.from(ctxt)
                    notificationManager.cancel(Integer.toString(key), key)

                    if (reschedule) {
                        //reschedule

                        if (locationKey.isEmpty()) {
                            rescheduleTask(key, ctxt)
                        } else {
                            nextLocation(key, ctxt, locationKey.toInt())
                        }
                    } else {
                        //delete
                        deleteTask(key, ctxt)
                    }

                    //TODO: refactor handleNotification for better code
                } else {
                    //open task in taskview
                    val intent = Intent(ctxt, MainActivity::class.java)
                }


            } else {
                Toast.makeText(ctxt, "Couldn't find task", Toast.LENGTH_SHORT).show()
            }


        }

        fun initializeDatabase(ctxt: Context) {
            GetDatabase.projectsDatabase = GetDatabase.getDatabase(ctxt)
        }

        fun rescheduleTask(key: Int, ctxt: Context) {
            Log.d("rescheduleNotification", "rescheduling notification")

            //DateTime taskDateTime = new DateTime();


            val intent = Intent(ctxt, Reschedule::class.java)
            intent.putExtra("key", key)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            ctxt.startActivity(intent)

            Log.d("rescheduleNotification", "activity called")


        }

        fun nextLocation(key: Int, ctxt: Context, locationKey: Int) {

            AsyncTask.execute {

                val task = GetDatabase.projectsDatabase.tasksDao()
                    .findTaskById(key)
                val reminder: LocationFence = task.locationReminders.first { it.key == locationKey }
                val exitReminder = LocationFence.Exit(reminder.lat, reminder.long, reminder.radius)
                reminder.trigger = exitReminder

                ProjectsUtils.update(task, async = false)
                val allTasks = GetDatabase.projectsDatabase.tasksDao().all.value!!.filter { !it.isArchived }
                Log.d("fenceTaskNext", "database search size: ${allTasks.size}")
                Log.d("fenceTaskNext", "database contains: ${allTasks.contains(task)}")
                Log.d("fenceTaskNext", "task trigger ids: ${task.locationReminders.mapNotNull { it.trigger?.key }}")
                Log.d("fenceTaskNext", "all reminder ids: ${allTasks.flatMap { it.locationReminders.map { it.key } }}")
                Log.d("fenceTaskNext", "all trigger ids: ${allTasks.flatMap { it.locationReminders.mapNotNull { it.trigger?.key } }}")
                requestFromFence(exitReminder, ctxt)

            }

        }

        fun deleteTask(key: Int, ctxt: Context) {

            Log.d("deleteNotification", "deleting notification")

            AsyncTask.execute {
                val task = Current.database().tasksDao().findTaskById(key)
                task.isArchived = true
                Current.database().tasksDao().updateTask(task)
/*
                //projectsDatabase.projectsDao().updateProject(project);
                if (NotificationHandler.checkActive(ctxt) && Current.projectKey() == task.projectId) {
                    Current.project()
                        .taskList = ArrayList(Current.database().tasksDao().getAllFromProject(task.projectId))
                    EventBus.getDefault().post(UpdateEvent(true))
                }*/
            }
        }


        fun createNotificationChannel(ctxt: Context, name: String) {
            // Create the NotificationChannel, but only on API 26+ because
            // the NotificationChannel class is new and not in the support library
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //define the importance level of the notification
                val importance = NotificationManager.IMPORTANCE_DEFAULT

                //build the actual notification channel, giving it a unique ID and name
                val channel = NotificationChannel(name, name, importance)

                //we can optionally add a description for the channel
                val description = "Shows notifications for tasks when they are due"
                channel.description = description

                //we can optionally set notification LED colour
                channel.lightColor = Cyanea.instance.accent

                // Register the channel with the system
                val notificationManager = ctxt.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
}


