package edu.missouri.niaaa.craving.services;

import edu.missouri.niaaa.craving.SurveyMenu;
import edu.missouri.niaaa.craving.Utilities;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;

public class SensorService extends Service {

	String TAG = "SensorService";
	
	PowerManager mPowerManager;
	WakeLock serviceWakeLock;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Utilities.Log_sys(TAG, "Service OnCreate");
		
		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		
		serviceWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK , "SensorServiceLock");
		serviceWakeLock.acquire();
		
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		
//		Intent it = new Intent("edu.missouri.niaaa.craving.MORNING_REPORT");
//		PendingIntent pi = PendingIntent.getBroadcast(getApplicationContext(), 0, it, Intent.FLAG_ACTIVITY_NEW_TASK);
//		int ti = (int) SystemClock.elapsedRealtime() + 5 * 1000;
//		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, ti, pi);
		
		Intent i = new Intent(this, SurveyMenu.class);
		i.putExtra("survey_name", "DRINKING_FOLLOWUP");
		i.putExtra("survey_file", "DrinkingFollowup.xml");	
		PendingIntent p = PendingIntent.getActivity(SensorService.this, 0,
			                i, Intent.FLAG_ACTIVITY_NEW_TASK);
		am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime()+5000 , p);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Utilities.Log_sys(TAG, "Service OnDestory");
		
		serviceWakeLock.release();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Utilities.Log_sys(TAG, "Service OnStartCommand");
		
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Utilities.Log_sys(TAG, "Service OnBind");
		return null;
	}

}
