package edu.missouri.niaaa.craving.services;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;
import edu.missouri.niaaa.craving.R;
import edu.missouri.niaaa.craving.Utilities;
import edu.missouri.niaaa.craving.sensor.SensorUtilities;
import edu.missouri.niaaa.craving.sensor.equivital.EquivitalRunnable;
import edu.missouri.niaaa.craving.sensor.internal.InternalRunnable;

public class SensorLocationService extends Service {


	String TAG = "SensorLocationService";

	PowerManager mPowerManager;
	WakeLock serviceWakeLock;

	EquivitalRunnable equivitalThread;
	InternalRunnable accelermetorThread;

	SensorManager mSensorManager;

	public static boolean cancelBlueToothFlag = false;
	public static Context serviceContext;

	/*	sensor*/
	private SoundPool mSoundP;
	private HashMap<Integer, Integer> soundsMap;
	Timer soundTimer;
	Timer voiceTimer;
	int soundStreamID;
	int voiceStreamID;
	public static boolean mIsRunning = false;



	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Utilities.Log_sys(TAG, "Service OnCreate");
		serviceContext = this;
		mIsRunning = true;

		mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		serviceWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK , "SensorServiceLock");
		serviceWakeLock.acquire();

		mSoundP = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
		soundsMap = new HashMap<Integer, Integer>();
		soundsMap.put(1, mSoundP.load(this, R.raw.alarm_sound, 1));
		soundsMap.put(2, mSoundP.load(this, R.raw.voice_sound, 1));
//		soundTimer = new Timer();
//		voiceTimer = new Timer();

		IntentFilter sensorIntent = new IntentFilter();
		sensorIntent.addAction(SensorUtilities.ACTION_CONNECT_SENSOR);
		sensorIntent.addAction(SensorUtilities.ACTION_DISCONNECT_SENSOR);
		sensorIntent.addAction(SensorUtilities.ACTION_LOST_CONNECTION_SOUND);
		this.registerReceiver(SensorReceiver,sensorIntent);


//		IntentFilter locationIntent = new IntentFilter();
//		locationIntent.addAction(LocationUtilities.ACTION_START_LOCATION);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Utilities.Log_sys(TAG, "Service OnDestory");

		this.unregisterReceiver(SensorReceiver);

		serviceWakeLock.release();
		mIsRunning = false;

		stopInternalThread(accelermetorThread);
		stopEquivitalThread();
		stopSound();
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
		return new MyBinder();
	}


	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		Utilities.Log_sys(TAG, "Service OnUnBind");
		return super.onUnbind(intent);
	}


/*	mBinder*/
	public class MyBinder extends Binder{
		public SensorLocationService getService(){
			return SensorLocationService.this;
		}
	}



/*	write to file*/
	public static boolean checkDataConnectivity() {
		ConnectivityManager connectivity = (ConnectivityManager) serviceContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}



/*	sensor broadcast receiver*/


	BroadcastReceiver SensorReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			Log.d(TAG,"sensor receiver action "+ action);

			if(action.equals(SensorUtilities.ACTION_CONNECT_SENSOR)){

				Toast.makeText(getApplicationContext(), R.string.sensor_connect, Toast.LENGTH_LONG).show();

				String address=intent.getStringExtra(SensorUtilities.KEY_ADDRESS);
				String deviceName=intent.getStringExtra(SensorUtilities.KEY_DEVICE_NAME);


				String userID = Utilities.getSP(context, Utilities.SP_LOGIN).getString(Utilities.SP_KEY_LOGIN_USERID, "0000");

				cancelBlueToothFlag = false;
				equivitalThread = EquivitalRunnable.getInstance(address, deviceName, userID);
				equivitalThread.run();
				accelermetorThread = InternalRunnable.getInstance(mSensorManager,Sensor.TYPE_ACCELEROMETER,SensorManager.SENSOR_DELAY_GAME, userID);
				accelermetorThread.run();

//				Calendar c=Calendar.getInstance();
//				SimpleDateFormat curFormater = new SimpleDateFormat("MMMMM_dd");
//				String dateObj =curFormater.format(c.getTime());
//				String file_name="Mac_Address"+dateObj+".txt";
//		        File f = new File(BASE_PATH,file_name);
//
//				if(f != null){
//					try {
//						writeToFile(f, address);
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}

				SharedPreferences sp = getSharedPreferences(Utilities.SP_LOGIN, Context.MODE_PRIVATE);
				if(!sp.contains(Utilities.SP_KEY_SENSOR_CONN_TS)){
					sp.edit().putLong(Utilities.SP_KEY_SENSOR_CONN_TS, Calendar.getInstance().getTimeInMillis()).commit();
				}

			}

			//hard reset
			else if(action.equals(SensorUtilities.ACTION_DISCONNECT_SENSOR)){
				Log.d(TAG, "action received: " + SensorUtilities.ACTION_DISCONNECT_SENSOR);
				stopInternalThread(accelermetorThread);
				stopEquivitalThread();
				stopSound();
			}

			else if(action.equals(SensorUtilities.ACTION_LOST_CONNECTION_SOUND)){

				//sound alert that bluetooth connection is lost, check to connect again
				Log.d(TAG, "sensor connection is lost, sound should come off");
				playSound();
				writeSensorConn();
			}
		}

	};


	private void startEquivitalThread(){

	}

	private void stopEquivitalThread(){

		if(equivitalThread != null){
			equivitalThread.stop();
		}
		writeSensorConn();
	}

	private void stopInternalThread(InternalRunnable internalThread) {

		if (internalThread != null) {
			internalThread.stop();

		}
	}
	private void writeSensorConn(){

		if(!cancelBlueToothFlag){
			//write to server
			Calendar c = Calendar.getInstance();
			SharedPreferences sp = getSharedPreferences(Utilities.SP_LOGIN, Context.MODE_PRIVATE);
			long startTimeStamp = sp.getLong(Utilities.SP_KEY_SENSOR_CONN_TS, c.getTimeInMillis());
			c.setTimeInMillis(startTimeStamp);

			try {
				Utilities.writeEventToFile(this, Utilities.CODE_SENSOR_CONN, "", "", "", "",
						Utilities.sdf.format(c.getTime()), Utilities.sdf.format(Calendar.getInstance().getTime()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sp.edit().remove(Utilities.SP_KEY_SENSOR_CONN_TS).commit();
		}

		cancelBlueToothFlag = true;
	}


/*	alarm sound for disconnection*/

	private void playSound(){
//		this.setVolumeControlStream(AudioManager.STREAM_ALARM);
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		am.setStreamVolume(AudioManager.STREAM_MUSIC, Utilities.VOLUME, AudioManager.FLAG_PLAY_SOUND);

		soundTimer = new Timer();
		voiceTimer = new Timer();
		soundTimer.schedule(new StartSound(),3000);
		voiceTimer.schedule(new StartVoice(),3000 + 20*1000);

		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
	}

	private void stopSound(){
		if(soundTimer != null) {
			soundTimer.cancel();
		}
		if(voiceTimer != null) {
			voiceTimer.cancel();
		}
		mSoundP.stop(soundStreamID);
		mSoundP.stop(voiceStreamID);
	}


    private class StartSound extends TimerTask {
    	@Override
    	public void run(){

    		soundStreamID = mSoundP.play(soundsMap.get(1), 1, 1, 1, 0, 1);
    	}
    }

    private class StartVoice extends TimerTask {
    	@Override
    	public void run(){

    		voiceStreamID = mSoundP.play(soundsMap.get(2), 1, 1, 1, 0, 1);
    	}
    }


}
