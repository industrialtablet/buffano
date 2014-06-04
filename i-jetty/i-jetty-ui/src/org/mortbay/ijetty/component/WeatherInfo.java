package org.mortbay.ijetty.component;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONObject;
import org.mortbay.ijetty.AppConstants;
import org.mortbay.ijetty.network.NetworkUtil;
import org.mortbay.ijetty.util.WeatherAndAddressUtil;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class WeatherInfo {
	private static WeatherInfo instance;
	private String weatherMsg;

	public synchronized static WeatherInfo getInstance() {
		if (instance == null)
			instance = new WeatherInfo();
		return instance;
	}

	public String getWeatherMsg() {
		return weatherMsg;
	}

	public void setWeatherMsg(String weatherMsg) {
		this.weatherMsg = weatherMsg;
	}

	public static Bitmap getWeatherBitmap(String url) {
		Bitmap bm = null;// 生成了一张bmp图像
		try {
			URL iconurl = new URL("http://m.weather.com.cn/img/b" + url
					+ ".gif");
			//Log.e("gary", "getWeatherBitmap iconurl: " + iconurl);
			URLConnection conn = iconurl.openConnection();
			conn.connect();
			// 获得图像的字符流
			InputStream is = conn.getInputStream();
			BufferedInputStream bis = new BufferedInputStream(is, 8192);
			bm = BitmapFactory.decodeStream(bis);
			bis.close();
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bm;
	}

	public void initWeatherInfo(String cityName, final Handler handler) {
		try {
			//Log.e("gary", "initWeatherInfo2 cityName: " + cityName);
			long cityId = WeatherAndAddressUtil.getCityIDByCityname(cityName);
			if(cityId == 0)
				return;
			final String serverURL = "http://m.weather.com.cn/data/" + cityId
					+ ".html";
			Runnable run = new Runnable() {

				public void run() {
					try {
						String result = NetworkUtil.readDataSync(serverURL,
								null, true);
						//Log.e("gary", "WeatherInfo: " + result);
						JSONObject allWeatherData = new JSONObject(result);
						JSONObject weatherData = allWeatherData
								.getJSONObject("weatherinfo");
						StringBuffer info = new StringBuffer();
						// info.append("地点:" + weatherData.getString("city"));
						// info.append(" 时间:" + weatherData.getString("date_y")
						// + "     " + weatherData.getString("week"));
						info.append(weatherData.getString("weather1"));
						info.append("  " + weatherData.getString("temp1"));
						String imgUrl1 = weatherData.getString("img1");
						String imgUrl2 = weatherData.getString("img2");
//						info.append("  " + weatherData.getString("wind1")
//								+ "  " + weatherData.getString("fl1"));
						weatherMsg = info.toString().replaceAll("\n", "").replaceAll("\r", "").trim();
						Message message = new Message();
						message.what = AppConstants.MSG_SHOW_WEATHER;
						message.getData().putString("weatherMsg", weatherMsg);
						message.getData().putString("imgUrl1", imgUrl1);
						message.getData().putString("imgUrl2", imgUrl2);
						handler.sendMessage(message);
//						Log.e("gary", "today weather: " + weatherMsg);
//						Log.e("gary", "today img1: " + imgUrl1);
//						Log.e("gary", "today img2: " + imgUrl2);
						// image1.setImageBitmap(getBitmap(weatherPicUrl1));
					} catch (Exception e) {
						Message message = new Message();
						message.what = AppConstants.MSG_SHOW_WEATHER;
						handler.sendMessage(message);
						
						e.printStackTrace();
					}

				}
			};
			new Thread(run).start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
