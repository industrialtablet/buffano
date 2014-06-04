package org.mortbay.ijetty.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.ijetty.AppConstants;
import org.mortbay.ijetty.MainApplication;
import org.mortbay.ijetty.util.AppInfo;
import org.mortbay.ijetty.util.FileUtil;
import org.mortbay.ijetty.util.LogUtil;
import org.mortbay.ijetty.util.PackageUtil;
import org.mortbay.ijetty.util.PlayListUtil;
import org.mortbay.ijetty.util.SNUtil;
import org.mortbay.ijetty.util.StringUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

public class InterfaceOp {
	private static Long iProtoHeartbeatTimes = 0L;
	/**
	 * 一级域名
	 */
	public static final String URL_DEFAULT_DOMAIN = "118.123.17.98:50905";//"www.solscloud.com";
	//"118.123.17.98:50905";
	//www.solscloud.com
	//"s-90722.gotocdn.com";
	//"s-63003.gotocdn.com";
	public static final String URL_DEFAULT_DOMAIN_TEST = "xiaofengyunji.solledlight.com";
	public static String URL_DOMAIN = URL_DEFAULT_DOMAIN;
	public static final String URL_ROOT_DIRECTORY = "/api";//"/newsystem/solledlight";
	public static final String PROTO_APP_VER = "1";
	public static final String PROTO_SUBMMIT_INFO = "2";
	public static final String PROTO_AD = "3";
	public static final String PROTO_MESSAGE = "4";
	public static final String PROTO_HEARTBEAT = "5";
	public static final String PROTO_ADDRESS = "6";
	public static final String PROTO_APPS_LOCATION = "7";
	public static final String PROTO_SUBMIT_DOWNLOAD_INFO = "8";
	public static final String PROTO_SUBMIT_APPS_LIST = "10";
	public static final String PROTO_FETCH_SN = "11";
	public static final String PROTO_PLAYLISTGET = "12";
	public static final String PROTO_PLAYLISTCONFIRM = "13";
	public static final String PROTO_APKSGET = "14";
	public static final String PROTO_APKSCONFIRM = "15";
	public static final String PROTO_REFRESHCONFIRM = "16";
	
	public static final String APP_ID_SETTING = "1";
	public static final String APP_ID_LUNCHER = "2";
	public static final String APP_ID_MEDIAPLAYER = "3";

