package org.mortbay.ijetty.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.ijetty.MainApplication;
import org.mortbay.ijetty.component.AppsLocation;
import org.mortbay.ijetty.component.LogoImg;
import org.mortbay.ijetty.component.Point;
import org.mortbay.ijetty.network.IRequestListener;
import org.mortbay.ijetty.network.InterfaceOp;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.text.TextUtils;

public class PackageUtil {

	public static Map<String, Point> mPackageNames = new HashMap<String, Point>();
//	public static List<String> hidePkgs = new LinkedList<String>();
	static {

		if (mPackageNames == null || mPackageNames.size() == 0) {
			mPackageNames.put("org.bug.company", new Point(2, 6)); // 公司
			mPackageNames.put("org.bug.ezagoo.shopping", new Point(1, 6)); // 购物车
			mPackageNames.put("org.bug.hcpgson", new Point(3, 6)); // 火车预定
			mPackageNames.put("org.bug.recharge", new Point(1, 5)); // 话费充值
			mPackageNames.put("org.bug.airticket", new Point(2, 5)); // 机票预定
			mPackageNames.put("org.bug.banktransfer", new Point(1, 7)); // 银行转账
			mPackageNames.put("org.bug.dispensary", new Point(2, 7)); // 酒店预定
			mPackageNames.put("org.bug.ezagoo.creditcards", new Point(1, 8)); // 信用卡
			mPackageNames.put("org.bug.ezagoo.donation", new Point(2, 8)); // 爱心捐赠
			mPackageNames.put("com.test.cpicclienttest", new Point(3, 8)); // 刷卡体验
			mPackageNames.put("org.bug.ezagoo.gamerecharge", new Point(1, 9)); // 游戏充值
			mPackageNames.put("org.bug.browser", new Point(1, 4)); // E网导航
			mPackageNames.put("org.bug.movie", new Point(2, 4)); // 电影票
			mPackageNames.put("org.bug.master", new Point(3, 4)); // 店主管理
			mPackageNames.put("com.example.installconfigsettings", new Point(1,
					3)); // 设置
			mPackageNames.put("org.bug.sharebills", new Point(2, 3)); // 设置
		}

//		hidePkgs.add(AppConstants.MEDIA_PACKAGE_NAME);
//		hidePkgs.add(MainApplication.getInstance().getPackageName());
//		hidePkgs.add("com.bill99.kuaishua");
//		hidePkgs.add("com.mytime");
//		hidePkgs.add("org.bug.ezagoopad");
	}

