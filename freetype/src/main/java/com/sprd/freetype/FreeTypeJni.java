package com.sprd.freetype;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;


public class FreeTypeJni {
    static {
        System.loadLibrary("freetype_jni");
    }

    public synchronized static void putWText(Mat img, String text, Point org, int fontFace, double fontScale, Scalar color) {
        putWText(img.nativeObj, text, org.x, org.y, fontFace, fontScale, color.val[0], color.val[1], color.val[2], color.val[3]);
    }

    public synchronized static void putText(Mat img, String text, Point org, int fontFace, double fontScale, Scalar color, int thickness) {
        putText(img.nativeObj, text, org.x, org.y, fontFace, fontScale, color.val[0], color.val[1], color.val[2], color.val[3], thickness);
    }

    public static native boolean setFont(String var0);

    public static native void getTextSize(String text,int fontFace,double fontScale,int thickness,int[] baseLine,double[] outSize);

    private static native void putText(long var0, String var2, double var3, double var5, int var7, double var8, double var10, double var12, double var14, double var16, int var18);

    private static native void putWText(long var0, String var2, double var3, double var5, int var7, double var8, double var10, double var12, double var14, double var16);
}
