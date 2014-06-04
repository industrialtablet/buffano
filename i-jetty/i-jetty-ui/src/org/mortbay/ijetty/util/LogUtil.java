package org.mortbay.ijetty.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;

public class LogUtil {
	private static boolean mDebugFlag = true;
	private static final String DEBUG_TAG = "LogUtil";
	private static String LOG_FILE_NAME = "/mnt/sdcard/log.txt";

	public static void enableDebug(boolean pDebugFlag) {
		mDebugFlag = pDebugFlag;
	}

	public static void log(String pMsg) {
		if (mDebugFlag) {
			Log.e(DEBUG_TAG, pMsg);
		}
	}

	private static void logToFile(File logFile, String text) {
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		BufferedWriter buf = null;
		try {
			buf = new BufferedWriter(new FileWriter(logFile, true));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			buf.append(sdf.format(new Date()) + "\t" + text);
			buf.newLine();
			buf.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (buf != null)
				try {
					buf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

		}
	}

	public static void logToDefaultFile(String text) {
		logToFile(new File(LOG_FILE_NAME), text);
	}
	
	public static void appendLog(String text) {
		String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
		File logFile = new File(Environment.getExternalStorageDirectory(),
				"cyxh-" + dateStr + ".log");
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		BufferedWriter buf = null;
		try {
			buf = new BufferedWriter(new FileWriter(logFile, true));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			buf.append(sdf.format(new Date())+"\t");
			buf.append(text);
			buf.newLine();
			buf.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (buf != null)
				try {
					buf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
}
