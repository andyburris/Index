package com.andb.apps.todo.utilities;

import com.andb.apps.todo.lists.ProjectList;
import com.andb.apps.todo.objects.Project;
import com.andb.apps.todo.objects.Tags;
import com.andb.apps.todo.objects.Tasks;

import java.util.ArrayList;

public class Current {

    public static ArrayList<Project> allProjects(){
        //name projectList?
        return ProjectList.INSTANCE.getProjectList();
    }

    public static int viewing(){
        return ProjectList.INSTANCE.getViewing();
    }

    public static void setViewing(int pos){
        ProjectList.INSTANCE.setViewing(pos);
    }

    public static Project project(){
        return allProjects().get(viewing());
    }

    public static ArrayList<Tasks> taskList(){
        return project().getTaskList();
    }

    public static ArrayList<Tasks> archiveTaskList(){
        return project().getArchiveList();
    }

    public static ArrayList<Tags> tagList(){
        return project().getTagList();
    }

    public static ArrayList<Integer> keyList(){
        return project().getKeyList();
    }


}
