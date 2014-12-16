package edu.missouri.niaaa.craving;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.missouri.niaaa.craving.activity.AdminManageActivity;
import edu.missouri.niaaa.craving.activity.MorningScheduler;
import edu.missouri.niaaa.craving.activity.SupportActivity;
import edu.missouri.niaaa.craving.activity.SurveyMenu;
import edu.missouri.niaaa.craving.activity.SuspensionTimePicker;
import edu.missouri.niaaa.craving.location.LocationUtilities;
import edu.missouri.niaaa.craving.sensor.SensorConnections;
import edu.missouri.niaaa.craving.services.SensorLocationService;
import edu.missouri.niaaa.craving.services.SensorLocationService.MyBinder;


public class MainActivity extends Activity {

	static String TAG = "Main activity~~~~~~~~";

	final static int INTENT_REQUEST_MAMAGE = 1;
	final static int INTENT_REQUEST_SUSPENSION = 2;
	final static int INTENT_REQUEST_BLUETOOTH = 9;

	Button section_1;
	Button section_2;
	Button section_3;
	Button section_4;
	Button section_5;
	Button section_6;
	Button section_7;
	Button section_8;
	Button section_9;

	InputMethodManager imm;
	SharedPreferences shp;
	Editor editor;
	String ID;
	String PWD;
	private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

//	static LocationManager locationM;

	ServiceConnection conn = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            MyBinder binder = (MyBinder)service;
            SensorLocationService bindService = binder.getService();
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//threadpolicy, maybe changed later
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);

		setContentView(R.layout.activity_main);

		setListeners();

		setSharedValue();

		IntentFilter suspensionIntent = new IntentFilter(Utilities.BD_ACTION_SUSPENSION);
		this.registerReceiver(suspensionReceiver, suspensionIntent);


        ////startSService();
        //
        //check if device is assigned with an ID
        shp = getSharedPreferences(Utilities.SP_LOGIN, Context.MODE_PRIVATE);
        ID = shp.getString(Utilities.SP_KEY_LOGIN_USERID, "");
        PWD = shp.getString(Utilities.SP_KEY_LOGIN_USERPWD, "");
        editor = shp.edit();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Log.d(TAG,"id is "+ID);

        if(ID.equals("")){
        	management();


            imm.toggleSoftInput(0, InputMethodManager.RESULT_HIDDEN);

        }else if(PWD.equals("")){
        	//set password

        	UserPWDSetDialog(this, ID).show();

        }else{
        	Log.d(TAG,"pwd is "+shp.getString(Utilities.SP_KEY_LOGIN_USERPWD, "get fail?"));
//        	startSService();

        	//set fun to 0
//        	sendBroadcast(new Intent(Utilities.BD_ACTION_DAEMON));

        	//restart gps
        	if(Utilities.completedMorningToday(this) || Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 3){
        		sendBroadcast(new Intent(LocationUtilities.ACTION_START_LOCATION));
        	}
        }
	}


	private void setSharedValue(){

		//public key
		try {
			Utilities.publicKey = getPublicKey();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), R.string.public_key_lost, Toast.LENGTH_SHORT).show();
			finish();
		}

		//ID