	/*
	 * 协议1 获得版本信息
	 */
	public static void protoAppVer(String appid, String appver,
			IRequestListener listener) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("proto", PROTO_APP_VER);
		params.put("appid", appid);
		params.put("ver", appver);
		String URL_APP_VER = "http://" + URL_DOMAIN + URL_ROOT_DIRECTORY
				+ "/appver.php";
		NetworkUtil.readDataASync(URL_APP_VER, params, false, listener);
	}

	/*
	 * 返回需要更新的URL，否则为null
	 */
	public static String processAppVer(JSONObject jsonObj) {
		if (jsonObj == null)
			return null;
		Boolean updateFlag = jsonObj.optBoolean("updateFlag", false);
		if (!updateFlag)
			return null;
		String appDownloadUrl = jsonObj.optString("downloadUrl", null);
		if (appDownloadUrl == null || !appDownloadUrl.startsWith("http://"))
			return null;
		return appDownloadUrl;
	}

	/*
	 * 协议2 提交客户端信息
	 */
	public static void protoSubmmitInfo(IRequestListener listener) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("proto", PROTO_SUBMMIT_INFO);
		params.put("account", "");
		params.put("password", "");
		params.put("phonenum", "");
		params.put("sn", "");
		params.put("code", "");
		String URL_SUBMMIT_INFO = "http://" + URL_DOMAIN + URL_ROOT_DIRECTORY
				+ "/submmitinfo.php";
		NetworkUtil.readDataASync(URL_SUBMMIT_INFO, params, false, listener);
	}

	/*
         * 协议X 获取播放清单信息，协议值待定,暂时定12
         */
	public static void protoPlayListGet(IRequestListener listener){
	           Map<String, String> params = new LinkedHashMap<String, String>();
	                params.put("proto", PROTO_PLAYLISTGET);
	                params.put("organize_id", AppConstants.ORGANIZE_ID);
	                params.put("imei", getImei());
	                params.put("sn", SNUtil.readSN());
	                if(AppConstants.NETWORK_TYPE.equals("ethernet"))
	                {
	                        params.put("mac_ether", AppConstants.ETH_MAC);
	                        params.put("mac_wifi", "");
	                }
	                else if(AppConstants.NETWORK_TYPE.equals("WIFI"))
	                {
	                        params.put("mac_ether", "");
	                        params.put("mac_wifi", AppConstants.WIFI_MAC);
	                }
	                else
	                {
	                        Log.e("InterfaceOp", "unkown network type!");
	                }
	                String URL_PLAYLIST = "http://" + URL_DOMAIN + URL_ROOT_DIRECTORY
	                                + "/playlists/read";
	                NetworkUtil.readDataASync(URL_PLAYLIST, params, false, listener);
	}
	
	 /*
         * 协议X 获取播放清单信息确认，协议值待定,暂时定13
         */
        public static void protoPlayListConfirm(IRequestListener listener){
                   Map<String, String> params = new LinkedHashMap<String, String>();
                        params.put("proto", PROTO_PLAYLISTCONFIRM);
                        params.put("organize_id", AppConstants.ORGANIZE_ID);
                        params.put("imei", getImei());
                        params.put("sn", SNUtil.readSN());
                        if(AppConstants.NETWORK_TYPE.equals("ethernet"))
                        {
                                params.put("mac_ether", AppConstants.ETH_MAC);
                                params.put("mac_wifi", "");
                        }
                        else if(AppConstants.NETWORK_TYPE.equals("WIFI"))
                        {
                                params.put("mac_ether", "");
                                params.put("mac_wifi", AppConstants.WIFI_MAC);
                        }
                        else
                        {
                                Log.e("InterfaceOp", "unkown network type!");
                        }
                        String URL_PLAYLIST = "http://" + URL_DOMAIN + URL_ROOT_DIRECTORY
                                        + "/playlists/complete";
                        NetworkUtil.readDataASync(URL_PLAYLIST, params, false, listener);
        }
        
        /*
         * 协议X 获取应用程序清单信息，协议值待定,暂时定14
         */
        public static void protoApksGet(IRequestListener listener){
            Map<String, String> params = new LinkedHashMap<String, String>();
                 params.put("proto", PROTO_APKSGET);
                 params.put("organize_id", AppConstants.ORGANIZE_ID);
                 params.put("imei", getImei());
                 params.put("sn", SNUtil.readSN());
                 if(AppConstants.NETWORK_TYPE.equals("ethernet"))
                 {
                         params.put("mac_ether", AppConstants.ETH_MAC);
                         params.put("mac_wifi", "");
                 }
                 else if(AppConstants.NETWORK_TYPE.equals("WIFI"))
                 {
                         params.put("mac_ether", "");
                         params.put("mac_wifi", AppConstants.WIFI_MAC);
                 }
                 else
                 {
                         Log.e("InterfaceOp", "unkown network type!");
                 }
                 String URL_PLAYLIST = "http://" + URL_DOMAIN + URL_ROOT_DIRECTORY
                                 + "/apk/read";
                 NetworkUtil.readDataASync(URL_PLAYLIST, params, false, listener);
        }        
        
        /*
         * 协议X 获取应用程序清单信息确认，协议值待定,暂时定15
         */        
        public static void protoApksConfirm(IRequestListener listener){
            Map<String, String> params = new LinkedHashMap<String, String>();
                 params.put("proto", PROTO_APKSCONFIRM);
                 params.put("organize_id", AppConstants.ORGANIZE_ID);
                 params.put("imei", getImei());
                 params.put("sn", SNUtil.readSN());
                 if(AppConstants.NETWORK_TYPE.equals("ethernet"))
                 {
                         params.put("mac_ether", AppConstants.ETH_MAC);
                         params.put("mac_wifi", "");
                 }
                 else if(AppConstants.NETWORK_TYPE.equals("WIFI"))
                 {
                         params.put("mac_ether", "");
                         params.put("mac_wifi", AppConstants.WIFI_MAC);
                 }
                 else
                 {
                         Log.e("InterfaceOp", "unkown network type!");
                 }
                 String URL_PLAYLIST = "http://" + URL_DOMAIN + URL_ROOT_DIRECTORY
                                 + "/apk/complete";
                 NetworkUtil.readDataASync(URL_PLAYLIST, params, false, listener);
        }   

        /*
         * 协议X 获取应用程序清单信息确认，协议值待定,暂时定16
         */        
        public static void protoRefreshConfirm(IRequestListener listener){
            Map<String, String> params = new LinkedHashMap<String, String>();
                 params.put("proto", PROTO_REFRESHCONFIRM);
                 params.put("organize_id", AppConstants.ORGANIZE_ID);
                 params.put("imei", getImei());
                 params.put("sn", SNUtil.readSN());
                 if(AppConstants.NETWORK_TYPE.equals("ethernet"))
                 {
                         params.put("mac_ether", AppConstants.ETH_MAC);
                         params.put("mac_wifi", "");
                 }
                 else if(AppConstants.NETWORK_TYPE.equals("WIFI"))
                 {
                         params.put("mac_ether", "");
                         params.put("mac_wifi", AppConstants.WIFI_MAC);
                 }
                 else
                 {
                         Log.e("InterfaceOp", "unkown network type!");
                 }
                 String URL_PLAYLIST = "http://" + URL_DOMAIN + URL_ROOT_DIRECTORY
                                 + "/refresh";
                 NetworkUtil.readDataASync(URL_PLAYLIST, params, false, listener);
        }   

        
	/*
	 * 协议3 获得广告信息
	 */
	public static class ADFile {
		public int id;
		public String filename;
		public String url;
		public long filesize;
		public long addtime;
		/**
		 * 该文件是否允许显示 delFlag = false 没有删除 delFlag = true 已经删除，客房端不需要显示
		 */
		public boolean delFlag;
	}

	public static void protoAds(IRequestListener listener) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("proto", PROTO_AD);
		String URL_AD = "http://" + URL_DOMAIN + URL_ROOT_DIRECTORY
				+ "/fetchadinfo.php";
		NetworkUtil.readDataASync(URL_AD, params, false, listener);
	}

	/*
	 * 返回需要更新的URL，否则为null
	 */
	public static List<ADFile> processAd(JSONObject jsonObj) {
		if (jsonObj == null)
			return null;
		JSONArray jsonArr = jsonObj.optJSONArray("adlist");
		if (jsonArr == null)
			return null;
		List<ADFile> ads = new LinkedList<ADFile>();
		JSONObject obj = null;
		for (int i = 0; i < jsonArr.length(); i++) {
			try {
				obj = jsonArr.getJSONObject(i);
			} catch (JSONException e) {
				e.printStackTrace();
				continue;
			}
			if (obj == null)
				continue;
			ADFile ad = new ADFile();
			ad.id = obj.optInt("id", 0);
			ad.filename = obj.optString("filename");
			ad.url = obj.optString("url");
			ad.filesize = obj.optInt("filesize", 0);
			ad.addtime = obj.optInt("addtime", 0);
			ad.delFlag = "Y".equalsIgnoreCase(obj.optString("del", "N"));
			ads.add(ad);
		}
		if (ads.size() < 1)
			return null;
		return ads;
	}

	public static class MainMessage {
		public int msgId;
		public String msgStr;

		public MainMessage(int msgId, String msgStr) {
			super();
			this.msgId = msgId;
			this.msgStr = msgStr;
		}

		@Override
		public String toString() {
			return "MainMessage [msgId=" + msgId + ", msgStr=" + msgStr + "]";
		}

	}

	public static MainMessage readMsg() {
		SharedPreferences sp = MainApplication.getInstance()
				.getSharedPreferences(AppConstants.CONFIG_FILENAME,
						Context.MODE_WORLD_READABLE);
		int msgId = sp.getInt("msgId", 1);
		String msgStr = sp.getString("msgStr", "");
		return new MainMessage(msgId, msgStr);

	}

	private static void writeMsg(MainMessage mainMessage) {
		SharedPreferences sp = MainApplication.getInstance()
				.getSharedPreferences(AppConstants.CONFIG_FILENAME,
						Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor ed = sp.edit();
		ed.putInt("msgId", mainMessage.msgId);
		ed.putString("msgStr", mainMessage.msgStr);
		ed.commit();

	}

	/*
	 * 协议4 获得版本信息
	 */
	public static void protoMessage(IRequestListener listener) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("proto", PROTO_MESSAGE);
		MainMessage mainMessage = readMsg();
		params.put("msgId", mainMessage.msgId + "");
		String URL_APP_VER = "http://" + URL_DOMAIN + URL_ROOT_DIRECTORY
				+ "/fetchmsg.php";
		NetworkUtil.readDataASync(URL_APP_VER, params, false, listener);
	}

	/*
	 * 返回需要更新的URL，否则为null
	 */
	public static String processMessage(JSONObject jsonObj) {
		if (jsonObj == null)
			return null;
		Boolean updateFlag = jsonObj.optBoolean("updateFlag", false);
		if (!updateFlag)
			return readMsg().msgStr;
		JSONObject msgJson = jsonObj.optJSONObject("msgslist");
		if (msgJson == null)
			return null;

		int msgId = 1;
		String tmp = null;
		tmp = msgJson.optString("id", "1");
		try {
			msgId = Integer.parseInt(tmp);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String message = msgJson.optString("messages", null);
		if (TextUtils.isEmpty(message))
			return null;
		tmp = decodeUnicode(message).toString();
		writeMsg(new MainMessage(msgId, tmp));
		return tmp;
	}

	public static StringBuffer decodeUnicode(final String dataStr) {
		final StringBuffer buffer = new StringBuffer();
		String tempStr = "";
		String operStr = dataStr;
		if (operStr != null && operStr.indexOf("\\u") == -1)
			return buffer.append(operStr);
		if (operStr != null && !operStr.equals("")
				&& !operStr.startsWith("\\u")) {
			tempStr = operStr.substring(0, operStr.indexOf("\\u"));
			operStr = operStr.substring(operStr.indexOf("\\u"),
					operStr.length());// operStr字符一定是以unicode编码字符打头的字符串
		}
		buffer.append(tempStr);
		// 循环处理,处理对象一定是以unicode编码字符打头的字符串
		while (operStr != null && !operStr.equals("")
				&& operStr.startsWith("\\u")) {
			tempStr = operStr.substring(0, 6);
			operStr = operStr.substring(6, operStr.length());
			String charStr = "";
			charStr = tempStr.substring(2, tempStr.length());
			char letter = (char) Integer.parseInt(charStr, 16); // 16进制parse整形字符串。
			buffer.append(new Character(letter).toString());
			if (operStr.indexOf("\\u") == -1) {
				buffer.append(operStr);
			} else { // 处理operStr使其打头字符为unicode字符
				tempStr = operStr.substring(0, operStr.indexOf("\\u"));
				operStr = operStr.substring(operStr.indexOf("\\u"),
						operStr.length());
				buffer.append(tempStr);
			}
		}
		return buffer;
	}

	public static void checkAppUpdate(String appid, String appver) {
		InterfaceOp.protoAppVer(appid, appver, new IRequestListener() {

			public void onError(Exception e) {
				LogUtil.log("checkAPPUpdate onError   " + e.getMessage());
			}

			public void onComplete(boolean isError, String errMsg,
					JSONObject respObj) {
				LogUtil.log("checkAPPUpdate onComplete =====>isError: "
						+ isError + "  respObj:" + respObj);
				if (isError)
					return;
				if (respObj == null)
					return;
				AppConstants.HEARTBEAT_TIME = respObj.optInt("heartBeat",
						AppConstants.HEARTBEAT_TIME_DEFAULT);
				AppConstants.AD_INTERVAL_TIME = respObj.optInt("adInterval",
						AppConstants.AD_INTERVAL_TIME_DEFAULT);
				if (AppConstants.AD_INTERVAL_TIME != AppConstants.AD_INTERVAL_TIME_DEFAULT)
					NetworkUtil
							.sendAdIntervalChangedBroadcast(AppConstants.AD_INTERVAL_TIME
									+ "");
				boolean updateFlag = respObj.optBoolean("updateFlag", false);
				if (!updateFlag)
					return;
				String url = respObj.optString("downloadUrl", null);
				if (TextUtils.isEmpty(url))
					return;
				final String downloadUrl = url.replaceAll("\\\\", "");
				// Log.e("gary", "downloadUrl: " + downloadUrl);
				File savedFile = new File(AppConstants.getMediaSdFolder() + "/"
						+ FileUtil.getFileName(downloadUrl));
				if (savedFile.exists() && savedFile.isFile()) {
					NetworkUtil.sendDownloadCompleteBroadcase(savedFile
							.getAbsolutePath());
					return;
				}
				Runnable run = new Runnable() {

					public void run() {
						NetworkUtil.sendRequestDownloadHandler(
								MainApplication.getInstance().getAppHandler(),
								downloadUrl,
								AppConstants.getMediaSdFolder() + "/"
										+ FileUtil.getFileName(downloadUrl)
										+ AppConstants.DOWNLOADING_FILE_PREFFIX);
					}
				};
				NetworkUtil.performOnBackgroundThread(run);
			}
		});
	}

	/*
	 * 协议5 发送心跳信息
	 */
	private static String imei;

	private static String readImei() {
		SharedPreferences sp = MainApplication.getInstance()
				.getSharedPreferences(AppConstants.CONFIG_FILENAME,
						Context.MODE_WORLD_READABLE);
		String imei = sp.getString("imei", "");
		return imei;

	}

	private static void writeImei(String imei) {
		SharedPreferences sp = MainApplication.getInstance()
				.getSharedPreferences(AppConstants.CONFIG_FILENAME,
						Context.MODE_WORLD_WRITEABLE);
		SharedPreferences.Editor ed = sp.edit();
		ed.putString("imei", imei);
		ed.commit();

	}

	public static String getImei() {
		if (!TextUtils.isEmpty(imei))
			return StringUtils.replaceBlank(imei);
		TelephonyManager telephonyManager = (TelephonyManager) MainApplication
				.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
		imei = telephonyManager.getDeviceId();
		if (TextUtils.isEmpty(imei) || imei.equals("null")) {
			imei = readImei();
			if (TextUtils.isEmpty(imei)) {
				imei = "IMEI_"
						+ UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
				writeImei(imei);
				Log.e("gary", "imei: " + imei);
			}
		}
		return StringUtils.replaceBlank(imei);
	}

	public static String toUnicode(String s) {
		if (TextUtils.isEmpty(s))
			return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) <= 256) {
				sb.append("\\u00");
			} else {
				sb.append("\\u");
			}
			sb.append(Integer.toHexString(s.charAt(i)));
		}
		return sb.toString();
	}

	private static String getInstalledAppInfo() {
		List<AppInfo> apps = PackageUtil.getInstalledAppsInfo();
		StringBuilder sb = new StringBuilder();
		sb.append("\"");
		for (AppInfo app : apps) {
			if (app == null)
				continue;
			if (TextUtils.isEmpty(app.mPackageName))
				continue;
			if (TextUtils.isEmpty(app.mAppName))
				continue;
			// if (app.mPackageName.equals(MainApplication.getInstance()
			// .getPackageName()))
			// continue;
			if (app.mPackageName.contains("&")
					|| app.mPackageName.contains(",")
					|| app.mPackageName.contains("?"))
				continue;
			// sb.append(app.mPackageName).append(":").append(1)
			// .append(",");
			sb.append(toUnicode(app.mPackageName)).append(":")
					.append(toUnicode(app.mAppName)).append(":")
					.append(toUnicode(app.mVersionName)).append(":")
					.append(toUnicode(app.mVersionCode)).append(",");
		}
		String infoStr = sb.toString();
		infoStr = infoStr.subSequence(0, infoStr.length() - 1).toString();
		infoStr = infoStr + "\"";
		return infoStr;
	}

	public static void protoHeartbeat(IRequestListener listener) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("proto", PROTO_HEARTBEAT);
		params.put("organize_id", AppConstants.ORGANIZE_ID);
		params.put("group_id", AppConstants.GROUP_ID);
		params.put("imei", getImei());
		params.put("sn", SNUtil.readSN());
		if(AppConstants.NETWORK_TYPE.equals("ethernet"))
		{
			params.put("mac_ether", AppConstants.ETH_MAC);
			params.put("mac_wifi", "");
		}
		else if(AppConstants.NETWORK_TYPE.equals("WIFI"))
		{
			params.put("mac_ether", "");
			params.put("mac_wifi", AppConstants.WIFI_MAC);
		}
		else
		{
			Log.e("InterfaceOp", "unkown network type!");
		}
		String DownloadStatus = "true";
		if(PlayListUtil.isPlayListFileSynced) DownloadStatus = "false";
		params.put("is_download",DownloadStatus);
		String URL_APP_VER = "http://" + URL_DOMAIN + URL_ROOT_DIRECTORY
				+ "/heartbeat";
		NetworkUtil.readDataASync(URL_APP_VER, params, false, listener);
	}
	/*
	public static void protoHeartbeat(String status, String pActivationState, IRequestListener listener) {
		Log.v("-------->",SNUtil.readSN());
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("proto", PROTO_HEARTBEAT);
		params.put("imei", getImei());
		params.put("sn", SNUtil.readSN());
		params.put("timestamp", System.currentTimeMillis() + "");
		params.put("status", status);
		params.put("activationstate", pActivationState);
//		params.put("appinfo", getInstalledAppInfo());  
		String URL_APP_VER = "http://" + URL_DOMAIN + URL_ROOT_DIRECTORY
				+ "/heartbeat.php";
		NetworkUtil.readDataASync(URL_APP_VER, params, false, listener);
	}
*/

