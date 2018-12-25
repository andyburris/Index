package com.andb.apps.todo;

import android.content.Intent;
import android.graphics.Color;
import android.util.Log;

public class Utilities {



    /**
     * Returns darker version of specified <code>color</code>.
     */
    public static int lighterDarker (int color, float factor) {


        int a = Color.alpha( color );
        int r = Color.red( color );
        int g = Color.green( color );
        int b = Color.blue( color );



        return Color.argb( a,
                Math.min(Math.max( (int)(r * factor), 0 ), 255),
                Math.min(Math.max( (int)(g * factor), 0 ), 255),
                Math.min(Math.max( (int)(b * factor), 0 ), 255) );


    }

    public static boolean lightOnBackground(int background) {
        int color = (int) Long.parseLong(Integer.toHexString(background), 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;


        return  ((r * 0.299 + g * 0.587 + b * 0.114) < 186);
    }

    public static int colorWithAlpha(int color, float alpha){
        int r = Color.red( color );
        int g = Color.green( color );
        int b = Color.blue( color );

        return Color.argb(Math.round(255*alpha), r, g, b);
    }

    public static int colorFromAlpha(int fgColor, int bgColor, float fgAlpha){
        int cr = Math.round(Color.red(fgColor) * fgAlpha + Color.red(bgColor) * (1-fgAlpha));
        int cg = Math.round(Color.green(fgColor) * fgAlpha + Color.green(bgColor) * (1-fgAlpha));
        int cb = Math.round(Color.blue(fgColor) * fgAlpha + Color.blue(bgColor) * (1-fgAlpha));

        return Color.argb(1, cr, cg, cb);

    }
}