//		locationM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}


	private Dialog UserPWDSetDialog(Context context, final String ID) {
		LayoutInflater inflater = LayoutInflater.from(context);
		final View textEntryView = inflater.inflate(R.layout.pin_input, null);
		TextView pinText = (TextView) textEntryView.findViewById(R.id.pin_text);
		pinText.setText(getString(R.string.user_setpwd_msg)+ID);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false);
		builder.setTitle(R.string.user_setpwd_title);
		builder.setView(textEntryView);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {

				EditText pinEdite = (EditText) textEntryView.findViewById(R.id.pin_edit);
				String pinStr = pinEdite.getText().toString();
				Utilities.Log("Pin Dialog", "pin String is "+pinStr);

				String data = null;
				try {
					data = Utilities.encryption(ID + "," + "3" + "," + pinStr);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

/*				check network*/

/*				prepare params for server*/
				HttpPost request = new HttpPost(Utilities.VALIDATE_ADDRESS);

				List<NameValuePair> params = new ArrayList<NameValuePair>();

				params.add(new BasicNameValuePair("data", data));

				// 		        //file_name
				// 		        params.add(new BasicNameValuePair("userID",ID));
				// 		        //function
				// 		        params.add(new BasicNameValuePair("pre","3"));
				// 		        //data
				// 		        params.add(new BasicNameValuePair("password",pinStr));

/*				check identity*/

 		        try {
 		        	request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

 		        	HttpResponse response = new DefaultHttpClient().execute(request);
 		        	if(response.getStatusLine().getStatusCode() == 200){
 		        		String result = EntityUtils.toString(response.getEntity());
 		        		Log.d("~~~~~~~~~~http post result3 ",result);

 		        		if(result.equals("NewUserIsCreated")){
 		        			//new pwd created
 		        			//format check

 		        			editor.putString(Utilities.SP_KEY_LOGIN_USERPWD, pinStr);
 		        			editor.commit();
 		        			PWD = shp.getString(Utilities.SP_KEY_LOGIN_USERPWD, "");


 		        			Utilities.scheduleAll(MainActivity.this);
 		        			Utilities.scheduleDaemon(MainActivity.this);
// 		        			startSService();
 		        		}else{
 		        			//imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
 		        			//imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

 		        			Toast.makeText(getApplicationContext(), R.string.set_upin_failed, Toast.LENGTH_SHORT).show();
 		        			//set return code

 		        			finish();
 		        		}
 		        	}
 		        } catch (Exception e) {
 		        	// TODO Auto-generated catch block

 		        	imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
 		        	imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

 		        	Toast.makeText(getApplicationContext(), R.string.set_upin_error, Toast.LENGTH_SHORT).show();
 		        	//set return code

 		        	finish();
 		        	e.printStackTrace();
 		        }
			}
		});

		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		    @Override
			public void onClick(DialogInterface dialog, int whichButton) {

		    	imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
				imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                finish();
		    }
		});

		return builder.create();
	}



	private void setListeners() {
		// TODO Auto-generated method stub
		section_1 = (Button) findViewById(R.id.section_label1);
		section_2 = (Button) findViewById(R.id.section_label2);
		section_3 = (Button) findViewById(R.id.section_label3);
		section_4 = (Button) findViewById(R.id.section_label4);
		section_5 = (Button) findViewById(R.id.section_label5);
		section_6 = (Button) findViewById(R.id.section_label6);
		section_7 = (Button) findViewById(R.id.section_label7);
		section_8 = (Button) findViewById(R.id.section_label8);
		section_9 = (Button) findViewById(R.id.section_label9);

		setServiceText();
		section_1.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "section 1 on click listener");

				String act = ((Button) view).getText().toString();
				//start service
				if(act.equals(getString(R.string.section_1))){
//					((Button)view).setText(R.string.section_2);

					StartSensorLocationService();

				}

				//stop service
				else{
//					((Button)view).setText(R.string.section_1);

					confirmStopService();
				}



			}
		});

		section_2.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "section 2 on click listener");
//				Intent i = new Intent(MainActivity.this,SensorLocationService.class);
//				stopService(i);

//				startActivity(new Intent(MainActivity.this, NotificationSystem.class));
			}
		});

		section_3.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "section 3 on click listener");

				if(!getSuspension()){
					startActivity(new Intent(MainActivity.this, SurveyMenu.class));
				}else{
					suspensionAlert();
				}

			}
		});

		section_4.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "section 4 on click listener");

				startActivity(new Intent(getApplicationContext(), SensorConnections.class));
			}
		});

		section_5.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "section 5 on click listener");

				if(!getSuspension()){
					int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
					if(hour >= 21 || hour <3){
						//verify user pin
						PinCheckDialog(MainActivity.this).show();
					}else{
						//alert dialog
						bedTimeCheckDialog();
					}
				}
				else{
				suspensionAlert();
				}

