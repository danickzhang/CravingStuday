package edu.missouri.niaaa.craving;

import java.util.Calendar;
import java.util.HashMap;

import edu.missouri.niaaa.craving.survey.XMLSurveyActivity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.PowerManager.WakeLock;
import android.util.Log;

public class SurveyBroadcast extends BroadcastReceiver {

	String TAG = "survey Broadcast";
	
  	SoundPool sp;
	private HashMap<Integer, Integer> soundsMap;
	MediaPlayer mp;
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Utilities.Log_sys(TAG, "broadcast on receive"+intent.getAction());
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		
		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);  
        WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Gank");  
        wl.acquire();  
        
//        sp = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
//		soundsMap = new HashMap<Integer, Integer>();
//		soundsMap.put(1, sp.load(context, R.raw.alarm_sound, 0));
        
        
        int base[] = {0, 1000*20};
        
        SharedPreferences shp = Utilities.getSP(context, Utilities.SP_SURVEY);
        String action = intent.getAction();
        String surveyName = intent.getStringExtra(Utilities.SV_NAME);
        String triggerSeq = Utilities.SP_KEY_TRIGGER_SEQ_MAP.get(surveyName);
        int triggerMax = Utilities.MAX_TRIGGER_MAP.get(surveyName);
        
        
/*      schedule survey*/
        if(action.equals(Utilities.BD_ACTION_SCHEDULE_ALL)){
        	
        	
        }
        
/*      schedule survey*/
//        else if(action.equals(Utilities.BD_ACTION_SCHEDULE_MORNING)){
        else if(action.equals(Utilities.BD_SCHEDULE_MAP.get(surveyName))){
    		Log.d("#####################################", ""+surveyName+" "+Utilities.getTimeFromLong(Calendar.getInstance().getTimeInMillis())
    				+" "+Utilities.getTimeFromLong(Utilities.getSP(context, Utilities.SP_BED_TIME).getLong(Utilities.SP_KEY_BED_TIME_LONG, -1)));

        	
        	Intent itTrigger = new Intent(Utilities.BD_TRIGGER_MAP.get(surveyName));
    		itTrigger.putExtra(Utilities.SV_NAME, surveyName);
    		PendingIntent piTrigger = PendingIntent.getBroadcast(context, 0, itTrigger, Intent.FLAG_ACTIVITY_NEW_TASK);
    		
    		//default time to 12:00 at noon
			Calendar c = Calendar.getInstance();
			if(c.get(Calendar.HOUR_OF_DAY) > 3){
				//next day
				c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR)+1);
			}
			c.set(Calendar.HOUR_OF_DAY, 12);
			c.set(Calendar.MINUTE, 0);
        	long defTime = c.getTimeInMillis(); 
        	long time = Long.MAX_VALUE;

        	//for morning survey
        	if(surveyName.equals(Utilities.SV_NAME_MORNING)){
        		time = Utilities.getSP(context, Utilities.SP_BED_TIME).getLong(Utilities.SP_KEY_BED_TIME_LONG, defTime);
        		Log.d("################################morning", "time is "+Utilities.getTimeFromLong(time));
        	}
        	
    		//for random survey
        	else if(surveyName.equals(Utilities.SV_NAME_RANDOM)){
        		time = Long.parseLong(Utilities.getSP(context, Utilities.SP_RANDOM_TIME).getString(Utilities.SP_KEY_RANDOM_TIME_SET, ""+time).split(",")[shp.getInt(triggerSeq, 0)]);
        		Log.d("################################", "time is "+Utilities.getTimeFromLong(time));
        	}
        	
    		//for followup survey
        	else{
        		time = Calendar.getInstance().getTimeInMillis()+Utilities.FOLLOWUP_IN_SECONDS*1000;
        	}
    		
        	//first place to set a schedule
        	am.set(AlarmManager.RTC_WAKEUP, time, piTrigger);
        	
        	
        }

