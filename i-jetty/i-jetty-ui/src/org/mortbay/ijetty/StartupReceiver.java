package org.mortbay.ijetty;

import org.mortbay.ijetty.util.LogUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class StartupReceiver extends BroadcastReceiver {
        static final String ACTION = "android.intent.action.BOOT_COMPLETED";
	public static long START_TIME;
	@Override
	public void onReceive(Context context, Intent intent) {
	    if (intent.getAction().equals(ACTION)){
		START_TIME = System.currentTimeMillis();
		Intent iJetty = new Intent(context,IJetty.class);
		iJetty.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		context.startActivity(iJetty);
		LogUtil.log("-----------------开机--------------" + START_TIME );
	    }
	}

}