package org.mortbay.ijetty;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;


public class DaemonService extends Service{
	//private MonitorThread hearbeatThread = null;
	private HeartBeatThread hearbeatThreadNew = null;
	private NetStatusMonitorThread netStatusMonitorThread = null;
	private PlayListMonitorThread playListMonitorThread = null;
	private ApkMonitorThread apkMonitorThread = null;
	//private SNThread snThread = null;
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		//心跳线程
//		hearbeatThread = MonitorThread.getInstance();
//		hearbeatThread.startup();
		
		hearbeatThreadNew = HeartBeatThread.getInstance();
		hearbeatThreadNew.startup();
		
		//网络状态
		netStatusMonitorThread = NetStatusMonitorThread.getInstance();
		netStatusMonitorThread.startup();
		
		//playlist状态
		playListMonitorThread = PlayListMonitorThread.getInstance();
		playListMonitorThread.startup();
		
		//apk状态
		apkMonitorThread = ApkMonitorThread.getInstance();
		apkMonitorThread.startup();
		//SN获取线程
//		snThread = SNThread.getInstance();
//		snThread.startup();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (hearbeatThreadNew != null){hearbeatThreadNew.shutdown(); hearbeatThreadNew = null;}
		if (netStatusMonitorThread != null){netStatusMonitorThread.shutdown(); netStatusMonitorThread = null;}
		if (playListMonitorThread != null){playListMonitorThread.shutdown(); playListMonitorThread = null;}
		if (apkMonitorThread != null){apkMonitorThread.shutdown(); apkMonitorThread = null;}
		//if (snThread != null){snThread.shutdown(); snThread = null;}
	}
}
