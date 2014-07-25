package org.mortbay.ijetty;

import org.mortbay.ijetty.util.PlayListUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebSettings;
import android.os.Bundle;

public class PlayListMonitorThread {
	private final static String TAG = "->PlayListMonitorThread";
	
	private Thread thread;
	private boolean doneFlag = false;
	private static PlayListMonitorThread instance;
	private static boolean threadStartFlag = false;

	String mActivityName = null;

	public synchronized static PlayListMonitorThread getInstance() {
		if (instance == null)
			instance = new PlayListMonitorThread();
		return instance;
	}

	private PlayListMonitorThread() {
		doneFlag = false;
		thread = new Thread() {
			@Override
			public void run() {
				process(this);
			}
		};
		thread.setName("Daemon PlayListMonitorThread thread");
		thread.setDaemon(false);
	}

	public void startup() {
		if (threadStartFlag)
			return;
		Log.i(TAG,"PlayListMonitorThread thread.start();");
		thread.start();

	}

	public void shutdown() {
	        if (threadStartFlag)
	            thread.stop();
	        Log.i(TAG,"PlayListMonitorThread thread.stop();");
	        doneFlag = true;
	}
	
	public static final char SPLIT_CHAR = '|';
	public void process(Thread thisThread) {
		threadStartFlag = true;
		try {
			while (!doneFlag && (thread == thisThread)) {
				try {
					if(!PlayListUtil.isPlayListFileSynced)
					{
					    //执行播放清单同步操作，主要为下载文件列表。
					    PlayListUtil.isPlayListFileSynced = PlayListUtil.syncPlayListFiles2();
					    PlayListUtil.isApkGetNeedConfirm = PlayListUtil.isPlayListFileSynced;
					}
					if(PlayListUtil.isApkGetNeedConfirm)
					{
					    PlayListUtil.confirmGetPlayListFiles();
					    PlayListUtil.isApkGetNeedConfirm = false;
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
				}
				finally {
					SystemClock.sleep((AppConstants.HEARTBEAT_TIME - 3) * 1000);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			threadStartFlag = false;
		}
	}
}
