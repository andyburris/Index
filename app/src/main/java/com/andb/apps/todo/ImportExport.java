package com.andb.apps.todo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.andb.apps.todo.filtering.FilteredLists;
import com.andb.apps.todo.filtering.Filters;
import com.andb.apps.todo.lists.TagLinkList;
import com.andb.apps.todo.lists.TagList;
import com.andb.apps.todo.lists.TaskList;
import com.andb.apps.todo.objects.TagLinks;
import com.andb.apps.todo.objects.Tags;
import com.andb.apps.todo.objects.Tasks;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;

public class ImportExport {

    static String folder = "/ToDo Backups";

    public static void importTasks(final Context ctxt) {
        if (isExternalStorageReadable()) {
            File path = new File(Environment.getExternalStorageDirectory() + folder);
            final ArrayList<File> fileList = new ArrayList<>(Arrays.asList(path.listFiles()));


            AlertDialog.Builder builder = new AlertDialog.Builder(ctxt);
            builder.setItems(path.list(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(ctxt, fileList.get(i).getName(), Toast.LENGTH_SHORT).show();

                    File file = fileList.get(i);
                    try {
                        String json = getStringFromFile(file);
                        Log.i("loadedJson", json);
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();

                        Type taskType = new TypeToken<ArrayList<Tasks>>() {
                        }.getType();
                        Type tagType = new TypeToken<ArrayList<Tags>>() {
                        }.getType();
                        Type linkType = new TypeToken<ArrayList<TagLinks>>() {
                        }.getType();
                        Type arrayListType = new TypeToken<ArrayList<String>>() {
                        }.getType();

                        ArrayList<String> importList;
                        importList = gson.fromJson(json, arrayListType);

                        String taskJson = importList.get(0);
                        String tagJson = importList.get(1);
                        String linkJson = importList.get(2);


                        Log.i("ImportedTaskList", taskJson);


                        TaskList.taskList = gson.fromJson(taskJson, taskType);
                        TagList.tagList = gson.fromJson(tagJson, tagType);
                        TagLinkList.linkList = gson.fromJson(linkJson, linkType);









                        /*ArrayList<ArrayList<?>> exportList = gson.fromJson(json, type);

                        if (exportList.get(0).clone() instanceof ArrayList) {
                            TaskList.taskList = (ArrayList<Tasks>) exportList.get(0).clone();
                        } else {
                            Log.i("loadBackup", "Tasklist not arraylist");
                        }
                        if (exportList.get(1).clone() instanceof ArrayList) {
                            TagList.tagList = (ArrayList<Tags>) exportList.get(1).clone();
                        } else {
                            Log.i("loadBackup", "Taglist not arraylist");
                        }
                        if (exportList.get(2).clone() instanceof ArrayList) {
                            TagLinkList.linkList = (ArrayList<TagLinks>) exportList.get(2).clone();
                        } else {
                            Log.i("loadBackup", "Taglist not arraylist");
                        }*/

                        /*TaskList.taskList = gson.fromJson(json, taskType);
                        TagList.tagList = gson.fromJson(json, tagType);
                        TagLinkList.linkList = gson.fromJson(json, linkType);*/


                        Toast.makeText(ctxt, "Tasks imported: " +
                                        Integer.toString(TaskList.taskList.size()) +
                                        ", Tags imported: " +
                                        Integer.toString(TagList.tagList.size()) +
                                        "Links imported: " +
                                        Integer.toString(TagLinkList.linkList.size()),
                                Toast.LENGTH_LONG).show();

                        for (Tasks task : TaskList.taskList) {
                            task.normalizeAfterImport();
                        }

                        FilteredLists.createFilteredTaskList(Filters.getCurrentFilter(), true);

                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.tasksDatabase.tasksDao().insertMultipleTasks(TaskList.taskList);
                            }
                        });


                        TagLinkList.saveTags(ctxt);
                        TagList.saveTags(ctxt);


                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ctxt, "File read failed", Toast.LENGTH_LONG).show();
                    }


                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctxt);
            builder.setMessage("Storage is not availible to read, try again")
                    .show();
        }
    }

    public static void exportTasks(Context ctxt) {
        if (isExternalStorageWritable()) {
            ArrayList<String> exportList = new ArrayList<>();
            /*exportList.add(TaskList.taskList);
            exportList.add(TagList.tagList);
            exportList.add(TagLinkList.linkList);*/

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type taskType = new TypeToken<ArrayList<Tasks>>() {
            }.getType();
            Type tagType = new TypeToken<ArrayList<Tags>>() {
            }.getType();
            Type linkType = new TypeToken<ArrayList<TagLinks>>() {
            }.getType();
            Type arrayListType = new TypeToken<ArrayList<String>>() {
            }.getType();

            String taskJson = gson.toJson(TaskList.taskList, taskType);
            String tagJson = gson.toJson(TagList.tagList, tagType);
            String linkJson = gson.toJson(TagLinkList.linkList, linkType);

            Log.i("ExportedTaskList", taskJson);


            exportList.add(taskJson);
            exportList.add(tagJson);
            exportList.add(linkJson);

            String finalJson = gson.toJson(exportList, arrayListType);

            File path = new File(Environment.getExternalStorageDirectory() + folder);
            if (!path.exists()) {
                path.mkdirs();
            }

            String filename = "Backup-" + DateTime.now().toString("yyyy-MM-dd-hh-mm");

            Log.d("fileSave", filename);

            File toSave = new File(path, filename);
            while (toSave.exists()) {
                filename = filename.subSequence(0, 23).toString(); /*cut out any added seconds*/
                filename = filename + DateTime.now().toString("-ss");
            }


            try {

                FileOutputStream outputStream = new FileOutputStream(toSave);
                outputStream.write(finalJson.getBytes());
                outputStream.flush();
                outputStream.close();

            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(ctxt);
            builder.setMessage("External storage is not availible to write, try again")
                    .show();
        }


    }


    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile(File fl) throws Exception {
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }
}

