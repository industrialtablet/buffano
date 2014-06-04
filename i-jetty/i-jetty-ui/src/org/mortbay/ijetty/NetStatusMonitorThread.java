package org.mortbay.ijetty;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;
import org.mortbay.ijetty.util.PropertiesUtils;

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
import android.util.Config;
import android.util.Log;
import android.webkit.WebSettings;
import android.os.Bundle;

public class NetStatusMonitorThread {
	private final static String TAG = "->NetStatusMonitorThread";
	
	private Thread thread;
	private boolean doneFlag = false;
	private static NetStatusMonitorThread instance;
	private static boolean threadStartFlag = false;
	private PropertiesUtils mPropertiesUtil = new PropertiesUtils();;

	String mActivityName = null;

	public synchronized static NetStatusMonitorThread getInstance() {
		if (instance == null)
			instance = new NetStatusMonitorThread();
		return instance;
	}

	private NetStatusMonitorThread() {
		doneFlag = false;
		thread = new Thread() {
			@Override
			public void run() {
				process(this);
			}
		};
		thread.setName("Daemon HeartBeatThread thread");
		thread.setDaemon(false);
	}

	public void startup() {
		if (threadStartFlag)
			return;
		Log.i(TAG,"HeartBeatThread thread.start();");
		thread.start();

	}

	public void shutdown() {
	        if (threadStartFlag)
	            thread.stop();
	        Log.i(TAG,"HeartBeatThread thread.stop();");
	        doneFlag = true;
	}

	public static final char SPLIT_CHAR = '|';

	public void process(Thread thisThread) {
		threadStartFlag = true;
		try {
			while (!doneFlag && (thread == thisThread)) {
				try {
					//判断网络是否已经连接可用
					if(isNetworkAvailable(MainApplication.getInstance()))
					{						
						AppConstants.NETWORK_STATUS = true;
						if(isOnline()){
						    AppConstants.ONLINE_STATUS = true;
						}
						else
						{
						    AppConstants.ONLINE_STATUS = false;
						}
						//IJetty.getInstance().mWebView.loadUrl(AppConstants.CLIENT_CUR_PLAYURL);
						if(AppConstants.NETWORK_TYPE.equals("ethernet"))
						{
							//获取以太网网络连接信息
							AppConstants.ETH_STATUS = 1;
							AppConstants.ETH_IP = getLocalIpAddress();
							AppConstants.ETH_MAC = getLocalMacAddressFromIp(MainApplication.getInstance());//getMacFromFile(MainApplication.getInstance());
							//Log.w("==>", AppConstants.ETH_IP);
							//Log.w("==>", AppConstants.ETH_MAC);
						}
						else if(AppConstants.NETWORK_TYPE.equals("WIFI"))
						{
							//获取WIFI网络信息
							WifiManager wifiManager = (WifiManager)MainApplication.getInstance().getSystemService(Context.WIFI_SERVICE);
							WifiInfo info = (null == wifiManager ? null : wifiManager.getConnectionInfo());
							if(wifiManager != null){
								AppConstants.WIFI_STATUS = wifiManager.getWifiState();
							}
							if (null != info) {
								AppConstants.WIFI_MAC = info.getMacAddress();
								AppConstants.WIFI_IP = intToIp(info.getIpAddress());
							}
						}
						else {
							Log.e("NetStatusMonitorThread", "unkown network type!");
						}
					}
					else {
						AppConstants.NETWORK_STATUS = false;
                                                File clientProps = new File(IJetty.__JETTY_DIR+"/"+IJetty.__ETC_DIR+"/properties.xml");
                                                if(clientProps.exists())
                                                    mPropertiesUtil.readPropertiesFileFromXML(clientProps.getAbsolutePath());
					}
				} catch (Exception e) {
					e.printStackTrace();
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
				}
				finally {
					SystemClock.sleep((AppConstants.NETSTATUSMONITOR - 3) * 1000);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			threadStartFlag = false;
		}
	}
	
	private String intToIp(int paramInt) {  
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."  
                + (0xFF & paramInt >> 24);  
    }
	
/*
	//获取本地IP
    public static String getLocalIpAddress() {  
           try {  
               for (Enumeration<NetworkInterface> en = NetworkInterface  
                               .getNetworkInterfaces(); en.hasMoreElements();) {  
                           NetworkInterface intf = en.nextElement();  
                          for (Enumeration<InetAddress> enumIpAddr = intf  
                                   .getInetAddresses(); enumIpAddr.hasMoreElements();) {  
                               InetAddress inetAddress = enumIpAddr.nextElement();  
                               if (!inetAddress.isLoopbackAddress()) {  
                               return inetAddress.getHostAddress().toString();  
                               }  
                          }  
                       }  
                   } catch (SocketException ex) {  
                       Log.e("WifiPreference IpAddress", ex.toString());  
                   }  
           
                return null;  
   }
*/
	public static String getLocalIpAddress() 
	{  
        try {  
            for (Enumeration<NetworkInterface> en = NetworkInterface  
                            .getNetworkInterfaces(); en.hasMoreElements();) {  
                        NetworkInterface intf = en.nextElement();  
                       for (Enumeration<InetAddress> enumIpAddr = intf  
                                .getInetAddresses(); enumIpAddr.hasMoreElements();) {  
                            InetAddress inetAddress = enumIpAddr.nextElement();  
                            if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {  
                            return inetAddress.getHostAddress().toString();  
                            }  
                       }  
                    }  
                } catch (SocketException ex) {  
                    Log.e("WifiPreference IpAddress", ex.toString());  
                }  
             return null;  
	}
	
	//根据IP获取本地Mac
    public static String getLocalMacAddressFromIp(Context context) {
        String mac_s= "";
       try {
            byte[] mac;
            NetworkInterface ne=NetworkInterface.getByInetAddress(InetAddress.getByName(getLocalIpAddress()));
            mac = ne.getHardwareAddress();
            mac_s = byte2hex(mac);
       } catch (Exception e) {
           e.printStackTrace();
       }
       
        return mac_s;
    }
    
    public static  String byte2hex(byte[] b) {
        StringBuffer hs = new StringBuffer(b.length);
        String stmp = "";
        int len = b.length;
        for (int n = 0; n < len; n++) {
         stmp = Integer.toHexString(b[n] & 0xFF);
         if (stmp.length() == 1)
          hs = hs.append(":").append("0").append(stmp);
         else {
          hs = hs.append(":").append(stmp);
         }
        }
        String retStr = String.valueOf(hs);
        retStr = retStr.substring(1, retStr.length());
        return retStr;
       }
    
    
    /** 
     * 网络是否可用 
     *  
     * @param activity 
     * @return 
     */  
    public static boolean isNetworkAvailable(Context context) {  
        ConnectivityManager connectivity = (ConnectivityManager) context  
                .getSystemService(Context.CONNECTIVITY_SERVICE);  
        if (connectivity == null) {  
        } else {  
            NetworkInfo[] info = connectivity.getAllNetworkInfo();  
            if (info != null) {  
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                    	AppConstants.NETWORK_TYPE = info[i].getTypeName();
                    	//Log.w("NetStatusMonitorThread->1", info[i].getTypeName());
                    	//Log.w("NetStatusMonitorThread->2", info[i].getSubtypeName());
                        return true;
                    }  
                }  
            }  
        }  
        return false;  
    }
    