	private static void getApps() {
		InterfaceOp.protoAppsLocation(new IRequestListener() {

			public void onError(Exception e) {
				// TODO Auto-generated method stub
				LogUtil.log("protoAppsLocation onError: " + e.getMessage());
			}

			public void onComplete(boolean isError, String errMsg,
					JSONObject respObj) {
				// TODO Auto-generated method stub
				if (isError)
					return;
				if (respObj == null)
					return;

				AppsLocation vAppsLoaction = new AppsLocation();
				boolean isChange = false;
				try {
//					vAppsLoaction.mRows = respObj.getInt("rows");
//					vAppsLoaction.mCold = respObj.getInt("cols");
//					if (vAppsLoaction.mRows != AppConstants.POLYGON_DEFAULT_ROWS
//							|| vAppsLoaction.mCold != AppConstants.POLYGON_DEFAULT_COLS)
//						isChange = true;
					JSONArray vRespArr = respObj.getJSONArray("applist");
					if (vRespArr != null && vRespArr.length() > 0 ) {
						
							vAppsLoaction.mApps = new HashMap<String, org.mortbay.ijetty.component.Point>();
							for (int i = 0; i < vRespArr.length(); i++) {
								JSONObject vJo = vRespArr.getJSONObject(i);
								Point vP = new Point(vJo.getInt("x"), vJo
										.getInt("y"));
								String vKey = vJo.getString("packagename");
								vAppsLoaction.mApps.put(vKey, vP);
								if (!isChange) {
//									if ((!mPackageNames.containsKey(vKey)
//											|| !mPackageNames.get(vKey).equals(vP)) && !hidePkgs.contains(vKey)) {
									if ((!mPackageNames.containsKey(vKey)
											|| !mPackageNames.get(vKey).equals(vP))) {
										isChange = true;
									}
								}
							}
							if(vAppsLoaction.mApps.size() != mPackageNames.size()) {
								isChange = true;
							}
						
					}
//					if (isChange) { // 重启
//						//FileUtil.putObject(AppConstants.APPS_LOCATION_PATH,	vAppsLoaction);
//						Handler handler = MainApplication.getInstance()
//								.getAppHandler();
//						if (handler != null)
//							handler.sendEmptyMessage(AppConstants.MSG_RELOCATE_LOGOIMG);
//					}

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		});
	}

//	public static final Point[] extAppPoints = new Point[] { new Point(2, 9),
//			new Point(1, 2), new Point(1, 10), new Point(2, 2),
//			new Point(2, 10), new Point(3, 2), new Point(3, 10),
//			new Point(1, 1), new Point(1, 11), new Point(2, 1),
//			new Point(2, 11) };
//	public static int extAppCount = 0;
//	private static Map<String, Point> extApps = new HashMap<String, Point>();

//	public synchronized static Point getAvailablePoint(String pkgname) {
//		if (extApps.containsKey(pkgname))
//			return extApps.get(pkgname);
//		if (extAppCount >= extAppPoints.length)
//			extAppCount = 0;
//		Point p = extAppPoints[extAppCount];
//		extApps.put(pkgname, p);
//		extAppCount++;
//		return p;
//	}

//	public synchronized static void addApp(String pkgname, String apkPath) {
//		// Log.e("gary", "addApp: " + pkgname + "   " + apkPath);
//		if (mPackageNames.containsKey(pkgname))
//			return;
//		if (TextUtils.isEmpty(pkgname) || TextUtils.isEmpty(apkPath))
//			return;
//		File f = new File(apkPath);
//		if (!f.exists())
//			return;
//		SharedPreferences sp = MainApplication
//				.getInstance()
//				.getSharedPreferences(AppConstants.PKG_SHAREDPREFERENCE_NAME, 0);
//		if (!sp.contains(pkgname))
//			sp.edit().putString(pkgname, apkPath).commit();
//
//	}

//	public synchronized static void removeApp(String pkgname) {
//		if (TextUtils.isEmpty(pkgname))
//			return;
//		SharedPreferences sp = MainApplication
//				.getInstance()
//				.getSharedPreferences(AppConstants.PKG_SHAREDPREFERENCE_NAME, 0);
//		if (sp.contains(pkgname)) {
//			sp.edit().remove(pkgname).commit();
//		}
//	}

//	private static void loadRecommendPkgName() {
//		SharedPreferences sp = MainApplication
//				.getInstance()
//				.getSharedPreferences(AppConstants.PKG_SHAREDPREFERENCE_NAME, 0);
//		Map<String, ?> map = sp.getAll();
//		if (map == null)
//			return;
//		Iterator<?> iter = map.entrySet().iterator();
//		while (iter.hasNext()) {
//			Map.Entry entry = (Map.Entry) iter.next();
//			String pkgname = (String) entry.getKey();
//			if (mPackageNames.containsKey(pkgname))
//				continue;
//			Point p = getAvailablePoint(pkgname);
//			mPackageNames.put(pkgname, p);
//		}
//
//		for (String pkgname : hidePkgs) {
//			if (mPackageNames.containsKey(pkgname))
//				mPackageNames.remove(pkgname);
//		}
//	}

//	public static List<String> fetchRecommendPkgNames() {
//		SharedPreferences sp = MainApplication
//				.getInstance()
//				.getSharedPreferences(AppConstants.PKG_SHAREDPREFERENCE_NAME, 0);
//		Map<String, ?> map = sp.getAll();
//		if (map == null)
//			return null;
//		List<String> pkgs = new LinkedList<String>();
//		Iterator<?> iter = map.entrySet().iterator();
//		while (iter.hasNext()) {
//			Map.Entry entry = (Map.Entry) iter.next();
//			String pkgname = (String) entry.getKey();
//
//			pkgs.add(pkgname);
//		}
//		return pkgs;
//	}

	public static List<LogoImg> getLocalApps(Context pContext) {
		getApps();
		PackageManager vPM = pContext.getPackageManager();
		List<LogoImg> vLogos = new ArrayList<LogoImg>();
		List<PackageInfo> vPkgInfos = vPM
				.getInstalledPackages(PackageManager.GET_ACTIVITIES);
//		loadRecommendPkgName();
		for (PackageInfo vPkgInfo : vPkgInfos) {
			ApplicationInfo appInfo = vPkgInfo.applicationInfo;
			// if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
			// || !mPoints.containsKey(appInfo.packageName))
			if (!mPackageNames.containsKey(appInfo.packageName))
				continue;
			if (vPkgInfo.activities == null || vPkgInfo.activities.length < 1)
				continue;

			LogoImg vLogo = new LogoImg();
			vLogo.setmPackgeName(appInfo.packageName);
			vLogo.setmAppName(appInfo.loadLabel(vPM).toString());
			BitmapDrawable vBD = (BitmapDrawable) appInfo.loadIcon(vPM);
			vLogo.setRowCol(mPackageNames.get(appInfo.packageName));
			vLogo.setBackgroundImg(new BitmapDrawable(vBD.getBitmap()));
			vLogos.add(vLogo);
		}
		return vLogos;
	}

	/**
	 * 获取当前APK的version code
	 * 
	 * @param context
	 *            上下文环境
	 * @return version code
	 */
	public static int getVersionCode(Context context) {
		PackageManager manager = context.getApplicationContext()
				.getPackageManager();
		int versionCode = 0;
		try {
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			versionCode = info.versionCode;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionCode;
	}

	public static String getVersionName(Context context) {
		PackageManager manager = context.getApplicationContext()
				.getPackageManager();
		String versionName = null;
		try {
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			versionName = info.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return versionName;
	}

	public static List<AppInfo> getInstalledAppsInfo() {
		List<AppInfo> vResult = new ArrayList<AppInfo>();
		PackageManager vPm = MainApplication.getInstance().getPackageManager();
		List<PackageInfo> vPkgInfo = vPm.getInstalledPackages(0);
		int count = vPkgInfo.size();
		File vFile = null;
		for (int i = 0; i < count; i++) {
			PackageInfo p = vPkgInfo.get(i);
			ApplicationInfo appInfo = p.applicationInfo;
			if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
				continue;
			} else {
				try {
					AppInfo vApp = new AppInfo();

					// vApp.mIcon = p.applicationInfo.loadIcon(vPm);
					vApp.mPackageName = appInfo.packageName;
					vApp.mAppName = p.applicationInfo.loadLabel(vPm).toString();
					vApp.mVersionName = p.versionName;
					vApp.mVersionCode = p.versionCode + "";
					vFile = new File(appInfo.publicSourceDir);
					// queryPacakgeSize(appInfo.packageName);
					vApp.mSize = vFile.length() + "";
					vResult.add(vApp);
				} catch (Exception e) {
					e.printStackTrace();
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					System.gc();
				}

			}
		}
		return vResult;
	}

	public static AppInfo getInstalledAppInfo(String packagename) {
		PackageManager vPm = MainApplication.getInstance().getPackageManager();
		List<PackageInfo> vPkgInfo = vPm.getInstalledPackages(0);
		int count = vPkgInfo.size();
		File vFile = null;
		for (int i = 0; i < count; i++) {
			PackageInfo p = vPkgInfo.get(i);
			ApplicationInfo appInfo = p.applicationInfo;
			if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {
				continue;
			}
			try {
				if (appInfo.packageName.equals(packagename)) {
					AppInfo vApp = new AppInfo();

					// vApp.mIcon = p.applicationInfo.loadIcon(vPm);
					vApp.mPackageName = appInfo.packageName;
					vApp.mAppName = p.applicationInfo.loadLabel(vPm).toString();
					vApp.mVersionName = p.versionName;
					vApp.mVersionCode = p.versionCode + "";
					vFile = new File(appInfo.publicSourceDir);
					// queryPacakgeSize(appInfo.packageName);
					vApp.mSize = vFile.length() + "";
					return vApp;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return null;
	}
}
