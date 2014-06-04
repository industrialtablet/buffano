package org.mortbay.ijetty.component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.mortbay.ijetty.component.Province.City;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Handler;
import android.util.Log;

public class AddressInfo {
	private String ip;
	private String proName;
	private String cityName;
	private static AddressInfo instance;

	public synchronized static AddressInfo getInstance() {
		if (instance == null)
			instance = new AddressInfo("127.0.0.1", null, null);
		return instance;
	}

	@Override
	public String toString() {
		return "AddressInfo [ip=" + ip + ", proName=" + proName + ", cityName="
				+ cityName + "]";
	}

	public AddressInfo(String ip, String proName, String cityName) {
		super();
		this.ip = ip;
		this.proName = proName;
		this.cityName = cityName;
	}

	public String getCityName() {
		return cityName;
	}

	public void setCityName(String cityName) {
		this.cityName = cityName;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getProName() {
		return proName;
	}

	public void setProName(String proName) {
		this.proName = proName;
	}

	

	
	
}
