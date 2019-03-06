package com.andb.apps.todo.utilities;

class ColorTest{
    public static int red(int color){
        int c = (int) Long.parseLong(Integer.toHexString(color), 16);
        return (c >> 16)&  0xFF;
    }

    public static int green(int color){
        int c = (int) Long.parseLong(Integer.toHexString(color), 16);
        return (c >> 8) & 0xFF;
    }

    public static int blue(int color){
        int c = (int) Long.parseLong(Integer.toHexString(color), 16);
        return (c) & 0xFF;
    }

    public static int rgb(int r, int g, int b){
        int rgb = r;
        rgb = (rgb << 8) + g;
        rgb = (rgb << 8) + b;
        return rgb;
    }

}