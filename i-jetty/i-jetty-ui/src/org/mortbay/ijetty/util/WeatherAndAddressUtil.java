package org.mortbay.ijetty.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.mortbay.ijetty.AppConstants;
import org.mortbay.ijetty.MainApplication;
import org.mortbay.ijetty.component.AddressInfo;
import org.mortbay.ijetty.component.Province;
import org.mortbay.ijetty.component.Province.City;
import org.mortbay.ijetty.component.WeatherInfo;
import org.mortbay.ijetty.network.NetworkUtil;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

public class WeatherAndAddressUtil {
	/**
	 * 根据网络获得当前所在省市
	 * 
	 * @param handler
	 */
	public static void initAddressInfo(final Handler handler) {
		final String queryUrl = "http://iframe.ip138.com/ic.asp";

		Runnable run = new Runnable() {

			public void run() {
				String ip = null;
				String proName = null;
				String cityName = null;
				String tmp = null;
				String sb = null;
				try {
					sb = NetworkUtil.readDataSync(queryUrl, null, true);

					// Log.e("gary", "initAddressInfo string: " +
					// sb.toString());
					// 获得IP
					int start = sb.indexOf("[");
					int end = sb.indexOf("]", start + 1);
					ip = sb.substring(start + 1, end);
					AddressInfo.getInstance().setIp(ip);
					// 获得省市
					start = sb.indexOf("来自：");
					end = sb.indexOf("市", start + 1);
					tmp = sb.substring(start + 3, end + 1);
					// 获得省
					end = tmp.indexOf("省");
					
					//普通省份
					if (end > 0) {
						proName = tmp.substring(0, end);
						// 获得市
						start = tmp.indexOf("省");
						end = tmp.indexOf("市", start + 1);
						cityName = tmp.substring(start + 1, end);
					}else{
						// 自治区省份
						end = tmp.indexOf("自治区");
						if(end > 0){
							proName = tmp.substring(0, end);
							// 获得市
							start = tmp.indexOf("自治区");
							end = tmp.indexOf("市", start + 3);
							cityName = tmp.substring(start + 3, end);
						}else{
							// 直辖市
							end = tmp.indexOf("市");
							cityName = tmp.substring(0, end);
						}
					}
					if(!TextUtils.isEmpty(proName))
						proName = proName.trim();
					if(!TextUtils.isEmpty(cityName))
						cityName = cityName.trim();
					AddressInfo.getInstance().setProName(proName);
					AddressInfo.getInstance().setCityName(cityName);
					WeatherInfo.getInstance().initWeatherInfo(
							AddressInfo.getInstance().getCityName(), handler);
				} catch (Exception e) {
					e.printStackTrace();
					Message message = new Message();
					message.what = AppConstants.MSG_SHOW_WEATHER;
					handler.sendMessage(message);

				}
			}
		};
		Thread t = new Thread(run);
		t.setDaemon(true);
		t.start();
	}

	/**
	 * 初始化城市列表信息
	 * 
	 * @param filename
	 * @return
	 */
	public static void initCitysInfo() {
		List<City> citys = null;
		Province pro = null;
		List<Province> pros = new LinkedList<Province>();
		BufferedReader br = null;
		String line = null;
		InputStream is = null;
		try {
			is = MainApplication.getInstance().getAssets()
					.open(AppConstants.PROVINCES_FILE_NAME);
			br = new BufferedReader(new InputStreamReader(is, "GB2312"));
			while ((line = br.readLine()) != null) {
				String[] datas = line.split("=");
				if (datas == null || datas.length != 2)
					continue;
				// Log.e("gary","datas[0].trim()   "+datas[0].trim());
				if (datas[0].trim().startsWith("[")
						&& datas[0].trim().endsWith("]")) {
					if (pro != null && citys != null) {
						pro.setCitys(citys);
						pros.add(pro);
					}
					pro = new Province();
					String proName = datas[0].trim();
					proName = proName.substring(1, proName.length() - 1);
					pro.setProName(proName);
					pro.setId(Integer.parseInt(datas[1].trim()));
					citys = new LinkedList<City>();
					continue;
				}
				City city = new City();
				city.setName(datas[1].trim());
				city.setId(Long.parseLong(datas[0].trim()));
				citys.add(city);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		AppConstants.PROVINCES_INFO = pros;
	}

	/**
	 * 通过城市名获得城市ID(获得天气预报)
	 * 
	 * @param pros
	 *            所有省份信息
	 * @param cityname
	 *            城市名
	 * @return 城市ID
	 */
	public static long getCityIDByCityname(String cityname) {
		long vId = 0L;
		if (TextUtils.isEmpty(cityname))
			return vId;
		List<Province> pros = AppConstants.PROVINCES_INFO;
		if (pros == null || pros.size() < 1)
			return vId;
		for (Province pro : pros) {
			for (City city : pro.getCitys()) {
				if (city.getName().equals(cityname))
					return city.getId();
			}
		}

		return vId;
	}

	/**
	 * 通过城市名获得省份ID(标识不同域名)
	 * 
	 * @param pros
	 *            所有省份信息
	 * @param cityname
	 *            城市名
	 * @return 省份ID
	 */
	public static int getProvinceIDByCityname(String cityname) {
		int vId = 0;
		if (TextUtils.isEmpty(cityname))
			return vId;
		List<Province> pros = AppConstants.PROVINCES_INFO;
		if (pros == null || pros.size() < 1)
			return vId;
		for (Province pro : pros) {
			for (City city : pro.getCitys()) {
				if (city.getName().equals(cityname))
					return pro.getId();
			}
		}

		return vId;
	}

}
