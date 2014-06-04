package org.bug;

import java.util.Timer;
import java.util.TimerTask;

public class Speak {
	private static final int SOUND_ON = 1;
	private static final int SOUND_OFF = 0;
	private static boolean machineFlag = false;
	static {
		String mode = android.os.Build.MODEL;
		machineFlag = mode.startsWith("f04ref_BYW_ZH");
	}

	public static void setOn() {

		if (machineFlag)
		{
			/**
			 * 注意: 必须延迟两秒或者以上
			 */
			Timer t = new Timer();
			t.schedule(new TimerTask() {
				
				@Override
				public void run() {
					setspeak(SOUND_ON);				
				}
			}, 2*1000);
		}
			

	}

	public static void setOff() {
		if (machineFlag)
			setspeak(SOUND_OFF);

	}

	static {
		System.loadLibrary("orgbugspeak");
	}

	private native static void setspeak(int onoff);
}