//				Intent i = new Intent(getApplicationContext(), MorningScheduler.class);
//				startActivity(i);

			}
		});

		setSuspensionText();
		section_6.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "section 6 on click listener");

				if(Utilities.completedMorningToday(MainActivity.this)){
					if(section_6.getText().equals(MainActivity.this.getString(R.string.section_6))){
						Log.d("test text 6", "suspension~~~~~~~~~~~");

						new AlertDialog.Builder(MainActivity.this)
						.setTitle(R.string.suspension_title)
						.setMessage(R.string.suspension_msg)
						.setCancelable(false)
						.setNegativeButton(android.R.string.cancel, null)
						.setPositiveButton(android.R.string.ok, new android.content.DialogInterface.OnClickListener() {

						    @Override
							public void onClick(DialogInterface arg0, int arg1) {
						    	Intent intent = new Intent(getApplicationContext(), SuspensionTimePicker.class);
								startActivityForResult(intent, 2);
						    }
						}).create().show();
					}
					else{
						Log.d("test text 6", "break suspension~~~~~~~~~~~");

						new AlertDialog.Builder(MainActivity.this)
						.setTitle(R.string.suspension_break_title)
						.setMessage(R.string.suspension_break_msg)
						.setCancelable(false)
						.setNegativeButton(android.R.string.cancel, null)
						.setPositiveButton(android.R.string.ok, new android.content.DialogInterface.OnClickListener() {

						    @Override
							public void onClick(DialogInterface arg0, int arg1) {
						    	section_6.setText(R.string.section_6);
								Utilities.getSP(MainActivity.this, Utilities.SP_SURVEY).edit().putBoolean(Utilities.SP_KEY_SURVEY_SUSPENSION, false).commit();

								//cancel suspension alarm
								AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

								Intent breakIntent = new Intent(Utilities.BD_ACTION_SUSPENSION);
				    			PendingIntent breakPi = PendingIntent.getBroadcast(getApplicationContext(), 0, breakIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
			//					getApplicationContext().sendBroadcast(breakIntent);

				    			am.cancel(breakPi);

				    			//write to server
				    			Calendar c = Calendar.getInstance();
				    			SharedPreferences sp = getSharedPreferences(Utilities.SP_LOGIN, Context.MODE_PRIVATE);
				    			long startTimeStamp = sp.getLong(Utilities.SP_KEY_SUSPENSION_TS, c.getTimeInMillis());
				    			c.setTimeInMillis(startTimeStamp);

				    			try {
				    				Utilities.writeEventToFile(MainActivity.this, Utilities.CODE_SUSPENSION, "", "", "", "",
				    						Utilities.sdf.format(c.getTime()), Utilities.sdf.format(Calendar.getInstance().getTime()));
				    			} catch (IOException e) {
				    				// TODO Auto-generated catch block
				    				e.printStackTrace();
				    			}
				    			sp.edit().remove(Utilities.SP_KEY_SUSPENSION_TS).commit();

				    			//volume
				            	AudioManager audiom = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
				            	audiom.setStreamVolume(AudioManager.STREAM_MUSIC, Utilities.VOLUME, AudioManager.FLAG_PLAY_SOUND);

				            	Vibrator v = (Vibrator) MainActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
				    	        v.vibrate(500);
				    	        Toast.makeText(getApplicationContext(), R.string.suspension_end, Toast.LENGTH_LONG).show();
						    }
						}).create().show();
					}
				}
				else{
					Toast.makeText(MainActivity.this, R.string.morning_report_unfinished, Toast.LENGTH_LONG).show();
				}
			}
		});

		section_7.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "section 7 on click listener");

				startActivity(new Intent(MainActivity.this, SupportActivity.class));
			}
		});

		section_8.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "section 8 on click listener ");

