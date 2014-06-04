package org.mortbay.ijetty.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import android.text.TextUtils;

public class SNUtil {
	private static final String SETUP_FILE_NAME = "/mnt/sdcard/Media/setup.txt";

	//TODO:如果文件不存在返回特定SN......
	public static String readSN() {
		String SN = null;
		String data = readLine();
		if (TextUtils.isEmpty(data))
			return null;
		String spStr[] = data.split(":::");
		if (spStr.length > 3)
			SN = spStr[2];

		return SN;
	}

	public static boolean writeSN(String sn) {
		String line = readLine();
		List<String> lst = new ArrayList<String>();
		if (TextUtils.isEmpty(line)) {
			lst.add("zq");
			lst.add("http://ey.ezagoo.com");
			lst.add(sn);
			lst.add("BT26KA2E2G2");
			lst.add("");
		} else {
			String infos[] = line.split(":::");
			for (int i = 0; i < infos.length; i++) {
				if (infos[i] == null) {
					lst.add("");
					continue;
				}
				lst.add(infos[i]);
			}
			if (lst.size() > 5)
				lst = lst.subList(0, 5);
			else if (lst.size() < 5)
				lst.add("");
			lst.set(2, sn);
		}
		String str = "";
		for (int i = 0; i < 5; i++) {
			str += lst.get(i) + ":::";
		}
		String s = str.substring(0, str.length() - 3);
		writeLine(s);
		line = readLine();
		return line.contains(sn);
	}

	private static String readLine() {
		String data = null;
		StringBuffer buffer = new StringBuffer();
		Reader in = null;
		FileInputStream fis = null;
		InputStreamReader isr = null;
		try {
			File f = new File(SETUP_FILE_NAME);
			if(!f.getParentFile().exists())
				f.getParentFile().mkdirs();
			if(!f.exists())
				return "null-sn-no-file";//文件不存在返回特殊SN
			fis = new FileInputStream(SETUP_FILE_NAME);
			isr = new InputStreamReader(fis, "GB2312");
			in = new BufferedReader(isr);
			int ch;
			while ((ch = in.read()) > -1) {
				buffer.append((char) ch);
			}

			data = buffer.toString();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
				if (isr != null)
					isr.close();
				if (fis != null)
					fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return data;
	}

	private static void writeLine(String line) {
		File file = new File(SETUP_FILE_NAME);
		BufferedWriter out = null;
		try {
			
			if (file.exists())
				file.delete();
			out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file, false)));
			out.write(line);
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static boolean isSingleCore() {
		String mode = android.os.Build.MODEL;
		return mode.startsWith("f04ref_BYW_ZH");
	}
}
