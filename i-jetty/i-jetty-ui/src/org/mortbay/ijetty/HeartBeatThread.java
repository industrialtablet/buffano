package org.mortbay.ijetty;

import java.io.File;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.ijetty.network.IRequestListener;
import org.mortbay.ijetty.network.InterfaceOp;
import org.mortbay.ijetty.util.AmlogicExt;
import org.mortbay.ijetty.util.ApkUtils;
import org.mortbay.ijetty.util.DateUtils;
import org.mortbay.ijetty.util.FileUtil;
import org.mortbay.ijetty.util.LogUtil;
import org.mortbay.ijetty.util.PlayListUtil;
import org.mortbay.ijetty.util.PropertiesUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.WebSettings;
import android.os.Bundle;

public class HeartBeatThread
{
    private final static String LOGCAT = "=>HeartBeatThread";

    private Thread thread;
    private boolean doneFlag = false;
    private static HeartBeatThread instance;
    private static boolean threadStartFlag = false;
    private PropertiesUtils mPropertiesUtil = new PropertiesUtils();;

    //	String mActivityName = null;

    public synchronized static HeartBeatThread getInstance()
    {
        if (instance == null)
            instance = new HeartBeatThread();
        return instance;
    }

    private HeartBeatThread()
    {
        doneFlag = false;
        thread = new Thread()
        {
            @Override
            public void run()
            {
                process(this);
            }
        };
        thread.setName("Daemon HeartBeatThread thread");
        thread.setDaemon(false);
    }

    public void startup()
    {
        if (threadStartFlag)
            return;
        Log.i(LOGCAT,"HeartBeatThread thread.start();");
        thread.start();

    }

    @SuppressWarnings("deprecation")
    public void shutdown()
    {
        if (threadStartFlag)
            thread.stop();
        Log.i(LOGCAT,"HeartBeatThread thread.stop();");
        doneFlag = true;
    }

    public static final char SPLIT_CHAR = '|';

