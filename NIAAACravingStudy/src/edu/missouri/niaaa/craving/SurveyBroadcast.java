package edu.missouri.niaaa.craving;

import java.io.IOException;
import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;
import edu.missouri.niaaa.craving.location.LocationUtilities;
import edu.missouri.niaaa.craving.survey.XMLSurveyActivity;

public class SurveyBroadcast extends BroadcastReceiver {

	String TAG = "survey Broadcast";



	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Utilities.Log_sys(TAG, "broadcast on receive"+intent.getAction());
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//		WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "SurveyBroadcast");
        WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SurveyBroadcast");
        wl.acquire(1*60*1000);


        SharedPreferences shp = Utilities.getSP(context, Utilities.SP_SURVEY);
        String action = intent.getAction();
        String surveyName = intent.getStringExtra(Utilities.SV_NAME);
        String triggerSeq = Utilities.SP_KEY_TRIGGER_SEQ_MAP.get(surveyName);
        int triggerMax = Utilities.MAX_TRIGGER_MAP.get(surveyName);


        //restart gps
        if(!surveyName.equals(Utilities.SV_NAME_MORNING)){
        	if(Utilities.completedMorningToday(context) || Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 3){
        		context.sendBroadcast(new Intent(LocationUtilities.ACTION_START_LOCATION));
        	}
        }


/*      suspension*/
		if (action.equals(Utilities.BD_ACTION_SUSPENSION)) {
        	Utilities.LogB(TAG, "broadcast at suspension");

        	shp.edit().putBoolean(Utilities.SP_KEY_SURVEY_SUSPENSION, false).commit();

        	AudioManager audiom = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        	audiom.setStreamVolume(AudioManager.STREAM_MUSIC, Utilities.VOLUME, AudioManager.FLAG_PLAY_SOUND);

        	Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	        v.vibrate(500);
        }

/*      reschedule survey*/
        else if(action.equals(Utilities.BD_ACTION_SCHEDULE_ALL)){
        	Utilities.LogB(TAG, "boot upppppppppppppppp!");

        	Utilities.reScheduleMorningSurvey(context);//contains the following
//        	Utilities.reScheduleRandom(context);

        }

/*      schedule survey*/
        else if(action.equals(Utilities.BD_SCHEDULE_MAP.get(surveyName))){
    		Utilities.LogB("#####################################", ""+surveyName+" "+Utilities.getTimeFromLong(Calendar.getInstance().getTimeInMillis())
    				+" "+Utilities.getTimeFromLong(Utilities.getSP(context, Utilities.SP_BED_TIME).getLong(Utilities.SP_KEY_BED_TIME_LONG, -1)));


        	Intent itTrigger = new Intent(Utilities.BD_TRIGGER_MAP.get(surveyName));
    		itTrigger.putExtra(Utilities.SV_NAME, surveyName);
    		PendingIntent piTrigger = PendingIntent.getBroadcast(context, 0, itTrigger, Intent.FLAG_ACTIVITY_NEW_TASK);

    		//default time to 12:00 at noon
//			Calendar c = Utilities.getMorningCal(Utilities.defHour, Utilities.defMinute);
    		Calendar c = Utilities.getDefaultMorningCal(context);

        	long defTime = c.getTimeInMillis();

        	long time = Long.MAX_VALUE;

        	//for morning survey
        	if(surveyName.equals(Utilities.SV_NAME_MORNING)){
        		time = Utilities.getSP(context, Utilities.SP_BED_TIME).getLong(Utilities.SP_KEY_BED_TIME_LONG, defTime);
        		Utilities.LogB("################################morning", "time is "+Utilities.getTimeFromLong(time));
        	}

    		//for random survey
        	else if(surveyName.equals(Utilities.SV_NAME_RANDOM)){
//        		time = Calendar.getInstance().getTimeInMillis();
        		time = Long.parseLong(Utilities.getSP(context, Utilities.SP_RANDOM_TIME).getString(Utilities.SP_KEY_RANDOM_TIME_SET, ""+time).split(",")[shp.getInt(triggerSeq, 0)]);
        		Utilities.LogB("################################", "time is "+Utilities.getTimeFromLong(time)+" "+triggerSeq);
        	}

    		//for followup survey
        	else{
        		//followup setting time only works for schedule look-up
        		Utilities.getSP(context, Utilities.SP_RANDOM_TIME).edit().putLong(Utilities.SP_KEY_DRINKING_TIME_SET, Calendar.getInstance().getTimeInMillis()).commit();

        		time = Calendar.getInstance().getTimeInMillis()+Utilities.FOLLOWUP_IN_SECONDS*1000;

				Log.d("sa======================", "set true");
//				shp.edit().putBoolean(Utilities.SP_KEY_SURVEY_UNDERDRINKING, true).commit();
        	}

        	//cancel exist reminder alarms if any
        	if(intent.getBooleanExtra(Utilities.SP_KEY_SURVEY_REMINDER_CANCEL, false)){
        		Utilities.LogB("################################ cancel", "cancel reminders");

        		Intent itReminder = new Intent(Utilities.BD_REMINDER_MAP.get(surveyName));
            	itReminder.putExtra(Utilities.SV_NAME, surveyName);
    			PendingIntent piReminder = PendingIntent.getBroadcast(context, 0, itReminder, Intent.FLAG_ACTIVITY_NEW_TASK);

    			//set undergoing and send reminder broadcast // an other way
        		//shp.edit().putBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, true).commit();
    			//am.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), piReminder);

    			//set reminder seq to 0
    			shp.edit().putInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, 0).commit();
//				shp.edit().putBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false).commit();
//
//				shp.edit().putString(Utilities.SP_KEY_SURVEY_UNDERREMINDERING, "").commit();

    			am.cancel(piReminder);
        	}

