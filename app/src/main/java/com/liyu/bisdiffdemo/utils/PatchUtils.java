package com.liyu.bisdiffdemo.utils;

public class PatchUtils {

    static {
        System.loadLibrary("update-lib");
    }

    public static native void bsPatch(String oldApk, String patch, String outApk);
}
