package com.andb.apps.todo.eventbus;

import com.andb.apps.todo.databases.ProjectsDatabase;
import com.andb.apps.todo.objects.Project;
import com.andb.apps.todo.objects.Tasks;

import java.util.ArrayList;

public class MigrateEvent {
    public ProjectsDatabase projectsDatabase;
    public ArrayList<Tasks> taskList;

    public MigrateEvent(ProjectsDatabase db, ArrayList<Tasks> taskList){
        this.projectsDatabase = db;
        this.taskList = taskList;
    }
}