//				startService(new Intent(getApplicationContext(), SensorLocationService.class));
//				bindService(new Intent(getApplicationContext(), SensorLocationService.class), conn, Context.BIND_AUTO_CREATE);

//				LocationManager locationM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//				LocationUtilities.startGPS(MainActivity.this, locationM);

//				LocationUtilities.requestLocation(SensorLocationService.locationM);

				sendBroadcast(new Intent(LocationUtilities.ACTION_START_LOCATION));

				//Utilities.scheduleRandomSurvey(MainActivity.this);
//				Utilities.reScheduleRandom(MainActivity.this);

//				Intent i = new Intent(Utilities.BD_ACTION_DAEMON);
//				i.putExtra(Utilities.BD_ACTION_DAEMON_FUN, -1);
//				sendBroadcast(i);
			}
		});

		section_9.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "section 9 on click listener"+// Utilities.getScheduleForToady(MainActivity.this)+ " "+//);

				Utilities.getSP(MainActivity.this, Utilities.SP_SURVEY).getInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, -1)+
				Utilities.getSP(MainActivity.this, Utilities.SP_SURVEY).getBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false) + " "+
				Utilities.getSP(MainActivity.this, Utilities.SP_SURVEY).getString(Utilities.SP_KEY_SURVEY_UNDERREMINDERING, "nothing"));

//				stopService(new Intent(getApplicationContext(), SensorLocationService.class));
//				unbindService(conn);

//				LocationManager locationM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//				LocationUtilities.stopGPS(getApplicationContext(), locationM);

//				LocationUtilities.removeLocation(SensorLocationService.locationM);

				sendBroadcast(new Intent(LocationUtilities.ACTION_STOP_LOCATION));

//				Utilities.cancelReminder(MainActivity.this);
//				Utilities.cancelTrigger(MainActivity.this);
//				Utilities.cancelSchedule(MainActivity.this);
//
//				Intent i = new Intent(Utilities.BD_ACTION_DAEMON);
//				i.putExtra(Utilities.BD_ACTION_DAEMON_FUNC, 0);
//				sendBroadcast(i);

				//recovery random survey

				//set trigger_seq based on current time schedule


				//send broad cast
//				Intent scheduleIntent = new Intent(Utilities.BD_ACTION_SCHEDULE_RANDOM);
//				scheduleIntent.putExtra(Utilities.SV_NAME, Utilities.SV_NAME_RANDOM);
//				getApplicationContext().sendBroadcast(scheduleIntent);


				/*clear all*/
				//Utilities.getSP(MainActivity.this, Utilities.SP_SURVEY).edit().clear().commit();

