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


    public static Tasks nextNotificationInProject(Project project){
        Tasks toReturn = null;
        for (Tasks tasks : project.getTaskList()){
            if(toReturn != null){
                if(tasks.getDateTime().isBefore(toReturn.getDateTime())){
                    toReturn = tasks;
                }
            }else {
                if (!tasks.getDateTime().equals(new DateTime(3000, 1, 1, 0, 0))){
                    toReturn = tasks;
                }
            }
        }
        return toReturn;
    }

    public static ArrayList<Object> nextNotificationAll(){
        ArrayList<Object> toReturn = new ArrayList<>();
        int project = -1;

        Tasks tasks = null;
        for (Project p : ProjectList.INSTANCE.getProjectList()){
            if (tasks!=null) {
                if (nextNotificationInProject(p).getDateTime().isBefore(tasks.getDateTime())) {
                    tasks = nextNotificationInProject(p);
                    project = ProjectList.INSTANCE.getProjectList().indexOf(p);
                }
            }else {
                if (nextNotificationInProject(p)!=null){
                    tasks = nextNotificationInProject(p);
                    project = ProjectList.INSTANCE.getProjectList().indexOf(p);
                }
            }
        }

        toReturn.add(tasks);
        toReturn.add(project);

        return toReturn;

    }
    public static ArrayList<Object> nextNotificationAll(ProjectsDatabase projectsDatabase){
        ArrayList<Object> toReturn = nextNotificationAll();

        Project project = ProjectList.INSTANCE.getProjectList().get((int) toReturn.get(1));
        project.getTaskList().get(project.getTaskList().indexOf((Tasks) toReturn.get(0))).setNotified(true);
        AsyncTask.execute(()-> projectsDatabase.projectsDao().updateProject(project));
        return toReturn;
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
