package org.mortbay.ijetty.javascript;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.mortbay.ijetty.AppConstants;
import org.mortbay.ijetty.IJetty;
import org.mortbay.ijetty.MainApplication;
import org.mortbay.ijetty.movieservice.MediaPlaybackService;
import org.mortbay.ijetty.movieservice.MyFloatView;
import org.mortbay.ijetty.util.LogUtil;
import org.mortbay.ijetty.util.StringUtils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.app.Activity;

public class ProxyBridge
{
    public final static String LOGCAT = "=>ProxyBridge";
    private Context mContext;

    public ProxyBridge(Context context)
    {
        mContext = context;
    }

    private static boolean execWithRoot(String... args)
    {
        Process proc = null;
        try
        {
            proc = Runtime.getRuntime().exec("/system/bin/sh",null,new File("/system/bin"));
            // proc = Runtime.getRuntime().exec("su");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        if (proc != null)
        {
            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(proc.getOutputStream())),true);
            for (String arg : args)
            {
                out.println(arg);
                //Log.e("gary", "cmd arg: " + arg);
            }
            try
            {
                String line;
                while ((line = in.readLine()) != null)
                {
                    LogUtil.log(line);
                    if (line.contains("Success"))
                        return true;
                    else if (line.contains("Error"))
                        return false;
                    else
                        return false;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                /**
                 * Root安装失败，尝试非ROOT安装
                 */
            }
            finally
            {

                try
                {
                    if (in != null)
                        in.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
                out.close();
                proc.destroy();
            }
        }
        return false;
    }

    @JavascriptInterface
    private static boolean movieViewStretch()
    {
        boolean isSuccess = false;
        String cmd = "echo 1 > /sys/class/video/screen_mode" + "\n";
        try
        {
            execWithRoot(cmd);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return isSuccess;
    }

    /*
     * 判断文件是否存在
     * @param filePath : 文件路径，可以是文件或者文件夹
     * */
    @JavascriptInterface
    public boolean fileExist(String filePath)
    {
        boolean isFileExist = false;
        File file = new File(AppConstants.getSdFolder() + "jetty/webapps/console/demo/" + filePath);
        //Log.w("===smallstar===", AppConstants.getSdFolder() + "jetty/webapps/console/demo/" + filePath);
        if (file.exists())
        {
            isFileExist = true;
        }
        //Log.w("===smallstar===", String.valueOf(isFileExist));
        return isFileExist;
    }

    /*
     * 系统设置
     * */
    @JavascriptInterface
    public void Settings()
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS); //系统设置
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainApplication.getInstance().startActivity(intent);
            }
        };
        Activity a = (Activity)mContext;
        a.runOnUiThread(runnable);
    }

    @JavascriptInterface
    public void wifiSet()
    {
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                Intent intentActivity = new Intent();// = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                if (android.os.Build.VERSION.SDK_INT > 10)
                {
                    intentActivity.setComponent(new ComponentName("com.android.settings","com.android.settings.wifi.WifiPickerActivity"));
                    intentActivity.putExtra("extra_prefs_show_button_bar",true);
                }
                else
                {
                    intentActivity = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                }
                intentActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainApplication.getInstance().startActivity(intentActivity);
            }
        };
        Activity a = (Activity)mContext;
        a.runOnUiThread(runnable);
    }

    /*
     * 播放网页助手函数
     * @param urlPlayListJson:网页播放清单字符串
     * */
    @JavascriptInterface
    public void playUrlAssist(String urlPlayListJson)
    {
        Log.w(LOGCAT,"playUrlAssist()" + urlPlayListJson);
        if(urlPlayListJson.isEmpty())
        {
            Log.e(LOGCAT,"playUrlAssist() error, urlPlayListJson is null.");
            return;
        }
        AppConstants.URLPLAYLIST = urlPlayListJson;
    }
    
    @JavascriptInterface
    public void settingTimeOut()
    {
        Log.w(LOGCAT,"settingTimeOut()");
        AppConstants.isSettingsTimeOut = true;
        IJetty.getInstance().mWebView.loadUrl(AppConstants.CLIENT_CUR_PLAYURL);
        IJetty.getInstance().mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        IJetty.getInstance().mWebView.clearHistory();
        IJetty.getInstance().mWebView.clearFormData();
        IJetty.getInstance().mWebView.clearCache(true);
    }

    /*
     * 获取屏幕分辨率
     * */
    @JavascriptInterface()
    public String getResolution()
    {
        return AppConstants.RESOLUTION;//Resolution
    }

    @JavascriptInterface
    public String getNetworkType()
    {
        return AppConstants.NETWORK_TYPE;
    }

    @JavascriptInterface
    public String getIp()
    {
        String ret = "";
        if (AppConstants.NETWORK_TYPE.equals("WIFI"))
        {
            ret = AppConstants.WIFI_IP;
        }
        else if (AppConstants.NETWORK_TYPE.equals("ethernet"))
        {
            ret = AppConstants.ETH_IP;
        }
        else
        {
            ret = "unkown network type!";
        }
        return ret;
    }

    @JavascriptInterface
    public String getMac()
    {
        String ret = "";
        if (AppConstants.NETWORK_TYPE.equals("WIFI"))
        {
            ret = AppConstants.WIFI_MAC;
        }
        else if (AppConstants.NETWORK_TYPE.equals("ethernet"))
        {
            ret = AppConstants.ETH_MAC;
        }
        else
        {
            ret = "unkown network type!";
        }
        return ret;
    }

    @JavascriptInterface
    public void ethSet()
    {
        if (StringUtils.replaceBlank(AppConstants.BOARD_MODEL).equals("A06"))
        {
            return;
        }
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                Intent intentActivity = new Intent();// = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
                if (android.os.Build.VERSION.SDK_INT > 10)
                {
                    intentActivity.setComponent(new ComponentName("com.android.settings","com.android.settings.ethernet.EthernetPickerActivity"));
                    intentActivity.putExtra("extra_prefs_show_button_bar",true);
                }
                else
                {
                    intentActivity = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
                }
                intentActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainApplication.getInstance().startActivity(intentActivity);
            }
        };
        Activity a = (Activity)mContext;
        a.runOnUiThread(runnable);
    }

    /*
     * 设置播放清单json格式字符串
     * movieViewPrepare调用之前需要设置
     * */
    @JavascriptInterface
    public void moviewViewSetPlayListJsonString(String jsonString)
    {
        Log.e(LOGCAT,"moviewViewSetPlayListJsonString:" + jsonString);
        if (jsonString.isEmpty())
        {
            Log.e(LOGCAT,"moviewViewSetPlayListJsonString(jsonString is empty.)");
            return;
        }
        MyFloatView.playListJsonString = jsonString;
    }

    /*
     * 准备打开视频前的第一步设置
     * @param x，y:视频窗口位置坐标
     * @param width, height:视频窗口长和宽
     * 
     * */
    @JavascriptInterface
    public void movieViewPrepare(float x, float y, float width, float height)
    {
        Log.w(LOGCAT,"movieViewPrepare()");
        MyFloatView.x = x;
        MyFloatView.y = y;
        MyFloatView.width = width;
        MyFloatView.height = height;
        Log.w(LOGCAT,String.valueOf(x) + "," + String.valueOf(y) + "," + String.valueOf(width) + "," + String.valueOf(height));
        //打开视频窗口
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                MyFloatView.listAllMediaFiles();
                Intent intent = new Intent(mContext,MediaPlaybackService.class);
                intent.setAction("createUI");
                mContext.startService(intent);
            }
        };
        Activity a = (Activity)mContext;
        a.runOnUiThread(runnable);
    }

    /*
     * 获取视频窗口准备状态
     * 准备好了才播放：true为准备好了
     * */
    @JavascriptInterface
    public boolean getMovieViewPrepareStatus()
    {
        Log.w(LOGCAT,"getMovieViewPrepareStatus()");
        return MyFloatView.mPlayViewPrepareStatus;
    }

    /*
     * 关闭视频窗口
     * */
    @JavascriptInterface
    public void movieViewClose()
    {
        //关闭视频窗口
        Log.w(LOGCAT,"movieViewClose()");
        if (!MyFloatView.mPlayViewStatus)
            return;
        MyFloatView.mPlayViewPrepareStatus = false;
        MyFloatView.mPlayViewStatus = false;
        MyFloatView.onExit();
    }

    /*
     * 启动开始播放视频
     * 
     * @param autoPlayList:是否自动轮流播放播单里面的视频，如果为false，当前视频播放完毕后将停止播放
     * */
    @JavascriptInterface
    public void movieViewStarPlay(boolean autoPlay)
    {
        Log.w(LOGCAT,"movieViewStarPlay()");
        if (autoPlay)
        {
            MyFloatView.mAutoPlayList = true;
        }
        else
        {
            MyFloatView.mAutoPlayList = false;
        }
        MyFloatView.startPlay();
    }

    /*
     * 更新播放窗口的大小
     * @param width, height:播放窗口的长和宽
     * */
    @JavascriptInterface
    public void updateViewSize(float width, float height)
    {
        Log.v(LOGCAT,"-------------------updateViewSize--------------------------");
        MyFloatView.width = width;
        MyFloatView.height = height;
        MyFloatView.updateViewSize();
        //            Runnable runnable = new Runnable() {
        //                public void run() {
        //                    
        //                }
        //            };
        //            Activity a = (Activity) mContext;
        //            a.runOnUiThread(runnable);	    
    }

    /*
     * 更新播放窗口的位置
     * 
     * param x,y:播放窗口的位置坐标
     * */
    @JavascriptInterface
    public void updateViewPosition(float x, float y)
    {
        Log.v("smallstar x",String.valueOf(x));
        Log.v("smallstar y",String.valueOf(y));
        MyFloatView.x = x;
        MyFloatView.y = y;
        MyFloatView.updateViewPosition();
        //            Log.v("smallstar", "-------------------updateViewPosition--------------------------");
        //            Runnable runnable = new Runnable() {
        //                public void run() {
        //                    
        //                }
        //            };
        //            Activity a = (Activity) mContext;
        //            a.runOnUiThread(runnable);	    
    }

    @JavascriptInterface
    public void moveLeft()
    {
        MyFloatView.x = 100;
        MyFloatView.y = 100;
        Log.v("smallstar","-------------------moveleft--------------------------");
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                MyFloatView.updateViewPosition();
            }
        };
        Activity a = (Activity)mContext;
        a.runOnUiThread(runnable);
    }

    @JavascriptInterface
    public void moveright()
    {
        Log.v("smallstar","-------------------moveright--------------------------");
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                MyFloatView.zoomIn();
            }
        };
        Activity a = (Activity)mContext;
        a.runOnUiThread(runnable);
    }
    /*
    @JavascriptInterface
    public void timeZoneSet() {
    	//mWebView.loadUrl("http://www.baidu.com");
        //Toast.makeText(getApplicationContext(),"xdtianyu",Toast.LENGTH_LONG).show();
    	Log.v("smallstar", "ethSet()");
    	Runnable runnable = new Runnable() {
    	    public void run() {
    	    	Intent intentActivity = new Intent();// = new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS);
    	    	if(android.os.Build.VERSION.SDK_INT > 10 ){  
    	    		intentActivity.setComponent(new ComponentName("com.android.settings",
    	    				"com.android.settings.ZonePickerActivity"));
    	    		intentActivity.putExtra("extra_prefs_show_button_bar", true);
    	    	}else {  
    	    		intentActivity = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
    	    	}
    	    	intentActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	    	MainApplication.getInstance().startActivity(intentActivity);
    	    }
    	};
    	Activity a = (Activity) mContext;
    	a.runOnUiThread(runnable);
    }
    */
}

/*
 * mHandler.post(new Runnable(){   public void run() { mWebView.loadUrl("javascript:wave()"); 
 * */