/*
	public static void protoHeartbeat(IRequestListener listener) {
		Log.v("-------->",SNUtil.readSN());
		Map<String, String> params = new LinkedHashMap<String, String>();
		iProtoHeartbeatTimes++;
		Log.v("-------->", "iProtoHeartbeatTimes=" + String.valueOf(iProtoHeartbeatTimes));
		
		String mac_ether = "";
		if(iProtoHeartbeatTimes < 16)
		{mac_ether = "7E-E9-D3-F7-49-" + "0" +Long.toHexString(iProtoHeartbeatTimes);}
		else if(iProtoHeartbeatTimes < 256)
		{mac_ether = "7E-E9-D3-F7-49-" + Long.toHexString(iProtoHeartbeatTimes);}
		else if(iProtoHeartbeatTimes < 4096)
		{
			mac_ether = "7E-E9-D3-F7-0" + 
					Long.toHexString(iProtoHeartbeatTimes).subSequence(0, 1) + "-" +
					Long.toHexString(iProtoHeartbeatTimes).subSequence(1, 3);}
		else {mac_ether = "7E-E9-D3-F7-49-78";}

		String mac_wifi = "";
		if(iProtoHeartbeatTimes < 16)
		{mac_wifi = "Y9-K0-V4-80-49-" + "0" +Long.toHexString(iProtoHeartbeatTimes);}
		else if(iProtoHeartbeatTimes < 256)
		{mac_wifi = "Y9-K0-V4-80-49-" + Long.toHexString(iProtoHeartbeatTimes);}
		else if (iProtoHeartbeatTimes < 4096) {
			mac_wifi = "Y9-K0-V4-80-0" + 
					Long.toHexString(iProtoHeartbeatTimes).subSequence(0, 1) + "-" +
					Long.toHexString(iProtoHeartbeatTimes).subSequence(1, 3);
		}
		else {mac_wifi = "Y9-K0-V4-80-49-C8";}
		
		Log.v("-------->",Long.toHexString(iProtoHeartbeatTimes));
		
		params.put("proto", PROTO_HEARTBEAT);
		params.put("organize_id", "1");
		params.put("imei", "IMEI_"
				+ UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
		params.put("sn", "SN_"
				+ UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
		params.put("mac_ether", mac_ether);
		params.put("mac_wifi", mac_wifi);
		params.put("is_download","true");
		//params.put("timestamp", System.currentTimeMillis() + "");
		String URL_APP_VER = "http://" + URL_DOMAIN + URL_ROOT_DIRECTORY
				+ "/heartbeat";
		NetworkUtil.readDataASync(URL_APP_VER, params, false, listener);
	}
*/
	/*
	 * 协议6 提交客户端地址信息
	 */

	public static void protoSubmmitAddr(String address, IRequestListener listener) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("proto", PROTO_ADDRESS);
		params.put("imei", getImei());
		params.put("addr", "\"" + toUnicode(address) + "\"");
		String URL_SUBMMIT_INFO = "http://" + URL_DOMAIN + URL_ROOT_DIRECTORY
				+ "/submmitAddr.php";
		NetworkUtil.readDataASync(URL_SUBMMIT_INFO, params, false, listener);
	}
	
	/*
	 * 协议7Luncher  显示应用位置整理
	 */

	public static void protoAppsLocation(IRequestListener listener) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("proto", PROTO_APPS_LOCATION);
		String URL_SUBMMIT_INFO = "http://" + URL_DOMAIN + URL_ROOT_DIRECTORY
				+ "/appsneaten.php";
		NetworkUtil.readDataASync(URL_SUBMMIT_INFO, params, false, listener);
	}
	
	/*
	 * 协议8 提交下载信息
	 */

	public static void protoSubmmitDownloadInfo(String url, String size, String state) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("proto", PROTO_SUBMIT_DOWNLOAD_INFO);
		params.put("imei", getImei());
		params.put("url", url);
		params.put("size", size);
		params.put("state", state);
		String URL_SUBMMIT_INFO = "http://" + URL_DOMAIN + URL_ROOT_DIRECTORY
				+ "/submmitdownloadinfo.php";
		NetworkUtil.readDataASync(URL_SUBMMIT_INFO, params, false, null);
	}
	
	/*
	 * 协议10 提交应用列表
	 */

	public static void protoSubmmitAppsList(IRequestListener listener) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("proto", PROTO_SUBMIT_APPS_LIST);
		params.put("imei", getImei());
		params.put("appinfo", getInstalledAppInfo());
		String URL_SUBMMIT_INFO = "http://" + URL_DOMAIN + URL_ROOT_DIRECTORY
				+ "/submitappslist.php";
		NetworkUtil.readDataASync(URL_SUBMMIT_INFO, params, false, listener);
	}
	/*
	 * 协议11 提交应用列表
	 */
	public static final String ETHERNET_INTERFACE_NAME = "eth0";
	public static final String WIFI_INTERFACE_NAME = "wlan0";
	public static final String IFCONFIG_CMD = "busybox ifconfig -a";
	public static final String MAC_FILTER = "HWaddr";
	public static final String RETURN_SPLIT = ";";

	private static String getMac(String interfaceName) {
		String content = execCmd(IFCONFIG_CMD);
		if (TextUtils.isEmpty(content))
			return null;
		if (!content.contains(MAC_FILTER))
			return null;
		String[] lines = content.split(RETURN_SPLIT);
		try {
			if (lines == null || lines.length < 1)
				return null;
			for (String line : lines) {
				if (!line.contains(MAC_FILTER))
					continue;
				if (!line.contains(interfaceName))
					continue;
				String mac = line.substring(
						line.indexOf(MAC_FILTER) + MAC_FILTER.length()).trim();
				return mac.replaceAll(":", "-");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String getWifiMac() {
		return getMac(WIFI_INTERFACE_NAME);
	}

	public static String getEthernetMac() {
		return getMac(ETHERNET_INTERFACE_NAME);
	}

	public static String execCmd(String cmd) {
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
				sb.append(strLine + RETURN_SPLIT);
			}
			return sb.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
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
		return null;
	}

	public static String getModel() {
		return Build.MODEL;
	}

	private static String getVer() {
		return Build.DISPLAY;
	}
	public static String getSN() {
		return SNUtil.readSN();
	}
	public static void protoFetchSN(IRequestListener listener) {
		Map<String, String> params = new LinkedHashMap<String, String>();
		params.put("proto", PROTO_FETCH_SN);  
		params.put("model", getModel());
		params.put("ver", getVer());
		params.put("sn", getSN());
		params.put("etherMac", getEthernetMac());
		params.put("wifiMac", getWifiMac());
		params.put("imei", getImei());
		String URL_SUBMMIT_INFO = "http://" + URL_DOMAIN + URL_ROOT_DIRECTORY
				+ "/fetchSN.php";
//		LogUtil.logToDefaultFile("fetch sn params: "+params.toString()); 
		NetworkUtil.readDataASync(URL_SUBMMIT_INFO, params, false, listener);
	}
}