    /**  
     * get the Mac Address from the file /proc/net/arp
     * @param context
     * @attention the file /proc/net/arp need exit 
	 * @return Mac Address
     */
    private String getMacFromFile(Context context){
    	String mIP = AppConstants.ETH_IP;//Config.getIpAddress(context);
    	Log.w("---》", mIP);
    	if(mIP == null || mIP.length()<=0)
    		return null;
//    	
//    	String mIP = "192.168.1.1";
        List<String> mResult = readFileLines("/proc/net/arp");
        
        Log.d(TAG,"=======  /proc/net/arp  =========");
        for(int i =0;i<mResult.size();++i)
        	Log.d("line",mResult.get(i));
        Log.d(TAG,"===========================");
        
        
        if(mResult !=null && mResult.size()>1){        	
        	for(int j =1;j<mResult.size();++j){
        		List<String> mList = new ArrayList<String>();
        		String[] mType = mResult.get(j).split(" ");
        		for(int i =0;i<mType.length;++i){
                	if(mType[i]!=null && mType[i].length()>0)
                		mList.add(mType[i]);
                }

        		if(mList!=null && mList.size()>4 && mList.get(0).equalsIgnoreCase(mIP)){
	                    String result="";
	                    String[] tmp = mList.get(3).split(":");
	            		for(int i = 0;i<tmp.length;++i){
	            			result +=tmp[i];
	            		}
	            		result = result.toUpperCase();
	            		Log.i(TAG,"Mac address(file): "+result);
            		return result;
        		}
        	}
        }
    	return null;
    }
    
    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    private static List<String> readFileLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        String tempString ="";
        List<String> mResult = new ArrayList<String>();
        try {
        	Log.i("result","以行为单位读取文件内容，一次读一整行：");
            reader = new BufferedReader(new FileReader(file));
            while((tempString = reader.readLine())!=null){
            	mResult.add(tempString);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        
        return mResult;
    }
    
    private boolean isOnline(){
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = null;
        try
        {
            response = httpclient.execute(new HttpGet("http://www.google.com"));
        }
        catch (ClientProtocolException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        StatusLine statusLine = response.getStatusLine();
        if(statusLine.getStatusCode() == HttpStatus.SC_OK){
            //网络连接正常
            return true;
              
        } else{
            return false;
                //关闭网络
        }

    }
}