//				shp.edit().putInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, 0).commit();
//				shp.edit().putBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false).commit();
//				shp.edit().putInt(triggerSeq, 0).commit();
			}
		});
	}

	private void setServiceText(){
		section_1.setText(SensorLocationService.mIsRunning? getString(R.string.section_2): getString(R.string.section_1));
	}

	private void setSuspensionText(){
		section_6.setText(!Utilities.getSP(MainActivity.this, Utilities.SP_SURVEY).getBoolean(Utilities.SP_KEY_SURVEY_SUSPENSION, false)?R.string.section_6:R.string.section_62);
	}

	private void StartSensorLocationService(){
		Intent i = new Intent(MainActivity.this,SensorLocationService.class);
		startService(i);

		section_1.setText(getString(R.string.section_2));
		Toast.makeText(getApplicationContext(), R.string.service_start, Toast.LENGTH_LONG).show();
	}

	private void StopSensorLocationService(){
		Intent i = new Intent(MainActivity.this,SensorLocationService.class);
		stopService(i);

		section_1.setText(getString(R.string.section_1));
		Toast.makeText(getApplicationContext(), R.string.service_stop, Toast.LENGTH_LONG).show();
	}

	private void confirmStopService(){
		Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
		.setCancelable(false)
		.setTitle(R.string.service_stop_title)
		.setMessage(R.string.service_stop_msg)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				StopSensorLocationService();
			}
		})
		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		})
		.create();
		alertDialog.show();
	}


	private boolean getSuspension(){
		return Utilities.getSP(MainActivity.this, Utilities.SP_SURVEY).getBoolean(Utilities.SP_KEY_SURVEY_SUSPENSION, false);
	}

	private void suspensionAlert(){
		Toast.makeText(getApplicationContext(), R.string.suspension_under, Toast.LENGTH_LONG).show();
	}

	private void bedTimeCheckDialog(){
		new AlertDialog.Builder(MainActivity.this)
	    .setTitle(R.string.bedtime_title)
	    .setMessage(R.string.bedtime_message)
	    .setCancelable(false)
	    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	        	dialog.cancel();
	        }
	    })
	    .create().show();
	}

	private Dialog PinCheckDialog(final Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		final View DialogView = inflater.inflate(R.layout.pin_input, null);
		TextView pinText = (TextView) DialogView.findViewById(R.id.pin_text);
		pinText.setText(R.string.pin_message);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false);
		builder.setTitle(R.string.pin_title);
		builder.setView(DialogView);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {

				EditText pinEdite = (EditText) DialogView.findViewById(R.id.pin_edit);
				String pinStr = pinEdite.getText().toString();
				Utilities.Log("Pin Dialog", "pin String is "+pinStr);

				if (pinStr.equals(Utilities.getPWD(context))){
		        	//Send the intent and trigger new Survey Activity....
		        	bedAlertDialog();
		        	dialog.cancel();
	        	}
	        	else {
	        		//New AlertDialog to show instruction.
	        		new AlertDialog.Builder(MainActivity.this)
	        		.setTitle(R.string.pin_title_wrong)
	        		.setMessage(R.string.pin_message_wrong)
	        		.setPositiveButton(android.R.string.yes, null)
	        		.create().show();
	        	}

	        	dialog.cancel();

			}
		});

		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		    @Override
			public void onClick(DialogInterface dialog, int whichButton) {

		    }
		});

		return builder.create();
	}


	private void bedAlertDialog(){
		new AlertDialog.Builder(MainActivity.this)
	    .setTitle(R.string.bedtime_title)
	    .setMessage(R.string.bedtime_message_confirm)
	    .setCancelable(false)
	    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {

				Intent i = new Intent(getApplicationContext(), MorningScheduler.class);
				startActivity(i);

	        	dialog.cancel();
	        }
	    })
	    .setNegativeButton(R.string.no, null)
	    .create().show();
	}




	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Utilities.Log_sys(TAG, "onActivityResule requestCode "+requestCode);
		Utilities.Log_sys(TAG, "onActivityResule resultCode "+resultCode);

		 switch (requestCode) {
	        case INTENT_REQUEST_MAMAGE:
	        	if(resultCode == Activity.RESULT_CANCELED){
//	        		stopSService();
	        		finish();

	        	}
	        	else if(resultCode == Activity.RESULT_OK){
	        		ID = shp.getString(Utilities.SP_KEY_LOGIN_USERID, "");
	        		UserPWDSetDialog(this, ID).show();

	        	}
	        	else{

	        	}

	        	break;

	        case INTENT_REQUEST_SUSPENSION:
	        	if(resultCode == 1){
	        		section_6.setText(R.string.section_62);
	        	}

	        	break;
		 }


	}

	private void management(){
		Intent serverIntent = new Intent(this, AdminManageActivity.class);
        startActivityForResult(serverIntent, INTENT_REQUEST_MAMAGE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


	/* set click listener for top-right menu */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

		//ENABLE BLUETOOTH
		if (item.getItemId() == R.id.Enable){
			if(btAdapter.isEnabled())			{
				Toast.makeText(getApplicationContext(), R.string.bluetooth_enabled ,Toast.LENGTH_LONG).show();
			}
			else{
				turnOnBt();
			}

            return true;
		}


		//DISABLE BLUETOOTH
		else if (item.getItemId() == R.id.Disable){
			btAdapter.disable();
			Toast.makeText(getApplicationContext(), R.string.bluetooth_disabled, Toast.LENGTH_LONG).show();
            return true;
		}

		//MANAGEMENT
		else if(item.getItemId() == R.id.manage){
			management();
		}

		// ABOUT
		else if(item.getItemId() == R.id.about){

			//initial versionCode
			int versionCode = 100;
			String versionName = "2.2";
			PackageInfo pinfo;
			try {
				pinfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
				versionCode = pinfo.versionCode;
				versionName = pinfo.versionName;
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// show current version, which is defined in Android Manifest
			Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
			.setCancelable(false)
			.setTitle(getString(R.string.menu_about)+"  ver."+versionName+"."+versionCode)
			.setMessage("User ID: "+ID+"\n"+Utilities.getScheduleForToady(MainActivity.this))
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub

				}
			})
			.create();
			alertDialog.show();
		}

		//		else if (item.getItemId() == R.id.feedback) {
		//			Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		//			intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { "mufreemyapps@gmail.com" });
		//			intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Feedback");
		//			intent.putExtra(android.content.Intent.EXTRA_TEXT, "Please describe your problems here:");
		//			intent.setType("text/html");
		//			startActivity(Intent.createChooser(intent, "Mail Chooser"));
		//
		//		}
		return super.onOptionsItemSelected(item);
	}


	private void turnOnBt(){
		// TODO Auto-generated method stub
		Intent Enable_Bluetooth=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(Enable_Bluetooth, INTENT_REQUEST_BLUETOOTH);
	}


	BroadcastReceiver suspensionReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Utilities.Log(TAG, "on receiver break suspension");

			section_6.setText(R.string.section_6);
