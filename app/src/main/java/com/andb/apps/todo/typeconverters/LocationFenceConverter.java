package com.andb.apps.todo.typeconverters;

import com.andb.apps.todo.data.model.reminders.LocationFence;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import androidx.room.TypeConverter;

import static com.andb.apps.todo.data.model.reminders.LocationFenceKt.TYPE_ENTER;
import static com.andb.apps.todo.data.model.reminders.LocationFenceKt.TYPE_EXIT;

public class LocationFenceConverter {
    private static Gson gson = new Gson();

    @TypeConverter
    public static ArrayList<LocationFence> locationFenceArrayList(String data) {
        if (data == null) {
            return new ArrayList<>();
        }

        Type listType = new TypeToken<ArrayList<SimpleLocationFence>>() {
        }.getType();

        ArrayList<SimpleLocationFence> simpleList = gson.fromJson(data, listType);
        ArrayList<LocationFence> fenceList = new ArrayList<>();
        for (SimpleLocationFence simpleLocationFence : simpleList) {
            fenceList.add(simpleLocationFence.toLocationFence());
        }

        return fenceList;
    }

    @TypeConverter
    public static String locationFenceToString(ArrayList<LocationFence> fenceList) {
        ArrayList<SimpleLocationFence> simpleList = new ArrayList<>();
        for (LocationFence fence : fenceList) {
            int duration = durationFromFence(fence);
            SimpleLocationFence simpleFence;
            if(fence.getTrigger()==null) {
                simpleFence = new SimpleLocationFence(fence.getLat(), fence.getLong(), fence.getRadius(), fence.getKey(), fence.toType(), duration);
            }else {
                LocationFence ft = fence.getTrigger();
                SimpleLocationFence trigger = new SimpleLocationFence(ft.getLat(), ft.getLong(), ft.getRadius(), ft.getKey(), ft.toType(), durationFromFence(ft));

                simpleFence = new SimpleLocationFence(fence.getLat(), fence.getLong(), fence.getRadius(), fence.getKey(), fence.toType(), duration, trigger);
            }
            simpleList.add(simpleFence);
        }
        return gson.toJson(simpleList);
    }

    private static int durationFromFence(LocationFence fence){
        if (fence instanceof LocationFence.Enter) {
            return  ((LocationFence.Enter) fence).getDuration();
        } else {
            return  -1;
        }
    }


}

class SimpleLocationFence {


    private double lat;
    private double lng;
    private int radius;
    private int key;
    private int type;
    private int duration;
    private SimpleLocationFence trigger;

    SimpleLocationFence(double lat, double lng, int radius, int key, int type, int duration) {
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.key = key;
        this.type = type;
        this.duration = duration;
    }

    SimpleLocationFence(double lat, double lng, int radius, int key, int type, int duration, SimpleLocationFence trigger) {
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
        this.key = key;
        this.type = type;
        this.duration = duration;
        this.trigger = trigger;
    }

    LocationFence toLocationFence() {
        LocationFence lf;
        switch (type) {
            case TYPE_ENTER:
                lf = new LocationFence.Enter(lat, lng, duration, key);
                break;
            case TYPE_EXIT:
                lf = new LocationFence.Exit(lat, lng, radius, key);
                break;
            default: //TYPE_NEAR
                lf = new LocationFence.Near(lat, lng, radius, key);
        }
        if(trigger!=null){
            lf.setTrigger(trigger.toLocationFence());
        }
        return lf;
    }


}