package com.andb.apps.todo.utilities;

import android.os.AsyncTask;

import com.andb.apps.todo.MainActivity;
import com.andb.apps.todo.databases.ProjectsDatabase;
import com.andb.apps.todo.lists.ProjectList;
import com.andb.apps.todo.objects.Project;
import com.andb.apps.todo.objects.Tasks;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Random;

public class ProjectsUtils{

    public static void update(Project project, ProjectsDatabase projectsDatabase){
        AsyncTask.execute(() -> projectsDatabase.projectsDao().updateProject(project));

    }

    public static void update(Project project){
        update(project, MainActivity.projectsDatabase);
    }

    public static void update(ProjectsDatabase projectsDatabase){
        update(Current.project(), projectsDatabase);
    }

    public static void update(){
        update(Current.project(), MainActivity.projectsDatabase);
    }

    public static Project projectFromKey(int key){
        for (Project p : Current.allProjects()){
            if (p.getKey()==key){
                return p;
            }
        }
        return null;
    }

    private static Random random = new Random();
    public static int keyGenerator(){
        int key = random.nextInt();
        ArrayList<Integer> keys = new ArrayList<>();
        keys.addAll(Current.keyList());
        for (Project project : Current.allProjects()){
            keys.add(project.getKey());
        }

        while (keys.contains(key)){
            key = random.nextInt();
        }
        return key;
    }
}