        	//first place to set a schedule
        	am.setExact(AlarmManager.RTC_WAKEUP, time, piTrigger);

        }

/*		trigger survey*/
        else if(action.equals(Utilities.BD_TRIGGER_MAP.get(surveyName))){
        	Utilities.LogB("*****************************", ""+shp.getInt(triggerSeq, -1));

        	//handle schedule
        	Intent itSchedule = new Intent(Utilities.BD_TRIGGER_MAP.get(surveyName));
        	itSchedule.putExtra(Utilities.SV_NAME, surveyName);
        	PendingIntent piSchedule = PendingIntent.getBroadcast(context, 0, itSchedule, Intent.FLAG_ACTIVITY_NEW_TASK);

        	int tri = shp.getInt(triggerSeq, 0);
        	shp.edit().putInt(triggerSeq, ++tri).commit();
        	if(tri < triggerMax){
        		Utilities.LogB("*****************************", "<"+triggerMax);

        		long time = Long.MAX_VALUE;
        		//for random survey
        		if(surveyName.equals(Utilities.SV_NAME_RANDOM)){
        			time = Long.parseLong(Utilities.getSP(context, Utilities.SP_RANDOM_TIME).getString(Utilities.SP_KEY_RANDOM_TIME_SET, ""+time).split(",")[tri]);
        			Utilities.LogB("*****************************", "time is "+Utilities.getTimeFromLong(time));
        		}

        		//for followup survey
        		else if(surveyName.equals(Utilities.SV_NAME_FOLLOWUP)){
        			time = Calendar.getInstance().getTimeInMillis()+Utilities.FOLLOWUP_IN_SECONDS*1000;
        		}

            	//set next trigger based on different type of survey
            	am.setExact(AlarmManager.RTC_WAKEUP, time, piSchedule);
        	}else{
        		Utilities.LogB("*****************************", "else");
        		am.cancel(piSchedule);
        		shp.edit().putInt(triggerSeq, 0).commit();

				if (surveyName.equals(Utilities.SV_NAME_FOLLOWUP)) {
					Log.d("sa======================", "set false");
					shp.edit().putBoolean(Utilities.SP_KEY_SURVEY_UNDERDRINKING, false).commit();
				}

        	}


        	//handle reminder
        	Intent itReminder = new Intent(Utilities.BD_REMINDER_MAP.get(surveyName));
        	itReminder.putExtra(Utilities.SV_NAME, surveyName);
			PendingIntent piReminder = PendingIntent.getBroadcast(context, 0, itReminder, Intent.FLAG_ACTIVITY_NEW_TASK);

			//bypass if under_remindering
			if(shp.getString(Utilities.SP_KEY_SURVEY_UNDERREMINDERING, "").equals("")){
				shp.edit().putString(Utilities.SP_KEY_SURVEY_UNDERREMINDERING, surveyName).commit();
			}

			Utilities.LogB("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!2", surveyName + " " + shp.getString(Utilities.SP_KEY_SURVEY_UNDERREMINDERING, "") + " " + shp.getBoolean(Utilities.SP_KEY_SURVEY_UNDERDRINKING, false));
			if (shp.getString(Utilities.SP_KEY_SURVEY_UNDERREMINDERING, "").equals(surveyName) &&
					!(surveyName.equals(Utilities.SV_NAME_RANDOM) && shp.getBoolean(Utilities.SP_KEY_SURVEY_UNDERDRINKING, false))) {
				am.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() ,piReminder);
			}else{
				Utilities.LogB("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!", surveyName + " " + shp.getString(Utilities.SP_KEY_SURVEY_UNDERREMINDERING, ""));
				Toast.makeText(context, surveyName+" has been skipped under current survey you are doing!", Toast.LENGTH_LONG).show();
				shp.edit().putString(Utilities.SP_KEY_SURVEY_UNDERREMINDERING, "").commit();

				try {
					// for under doing some TRIGGERED survey, the new one will be skipped
					// Random
					// Drinking follow-ups

					String seq = "";
					int s = shp.getInt(triggerSeq, 0) != 0 ? shp.getInt(triggerSeq, 0) : Utilities.MAX_TRIGGER_MAP.get(surveyName);
					if(surveyName.equals(Utilities.SV_NAME_RANDOM)){
						seq = "," + s;
					}

					Utilities.writeEventToFile(context, (surveyName.equals(Utilities.SV_NAME_RANDOM) ? Utilities.CODE_SKIP_BLOCK_SURVEY_RANDOM : Utilities.CODE_SKIP_BLOCK_SURVEY_DRINKING),
							"", "", "", "",
							"", Utilities.sdf.format(Calendar.getInstance().getTime()) + seq);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}


/*		reminder survey*/
		else if(action.equals(Utilities.BD_REMINDER_MAP.get(surveyName))){
			Utilities.LogB("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^", ""+shp.getInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, 0)+" "+shp.getBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false));

			Intent launchSurvey = new Intent(context, XMLSurveyActivity.class);
