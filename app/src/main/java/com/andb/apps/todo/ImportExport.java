package com.andb.apps.todo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.andb.apps.todo.databases.TagLinks;
import com.andb.apps.todo.eventbus.UpdateEvent;
import com.andb.apps.todo.filtering.FilteredLists;
import com.andb.apps.todo.filtering.Filters;
import com.andb.apps.todo.lists.ProjectList;
import com.andb.apps.todo.objects.Project;
import com.andb.apps.todo.objects.Tags;
import com.andb.apps.todo.objects.Tasks;
import com.andb.apps.todo.utilities.Current;
import com.andb.apps.todo.utilities.ProjectsUtils;
import com.andb.apps.todo.views.CyaneaDialog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.jaredrummler.cyanea.Cyanea;

import org.greenrobot.eventbus.EventBus;
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
import java.util.Random;

public class ImportExport {

    static String folder = "/ToDo Backups";

    public static void importTasks(final Context ctxt) {
        if (isExternalStorageReadable()) {
            File path = new File(Environment.getExternalStorageDirectory() + folder);
            final ArrayList<File> fileList = new ArrayList<>(Arrays.asList(path.listFiles()));

            CyaneaDialog.Builder builder = new CyaneaDialog.Builder(ctxt);
            builder.setItems(path.list(), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Toast.makeText(ctxt, fileList.get(i).getName(), Toast.LENGTH_SHORT).show();

                    File file = fileList.get(i);
                    try {
                        String json = getStringFromFile(file);
                        Log.i("loadedJson", json);
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();

                        Type arrayListType = new TypeToken<ArrayList<String>>() {
                        }.getType();
                        Type taskType = new TypeToken<ArrayList<Tasks>>() {
                        }.getType();
                        Type tagType = new TypeToken<ArrayList<Tags>>() {
                        }.getType();
                        Type stringType = new TypeToken<String>() {
                        }.getType();
                        Type linkType = new TypeToken<ArrayList<TagLinks>>() {
                        }.getType();
                        Type objectType = new TypeToken<Object>() {
                        }.getType();


                        ArrayList<String> importList;
                        importList = gson.fromJson(json, arrayListType);

                        String taskJson = importList.get(0);
                        String tagJson = importList.get(1);
                        String nameJson = importList.get(2);


                        Log.i("ImportedTaskList", taskJson);


                        ArrayList<Tasks> oldTaskList = gson.fromJson(taskJson, taskType);


                        ArrayList<Tasks> archiveList = new ArrayList<>();//MAYBEDO: backup/import archive tasks

                        int projectKey = ProjectsUtils.keyGenerator();

                        ArrayList<Integer> keyList = new ArrayList<>();
                        ArrayList<Tasks> taskList = new ArrayList<>();
                        for (Tasks task : oldTaskList) {
                            task.setProjectId(projectKey);

                            int key = task.getListKey();
                            while (keyList.contains(key)) {
                                key = new Random().nextInt();
                            }
                            taskList.add(task.withKey(key));
                            keyList.add(task.getListKey());
                        }


                        ArrayList<Tags> tagList = gson.fromJson(tagJson, tagType);

                        for (int t = 0; t < tagList.size(); t++) {
                            Tags tags = tagList.get(t);
                            tags.setIndex(t);
                            tags.setProjectId(projectKey);
                        }


                        String projectName = "Tasks";

                        if (gson.fromJson(nameJson, objectType) instanceof ArrayList) {//old taglinks
                            ArrayList<TagLinks> linkList = gson.fromJson(nameJson, linkType);

                            for (int j = 0; j < tagList.size(); j++) {
                                Tags tags = tagList.get(j);
                                for (TagLinks tagLinks : linkList) {
                                    if (tagLinks.tagParent() == j) {
                                        tags.setChildren(tagLinks.getAllTagLinks());
                                    }
                                }
                            }
                        } else {//name
                            projectName = gson.fromJson(nameJson, stringType);
                        }

                        Project newProject = new Project(projectKey, projectName, taskList, archiveList, tagList, Cyanea.getInstance().getAccent(), Current.allProjects().size());
                        Current.allProjects().add(newProject);
                        ProjectList.INSTANCE.setViewing(Current.allProjects().size() - 1);


                        FilteredLists.INSTANCE.createFilteredTaskList(Filters.getCurrentFilter(), true);
                        EventBus.getDefault().post(new UpdateEvent(false));

                        Log.d("projectKey", Integer.toString(projectKey));
                        for (Tasks task : taskList) {
                            Log.d("importProjectKey", Integer.toString(task.getProjectId()));
                        }

                        AsyncTask.execute(() -> {
                                    Current.database().projectsDao().insertOnlySingleProject(newProject);
                                    Current.database().tasksDao().insertMultipleTasks(taskList);
                                    Current.database().tagsDao().insertMultipleTags(tagList);
                                }
                        );

                        Log.i("ImportedTaskList", Current.project().getTaskList().toString());


                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(ctxt, "File read failed", Toast.LENGTH_LONG).show();
                    }


                }
            });

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        } else {
            CyaneaDialog.Builder builder = new CyaneaDialog.Builder(ctxt);
            builder.setMessage("Storage is not availible to read, try again")
                    .show();
        }
    }

    public static void exportTasks(Context ctxt) {
        if (isExternalStorageWritable()) {
            ArrayList<String> exportList = new ArrayList<>();

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type taskType = new TypeToken<ArrayList<Tasks>>() {
            }.getType();
            Type tagType = new TypeToken<ArrayList<Tags>>() {
            }.getType();
            Type arrayListType = new TypeToken<ArrayList<String>>() {
            }.getType();

            String taskJson = gson.toJson(Current.taskList(), taskType);
            String tagJson = gson.toJson(Current.tagList(), tagType);

            Log.i("ExportedTaskList", taskJson);


            exportList.add(taskJson);
            exportList.add(tagJson);
            exportList.add(Current.project().getName());

            String finalJson = gson.toJson(exportList, arrayListType);

            File path = new File(Environment.getExternalStorageDirectory() + folder);
            if (!path.exists()) {
                path.mkdirs();
            }

            String filename = Current.project().getName() + "-" + DateTime.now().toString("yyyy-MM-dd-hh-mm");

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
            CyaneaDialog.Builder builder = new CyaneaDialog.Builder(ctxt);
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

