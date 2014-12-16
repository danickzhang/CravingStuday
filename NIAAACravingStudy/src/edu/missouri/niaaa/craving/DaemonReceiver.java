package edu.missouri.niaaa.craving;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import edu.missouri.niaaa.craving.location.LocationUtilities;
import edu.missouri.niaaa.craving.sensor.SensorUtilities;

public class DaemonReceiver extends BroadcastReceiver {

	final static String TAG = "Daemon Receiver";
	long tolerace = 60*1000;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Utilities.Log(TAG, "on receiver daemon");

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		int fun = intent.getIntExtra(Utilities.BD_ACTION_DAEMON_FUNC, 0);

		if(fun == 0){//set alarm
			Utilities.Log(TAG, "on receiver daemon 0");
			//Noon
			Intent itTrigger1 = new Intent(Utilities.BD_ACTION_DAEMON);
			itTrigger1.putExtra(Utilities.BD_ACTION_DAEMON_FUNC, 1);//int
			PendingIntent piTrigger1 = PendingIntent.getBroadcast(context, 1, itTrigger1, Intent.FLAG_ACTIVITY_NEW_TASK);

			am.setExact(AlarmManager.RTC_WAKEUP, getProperTime(12, 20), piTrigger1);

			//Midnight
			Intent itTrigger2 = new Intent(Utilities.BD_ACTION_DAEMON);
			itTrigger2.putExtra(Utilities.BD_ACTION_DAEMON_FUNC, 2);//int
			PendingIntent piTrigger2 = PendingIntent.getBroadcast(context, 2, itTrigger2, Intent.FLAG_ACTIVITY_NEW_TASK);

			am.setExact(AlarmManager.RTC_WAKEUP, getProperTime(23, 59), piTrigger2);

			//Three oclock
			Intent itTrigger3 = new Intent(Utilities.BD_ACTION_DAEMON);
			itTrigger3.putExtra(Utilities.BD_ACTION_DAEMON_FUNC, 3);//int
			PendingIntent piTrigger3 = PendingIntent.getBroadcast(context, 3, itTrigger3, Intent.FLAG_ACTIVITY_NEW_TASK);

			am.setExact(AlarmManager.RTC_WAKEUP, getProperTime(3, 0), piTrigger3);


		}
		else if(fun == 1){//Noon
			Utilities.Log(TAG, "on receiver daemon 1");

    		//today at noon
			Utilities.morningComplete(context, true);

			Toast.makeText(context, "Noon daemon trigger random popups for you.", Toast.LENGTH_LONG).show();

			//Noon
			Intent itTrigger1 = new Intent(Utilities.BD_ACTION_DAEMON);
			itTrigger1.putExtra(Utilities.BD_ACTION_DAEMON_FUNC, 1);//int
			PendingIntent piTrigger1 = PendingIntent.getBroadcast(context, 1, itTrigger1, Intent.FLAG_ACTIVITY_NEW_TASK);

			am.setExact(AlarmManager.RTC_WAKEUP, getProperTime(12, 20), piTrigger1);
		}
		else if(fun == -1){//cancel noon
			Utilities.Log(TAG, "on receiver daemon -1");

			Intent itTrigger1 = new Intent(Utilities.BD_ACTION_DAEMON);
			itTrigger1.putExtra(Utilities.BD_ACTION_DAEMON_FUNC, 1);//int
			PendingIntent piTrigger1 = PendingIntent.getBroadcast(context, 1, itTrigger1, Intent.FLAG_ACTIVITY_NEW_TASK);

			am.setExact(AlarmManager.RTC_WAKEUP, getProperTime(12, 20)+Utilities.getDayLong(), piTrigger1);
		}
		else if(fun == 2){//Midnight
			Utilities.Log(TAG, "on receiver daemon 2");

			//craving close sensor
			Intent i = new Intent(SensorUtilities.ACTION_DISCONNECT_SENSOR);
			context.sendBroadcast(i);

			//cancel all survey (follow-ups are allowed base on new requirement)
			Utilities.cancelSchedule(context);

			//reset sp
//			Utilities.getSP(context, Utilities.SP_RANDOM_TIME).edit().clear().commit();
//			Utilities.getSP(context, Utilities.SP_SURVEY).edit().clear().commit();


			Toast.makeText(context, "MIND close sensor and cancel all the survey", Toast.LENGTH_LONG).show();

			//Midnight
			Intent itTrigger2 = new Intent(Utilities.BD_ACTION_DAEMON);
			itTrigger2.putExtra(Utilities.BD_ACTION_DAEMON_FUNC, 2);//int
			PendingIntent piTrigger2 = PendingIntent.getBroadcast(context, 2, itTrigger2, Intent.FLAG_ACTIVITY_NEW_TASK);

			am.setExact(AlarmManager.RTC_WAKEUP, getProperTime(23, 59), piTrigger2);
		}
		else if(fun == 3){//three oclock
			Utilities.Log(TAG, "on receiver daemon 3");

			//close location
			context.sendBroadcast(new Intent(LocationUtilities.ACTION_STOP_LOCATION));

			//next day at 3
			//default
			Calendar d = Utilities.getDefaultMorningCal(context);
			long defTime = d.getTimeInMillis();
			int hour = d.get(Calendar.HOUR_OF_DAY);
			int minute = d.get(Calendar.MINUTE);

			//current
			Calendar c = Calendar.getInstance();

			//morning
			Calendar m = Calendar.getInstance();
			m.setTimeInMillis(Utilities.getSP(context, Utilities.SP_BED_TIME).getLong(Utilities.SP_KEY_BED_TIME_LONG, -1));

			if(c.before(m)){
				//set m as morning
				hour = m.get(Calendar.HOUR_OF_DAY);
				minute = m.get(Calendar.MINUTE);
			}

			Utilities.bedtimeComplete(context, hour, minute);

			//cancel followup
			Utilities.cancelTrigger(context);

			//reset sp
//			Utilities.getSP(context, Utilities.SP_RANDOM_TIME).edit().clear().commit();
//			Utilities.getSP(context, Utilities.SP_SURVEY).edit().clear().commit();

			Toast.makeText(context, "THRE cancel survey for you", Toast.LENGTH_LONG).show();

			//Three oclock
			Intent itTrigger3 = new Intent(Utilities.BD_ACTION_DAEMON);
			itTrigger3.putExtra(Utilities.BD_ACTION_DAEMON_FUNC, 3);//int
			PendingIntent piTrigger3 = PendingIntent.getBroadcast(context, 3, itTrigger3, Intent.FLAG_ACTIVITY_NEW_TASK);

			am.setExact(AlarmManager.RTC_WAKEUP, getProperTime(3, 0), piTrigger3);

			//reset all, send 0 broadcast??


		}
		else if(fun == -3){//cancel three oclock //useless for now
			Utilities.Log(TAG, "on receiver daemon -3");

//			Intent itTrigger3 = new Intent(Utilities.BD_ACTION_DAEMON);
//			itTrigger3.putExtra(Utilities.BD_ACTION_DAEMON_FUNC, 3);//int
//			PendingIntent piTrigger3 = PendingIntent.getBroadcast(context, 3, itTrigger3, Intent.FLAG_ACTIVITY_NEW_TASK);
//
//			am.setExact(AlarmManager.RTC_WAKEUP, getProperTime(3, 0)+Utilities.getDayLong(), piTrigger3);
		}
		else{

		}

	}

	private long getProperTime(int hour, int minute){
		Calendar c = Calendar.getInstance();
		Calendar s = Calendar.getInstance();
		s.set(Calendar.HOUR_OF_DAY, hour);
		s.set(Calendar.MINUTE, minute);
		s.set(Calendar.SECOND, 0);
		s.set(Calendar.MILLISECOND, 0);
		if(c.after(s)){
			return s.getTimeInMillis() + Utilities.getDayLong();
		}
		return s.getTimeInMillis();
	}

}
