package org.mortbay.ijetty.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.ijetty.IJetty;
import org.mortbay.ijetty.MainApplication;
import org.mortbay.ijetty.R;
import org.mortbay.ijetty.network.IRequestListener;
import org.mortbay.ijetty.network.InterfaceOp;
import org.mortbay.ijetty.network.NetworkUtil;
import org.mortbay.ijetty.util.PlayListUtil.DownloadFile;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.webkit.WebSettings;

//import android.content.pm.IPackageInstallObserver;
//import android.content.pm.IPackageDeleteObserver;

public class ApkUtils {
	
//import android.content.pm.IPackageInstallObserver;
//import android.content.pm.IPackageDeleteObserver;
    private final static String TAG = "ApkUtils";
    public static String apkPushVersion = "";
    public static boolean isApkChanged = false;
    public static boolean isApkFileSynced = true;
    public static boolean isApkGetNeedConfirm = false;
    private static String mStrFileList;
    public static List<DownloadFile> mListDownloadFiles = new ArrayList<DownloadFile>();
    

    public static final int SUCCEEDED = 1;
    public static final int FAILED = 0;
    public static final String ACTION_INSTALL_COMPLETE = "com.mylayout.app.pm.silent.install";
    public static final String ACTION_UNINSTALL_COMPLETE = "com.mylayout.app.pm.silent.uninstall";
    
    public static class DownloadFile{
        String url;
        String fileName;
        String time;
        String size;
        String folder;
    }
    
    public static boolean syncApkFiles()
    {
        boolean filesDownloadFinished = false;
        File uploadDir = new File(IJetty.__JETTY_DIR + "/" + IJetty.__WEBAPP_DIR + "/" + IJetty.__UPLOAD_DIR);
        if (!uploadDir.exists())
        {
            boolean made = uploadDir.mkdirs();
            Log.i(TAG,"Made " + uploadDir + ": " + made);
        }
        if(mListDownloadFiles.isEmpty()) {filesDownloadFinished = true; return filesDownloadFinished;}
        int i = 0;
        for(DownloadFile downloadFile : mListDownloadFiles)
        {
            //判断文件夹是否存在
            File downloadFolder = new File(IJetty.__JETTY_DIR + "/" + IJetty.__WEBAPP_DIR + "/" + IJetty.__UPLOAD_DIR + "/" + downloadFile.folder);
            if(!downloadFolder.exists()){boolean made = downloadFolder.mkdirs();Log.i(TAG,"Made " + downloadFolder + ": " + made);}
            //判断文件是否存在
            File file = new File(IJetty.__JETTY_DIR + "/" + IJetty.__WEBAPP_DIR + "/" + IJetty.__UPLOAD_DIR + "/" + downloadFile.folder + "/" + downloadFile.fileName);
            if(!file.exists())
            {//文件不存在下载
                NetworkUtil.requestDownload(downloadFile.url, IJetty.__JETTY_DIR + "/" + IJetty.__WEBAPP_DIR + "/" + IJetty.__UPLOAD_DIR + "/" + downloadFile.folder);
            }
            else
            {//文件存在，从mListDownloadFiles移除
                mListDownloadFiles.remove(i);
                break;
            }
            i++;
        }
        return filesDownloadFinished;
    }
    
    //确认APK下载完
    public static void confirmGetApks()
    {
        InterfaceOp.protoApksConfirm(new IRequestListener() {
            public void onError(Exception e) {
                    LogUtil.log("confirmGetApks onError   " + e.getMessage());
            }
            public void onComplete(boolean isError, String errMsg,
                            JSONObject respObj) {
                    if(respObj.optString("result", "").equals("false"))
                    {
                        Log.e("====smallstar=====", respObj.optString("error", ""));
                        return;
                    }
            }
        });
    }
    
