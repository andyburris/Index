package com.andb.apps.todo.utilities;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;

import static java.lang.Math.abs;

public class Utilities {


    /**
     * Returns darker version of specified <code>color</code>.
     */
    public static int lighterDarker(int color, float factor) {


        int a = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);


        return Color.argb(a,
                Math.min(Math.max((int) (r * factor), 0), 255),
                Math.min(Math.max((int) (g * factor), 0), 255),
                Math.min(Math.max((int) (b * factor), 0), 255));


    }

    public static int sidedLighterDarker(int color, float factor){
        if(lightOnBackground(color)){
            return lighterDarker(color, 1+factor);
        }else {
            return lighterDarker(color, factor);
        }
    }

    public static boolean lightOnBackground(int background) {
        int color = (int) Long.parseLong(Integer.toHexString(background), 16);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = (color >> 0) & 0xFF;


        return ((r * 0.299 + g * 0.587 + b * 0.114) < 186);
    }

    public static int textFromBackground(int background) {
        if (lightOnBackground(background)) {
            return Color.WHITE;
        } else {
            return Color.BLACK;
        }
    }

    public static int colorWithAlpha(int color, float alpha) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        return Color.argb(Math.round(255 * alpha), r, g, b);
    }

    public static int colorFromAlpha(int fgColor, int bgColor, float fgAlpha) {
        int cr = Math.round(Color.red(fgColor) * fgAlpha + Color.red(bgColor) * (1 - fgAlpha));
        int cg = Math.round(Color.green(fgColor) * fgAlpha + Color.green(bgColor) * (1 - fgAlpha));
        int cb = Math.round(Color.blue(fgColor) * fgAlpha + Color.blue(bgColor) * (1 - fgAlpha));

        return Color.rgb(cr, cg, cb);

    }

    public static int colorBetween(int cStart, int cEnd, float amount) {

        int r1 = Color.red(cStart);
        int g1 = Color.green(cStart);
        int b1 = Color.blue(cStart);


        int r2 = Color.red(cEnd);
        int g2 = Color.green(cEnd);
        int b2 = Color.blue(cEnd);


        int r = Math.round(r1 + (abs(r1 - r2) * amount));
        r = Math.round(transposeRange(0f, 1f, r1, r2, amount));
        int g = Math.round(g1 + (abs(g1 - g2) * amount));
        g = Math.round(transposeRange(0f, 1f, g1, g2, amount));
        int b = Math.round(b1 + (abs(b1 - b2) * amount));
        b = Math.round(transposeRange(0f, 1f, b1, b2, amount));

        //System.out.println("colorBetween - r1: " + r1 + ", g1: " + g1 + ", b1: " + b1 + ", r2: " + r2 + ", g2: " + g2 + ", b2: " + b2 + ", r: " + r + ", g: " + g + ", b: " + b);

        return Color.rgb(r, g, b);
    }

    public static int desaturate(int color, double desaturateBy) {

        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        double L = 0.3 * r + 0.6 * g + 0.1 * b;
        int new_r = (int) Math.round(r + desaturateBy * (L - r));
        int new_g = (int) Math.round(g + desaturateBy * (L - g));
        int new_b = (int) Math.round(b + desaturateBy * (L - b));

        return Color.argb(Color.alpha(color), new_r, new_g, new_b);
    }

    public static int pxFromDp(int dp) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return Math.round(dp * scale);
    }

    public static float transposeRange(float oldMin, float oldMax, float newMin, float newMax, float oldValue) {
        float oldRange = (oldMax - oldMin);
        float newRange = (newMax - newMin);
        return (((oldValue - oldMin) * newRange) / oldRange) + newMin;
    }

    public static int visibilityFromBoolean(boolean bool) {
        if (bool) {
            return View.VISIBLE;
        } else {
            return View.GONE;
        }
    }


    public void expand(final View v) {
        TranslateAnimation anim = new TranslateAnimation(0.0f, 0.0f, -v.getHeight(), 0.0f);
        v.setVisibility(View.VISIBLE);

        anim.setDuration(300);
        anim.setInterpolator(new AccelerateInterpolator(0.5f));
        v.startAnimation(anim);
    }

    public void collapse(final View v) {
        TranslateAnimation anim = new TranslateAnimation(0.0f, 0.0f, 0.0f, -v.getHeight());
        Animation.AnimationListener collapselistener = new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setVisibility(View.GONE);
            }
        };

        anim.setAnimationListener(collapselistener);

        anim.setDuration(300);
        anim.setInterpolator(new AccelerateInterpolator(0.5f));
        v.startAnimation(anim);
    }
}