//			launchSurvey.putExtra(Utilities.SV_FILE, Utilities.SV_MAP.get(surveyName));
			launchSurvey.putExtra(Utilities.SV_NAME, surveyName);
			launchSurvey.putExtra(Utilities.SV_AUTO_TRIGGERED, true);
			launchSurvey.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			launchSurvey.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

			//under reminder counting
			if(shp.getInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, 0) < Utilities.MAX_REMINDER && !shp.getBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false)){// <max, false
				Utilities.LogB("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^","if 1");

				//reminder req +1
				shp.edit().putInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, shp.getInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, 0)+1).commit();

				//set next reminder
	        	Intent itReminder = new Intent(Utilities.BD_REMINDER_MAP.get(surveyName));
	        	itReminder.putExtra(Utilities.SV_NAME, surveyName);
				PendingIntent piReminder = PendingIntent.getBroadcast(context, 0, itReminder, Intent.FLAG_ACTIVITY_NEW_TASK);

				am.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis()+Utilities.REMINDER_IN_SECONDS*1000 ,piReminder);


//				Intent launchSurvey = new Intent(context, XMLSurveyActivity.class);
//				launchSurvey.putExtra("survey_file", "MorningReportParcel.xml");
//				launchSurvey.putExtra("survey_name", "MORNING_REPORT");
//				launchSurvey.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				launchSurvey.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

