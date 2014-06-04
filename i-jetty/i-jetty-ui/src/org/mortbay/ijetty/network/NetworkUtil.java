package org.mortbay.ijetty.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.mortbay.ijetty.AppConstants;
import org.mortbay.ijetty.MainApplication;
import org.mortbay.ijetty.util.FileUtil;
import org.mortbay.ijetty.util.LogUtil;
import org.mortbay.ijetty.util.WeatherAndAddressUtil;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class NetworkUtil {
	private static HttpUriRequest getHttpConn(String url,
			Map<String, String> params) throws MalformedURLException,
			IOException {
		if (TextUtils.isEmpty(url)){Log.e("->NetworkUtil","url is null.");return null;}
		if (params != null) {
			Set<String> keys = params.keySet();
			String currElem = "";
			HttpPost post = new HttpPost(url);
			List<NameValuePair> ps = new ArrayList<NameValuePair>();
			for (Iterator<String> it = keys.iterator(); it.hasNext();) {
				currElem = (String) it.next();
				//LogUtil.log(currElem + "=" + params.get(currElem));
				ps.add(new BasicNameValuePair(currElem, params.get(currElem)));
//				ps.add(new BasicNameValuePair(currElem, URLEncoder.encode(params.get(currElem))));
			}
			post.setEntity(new UrlEncodedFormEntity(ps, HTTP.UTF_8));
			return post;
		} else {
			return new HttpGet(url);
		}
	}

	public static String getUserAgent() {
		String ua = "Mozilla/5.0 (Linux; U; Android 4.0.4; zh-cn; "
				+ InterfaceOp.getImei()
				+ "/"
				+ Build.MODEL
				+ " ) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Safari/534.30";
		return ua;
	}
	/**
	 * 同步请求
	 * 
	 * @param url
	 *            地址
	 * @param params
	 *            参数
	 * @param isGB2312
	 *            返回数据是否GB2312编码
	 * @return 服务器响应的字符串
	 * @throws MalformedURLException
	 * @throws IOException
	 * @throws Exception
	 */
	public static String readDataSync(String url, Map<String, String> params,
			boolean isGB2312) throws MalformedURLException, IOException,
			Exception {
		//Log.e("gary", "=========>url:" + url + "    params: " + params);
		HttpUriRequest request = getHttpConn(url, params);
		HttpClient client = new DefaultHttpClient();
		final HttpParams ps = client.getParams();
		HttpConnectionParams.setConnectionTimeout(ps,
				AppConstants.TIMEOUT_ESTABLISH_CONNECTION);
		HttpConnectionParams.setSoTimeout(ps, AppConstants.TIMEOUT_REQUEST);
		ConnManagerParams.setTimeout(ps, AppConstants.TIMEOUT_FETCH_CONNECTION);
		ps.setParameter(CoreProtocolPNames.USER_AGENT, getUserAgent());
		HttpResponse response = null;
		HttpEntity httpEntity = null;
		String respStr = null;
		try {
			response = client.execute(request);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				//Log.e("->NetworkUtil", "200 OK (HTTP/1.0 - RFC 1945)");
				httpEntity = response.getEntity();
				if (isGB2312)
					respStr = EntityUtils.toString(httpEntity, "GB2312");
				else
					respStr = EntityUtils.toString(httpEntity, "UTF-8");
				if (TextUtils.isEmpty(respStr)){Log.w("--->A", "A");return null;}
			}
			else if(response.getStatusLine().getStatusCode() == HttpStatus.SC_NOT_FOUND){
				Log.e("->NetworkUtil", "404 Not Found (HTTP/1.0 - RFC 1945)");
			}
			else {
				Log.e("->NetworkUtil", String.valueOf(HttpStatus.SC_NOT_FOUND));
			}
		} catch (UnknownHostException e) {
			throw e;
		} catch (MalformedURLException e) {
			throw e;
		} catch (NoHttpResponseException e){
			throw e;
		} catch (IOException e) {
			throw e;
		} catch (OutOfMemoryError e) {
			throw e;
		} catch (Exception e) {
			throw e;
		} finally {
			if (httpEntity != null){httpEntity.consumeContent();}
			if (client != null){client.getConnectionManager().shutdown();}
		}
		return respStr;
	}

	/**
	 * 异步请求
	 * 
	 * @param url
	 *            地址
	 * @param params
	 *            参数
	 * @param listener
	 *            请求回调
	 */
	public static void readDataASync(final String url,
			final Map<String, String> params, final boolean isGB2312,
			final IRequestListener listener) {

		final Runnable runnable = new Runnable() {
			public void run() {
				JSONObject jsonObj = null;
				try {
					String respStr = readDataSync(url, params, isGB2312);
					jsonObj = new JSONObject(respStr);
					Boolean successFlag = jsonObj.optBoolean("result", false);
					if (listener != null)
					{
						listener.onComplete(!successFlag,jsonObj.optString("errmsg", "Response contains no error message"),	jsonObj);
					}
				} catch(NoHttpResponseException e){ 
					Log.e("->NetworkUtil", "NoHttpResponseException");
					e.printStackTrace();
					if(listener != null){listener.onError(new IOException("NoHttpResponseException error"));}
				} catch (UnknownHostException e) {
					e.printStackTrace();
					if (listener != null)
						listener.onError(new IOException("UnkownHost error"));
				} catch (MalformedURLException e) {
					e.printStackTrace();
					if (listener != null)
						listener.onError(new IOException("URL error"));
				} catch (SocketTimeoutException e) {
					e.printStackTrace();
					if (listener != null)
						listener.onError(new IOException("network timeout"));
				} catch (IOException e) {
					e.printStackTrace();
					if (listener != null)
						listener.onError(new IOException("network error"));
					// if (InterfaceOp.URL_DEFAULT_DOMAIN
					// .equals(InterfaceOp.URL_DOMAIN)) {
					// listener.onError(new IOException("network error"));
					// } else {
					// NetworkUtil.setDefaultDomain();
					// }
				} catch (JSONException e) {
					e.printStackTrace();
					if (listener != null)
						listener.onError(new IOException("Response is not json string"));
				} catch (OutOfMemoryError e) {
					e.printStackTrace();
					if (listener != null)
						listener.onError(new IOException("Memorry overflow"));
					System.gc();
				} catch (Exception e) {
					e.printStackTrace();
					if (listener != null)
						listener.onError(new IOException("Unkown error"));
				}
			}
		};
		performOnBackgroundThread(runnable);
	}

	public static void performOnBackgroundThread(final Runnable runnable) {
		final Thread t = new Thread() {
			@Override
			public void run() {
				try {
					runnable.run();
				} catch (OutOfMemoryError err) {
					err.printStackTrace();
					System.gc();
				} finally {

				}
			}
		};
		t.setDaemon(true);
		t.start();
	}

	public static void setDefaultDomain() {
		InterfaceOp.URL_DOMAIN = InterfaceOp.URL_DEFAULT_DOMAIN;
	}

	public static void setDomainByProvince(String cityName) {
		int provinceId = WeatherAndAddressUtil
				.getProvinceIDByCityname(cityName);
		// Log.e("gary", "setDomainByProvince provinceId: " + provinceId);
		if (provinceId == 0) {
			InterfaceOp.URL_DOMAIN = InterfaceOp.URL_DEFAULT_DOMAIN;
			return;
		}
		InterfaceOp.URL_DOMAIN = AppConstants.URL_DOMAIN_PREFIX + provinceId
				+ "." + InterfaceOp.URL_DEFAULT_DOMAIN;
	}

	/**
	 * 发送下载完成广播
	 * 
	 * @param filename
	 *            下载完成文件名
	 */
	public static void sendDownloadCompleteBroadcase(String filename) {
		Log.e("smallstar", "sendDownloadCompleteBroadcase filename: " + filename);
		Intent intent = new Intent();
		intent.setAction(AppConstants.ACTION_DOWN_FINISH);
		intent.putExtra("filename", filename);
		MainApplication.getInstance().sendBroadcast(intent);

	}

	/**
	 * 广告数据发送间隔时间
	 * 
	 * @param interval
	 */
	public static void sendAdIntervalChangedBroadcast(String interval) {
		// Log.e("gary", "ad interval time interval: " + interval);
		Intent intent = new Intent();
		intent.setAction(AppConstants.ACTION_AD_INTERVAL);
		intent.putExtra("adInterval", interval);
		MainApplication.getInstance().sendBroadcast(intent);

	}

	public static void sendRequestDownloadHandler(Handler handler,
			String downloadUrl, String savedName) {
		if (handler == null)
			return;
		Message msg = handler.obtainMessage(AppConstants.MSG_REQUEST_DOWNLOAD);
		msg.getData().putString("downloadUrl", downloadUrl);
		msg.getData().putString("savedName", savedName);
		handler.sendMessage(msg);
	}
	
        /**
         * 异步请求下载
         * @param downloadUrl
         * @param saveUrl
         */
        public static void requestDownload(final String downloadUrl, final String saveUrl) {
                Runnable run = new Runnable() {

                        public void run() {
                                NetworkUtil.sendRequestDownloadHandler(
                                                MainApplication.getInstance().getAppHandler(),
                                                downloadUrl,
                                                saveUrl + "/" + FileUtil.getFileName(downloadUrl)
                                                                + AppConstants.DOWNLOADING_FILE_PREFFIX);
                        }
                };
                NetworkUtil.performOnBackgroundThread(run);
        }
        
	/**
	 * 异步请求下载
	 * @param downloadUrl
	 */
	public static void requestDownload(final String downloadUrl) {
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

	
	public static Bitmap getPicBitmap(String picUrl) throws IOException {
		if (TextUtils.isEmpty(picUrl))
			return null;

		HttpUriRequest request = getHttpConn(picUrl, null);
		HttpClient client = new DefaultHttpClient();
		HttpResponse response = null;
		HttpEntity httpEntity = null;

		InputStream is = null;
		Bitmap bmp = null;
		byte[] vBuff = null;
		ByteArrayOutputStream vBaos = null;
		try {
			response = client.execute(request);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				if (client != null)
					client.getConnectionManager().shutdown();
				return null;
			}
			httpEntity = response.getEntity();
			is = httpEntity.getContent();
			if (is == null) {
				if (httpEntity != null)
					httpEntity.consumeContent();
				if (client != null)
					client.getConnectionManager().shutdown();
				return null;
			}

			vBaos = new ByteArrayOutputStream();
			vBuff = new byte[1024];
			int vCount = -1;
			while (-1 != (vCount = is.read(vBuff, 0, vBuff.length))) {
				vBaos.write(vBuff, 0, vCount);
			}
			bmp = BitmapFactory.decodeByteArray(vBaos.toByteArray(), 0,
					vBaos.size());
		} catch (IOException e) {
			throw e;
		} catch (Exception e3) {
			e3.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} finally {
			if (is != null)
				is.close();
			if (httpEntity != null)
				httpEntity.consumeContent();
			if (client != null)
				client.getConnectionManager().shutdown();
			if (vBaos != null)
				vBaos.close();
			is = null;
			vBuff = null;
			System.gc();
		}
		return bmp;
	}
}