    //获取需要下载的apk
    public static void getApks()
    {
        InterfaceOp.protoApksGet(new IRequestListener() {
            public void onError(Exception e) {
                    LogUtil.log("getApkList onError   " + e.getMessage());
            }
            public void onComplete(boolean isError, String errMsg,
                            JSONObject respObj) {
                    if(respObj.optString("result", "") == "false")
                    {
                        Log.e("====smallstar=====", respObj.optString("error", ""));
                        return;
                    }
                    //========================playlist==========================
                    if(respObj.optString("files", "") == "")
                    {
                        Log.e("====smallstar=====", "files is null!");
                        return;
                    }
                    else
                    {
                        Log.w("====smallstar=====", respObj.optString("files", ""));
                        mStrFileList = respObj.optString("files", "");
                    }
                    try
                    {
                        JSONArray jsonar = new JSONArray(mStrFileList);
                        for(int i=0; i<jsonar.length(); i++)
                        {
                            JSONObject oj = jsonar.getJSONObject(i);
                            DownloadFile downloadFile = new DownloadFile();
                            Log.v("=====smallstar====", oj.getString("url"));
                            downloadFile.url = oj.getString("url");
                            downloadFile.fileName = downloadFile.url.substring(downloadFile.url.lastIndexOf("/")+1);
                            downloadFile.time = oj.getString("time");
                            downloadFile.size = oj.getString("size");
                            downloadFile.folder = oj.getString("folder");
                            mListDownloadFiles.add(downloadFile);
                        }
                    }
                    catch (JSONException e)
                    {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    isApkChanged = false;
                    isApkFileSynced = false;
            }
        });
    }

	public static void uninstallApkWithSilent(final String packageName) {
//		if(packageName.contains("com.mylayout.app"))
//			return;
//		PackageManager pm = MainApplication.getInstance().getPackageManager();
//		PackageDeleteObserver observer = new PackageDeleteObserver();
//		pm.deletePackage(packageName, observer, 0);
	}

//	private static class PackageDeleteObserver extends
//			IPackageDeleteObserver.Stub {
//		public void packageDeleted(String packageName, int returnCode) {
//			Log.e("gary", "Delete Complete==>packageName: " + packageName
//					+ " result: " + returnCode);
//			//PackageUtil.removeApp(packageName);
//		}
//	}
//
//	private static class PackageInstallObserver extends
//			IPackageInstallObserver.Stub {
//		public void packageInstalled(String packageName, int returnCode) {
//			Log.e("gary", "Install Complete==>packageName: " + packageName
//					+ " result: " + returnCode);
//		}
//	};

	public static void sendBroadcast2(String action, int returnCode,
			String packagename) {
		Intent intent = new Intent();
		intent.setAction(action);
		intent.putExtra("returncode", returnCode);
		intent.putExtra("packagename", packagename);
		MainApplication.getInstance().sendBroadcast(intent);

	}

	public synchronized static void installAndStartApkNoRoot(final Context ctx,
			final String fileName) {
//		if (TextUtils.isEmpty(fileName))
//			return;
//		if (!fileName.toLowerCase().endsWith(".apk"))
//			return;
//		File f = new File(fileName);
//		try {
//			if (!f.exists())
//				return;
//			if (!f.isFile())
//				return;
//			Uri uri = Uri.fromFile(f);
//			int installFlags = 0;
//			PackageManager pm = ctx.getPackageManager();
//			String packagename = getApkPackagename(ctx, fileName);
//			if(TextUtils.isEmpty(packagename)){
//				f.delete();
//				return ;
//			}
//			Log.e("gary", "packagename: " + packagename);
//			PackageInfo pi = null;
//			try {
//				pi = pm.getPackageInfo(packagename,
//						PackageManager.GET_UNINSTALLED_PACKAGES);
//			} catch (NameNotFoundException e) {
//				e.printStackTrace();
//			}
//			// package exists
//			if (pi != null) {
//				// if (packagename.equals("com.bill99.kuaishua"))
//				 installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
//				// else
//				// uninstallApkWithSilent(packagename);
//			}
//			//if(!PackageUtil.hidePkgs.contains(packagename))
//				//PackageUtil.addApp(packagename, fileName);
//			PackageInstallObserver observer = new PackageInstallObserver();
//			pm.installPackage(uri, observer, installFlags, packagename);
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			if(f != null && f.exists()){
//				f.delete();
//			}
//		}
	}

	public static void startApk(Context ctx, String apkPath) {
		final PackageManager pm = ctx.getPackageManager();
		PackageInfo pInfo = pm.getPackageArchiveInfo(apkPath,
				PackageManager.GET_ACTIVITIES);
		if (pInfo == null)
			return;
		String packageName = pInfo.packageName;
		Intent intent = pm.getLaunchIntentForPackage(packageName);
		if (intent == null)
			return;
		ctx.startActivity(intent);
	}

//	public static void installAndStartApk(final Context context,
//			final String apkPath) {
//		if (TextUtils.isEmpty(apkPath) || (context == null))
//			return;
//
//		File file = new File(apkPath);
//		if (!file.exists())
//			return;
//
//		Thread t = new Thread() {
//			public void run() {
//				String packageName = getApkPackagename(context, apkPath);
//				if (silentInstall(apkPath)) {
//					List<ResolveInfo> matches = findActivitiesForPackage(
//							context, packageName);
//					LogUtil.log("matches: " + matches);
//					if ((matches != null) && (matches.size() > 0)) {
//						ResolveInfo resolveInfo = matches.get(0);
//						ActivityInfo activityInfo = resolveInfo.activityInfo;
//						startApk(activityInfo.packageName, activityInfo.name);
//					}
//				}
//			};
//		};
//		t.setDaemon(true);
//		t.start();
//
//	}

	public static String getApkPackagename(Context context, String apkPath) {
		if (apkPath == null)
			return null;

		PackageManager pm = context.getPackageManager();
		PackageInfo info = pm.getPackageArchiveInfo(apkPath,
				PackageManager.GET_ACTIVITIES);
		if (info == null)
			return null;

		return info.packageName;
	}

	private static List<ResolveInfo> findActivitiesForPackage(Context context,
			String packageName) {
		LogUtil.log("findActivitiesForPackage packagename:" + packageName);
		final PackageManager pm = context.getPackageManager();

		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		mainIntent.setPackage(packageName);

		final List<ResolveInfo> apps = pm.queryIntentActivities(mainIntent, 0);
		return apps != null ? apps : new ArrayList<ResolveInfo>();
	}

	public static boolean silentInstall(String apkPath) {
		String cmd1 = "chmod 777 " + apkPath + " \n";
		String cmd2 = "LD_LIBRARY_PATH=/vendor/lib:/system/lib pm install -r "
				+ apkPath + " \n";
		LogUtil.log("silentInstall cmd1: " + cmd1);
		LogUtil.log("silentInstall cmd2: " + cmd2);
		return execWithRoot(cmd1, cmd2);
	}

	private static boolean execWithRoot(String... args) {
		Process proc = null;
		try {
			proc = Runtime.getRuntime().exec("/system/bin/sh", null,
					new File("/system/bin"));
			// proc = Runtime.getRuntime().exec("su");
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (proc != null) {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					proc.getInputStream()));
			PrintWriter out = new PrintWriter(new BufferedWriter(
					new OutputStreamWriter(proc.getOutputStream())), true);
			for (String arg : args) {
				out.println(arg);
				//Log.e("gary", "cmd arg: " + arg);
			}
			try {
				String line;
				while ((line = in.readLine()) != null) {
					LogUtil.log(line);
					if (line.contains("Success"))
						return true;
					else if (line.contains("Error"))
						return false;
					else
						return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				/**
				 * Root安装失败，尝试非ROOT安装
				 */

			} finally {

				try {
					if (in != null)
						in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				out.close();
				proc.destroy();
			}
		}
		return false;
	}

//	public static void startApk(final String packageName,
//			final String activityName) {
//		new Thread(new Runnable() {
//
//			public void run() {
//				String cmd = "am start -n " + packageName + "/" + activityName
//						+ " \n";
//				try {
//					execWithRoot(cmd);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//
//			}
//		}).start();
//	}
	public static void startApk(final String packageName,
			final String activityName) {
		try {
			Intent intent = new Intent();
			intent.setComponent(new ComponentName(packageName, packageName
					+ activityName));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			MainApplication.getInstance().startActivity(intent);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void startService( String pakcageName, String className) {

		Intent intent = new Intent();
		intent.setClassName(pakcageName,
				className);
		MainApplication.getInstance().startService(intent);
	}
	
	public static AppInfo getApkFileInfo(Context ctx, String apkPath) {
		Drawable vDefaultDraw = ctx.getResources().getDrawable(R.drawable.ic_launcher_1);
		File apkFile = new File(apkPath);
		if (!apkFile.exists() || !apkPath.toLowerCase().endsWith(".apk")) {
			System.out.println("文件路径不正确");
			return null;
		}
		AppInfo appInfoData;
		String PATH_PackageParser = "android.content.pm.PackageParser";
		String PATH_AssetManager = "android.content.res.AssetManager";
		try {
			// 反射得到pkgParserCls对象并实例化,有参数
			Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
			Class<?>[] typeArgs = { String.class };
			Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
			Object[] valueArgs = { apkPath };
			Object pkgParser = pkgParserCt.newInstance(valueArgs);

			// 从pkgParserCls类得到parsePackage方法
			DisplayMetrics metrics = new DisplayMetrics();
			metrics.setToDefaults();// 这个是与显示有关的, 这边使用默认
			typeArgs = new Class<?>[] { File.class, String.class,
					DisplayMetrics.class, int.class };
			Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod(
					"parsePackage", typeArgs);

			valueArgs = new Object[] { new File(apkPath), apkPath, metrics, 0 };

			// 执行pkgParser_parsePackageMtd方法并返回
			Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser,
					valueArgs);

			// 从返回的对象得到名为"applicationInfo"的字段对象
			if (pkgParserPkg == null) {
				return null;
			}
			Field appInfoFld = pkgParserPkg.getClass().getDeclaredField(
					"applicationInfo");

			// 从对象"pkgParserPkg"得到字段"appInfoFld"的值
			if (appInfoFld.get(pkgParserPkg) == null) {
				return null;
			}
			ApplicationInfo info = (ApplicationInfo) appInfoFld
					.get(pkgParserPkg);

			// 反射得到assetMagCls对象并实例化,无参
			Class<?> assetMagCls = Class.forName(PATH_AssetManager);
			Object assetMag = assetMagCls.newInstance();
			// 从assetMagCls类得到addAssetPath方法
			typeArgs = new Class[1];
			typeArgs[0] = String.class;
			Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod(
					"addAssetPath", typeArgs);
			valueArgs = new Object[1];
			valueArgs[0] = apkPath;
			// 执行assetMag_addAssetPathMtd方法
			assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);

			// 得到Resources对象并实例化,有参数
			Resources res = ctx.getResources();
			typeArgs = new Class[3];
			typeArgs[0] = assetMag.getClass();
			typeArgs[1] = res.getDisplayMetrics().getClass();
			typeArgs[2] = res.getConfiguration().getClass();
			Constructor<Resources> resCt = Resources.class
					.getConstructor(typeArgs);
			valueArgs = new Object[3];
			valueArgs[0] = assetMag;
			valueArgs[1] = res.getDisplayMetrics();
			valueArgs[2] = res.getConfiguration();
			res = (Resources) resCt.newInstance(valueArgs);

			// 读取apk文件的信息
			appInfoData = new AppInfo();
			if (info != null) {
				if (info.icon != 0) {// 图片存在，则读取相关信息
					Drawable icon = res.getDrawable(info.icon);// 图标
					appInfoData.mIcon = icon;
				} else {
					appInfoData.mIcon = vDefaultDraw;
				}
				if (info.labelRes != 0) {
					String neme = (String) res.getText(info.labelRes);// 名字
					appInfoData.mAppName = neme;
				} else {
					String apkName = apkFile.getName();
					appInfoData.mAppName = apkName.substring(0,
							apkName.lastIndexOf("."));
				}
				String pkgName = info.packageName;// 包名
				appInfoData.mPackageName = pkgName;
			} else {
				return null;
			}
			PackageManager pm = ctx.getPackageManager();
			PackageInfo packageInfo = pm.getPackageArchiveInfo(apkPath,
					PackageManager.GET_ACTIVITIES);
			if (packageInfo != null) {
				appInfoData.mVersionName = packageInfo.versionName;// 版本号
				appInfoData.mVersionCode = packageInfo.versionCode + "";// 版本码
			}
			return appInfoData;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean uninstallSlient(String packagename) {
		boolean isSuccess = false;

		String cmd = "pm uninstall " + packagename +  " \n";
		try {
			execWithRoot(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSuccess;
	}
	
	public static boolean installSlient(String filePath) {
		boolean isSuccess = false;

		String cmd = "pm install -r " + filePath +  " \n";
		try {
			execWithRoot(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSuccess;
	}
	
	public static boolean installSlient2(Context context, String filePath) {
		if (TextUtils.isEmpty(filePath))
			return false;
		File file = new File(filePath);
		if (!file.exists()) {
			return false;
		}

		String[] args = { "pm", "install", "-r", filePath };
		ProcessBuilder processBuilder = new ProcessBuilder(args);

		Process process = null;
		BufferedReader successResult = null;
		BufferedReader errorResult = null;
		StringBuilder successMsg = new StringBuilder();
		StringBuilder errorMsg = new StringBuilder();
		try {
			process = processBuilder.start();
			successResult = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			errorResult = new BufferedReader(new InputStreamReader(
					process.getErrorStream()));
			String s;

			while ((s = successResult.readLine()) != null) {
				successMsg.append(s);
			}

			while ((s = errorResult.readLine()) != null) {
				errorMsg.append(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (successResult != null) {
					successResult.close();
				}
				if (errorResult != null) {
					errorResult.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (process != null) {
				process.destroy();
			}
		}

		if (successMsg.toString().contains("Success")
				|| successMsg.toString().contains("success")) {
			return true;
		} else {
			return false;
		}
	}
	public static boolean uninstallSlient2(Context context, String packagename) {
		if (TextUtils.isEmpty(packagename))
			return false;

		String[] args = { "pm", "uninstall", packagename };
		ProcessBuilder processBuilder = new ProcessBuilder(args);

		Process process = null;
		BufferedReader successResult = null;
		BufferedReader errorResult = null;
		StringBuilder successMsg = new StringBuilder();
		StringBuilder errorMsg = new StringBuilder();
		try {
			process = processBuilder.start();
			successResult = new BufferedReader(new InputStreamReader(
					process.getInputStream()));
			errorResult = new BufferedReader(new InputStreamReader(
					process.getErrorStream()));
			String s;

			while ((s = successResult.readLine()) != null) {
				successMsg.append(s);
			}

			while ((s = errorResult.readLine()) != null) {
				errorMsg.append(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (successResult != null) {
					successResult.close();
				}
				if (errorResult != null) {
					errorResult.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (process != null) {
				process.destroy();
			}
		}

		if (successMsg.toString().contains("Success")
				|| successMsg.toString().contains("success")) {
			return true;
		} else {
			return false;
		}
	}

    public static boolean isBackgroundRunning(Context context, String packageName)
    {
        //判断应用是否在运行 
        Log.i(TAG,"isBackgroundRunning().................");
        ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        KeyguardManager keyguardManager = (KeyguardManager)context.getSystemService(Context.KEYGUARD_SERVICE);
        if (activityManager == null)
            return false;
        // get running application processes
        List<ActivityManager.RunningAppProcessInfo> processList = activityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo process : processList)
        {
            //Log.i(TAG, process.processName);
            if (process.processName.startsWith(packageName))
            {
                boolean isBackground = process.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND && process.importance != RunningAppProcessInfo.IMPORTANCE_VISIBLE;
                boolean isLockedState = keyguardManager.inKeyguardRestrictedInputMode();
                if (isBackground || isLockedState)
                    return true;
                else
                    return false;
            }
        }
        return false;
    }
	
	public static boolean isApkInstalled(String filename) {
		String packagename = ApkUtils.getApkPackagename(
				MainApplication.getInstance(), filename);
		AppInfo installAppInfo = PackageUtil.getInstalledAppInfo(packagename);
		// 未安装
		if (installAppInfo == null)
			return false;
		AppInfo uninstallAppInfo = ApkUtils.getApkFileInfo(
				MainApplication.getInstance(), filename);
		int uninstallVersionCode = 0, installVersionCode = 0;
		// 已经安装，对比版本号
		try {
			uninstallVersionCode = Integer
					.parseInt(uninstallAppInfo.mVersionCode);
			installVersionCode = Integer.parseInt(installAppInfo.mVersionCode);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		if (uninstallVersionCode != installVersionCode)
			return false;
		return true;
	}
	
	public static void execCmdThread(final String cmd) {
		Thread t = new Thread(new Runnable() {

			public void run() {
				try {
					doExecCmd(cmd);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		t.setDaemon(true);
		t.start();
	}
	public static String doExecCmd(String cmd) {
		Runtime runtime = Runtime.getRuntime();
		BufferedReader br = null;
		InputStream input = null;
		try {
			Process process = runtime.exec(cmd);

			input = process.getInputStream();
			br = new BufferedReader(new InputStreamReader(input));
			StringBuilder sb = new StringBuilder();
			String strLine;
			while (null != (strLine = br.readLine())) {
				sb.append(strLine + "\r\n");
			}
			String content = sb.toString();
			return content;
		} catch (IOException e) {
			e.printStackTrace();
			return e.getMessage();
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (input != null)
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	public static void sendDebuggableBroadcast(boolean debugFlag) {
		Intent intent = new Intent();
		if (MainApplication.singlgCoreFlag) {
			intent.setAction("com.mytime.action.debug.flag.baomi");
		} else {
			intent.setAction("com.mytime.ycf.action.debug.flag.baomi");

		}
		intent.putExtra("debugFlag", debugFlag);
		MainApplication.getInstance().sendBroadcast(intent);
	}
}