/*		trigger survey*/
        else if(action.equals(Utilities.BD_TRIGGER_MAP.get(surveyName))){
        	Log.d("*****************************", ""+shp.getInt(triggerSeq, -1));
        	
        	//handle schedule
        	Intent itSchedule = new Intent(Utilities.BD_TRIGGER_MAP.get(surveyName));
        	itSchedule.putExtra(Utilities.SV_NAME, surveyName);
        	PendingIntent piSchedule = PendingIntent.getBroadcast(context, 0, itSchedule, Intent.FLAG_ACTIVITY_NEW_TASK);
        	
        	int tri = shp.getInt(triggerSeq, 0);
        	shp.edit().putInt(triggerSeq, ++tri).commit();
        	if(tri < triggerMax){
        		Log.d("*****************************", "<"+triggerMax);
        		
        		long time = Long.MAX_VALUE;
        		//for random survey
        		if(surveyName.equals(Utilities.SV_NAME_RANDOM)){
        			time = Long.parseLong(Utilities.getSP(context, Utilities.SP_RANDOM_TIME).getString(Utilities.SP_KEY_RANDOM_TIME_SET, ""+time).split(",")[tri]);
        			Log.d("################################", "time is "+Utilities.getTimeFromLong(time));
        		}
        		
        		//for followup survey
        		else if(surveyName.equals(Utilities.SV_NAME_FOLLOWUP)){
        			time = Calendar.getInstance().getTimeInMillis()+Utilities.FOLLOWUP_IN_SECONDS*1000; 
        		}
            	
            	//set next trigger based on different type of survey
            	am.set(AlarmManager.RTC_WAKEUP, time, piSchedule);
        	}else{
        		Log.d("*****************************", "else");
        		am.cancel(piSchedule);
        		shp.edit().putInt(triggerSeq, 0).commit();
        	}
        	
        	
        	//handle reminder
        	Intent itReminder = new Intent(Utilities.BD_ACTION_REMINDER_SURVEY);
        	itReminder.putExtra(Utilities.SV_NAME, surveyName);
			PendingIntent piReminder = PendingIntent.getBroadcast(context, 0, itReminder, Intent.FLAG_ACTIVITY_NEW_TASK);
			
			long ti = SystemClock.elapsedRealtime();
			
			am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, ti, Utilities.REMINDER_IN_SECONDS*1000 ,piReminder);
			
		}
		
		
		
		
/*		reminder survey*/
		else if(action.equals(Utilities.BD_ACTION_REMINDER_SURVEY)){
			Log.d("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^", ""+shp.getInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, 0));
			
			Intent launchSurvey = new Intent(context, XMLSurveyActivity.class);
			launchSurvey.putExtra(Utilities.SV_FILE, Utilities.SV_MAP.get(surveyName));
			launchSurvey.putExtra(Utilities.SV_NAME, surveyName);
			launchSurvey.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			launchSurvey.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			
			if(shp.getInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, 0) < Utilities.MAX_REMINDER && !shp.getBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false)){
				shp.edit().putInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, shp.getInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, 0)+1).commit();
				
				Log.d("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^","if 1");
				
//				Intent launchSurvey = new Intent(context, XMLSurveyActivity.class);
//				launchSurvey.putExtra("survey_file", "MorningReportParcel.xml");
//				launchSurvey.putExtra("survey_name", "MORNING_REPORT");
//				launchSurvey.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				launchSurvey.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				context.startActivity(launchSurvey);
			}
			else if(shp.getInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, 0) < Utilities.MAX_REMINDER && shp.getBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false)){
				Log.d("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^","if 2");
				
				shp.edit().putInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, Utilities.MAX_REMINDER).commit();
				
				Intent it = new Intent(Utilities.BD_ACTION_REMINDER_SURVEY);
				it.putExtra(Utilities.SV_NAME, surveyName);
				PendingIntent pi = PendingIntent.getBroadcast(context, 0, it, Intent.FLAG_ACTIVITY_NEW_TASK);
//				am.cancel(operation);
				
				long ti = SystemClock.elapsedRealtime() + Utilities.COMPLETE_SURVEY_IN_SECONDS*1000;
				am.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, ti, pi);
				
			}
			else
			{
				Log.d("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^","if 3");
//				Intent launchSurvey = new Intent(context, XMLSurveyActivity.class);
//				launchSurvey.putExtra("survey_file", "MorningReportParcel.xml");
//				launchSurvey.putExtra("survey_name", "MORNING_REPORT");
//				launchSurvey.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				launchSurvey.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				launchSurvey.putExtra(Utilities.SP_KEY_SURVEY_REMINDER_LAST, true);
				context.startActivity(launchSurvey);
				
				//startActivity should be first
				
				Intent it = new Intent(Utilities.BD_ACTION_REMINDER_SURVEY);
				it.putExtra(Utilities.SV_NAME, surveyName);
				PendingIntent pi = PendingIntent.getBroadcast(context, 0, it, Intent.FLAG_ACTIVITY_NEW_TASK);
				am.cancel(pi);
				
				shp.edit().putInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, 0).commit();
				shp.edit().putBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false).commit();
			}
			
			
		}
		
		else if(action.equals("sounds_alarm")){
			Log.d(TAG, "~~~~sound alarm");
			mp = MediaPlayer.create(context, R.raw.alarm_sound);
	    	mp.start();
			
		}

				
//		Intent it = new Intent(context, SurveyMenu.class);
//		it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		context.startActivity(it);
		
		wl.release();
	}

}