    public void process(Thread thisThread)
    {
        threadStartFlag = true;
        try
        {
            while (!doneFlag && (thread == thisThread))
            {
                try
                {
                    if(ApkUtils.isHome())
                    {
                        Log.e("smallstar", "is home!..................");
                        ApkUtils.startApk("org.mortbay.ijetty", ".IJetty");
                        IJetty.getInstance().mWebView.getSettings().setCacheMode( WebSettings.LOAD_CACHE_ELSE_NETWORK);
                        //mWebView.getSettings().setCacheMode( WebSettings.LOAD_NO_CACHE);
                        IJetty.getInstance().mWebView.clearHistory();
                        IJetty.getInstance().mWebView.clearFormData();
                        IJetty.getInstance().mWebView.clearCache(true);
                        IJetty.getInstance().mWebView.reload();
                    }
                    
                    //TODO:检查是否安装了软件管理工具，如果没有安装到指定地址下载安装
                    if(!ApkUtils.isBackgroundRunning(IJetty.getInstance(), "com.smallstar.androidlibapp"))
                    {
                        Log.e("smallstar", "com.smallstar.androidlibapp is not running.");
                        ApkUtils.startApk("com.smallstar.androidlibapp", ".BootStartActivity");
                    }

                    //检查是否需要升级console.war
                    //Log.w("===smalsltar===", AppConstants.getMediaSdFolder() + "/console.war");
                    File warFile = new File(AppConstants.getMediaSdFolder() + "/console.war");
                    if(warFile.exists())
                    {
                        //(1)copy file to tmp,(2)rename war file,(3)stopService and delay,(4)starService,(5)install war file
                        try
                        {
                            Log.w("====smallstar=====", "(1)copy file to tmp");
                            FileUtil.copyFile(warFile, new File(IJetty.__JETTY_DIR+"/"+IJetty.__TMP_DIR +"/"+"console.war"));
                            Log.w("====smallstar=====", "(2)rename war file");
                            warFile.renameTo(new File(AppConstants.getMediaSdFolder() + "/console_old.war"));
                        }
                        catch (IOException e)
                        {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        Log.w("====smallstar=====", "(3)stopService and delay");
                        IJetty.getInstance().stopService(new Intent(IJetty.getInstance(),IJettyService.class));
                        
                        warFile = new File(IJetty.__JETTY_DIR+"/"+IJetty.__TMP_DIR + "/" + "console.war");
                        if(warFile.exists()){
                            try
                            {
                                File webappDir = new File (IJetty.__JETTY_DIR+"/"+IJetty.__WEBAPP_DIR);
                                Log.w("====smallstar=====", webappDir.getPath());
                                String name = warFile.getName();
                                if (name.endsWith(".war") || name.endsWith(".jar"))
                                    name = name.substring(0, name.length()-4);
                                Log.w("====smallstar=====", "(5)install war file");
                                Installer.install(warFile, warFile.getPath(), webappDir, name, false);
                                warFile.delete();
                            }
                            catch (Exception e)
                            {
                                Log.e("Jetty", "Bad resource", e);
                            }
                        }
                        Log.w("====smallstar=====", "(4)starService");
                        Intent intent = new Intent(IJetty.getInstance(),IJettyService.class);
                        intent.putExtra(IJetty.getInstance().__PORT,IJetty.getInstance().__PORT_DEFAULT);
                        intent.putExtra(IJetty.getInstance().__NIO,IJetty.getInstance().__NIO_DEFAULT);
                        intent.putExtra(IJetty.getInstance().__SSL,IJetty.getInstance().__SSL_DEFAULT);
                        intent.putExtra(IJetty.getInstance().__CONSOLE_PWD,IJetty.getInstance().__CONSOLE_PWD_DEFAULT);
                        IJetty.getInstance().startService(intent);
                    }
                    
                    InterfaceOp.protoHeartbeat(new IRequestListener()
                    {
                        public void onError(Exception e)
                        {
                            LogUtil.log("heartbeat onError: " + e.getMessage());
                        }

                        public void onComplete(boolean isError, String errMsg, JSONObject respObj)
                        {
                            Log.e(LOGCAT, "heartbeat onComplete=>isError: " + isError + "  respObj:" + respObj);
                            if (isError || (respObj == null))
                            {
                                return;
                            }
                            

                            String organize_id = respObj.optString("organize_id","");
                            if (!TextUtils.isEmpty(organize_id))
                            {
                                organize_id = organize_id.trim();
                                //Log.w(TAG,"===>" + organize_id);
                            }
                            String group_id = respObj.optString("group_id","");
                            if (!TextUtils.isEmpty(group_id))
                            {
                                group_id = group_id.trim();
                                //Log.w(TAG,"===>" + group_id);
                                if (!group_id.equals(AppConstants.GROUP_ID))
                                {
                                    AppConstants.GROUP_ID = group_id;
                                }
                            }
                            
                            String imei = respObj.optString("imei","");
                            if (!TextUtils.isEmpty(imei))
                            {
                                imei = imei.trim();
                                //Log.w(TAG,"===>imei:" + imei);
                                //Log.w(TAG,"===>SDPATH:" + AppConstants.getMediaSdFolder());
                                //Log.w(TAG,"===>MODEL:" + Build.MODEL);
                                //Log.w(TAG,"--->" + AmlogicExt.getExternalStorage2Directory().getPath());

                            }
//                            String playURL = respObj.optString("link","");
//                            if (!TextUtils.isEmpty(playURL))
//                            {
//                                playURL = playURL.trim();
//                                Log.w(LOGCAT,"===>playURL:" + playURL);
//                                Log.w(LOGCAT,"===>CLIENT_CUR_PLAYURL:" + AppConstants.CLIENT_CUR_PLAYURL);
//                                if (!playURL.equals(AppConstants.CLIENT_CUR_PLAYURL))
//                                {
//                                    AppConstants.CLIENT_CUR_PLAYURL = playURL;
//                                    //保存URL到配置文件
//                                    File clientProps = new File(IJetty.__JETTY_DIR + "/" + IJetty.__ETC_DIR + "/properties.xml");
//                                    mPropertiesUtil.readPropertiesFileFromXML(clientProps.getAbsolutePath());
//                                    mPropertiesUtil.setPlayUrl(AppConstants.CLIENT_CUR_PLAYURL);
//                                    mPropertiesUtil.writePropertiesFileToXML(clientProps.getAbsolutePath());
//                                    IJetty.getInstance().mWebView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
//                                    IJetty.getInstance().mWebView.loadUrl(AppConstants.CLIENT_CUR_PLAYURL);
//                                }
//                            }
                            
                            String playListVersion = respObj.optString("version_program","");
                            if (!playListVersion.equals(PlayListUtil.playListVersion))
                            {
                                //Log.w(LOGCAT,"===>playListVersion:" + playListVersion);
                                //保存播单版本到配置文件
                                File clientProps = new File(IJetty.__JETTY_DIR + "/" + IJetty.__ETC_DIR + "/properties.xml");
                                mPropertiesUtil.readPropertiesFileFromXML(clientProps.getAbsolutePath());
                                mPropertiesUtil.setPlayListVersion(playListVersion);//保存节目单推送版本记录
                                mPropertiesUtil.writePropertiesFileToXML(clientProps.getAbsolutePath());
                                PlayListUtil.playListVersion = playListVersion;
                                PlayListUtil.isPlayListChanged = true;//转PlayList线程处理
                            }

                            String apkPushVersion = respObj.optString("version_apk","");
                            if(!apkPushVersion.equals(ApkUtils.apkPushVersion))
                            {
                                File clientProps = new File(IJetty.__JETTY_DIR + "/" + IJetty.__ETC_DIR + "/properties.xml");
                                mPropertiesUtil.readPropertiesFileFromXML(clientProps.getAbsolutePath());
                                mPropertiesUtil.setApkPushVersion(apkPushVersion);//保存APK推送版本记录
                                mPropertiesUtil.writePropertiesFileToXML(clientProps.getAbsolutePath());
                                ApkUtils.apkPushVersion = apkPushVersion;
                                ApkUtils.isApkChanged = true;//转apk线程处理
                            }
                            
                            //urlplaylist处理
                            if(!AppConstants.URLPLAYLIST.isEmpty())
                            {
                                Log.e(LOGCAT, "AppConstants.URLPLAYLIST:=" + AppConstants.URLPLAYLIST);
                                JSONArray jsonar=null;
                                String playUrl = "";
                                try {
                                    jsonar = new JSONArray(AppConstants.URLPLAYLIST);
                                    for(int i=0; i<jsonar.length(); i++)
                                    {
                                        JSONObject oj = jsonar.getJSONObject(i);
                                        Long tsStart = Long.parseLong(oj.getString("time_start"));
                                        Long tsEnd = Long.parseLong(oj.getString("time_end"));
                                        Long tsNow = System.currentTimeMillis()/1000;
                                        String ts = tsNow.toString();
                                        Log.v(LOGCAT, "=============================");
                                        Log.v(LOGCAT, DateUtils.getDateToString(tsStart) + "(Start)");
                                        Log.v(LOGCAT, DateUtils.getDateToString(tsNow) + "(Now)");
                                        Log.v(LOGCAT, DateUtils.getDateToString(tsEnd) + "(End)");
                                        Log.v(LOGCAT, "=============================");
                                        if(tsStart < tsNow && tsNow < tsEnd)
                                        {
                                            playUrl = oj.getString("url");
                                            //Log.v(LOGCAT,playUrl);
                                            break;
                                        }
                                    }
                                    Log.e(LOGCAT, "********************************************");
                                    Log.e(LOGCAT, "playUrl:" + playUrl);
                                    Log.e(LOGCAT, "");Log.e(LOGCAT, "AppConstants.CLIENT_CUR_PLAYURL:" + AppConstants.CLIENT_CUR_PLAYURL);
                                    Log.e(LOGCAT, "********************************************");
                                    if(!playUrl.isEmpty() && !playUrl.equals(AppConstants.CLIENT_CUR_PLAYURL))
                                    {
                                        IJetty.getInstance().mWebView.getSettings().setCacheMode( WebSettings.LOAD_CACHE_ELSE_NETWORK);
                                        //mWebView.getSettings().setCacheMode( WebSettings.LOAD_NO_CACHE);
                                        IJetty.getInstance().mWebView.clearHistory();
                                        IJetty.getInstance().mWebView.clearFormData();
                                        IJetty.getInstance().mWebView.clearCache(true);
                                        
                                        //IJetty.getInstance().mWebView.setIntegerProperty("loadUrlTimeoutValue", 60000);
                                        IJetty.getInstance().mWebView.loadUrl(playUrl);
                                        AppConstants.CLIENT_CUR_PLAYURL = playUrl;
                                    }
                                    if(playUrl.isEmpty() && !AppConstants.CLIENT_CUR_PLAYURL.equals(AppConstants.CLIENT_DEFAULT2_PLAYURL))
                                    {
                                        IJetty.getInstance().mWebView.getSettings().setCacheMode( WebSettings.LOAD_CACHE_ELSE_NETWORK);
                                        //mWebView.getSettings().setCacheMode( WebSettings.LOAD_NO_CACHE);
                                        IJetty.getInstance().mWebView.clearHistory();
                                        IJetty.getInstance().mWebView.clearFormData();
                                        IJetty.getInstance().mWebView.clearCache(true);
                                        IJetty.getInstance().mWebView.loadUrl(AppConstants.CLIENT_DEFAULT2_PLAYURL);
                                        AppConstants.CLIENT_CUR_PLAYURL = AppConstants.CLIENT_DEFAULT2_PLAYURL;
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            else
                            {
                                if(!AppConstants.CLIENT_CUR_PLAYURL.equals(AppConstants.CLIENT_DEFAULT2_PLAYURL))
                                {
                                    IJetty.getInstance().mWebView.getSettings().setCacheMode( WebSettings.LOAD_CACHE_ELSE_NETWORK);
                                    //mWebView.getSettings().setCacheMode( WebSettings.LOAD_NO_CACHE);
                                    IJetty.getInstance().mWebView.clearHistory();
                                    IJetty.getInstance().mWebView.clearFormData();
                                    IJetty.getInstance().mWebView.clearCache(true);
                                    IJetty.getInstance().mWebView.loadUrl(AppConstants.CLIENT_DEFAULT2_PLAYURL);                                    
                                    AppConstants.CLIENT_CUR_PLAYURL = AppConstants.CLIENT_DEFAULT2_PLAYURL;
                                }
                            }
                            
                            //refresh
                            boolean refreshStatus = respObj.optBoolean("refresh");//optString("refresh","");
                            //Log.w(LOGCAT,"refreshStatus:" + refreshStatus);
                            if(refreshStatus)
                            {
                                IJetty.getInstance().mWebView.getSettings().setCacheMode( WebSettings.LOAD_CACHE_ELSE_NETWORK);
                                //mWebView.getSettings().setCacheMode( WebSettings.LOAD_NO_CACHE);
                                IJetty.getInstance().mWebView.clearHistory();
                                IJetty.getInstance().mWebView.clearFormData();
                                IJetty.getInstance().mWebView.clearCache(true);
                                IJetty.getInstance().mWebView.reload();
                                //IJetty.getInstance().mWebView.loadUrl(AppConstants.CLIENT_CUR_PLAYURL);
                                InterfaceOp.protoRefreshConfirm(new IRequestListener()
                                {
                                    public void onError(Exception e)
                                    {
                                        LogUtil.log("protoRefreshConfirm onError: " + e.getMessage());
                                    }

                                    public void onComplete(boolean isError, String errMsg, JSONObject respObj)
                                    {
                                        //Log.e("smallstar", "heartbeat onComplete=>isError: " + isError + "  respObj:" + respObj);
                                        if (isError || (respObj == null))
                                        {
                                            return;
                                        }
                                        String result = respObj.optString("result","");
                                        if (!result.equals("true"))
                                        {
                                            Log.w("protoRefreshConfirm return",result);
                                            return;
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                catch (OutOfMemoryError e)
                {
                    e.printStackTrace();
                }
                finally
                {
                    SystemClock.sleep((AppConstants.HEARTBEAT_TIME - 3) * 1000);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            threadStartFlag = false;
        }
    }
}
