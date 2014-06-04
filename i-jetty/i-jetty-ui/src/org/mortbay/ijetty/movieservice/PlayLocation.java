package org.mortbay.ijetty.movieservice;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import org.mortbay.ijetty.AppConstants;

public class PlayLocation implements Serializable {
	private static final long serialVersionUID = 1L;
	private String filename;
	private int pos;
	private static PlayLocation instance;
	public static final String FILE_PLAY_LOCATION = AppConstants.getMediaSdFolder()+"/PlayLocation.obj";

	private PlayLocation() {
		filename = AppConstants.getMediaSdFolder()+"/invalidFilename";
		pos = 0;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public int getPos() {
		return pos;
	}

	public void setPos(int pos) {
		this.pos = pos;
	}

	public static synchronized PlayLocation getInstance() {
		if (instance == null)
			instance = new PlayLocation();
		return instance;
	}

	public synchronized static void saveLocation() {
		ObjectOutputStream out = null;
		FileOutputStream fos = null;
		PlayLocation locationObj = PlayLocation.getInstance();
		try {
			fos = new FileOutputStream(FILE_PLAY_LOCATION);
			out = new ObjectOutputStream(fos);
			out.writeObject(locationObj);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (out != null)
					out.close();
				if (fos != null)
					fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized static PlayLocation restoreLocation() {
		ObjectInputStream ois = null;
		FileInputStream fis = null;
		PlayLocation obj = null;
		File f = null;
		try {
			f = new File(FILE_PLAY_LOCATION);
			if (!f.exists())
				return null;
			fis = new FileInputStream(f);
			ois = new ObjectInputStream(fis);
			obj = (PlayLocation) ois.readObject();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (StreamCorruptedException e) {
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
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (f != null && f.exists())
				f.delete();
		}
		if (obj != null)
			instance = obj;
		return obj;
	}

	// public static void deletePlayLocationFile() {
	// File f = new File(FILE_PLAY_LOCATION);
	// if (f.exists())
	// f.delete();
	// }

}