//				if(!shp.getBoolean(Utilities.SP_KEY_SURVEY_SUSPENSION, false) || !surveyName.equals(Utilities.SV_NAME_RANDOM)){
				if((!shp.getBoolean(Utilities.SP_KEY_SURVEY_SUSPENSION, false) || !surveyName.equals(Utilities.SV_NAME_RANDOM)) && !shp.getBoolean("undermangoing", false)){
					context.startActivity(launchSurvey);
					Log.d("XXXXXXXXXXXXXXXX", "start activity");
				}
				else if(shp.getBoolean(Utilities.SP_KEY_SURVEY_SUSPENSION, false)){
					Toast.makeText(context, surveyName+" has been skipped under suspension!", Toast.LENGTH_LONG).show();
					Log.d("XXXXXXXXXXXXXXXX", "under suspension " + surveyName + " " + shp.getInt(triggerSeq, 0));

					try {
						// since suspension doesn't skip drinking follow-ups and morning, this is only for random

						String seq = "";
						int s = shp.getInt(triggerSeq, 0) != 0 ? shp.getInt(triggerSeq, 0) : Utilities.MAX_TRIGGER_MAP.get(surveyName);
						if (surveyName.equals(Utilities.SV_NAME_RANDOM)) {
							seq = "," + s;
						}
						Utilities.writeEventToFile(context, Utilities.CODE_SKIP_BLOCK_SURVEY_RANDOM,
								"", "", "",	"",
								"", Utilities.sdf.format(Calendar.getInstance().getTime()) + seq);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			//survey under going, cancel the following reminder
			else if(shp.getInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, 0) <= Utilities.MAX_REMINDER && shp.getBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false)){// <=max, true
				Utilities.LogB("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^","if 2");

				shp.edit().putInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, Utilities.MAX_REMINDER+1).commit();

				Intent it = new Intent(Utilities.BD_REMINDER_MAP.get(surveyName));
				it.putExtra(Utilities.SV_NAME, surveyName);
				PendingIntent pi = PendingIntent.getBroadcast(context, 0, it, Intent.FLAG_ACTIVITY_NEW_TASK);
//				am.cancel(operation);

				long ti = Calendar.getInstance().getTimeInMillis() + Utilities.COMPLETE_SURVEY_IN_SECONDS*1000;
				am.setExact(AlarmManager.RTC_WAKEUP, ti, pi);

			}

			//reset by xml ondestroy
			else if(shp.getInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, 0) == Utilities.MAX_REMINDER+2){// ==max+2
				//startActivity should be first

				Intent it = new Intent(Utilities.BD_REMINDER_MAP.get(surveyName));
				it.putExtra(Utilities.SV_NAME, surveyName);
				PendingIntent pi = PendingIntent.getBroadcast(context, 0, it, Intent.FLAG_ACTIVITY_NEW_TASK);
				am.cancel(pi);

				shp.edit().putInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, 0).commit();
				shp.edit().putBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false).commit();

				shp.edit().putString(Utilities.SP_KEY_SURVEY_UNDERREMINDERING, "").commit();
			}

			//reminder enough times or survey under going, terminate current unfinished survey, cancel reminder if any
			else
			{
				Utilities.LogB("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^","if 3");
//				Intent launchSurvey = new Intent(context, XMLSurveyActivity.class);
//				launchSurvey.putExtra("survey_file", "MorningReportParcel.xml");
//				launchSurvey.putExtra("survey_name", "MORNING_REPORT");
//				launchSurvey.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				launchSurvey.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

//				if(!(shp.getInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, Utilities.MAX_REMINDER+1) == Utilities.MAX_REMINDER+1 && !shp.getBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false))){

				launchSurvey.putExtra(Utilities.SV_REMINDER_LAST, true);
				context.startActivity(launchSurvey);

//				}

				//startActivity should be first

				Intent it = new Intent(Utilities.BD_REMINDER_MAP.get(surveyName));
				it.putExtra(Utilities.SV_NAME, surveyName);
				PendingIntent pi = PendingIntent.getBroadcast(context, 0, it, Intent.FLAG_ACTIVITY_NEW_TASK);
				am.cancel(pi);

				shp.edit().putInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, 0).commit();
				shp.edit().putBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false).commit();

				shp.edit().putString(Utilities.SP_KEY_SURVEY_UNDERREMINDERING, "").commit();

			}


		}

		else{

		}


		wl.release();
	}

}
