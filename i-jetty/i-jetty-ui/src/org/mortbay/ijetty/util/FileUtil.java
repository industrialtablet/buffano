package org.mortbay.ijetty.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.mortbay.ijetty.AppConstants;
import org.mortbay.ijetty.network.InterfaceOp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class FileUtil {
	public static class SyncListFile {
		public List<File> delFiles;
		public List<File> addFiles;

		@Override
		public String toString() {
			return "SyncListFile [delFiles=" + delFiles + ", addFiles="
					+ addFiles + "]";
		}

		public List<File> getDelFiles() {
			return delFiles;
		}

		public void setDelFiles(List<File> delFiles) {
			this.delFiles = delFiles;
		}

		public List<File> getAddFiles() {
			return addFiles;
		}

		public void setAddFiles(List<File> addFiles) {
			this.addFiles = addFiles;
		}

	}

	public static String getFileName(String url) {
		return url.substring(url.lastIndexOf('/') + 1);
	}

	public static String getDownloadUrl(List<InterfaceOp.ADFile> ads,
			String filename) {
		if (TextUtils.isEmpty(filename))
			return null;
		for (InterfaceOp.ADFile ad : ads) {
			if (TextUtils.isEmpty(ad.filename))
				continue;
			String fn = FileUtil.getFileName(ad.url);
			if (fn.equals(filename))
				return ad.url.replaceAll("\\\\", "");
		}
		return null;
	}

	public static SyncListFile syncList(List<File> srcLst, List<File> dstLst) {
		List<File> delFiles = new LinkedList<File>();
		List<File> addFiles = new LinkedList<File>();
		SyncListFile syncLst = new SyncListFile();
		if (dstLst == null && srcLst == null) {
			return syncLst;
		}
		if (dstLst == null) {
			if (srcLst != null) {
				delFiles.addAll(srcLst);
				syncLst.setDelFiles(delFiles);
			}
			return syncLst;
		} else if (srcLst == null) {
			if (dstLst != null) {
				addFiles.addAll(dstLst);
				syncLst.setAddFiles(addFiles);
			}
			return syncLst;
		}
		delFiles.addAll(srcLst);
		addFiles.addAll(dstLst);
		for (File file : dstLst) {
			if (delFiles.contains(file))
				delFiles.remove(file);
		}
		for (File file : srcLst) {
			if (addFiles.contains(file))
				addFiles.remove(file);
		}
		syncLst.setAddFiles(addFiles);
		syncLst.setDelFiles(delFiles);
		return syncLst;
	}

	public static SyncListFile syncListADS(List<File> srcLst, Map<File, Long> dstLst) {
		List<File> delFiles = new LinkedList<File>();
		List<File> addFiles = new LinkedList<File>();
		SyncListFile syncLst = new SyncListFile();
		if (dstLst == null && srcLst == null) {
			return syncLst;
		}
		if (dstLst == null) {
			if (srcLst != null) {
				delFiles.addAll(srcLst);
				syncLst.setDelFiles(delFiles);
			}
			return syncLst;
		} else if (srcLst == null) {
			if (dstLst != null) {
				addFiles.addAll(dstLst.keySet());
				syncLst.setAddFiles(addFiles);
			}
			return syncLst;
		}
		delFiles.addAll(srcLst);
		addFiles.addAll(dstLst.keySet());
//		for (File file : dstLst) {
//			if (delFiles.contains(file))
//				delFiles.remove(file);
//		}
		
		Iterator<Entry<File, Long>> iter = dstLst.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<File, Long> entry = (Entry<File, Long>) iter.next(); 
			File file = entry.getKey();
			long filesize = entry.getValue();
			
			if (delFiles.contains(file) && file.length() == filesize)
				delFiles.remove(file);
		}
		
		for (File file : srcLst) {
			if (addFiles.contains(file)) {
				long filesize = file.length();
				long adsize = dstLst.get(file);
				if (filesize == adsize)
					addFiles.remove(file);
			}
		}
		syncLst.setAddFiles(addFiles);
		syncLst.setDelFiles(delFiles);
		return syncLst;
	}
	
	public static List<File> getDstList(List<InterfaceOp.ADFile> ads) {
		List<File> dstFiles = new LinkedList<File>();
		for (InterfaceOp.ADFile ad : ads) {
			if (TextUtils.isEmpty(ad.filename))
				continue;
			if (ad.delFlag)
				continue;
			if (TextUtils.isEmpty(ad.url))
				continue;
			dstFiles.add(new File(AppConstants.getMediaSdFolder() + "/"
					+ FileUtil.getFileName(ad.url)));
		}
		if (dstFiles.size() < 1)
			return null;
		return dstFiles;
	}

	public static Map<File, Long> getDstListADS(List<InterfaceOp.ADFile> ads) {
		Map<File, Long> dstFiles = new LinkedHashMap<File, Long>();
		for (InterfaceOp.ADFile ad : ads) {
			if (TextUtils.isEmpty(ad.filename))
				continue;
			if (ad.delFlag)
				continue;
			if (TextUtils.isEmpty(ad.url))
				continue;
			dstFiles.put(
					new File(AppConstants.getMediaSdFolder() + "/"
							+ FileUtil.getFileName(ad.url)), ad.filesize);
		}
		if (dstFiles.size() < 1)
			return null;
		return dstFiles;
	}
	
	public static List<File> getSrcList() {
		File mediaFolder = new File(AppConstants.getMediaSdFolder());
		if (!mediaFolder.exists())
			mediaFolder.mkdirs();
		File[] srcFileArrs = mediaFolder.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if (file == null)
					return false;
				if (file.isDirectory())
					return false;
				String path = file.getAbsolutePath().toLowerCase();
				if (path.endsWith(".mp4") || path.endsWith(".rmvb")
				                || path.endsWith(".mpg") || path.endsWith(".vob")
						|| path.endsWith(".3gp") || path.endsWith(".avi")
						|| path.endsWith(".rm") || path.endsWith(".mov")
						|| path.endsWith(".flv") || path.endsWith(".mkv"))
					return true;
				return false;
			}
		});
		if(srcFileArrs == null) return null;
		List<File> srcLst = Arrays.asList(srcFileArrs);
		if (srcLst == null || srcLst.size() < 1)
			return null;
		return srcLst;
	}
	
	public static List<String> getSrcFileNameList(){
	    List<File> localFiles = FileUtil.getSrcList();
	    List<String> filesNameList = new ArrayList<String>();
	    for(final File f : localFiles)
	    {
	        filesNameList.add(f.getName());
	    }
	    return filesNameList;
	}

	public static File renameTmp(String tmpFilename) {
		if (!tmpFilename.endsWith(".tmp"))
			return null;
		File file = new File(tmpFilename);
		if (!file.exists())
			return null;
		if (!file.isFile())
			return null;
		int endIndex = tmpFilename.lastIndexOf(".tmp");
		File newFile = new File(tmpFilename.substring(0, endIndex));
		if (newFile.exists()) {
			newFile.delete();
		}
		if (file.renameTo(newFile))
			return newFile;
		return null;
	}

	public static boolean putObject(String path, Object object) {
		FileOutputStream fos = null;
		ObjectOutputStream oos = null;
		File f = new File(path);
		try {
			fos = new FileOutputStream(f);
			oos = new ObjectOutputStream(fos);
			oos.writeObject(object); // 括号内参数为要保存java对象
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (oos != null)
					oos.close();
				if (fos != null)
					fos.close();
			} catch (Exception e) {
				f.delete();
				e.printStackTrace();
			}
		}
	}

	public static Object getObject(String path) {
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		File f = new File(path);
		try {
			fis = new FileInputStream(f);
			ois = new ObjectInputStream(fis);
			return ois.readObject();// 强制类型转换

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (ois != null)
					ois.close();
				if (fis != null)
					fis.close();
			} catch (Exception e) {
				f.delete();
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static boolean checkSDMounted() {
		String cmd = "df";
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
				sb.append(strLine + "\r\n");
			}
			String content = sb.toString();
//			Log.e("gary", "content: :" + content);
			return content.contains("external_");
		} catch (IOException e) {
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
		return false;
	}

	
//	public static boolean hasSdcard() {
//		StatFs stat = new StatFs(AppConstants.MEDIA_SD_FOLDER);
//		long bytesAvailable = (long) stat.getBlockSize()
//				* (long) stat.getBlockCount();
//		if(bytesAvailable > 3000000000L)
//			return true;
//		return false;
//	}
	
	
	public static void getSdFolder(Context pContext) {
		PackageManager vPM = pContext.getPackageManager();
		try {
			PackageInfo vPackageInfo = vPM.getPackageInfo(
					"com.mylayout.app.media", 0);
			if (vPackageInfo != null && vPackageInfo.versionCode > 19) {
				File vDelFile = new File(Environment
						.getExternalStorageDirectory().getAbsolutePath()
						+ "/"
						+ AppConstants.MEDIA_FOLDER);
				File[] vFiles = vDelFile.listFiles();
				if(vFiles != null && vFiles.length > 0) {
					for(File vFile : vDelFile.listFiles()) {
						vFile.delete();
					}
				}
				vDelFile.delete();
			}

		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// 复制文件   
	public static void copyFile(File sourceFile,File targetFile)   
	throws IOException{  
	        // 新建文件输入流并对它进行缓冲   
	        FileInputStream input = new FileInputStream(sourceFile);  
	        BufferedInputStream inBuff=new BufferedInputStream(input);  
	  
	        // 新建文件输出流并对它进行缓冲   
	        FileOutputStream output = new FileOutputStream(targetFile);  
	        BufferedOutputStream outBuff=new BufferedOutputStream(output);  
	          
	        // 缓冲数组   
	        byte[] b = new byte[1024 * 5];  
	        int len;  
	        while ((len =inBuff.read(b)) != -1) {  
	            outBuff.write(b, 0, len);  
	        }  
	        // 刷新此缓冲的输出流   
	        outBuff.flush();  
	          
	        //关闭流   
	        inBuff.close();  
	        outBuff.close();  
	        output.close();  
	        input.close();  
	    }
}