//			Utilities.getSP(MainActivity.this, Utilities.SP_SURVEY).edit().putBoolean(Utilities.SP_KEY_SURVEY_SUSPENSION, false).commit();

			//write to server
			Calendar c = Calendar.getInstance();
			SharedPreferences sp = getSharedPreferences(Utilities.SP_LOGIN, Context.MODE_PRIVATE);
			long startTimeStamp = sp.getLong(Utilities.SP_KEY_SUSPENSION_TS, c.getTimeInMillis());
			c.setTimeInMillis(startTimeStamp);

			try {
				Utilities.writeEventToFile(MainActivity.this, Utilities.CODE_SUSPENSION, "", "", "", "",
						Utilities.sdf.format(c.getTime()), Utilities.sdf.format(Calendar.getInstance().getTime()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sp.edit().remove(Utilities.SP_KEY_SUSPENSION_TS).commit();

			Toast.makeText(getApplicationContext(), R.string.suspension_end, Toast.LENGTH_LONG).show();
		}
	};


	private PublicKey getPublicKey() throws Exception {
		// TODO Auto-generated method stub
        InputStream is = getResources().openRawResource(R.raw.publickey);
		ObjectInputStream ois = new ObjectInputStream(is);

		BigInteger m = (BigInteger)ois.readObject();
		BigInteger e = (BigInteger)ois.readObject();
	    RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);


	    KeyFactory fact = KeyFactory.getInstance("RSA", "BC");
	    PublicKey pubKey = fact.generatePublic(keySpec);

		return pubKey;
	}

//================================================================================================================================
//================================================================================================================================

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Utilities.Log_sys(TAG, "onRestart");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Utilities.Log_sys(TAG, "onStart");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Utilities.Log_sys(TAG, "onResume");
		setSuspensionText();
		setServiceText();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Utilities.Log_sys(TAG, "onPause");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Utilities.Log_sys(TAG, "onStop");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Utilities.Log_sys(TAG, "onDestroy");

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

		this.unregisterReceiver(suspensionReceiver);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}






	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}




}
