package org.mortbay.ijetty.util;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;

public class AmlogicExt {
    private final static String TAG = "AmlogicExt";

    private static Method sGetExternalStorage2State;
    private static Method sGetExternalStorage2Directory;
    private static Method sGetInternalStorageState;
    private static Method sGetInternalStorageDirectory;

    static {
        initCompatibility();
    };

    private static void initCompatibility() {
        try {
            sGetExternalStorage2State = Environment.class.getMethod(
                "getExternalStorage2State", (Class<?>[]) null);
            sGetExternalStorage2Directory = Environment.class.getMethod(
                "getExternalStorage2Directory", (Class<?>[]) null);
            sGetInternalStorageState = Environment.class.getMethod(
                "getInternalStorageState", (Class<?>[]) null);
            sGetInternalStorageDirectory = Environment.class.getMethod(
                "getInternalStorageDirectory", (Class<?>[]) null);
        } catch (NoSuchMethodException e) {
            // at least one of the methods is not available
            // clear all others. all or nothing
            sGetExternalStorage2State = null;
            sGetExternalStorage2Directory = null;
            sGetInternalStorageState = null;
            sGetInternalStorageDirectory = null;
        }
    }

    public static boolean isSupported() {
        return (sGetExternalStorage2State != null &&
                sGetExternalStorage2Directory != null &&
                sGetInternalStorageState != null &&
                sGetInternalStorageDirectory != null);
    }

    public static String getExternalStorage2State() {
        if (sGetExternalStorage2State != null) {
            try {
                return (String) sGetExternalStorage2State.invoke(null, (Object[]) null);
            } catch (Exception e) {
                //there won't be any exception from this call
            }
        }
        return Environment.MEDIA_REMOVED;
    }

    public static File getExternalStorage2Directory() {
        if (sGetExternalStorage2Directory != null) {
            try {
                return (File) sGetExternalStorage2Directory.invoke(null, (Object[]) null);
            } catch (Exception e) {
                //there won't be any exception from this call
            }
        }
        return null;
    }

    public static String getInternalStorageState() {
        if (sGetInternalStorageState != null) {
            try {
                return (String) sGetInternalStorageState.invoke(null, (Object[]) null);
            } catch (Exception e) {
                //there won't be any exception from this call
            }
        }
        return Environment.MEDIA_REMOVED;
    }

    public static File getInternalStorageDirectory() {
        if (sGetInternalStorageDirectory != null) {
            try {
                return (File) sGetInternalStorageDirectory.invoke(null, (Object[]) null);
            } catch (Exception e) {
                //there won't be any exception from this call
            }
        }
        return null;
    }
}
