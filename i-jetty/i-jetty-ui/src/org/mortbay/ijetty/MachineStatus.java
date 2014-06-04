package org.mortbay.ijetty;

public class MachineStatus {
	public static final String MACHINE_STATUS_ONLINE = "0";
	public static final String MACHINE_STATUS_DOWNLOADING = "1";
	public static final String MACHINE_STATUS_OFFLINE = "2";
	
	public static String KUAISHUA_ACTIVATION_STATE = "0";

	public static String getStatus() {
		return org.mortbay.ijetty.network.DownloadManager.getDownloadThreadCount() > 0 ? MACHINE_STATUS_DOWNLOADING
				: MACHINE_STATUS_ONLINE;
	}

}
