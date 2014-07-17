package edu.missouri.niaaa.craving;


import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import edu.missouri.niaaa.craving.services.SensorService;
import edu.missouri.niaaa.craving.survey.XMLSurveyActivity;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class MainActivity extends Activity {

	static String TAG = "Main activity~~~~~~~~";

	Button section_1;
	Button section_2;
	Button section_3;
	Button section_4;
	Button section_5;
	Button section_6;
	Button section_7;
	Button section_8;
	Button section_9;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		setListeners();
		
//		Utilities.getSP(this).edit().putBoolean("underGoing", false).commit();
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
		
		section_1.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "section 1 on click listener");
				
				Intent i = new Intent(MainActivity.this,SensorService.class);
				startService(i);
			}
		});
		
		section_2.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "section 2 on click listener");
				Intent i = new Intent(MainActivity.this,SensorService.class);
				stopService(i);
			}
		});
		
		section_3.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "section 3 on click listener");
				
				startActivity(new Intent(MainActivity.this,SurveyMenu.class));
			}
		});
		
		section_4.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "section 4 on click listener");
				

			}
		});
		
		section_5.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "section 5 on click listener");
				
				Intent i = new Intent(getApplicationContext(), MorningScheduler.class);
				startActivity(i);
				
			}
		});
		
		section_6.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "section 6 on click listener");
				
				section_6.setText("Break S");
			}
		});
		
		section_7.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "section 7 on click listener ");
				
				Utilities.getSP(MainActivity.this, Utilities.SP_SURVEY).edit().putInt(Utilities.SP_KEY_SURVEY_TRIGGER_SEQ_FOLLOWUP, 0).commit();
				
				Intent scheduleIntent = new Intent(Utilities.BD_ACTION_SCHEDULE_FOLLOWUP);
				scheduleIntent.putExtra(Utilities.SV_NAME, Utilities.SV_NAME_FOLLOWUP);
				getApplicationContext().sendBroadcast(scheduleIntent);
			}
		});
		
		section_8.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Calendar c = Calendar.getInstance();
				c.set(Calendar.HOUR_OF_DAY, 12);//23
				c.set(Calendar.MINUTE, 30);//59
				
				long base = Calendar.getInstance().getTimeInMillis();
				long peak = c.getTimeInMillis();
				
				long unit = (peak-base)/7;
				long r_unit = (peak-base)/21;
				
				String random_schedule = new String();
				
				for(int i=1;i<7;i++){
					random_schedule = random_schedule + (base+unit*i+(new Random().nextInt((int) (2*r_unit))-r_unit)+",");
				}
				
				
				Utilities.Log(TAG, "section 8 on click listener "+Calendar.getInstance().getTimeInMillis()+" "+c.getTimeInMillis()+" unit "+unit+" r_unit "+r_unit);
				
				String strArr[] = random_schedule.split(",");
				for(String str: strArr){
					Calendar c2 = Calendar.getInstance();
					c2.setTimeInMillis(Long.parseLong(str));
					Log.d(TAG, "each item is "+str+" "+c2.get(Calendar.HOUR_OF_DAY)+":"+c2.get(Calendar.MINUTE));
				}
				
				Utilities.getSP(MainActivity.this, Utilities.SP_RANDOM_TIME).edit().putString(Utilities.SP_KEY_RANDOM_TIME_SET, random_schedule).commit();
				
				Utilities.getSP(MainActivity.this, Utilities.SP_SURVEY).edit().putInt(Utilities.SP_KEY_SURVEY_TRIGGER_SEQ_RANDOM, 0).commit();
				
				Intent scheduleIntent = new Intent(Utilities.BD_ACTION_SCHEDULE_RANDOM);
				scheduleIntent.putExtra(Utilities.SV_NAME, Utilities.SV_NAME_RANDOM);
				getApplicationContext().sendBroadcast(scheduleIntent);
				
			}
		});
		section_9.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "section 9 on click listener");
				
				
				//recovery random survey
				
				//set trigger_seq
				
				
				//send broad cast
				Intent scheduleIntent = new Intent(Utilities.BD_ACTION_SCHEDULE_RANDOM);
				scheduleIntent.putExtra(Utilities.SV_NAME, Utilities.SV_NAME_RANDOM);
				getApplicationContext().sendBroadcast(scheduleIntent);
				
				
				/*clear all*/
				//Utilities.getSP(MainActivity.this, Utilities.SP_SURVEY).edit().clear().commit();
				
//				shp.edit().putInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, 0).commit();
//				shp.edit().putBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false).commit();
//				shp.edit().putInt(triggerSeq, 0).commit(); 
			}
		});
	}

	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Utilities.Log_sys(TAG, "onActivityResule requestCode "+requestCode);
		Utilities.Log_sys(TAG, "onActivityResule resultCode "+resultCode);
		
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
		
		//DISABLE BLUETOOTH
		
		//MANAGEMENT
		
		// ABOUT
		if(item.getItemId() == R.id.about){
			
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
			.setTitle(R.string.menu_about)
			.setMessage(versionName+"."+versionCode)
			.setPositiveButton("OK", new DialogInterface.OnClickListener() { 

				@Override 
				public void onClick(DialogInterface dialog, int which) { 
					// TODO Auto-generated method stub  
					
				} 
			})
			.create();
			alertDialog.show();
		}
		return super.onOptionsItemSelected(item);
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
	}
	
	
	
	


	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
	}

	


}
