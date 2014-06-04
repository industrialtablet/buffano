package org.mortbay.ijetty;

import android.app.Application;
import android.os.Handler;
import android.view.WindowManager;

/*
 * 使用该类需要在AndroidManifest.xml文件中<application></application>域新增属性
 * android:name=".MainApplication"，否则程序是调用MainApplication为空指针
 * */
public class MainApplication extends Application {

	private static MainApplication instance;
	private Handler appHandler;
	public static String ADDR = "";
	public static boolean singlgCoreFlag = false;
	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		CrashHandler.getInstance().init(this);
		//WeatherAndAddressUtil.initCitysInfo();
		String mode = android.os.Build.MODEL;
                singlgCoreFlag = mode.startsWith("f04ref_BYW_ZH");
	}
	
	private WindowManager.LayoutParams windowParams = new WindowManager.LayoutParams();

	public WindowManager.LayoutParams getWindowParams() {
		return windowParams;
	}

	public static MainApplication getInstance() {
		return instance;
	}

	public Handler getAppHandler() {
		return appHandler;
	}

	public void setAppHandler(Handler appHandler) {
		this.appHandler = appHandler;
	}
}
