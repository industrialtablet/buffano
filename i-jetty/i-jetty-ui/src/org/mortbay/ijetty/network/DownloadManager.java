package org.mortbay.ijetty.network;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.mortbay.ijetty.AppConstants;
import org.mortbay.ijetty.MainApplication;
import org.mortbay.ijetty.R;
import org.mortbay.ijetty.util.FileUtil;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

public class DownloadManager {
	private static DownloadManager INSTANCE = new DownloadManager();
	private Map<String, String> mDownload;

	private DownloadManager() {
		mDownload = new HashMap<String, String>();
	}

	public static DownloadManager getInstance() {
		return INSTANCE;
	}

	private static int mThreadCount = 0;

	public static int getDownloadThreadCount() {
		return mThreadCount;
	}
	
	private void submitDownloadInfo(String pUrl, String pSavePath , boolean state) {
		File vFile = new File(pSavePath);
		if(!vFile.exists()) {
			vFile = new File(pSavePath.replace(".tmp", ""));
		}
		InterfaceOp.protoSubmmitDownloadInfo(pUrl, vFile.length()+"" , state ? "1" : "0");
	}

	class DownloadTask extends AsyncTask<String, String, Boolean> {
		String mKey;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			mThreadCount++;
			super.onPreExecute();
			// Toast.makeText(MainApplication.getInstance(), "开始下载",
			// Toast.LENGTH_SHORT).show();
		}

		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			mKey = params[0];
			boolean vResult = download(params[0], params[1]);
			submitDownloadInfo(params[0], params[1], vResult);   //提交下载信息
			return vResult;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			mThreadCount--;
			super.onPostExecute(result);
			String vHint = "";
			if (result) {
				vHint = "下载完成";
			} else {
				vHint = "下载失败,或文件已存在！";
			}
			// Toast.makeText(MainApplication.getInstance(), vHint,
			// Toast.LENGTH_SHORT).show();
			mDownload.remove(mKey);
		}

	}

	public void startDownload(String pUrl, String pSavePath) {
		// Log.e("gary", "Downloading filename: "+pSavePath);
		if (AppConstants.IS_MEDIA_MOUNTED || FileUtil.checkSDMounted()) {
				if (mDownload.containsKey(pUrl)) {
					// Toast.makeText(MainApplication.getInstance(), "正在下载中",
					// Toast.LENGTH_SHORT).show();
				} else {
					mDownload.put(pUrl, pSavePath);
					new DownloadTask().execute(pUrl, pSavePath);
				}
				return;
		}
			Toast.makeText(MainApplication.getInstance(), R.string.input_sd, Toast.LENGTH_LONG)
				.show();			
			File vDelFile = new File(AppConstants.getMediaSdFolder());
			File[] vFiles = vDelFile.listFiles();
			if(vFiles != null && vFiles.length > 0) {
				for(File vFile : vDelFile.listFiles()) {
					vFile.delete();
				}
			}
			vDelFile.delete();
	}

	private void recurseMkDirs(File file) {
		if (file.getParentFile().exists()) {
			file.mkdir();
		} else {
			recurseMkDirs(file.getParentFile());
			file.mkdir();
		}
	}

	public boolean download(String pUrl, String pSavePath) {
		HttpURLConnection vConn = null;
		byte[] vBuffer = new byte[2048];
		InputStream vIs = null;
		RandomAccessFile vRaf = null;
		try {
			long vCurrPos = 0;
			long vTotalSize = 0;
			vConn = getHttpURLConnection(pUrl, "GET");

			File vSaveFile = new File(pSavePath);

			// 循环创建父目录
			File vParentDir = vSaveFile.getParentFile();
			if (!vParentDir.exists()) {
				recurseMkDirs(vParentDir);
			}
			if (vSaveFile.exists()) {
				vCurrPos = vSaveFile.length();
				vConn.setRequestProperty("Range", "bytes=" + vCurrPos + "-");
				// if(vConn.getContentLength() == vCurrPos) return false;
				Log.e("gary",
						"vConn.getContentLength(): " + vConn.getContentLength());
				if (vConn.getContentLength() == -1) {
//					long filesize = getFileSize(pUrl);
//					if (filesize == -1)
//						return false;
//					if (filesize == vCurrPos) {
						NetworkUtil.sendDownloadCompleteBroadcase(pSavePath);
						return true;
//					} else{
//						vSaveFile.delete();
//						return false;
//					}
				}
			}
			if (vConn.getResponseCode() == HttpURLConnection.HTTP_OK
					|| vConn.getResponseCode() == HttpURLConnection.HTTP_PARTIAL) {

				vTotalSize = vConn.getContentLength() + vCurrPos;

				int vCount;
				int vCurrProgress = 0;
				int vLastProgress = 0;
				vIs = vConn.getInputStream();

				vRaf = new RandomAccessFile(pSavePath, "rw");
				vRaf.seek(vCurrPos);

				long vLastTime = 0, vCurTime = 0;

				while (-1 != (vCount = vIs.read(vBuffer, 0, vBuffer.length))) {
					vRaf.write(vBuffer, 0, vCount);
					vCurrPos += vCount;
					if ((vCurTime = SystemClock.uptimeMillis()) - vLastTime > 1500) {
						// if((vCurTime = System.currentTimeMillis())-vLastTime
						// > 1500){
						vCurrProgress = (int) (vCurrPos * 100 / vTotalSize);
						if (vCurrProgress > vLastProgress) {
							vLastTime = vCurTime;
							vLastProgress = vCurrProgress;
							// LogUtil.log("vCurrProgress:" + vCurrProgress);
						}
					}
				}
				NetworkUtil.sendDownloadCompleteBroadcase(pSavePath);
				return true;
			}

		} catch (java.net.UnknownHostException e) {
			e.printStackTrace();
			return false;
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			if (vRaf != null) {
				try {
					vRaf.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (vIs != null)
				try {
					vIs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (vConn != null) {
				vConn.disconnect();
			}
			vBuffer = null;

			System.gc();
		}
		return false;
	}

	private HttpURLConnection getHttpURLConnection(String pUrl, String pMethod)
			throws IOException {
		HttpURLConnection vConn = null;

		URL vUrl = new URL(pUrl);
		vConn = (HttpURLConnection) vUrl.openConnection();
		// httpURLConnection.setRequestProperty("X-Online-Host", "*/*");
		vConn.setInstanceFollowRedirects(true);
		vConn.setConnectTimeout(150 * 1000);
		vConn.setReadTimeout(5 * 1000); // 读取流超时时间
		vConn.setRequestMethod(pMethod);
		vConn.setRequestProperty("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		vConn.setRequestProperty("Connection", "Keep-Alive");
		return vConn;
	}

	public static String getFileName(String url) {
		return url.substring(url.lastIndexOf('/') + 1);
	}

	public long getFileSize(String urlStr) {
		URL url = null;
		long filesize = -1L;
		HttpURLConnection conn = null;
		try {
			url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(AppConstants.TIMEOUT_FETCH_CONNECTION);
			conn.setRequestMethod("GET");
			conn.setRequestProperty(
					"Accept",
					"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
			conn.setRequestProperty("Accept-Language", "zh-CN");
			conn.setRequestProperty("Referer", urlStr);
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.connect();
			if (conn.getResponseCode() == 200) {
				filesize = conn.getContentLength();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			System.gc();
		} finally {
			if (conn != null)
				conn.disconnect();
		}
		return filesize;
	}
}
