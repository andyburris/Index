package com.andb.apps.todo;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

public class Utilities {
    /**
     * Returns darker version of specified <code>color</code>.
     */
    public static int lighterDarker (int color, float factor) {
        //Log.d("lightenDarken", Integer.toHexString(color));


        int a = Color.alpha( color );
        int r = Color.red( color );
        int g = Color.green( color );
        int b = Color.blue( color );



        /*int returnColor =  */ return Color.argb( a,
                Math.min(Math.max( (int)(r * factor), 0 ), 255),
                Math.min(Math.max( (int)(g * factor), 0 ), 255),
                Math.min(Math.max( (int)(b * factor), 0 ), 255) );

        //Log.d("lightenDarken", Integer.toHexString(returnColor));

        //return returnColor;

    }

    public static boolean lightOnBackground(int background) {
        int color = (int) Long.parseLong(Integer.toHexString(background), 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;


        return  ((r * 0.299 + g * 0.587 + b * 0.114) < 186);
    }

}
