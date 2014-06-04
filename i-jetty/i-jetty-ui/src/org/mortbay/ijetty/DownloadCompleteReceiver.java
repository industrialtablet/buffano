package org.mortbay.ijetty;

import java.io.File;

import org.mortbay.ijetty.util.ApkUtils;
import org.mortbay.ijetty.util.FileUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class DownloadCompleteReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context context, Intent intent) {
		String fn = intent.getStringExtra("filename");
		if (fn == null)
			return;
		File newFile = null;
		if (fn.endsWith(".tmp")) {
			newFile = FileUtil.renameTmp(fn);
		} else {
			newFile = new File(fn);
		}
		if (newFile == null || !newFile.exists())
			return;
		final String filename = newFile.getAbsolutePath();
		if (filename.endsWith(".apk")) {
		    Intent intentNew = new Intent();
	                Log.e("smallstar", "sendAPKDownloadCompleteBroadcase filename: " + filename);
	                intentNew.setAction(AppConstants.ACTION_APK_DOWN_FINISH);
	                intentNew.putExtra("filename", filename);
	                MainApplication.getInstance().sendBroadcast(intentNew);
		}
	}

}
