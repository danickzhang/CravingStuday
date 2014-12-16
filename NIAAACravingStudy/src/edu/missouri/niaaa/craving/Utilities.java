package edu.missouri.niaaa.craving;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.DetectedActivity;

import edu.missouri.niaaa.craving.location.ActivityRecognitionService;
import edu.missouri.niaaa.craving.location.LocationBroadcast;
import edu.missouri.niaaa.craving.location.LocationUtilities;

public class Utilities {

/*	survey type*/
	public final static String SV_FILE = "survey_file";
	public final static String SV_NAME = "survey_name";
	public final static String SV_REMINDER_LAST = "SURVEY_REMINDER_LAST";
	public final static String SV_AUTO_TRIGGERED = "SURVEY_AUTO_TRIGGERED";

	public final static String SV_FILE_MORNING = "MorningReportParcel.xml";
	public final static String SV_NAME_MORNING = "MORNING_REPORT";

	public final static String SV_FILE_DRINKING = "InitialDrinkingParcel.xml";
	public final static String SV_NAME_DRINKING = "INITIAL_DRINKING";

	public final static String SV_FILE_MOOD = "MoodDysregulationParcel.xml";
	public final static String SV_NAME_MOOD = "MOOD_DYSREGULATION";

	public final static String SV_FILE_CRAVING = "CravingEpisodeParcel.xml";
	public final static String SV_NAME_CRAVING = "CRAVING_EPISODE";

	public final static String SV_FILE_RANDOM = "RandomAssessmentParcel.xml";
	public final static String SV_NAME_RANDOM = "RANDOM_ASSESSMENT";

	public final static String SV_FILE_FOLLOWUP = "DrinkingFollowupParcel.xml";
	public final static String SV_NAME_FOLLOWUP = "DRINKING_FOLLOWUP";

	public final static HashMap<String, String> SV_MAP = new HashMap<String, String>(){
		{
			put(SV_NAME_MORNING, SV_FILE_MORNING);
			put(SV_NAME_DRINKING, SV_FILE_DRINKING);
			put(SV_NAME_MOOD, SV_FILE_MOOD);
			put(SV_NAME_CRAVING, SV_FILE_CRAVING);
			put(SV_NAME_RANDOM, SV_FILE_RANDOM);
			put(SV_NAME_FOLLOWUP, SV_FILE_FOLLOWUP);
		}
	};

/*	survey config*/
	public final static int MAX_REMINDER = 3;
	public final static int MAX_TRIGGER_MORNING = 1;//1
	public final static int MAX_TRIGGER_RANDOM = 6;//6
	public final static int MAX_TRIGGER_FOLLOWUP = 3;//3
	public final static int VOLUME = 8;//10
	public final static String PHONE_BASE_PATH = "sdcard/TestResult_craving/";
	public final static int CODE_NAME_MORNING = 1;
	public final static int CODE_NAME_DRINKING = 2;
	public final static int CODE_NAME_MOOD = 3;
	public final static int CODE_NAME_CRAVING = 4;
	public final static int CODE_NAME_RANDOM = 5;
	public final static int CODE_NAME_FOLLOW = 6;
	public final static int CODE_SUSPENSION = 7;
	public final static int CODE_BEDTIME = 8;
	public final static int CODE_SENSOR_CONN = 9;
	public final static int CODE_SCHEDULE_MANUALLY = 10;
	public final static int CODE_SCHEDULE_AUTOMATIC = 11;
	public final static int CODE_SKIP_BLOCK_SURVEY_RANDOM = 12;
	public final static int CODE_SKIP_BLOCK_SURVEY_DRINKING = 13;
	public final static boolean RELEASE = false;


	public final static HashMap<String, Integer> MAX_TRIGGER_MAP = new HashMap<String, Integer>(){
		{
			put(SV_NAME_MORNING, MAX_TRIGGER_MORNING);
			put(SV_NAME_RANDOM, MAX_TRIGGER_RANDOM);
			put(SV_NAME_FOLLOWUP, MAX_TRIGGER_FOLLOWUP);
			put(SV_NAME_DRINKING, 	0);// these three for manually
			put(SV_NAME_MOOD, 		0);
			put(SV_NAME_CRAVING, 	0);
		}
	};

	public final static int REMINDER_IN_SECONDS = 5*60;
	public final static int COMPLETE_SURVEY_IN_SECONDS = 7*60;
	public final static int FOLLOWUP_IN_SECONDS = 30*60;
	public final static int SUSPENSION_INTERVAL_IN_SECOND = 15*60;
	public final static String TIME_NONE = "none";
	public final static int defHour = 12;
	public final static int defMinute = 0;
	public final static int PREFIX_LEN = 35;

	public final static String[] SUSPENSION_DISPLAY = {"  15 minutes  ","  30 minutes  ","  45 minutes  ","  60 minutes  ",
		"  1 hour & 15 minutes  ","  1 & half hour  ","  1 hour & 45 minutes  ","  2 hours  "};


/*	shared preferences*/
	/*bed time info*/
	public final static String SP_BED_TIME = "edu.missouri.niaaa.craving.BEDTIME_INFO";

	public final static String SP_KEY_BED_TIME_HOUR = "BEDTIME_INFO_HOUR";
	public final static String SP_KEY_BED_TIME_MINUTE = "BEDTIME_INFO_MINUTE";
	public final static String SP_KEY_BED_TIME_LONG = "BEDTIME_INTO_LONG";
	public final static String SP_KEY_MORNING_COMPLETE_TIME = "MORNING_COMPLETE_TIME";

	/*random time set*/
	public final static String SP_RANDOM_TIME = "edu.missouri.niaaa.craving.RANDOM_TIME_INFO";

	public final static String SP_KEY_RANDOM_TIME_SET = "RANDOM_TIME_SET";
	public final static String SP_KEY_DRINKING_TIME_SET = "DRINKING_TIME_SET";

	/*survey*/
	public final static String SP_SURVEY = "edu.missouri.niaaa.craving.SURVEY";

	public final static String SP_KEY_SURVEY_REMINDER_SEQ = "SURVEY_REMINDER_SEQ";
	public final static String SP_KEY_SURVEY_REMINDER_CANCEL = "SURVEY_REMINDER_CANCEL";
	public final static String SP_KEY_SURVEY_UNDERGOING = "SURVEY_UNDERGOING";
	public final static String SP_KEY_SURVEY_UNDERREMINDERING = "SURVEY_UNDER_REMINDERING";
	public final static String SP_KEY_SURVEY_SUSPENSION = "SURVEY_SUSPENSION";
	public final static String SP_KEY_SURVEY_UNDERDRINKING = "SURVAY_UNDERDRINKING";

	public final static String SP_KEY_SURVEY_UNDERREMINDERING_MORNING = "SURVEY_UNDER_REMINDERING_MORNING";
	public final static String SP_KEY_SURVEY_UNDERREMINDERING_RANDOM = "SURVEY_UNDER_REMINDERING_RANDOM";
	public final static String SP_KEY_SURVEY_UNDERREMINDERING_FOLLOWUP = "SURVEY_UNDER_REMINDERING_FOLLOWUP";

	public final static HashMap<String, String> SP_KEY_UNDERREMINDERING_MAP = new HashMap<String, String>(){
		{
			put(SV_NAME_MORNING, SP_KEY_SURVEY_UNDERREMINDERING_MORNING);
			put(SV_NAME_RANDOM, SP_KEY_SURVEY_UNDERREMINDERING_RANDOM);
			put(SV_NAME_FOLLOWUP, SP_KEY_SURVEY_UNDERREMINDERING_FOLLOWUP);
		}
	};

	public final static String SP_KEY_SURVEY_TRIGGER_SEQ_MORNING = "SURVEY_TRIGGER_SEQ_MORNING";
	public final static String SP_KEY_SURVEY_TRIGGER_SEQ_RANDOM = "SURVEY_TRIGGER_SEQ_RANDOM";
	public final static String SP_KEY_SURVEY_TRIGGER_SEQ_FOLLOWUP = "SURVEY_TRIGGER_SEQ_FOLLOWUP";

	public final static HashMap<String, String> SP_KEY_TRIGGER_SEQ_MAP = new HashMap<String, String>(){
		{
			put(SV_NAME_MORNING, SP_KEY_SURVEY_TRIGGER_SEQ_MORNING);
			put(SV_NAME_RANDOM, SP_KEY_SURVEY_TRIGGER_SEQ_RANDOM);
			put(SV_NAME_FOLLOWUP, SP_KEY_SURVEY_TRIGGER_SEQ_FOLLOWUP);
		}
	};


	/*user login info*/
	public final static String SP_LOGIN = "edu.missouri.niaaa.craving.LOGIN";

	public final static String SP_KEY_LOGIN_STUDY_STARTTIME = "STUDY_START_DAY";
	public final static String SP_KEY_LOGIN_USERID = "USER_ID";
	public final static String SP_KEY_LOGIN_USERPWD = "USER_PWD";

	public final static String SP_KEY_SUSPENSION_TS = "SUSPENSION_TS";
	public final static String SP_KEY_SUSPENSION_CHOICE = "SUSPENSION_CHOICE";
	public final static String SP_KEY_SENSOR_CONN_TS = "SENSOR_CONN_TS";

	/*reminder into*/
	public final static String SP_REMINDER_INFO = "edu.missouri.niaaa.craving.REMINDER_INFO";

	public final static String SP_KEY_REMINDER_INFO_1 = "REMINDER_INFO_1";
	public final static String SP_KEY_REMINDER_INFO_2 = "REMINDER_INFO_2";
	public final static String SP_KEY_REMINDER_INFO_3 = "REMINDER_INFO_3";


/*	broadcast*/
	public final static String BD_ACTION_SCHEDULE_ALL = "edu.missouri.niaaa.craving.ACTION_SCHEDULE_ALL";
//	public final static String BD_ACTION_REMINDER_SURVEY = "edu.missouri.niaaa.craving.REMINDER";
	public final static String BD_ACTION_SUSPENSION = "edu.missouri.niaaa.craving.SUSPENSION";
//	public final static String BD_ACTION_DAEMON_NOON = "edu.missouri.niaaa.craving.DAEMON_NOON";
//	public final static String BD_ACTION_DAEMON_MIDN = "edu.missouri.niaaa.craving.DAEMON_MIDNIGHT";
//	public final static String BD_ACTION_DAEMON_THRE = "edu.missouri.niaaa.craving.DAEMON_THREEOCLOCK";
	public final static String BD_ACTION_DAEMON = "edu.missouri.niaaa.craving.DAEMON";
	public final static String BD_ACTION_DAEMON_FUNC = "DAEMON_FUNCTION";

	public final static String BD_ACTION_SCHEDULE_MORNING = "edu.missouri.niaaa.craving.SCHEDULE_MORNING";
	public final static String BD_ACTION_TRIGGER_MORNING = "edu.missouri.niaaa.craving.TRIGGER_MORNING";
	public final static String BD_ACTION_REMINDER_MORNING = "edu.missouri.niaaa.craving.REMINDER_MORNING";

	public final static String BD_ACTION_SCHEDULE_RANDOM = "edu.missouri.niaaa.craving.SCHEDULE_RANDOM";
	public final static String BD_ACTION_TRIGGER_RANDOM = "edu.missouri.niaaa.craving.TRIGGER_RANDOM";
	public final static String BD_ACTION_REMINDER_RANDOM = "edu.missouri.niaaa.craving.REMINDER_RANDOM";

	public final static String BD_ACTION_SCHEDULE_FOLLOWUP = "edu.missouri.niaaa.craving.SCHEDUL_FOLLOWUP";
	public final static String BD_ACTION_TRIGGER_FOLLOWUP = "edu.missouri.niaaa.craving.TRIGGER_FOLLOWUP";
	public final static String BD_ACTION_REMINDER_FOLLOWUP = "edu.missouri.niaaa.craving.REMINDER_FOLLOWUP";

	public final static String BD_ACTION_REMINDER_DRINKING = "edu.missouri.niaaa.craving.REMINDER_DRINKING";
	public final static String BD_ACTION_REMINDER_MOOD = "edu.missouri.niaaa.craving.REMINDER_MOOD";
	public final static String BD_ACTION_REMINDER_CRAVING = "edu.missouri.niaaa.craving.REMINDER_CRAVING";

	public final static HashMap<String, String> BD_SCHEDULE_MAP = new HashMap<String, String>(){
		{
			put(SV_NAME_MORNING, BD_ACTION_SCHEDULE_MORNING);
			put(SV_NAME_RANDOM, BD_ACTION_SCHEDULE_RANDOM);
			put(SV_NAME_FOLLOWUP, BD_ACTION_SCHEDULE_FOLLOWUP);
		}
	};
	public final static HashMap<String, String> BD_TRIGGER_MAP = new HashMap<String, String>(){
		{
			put(SV_NAME_MORNING, BD_ACTION_TRIGGER_MORNING);
			put(SV_NAME_RANDOM, BD_ACTION_TRIGGER_RANDOM);
			put(SV_NAME_FOLLOWUP, BD_ACTION_TRIGGER_FOLLOWUP);
		}
	};
//	public final static HashMap<String, String> BD_REMINDER_MAP = new HashMap<String, String>(){
//		{
//			put(SV_NAME_MORNING, BD_ACTION_REMINDER_MORNING);
//			put(SV_NAME_RANDOM, BD_ACTION_REMINDER_RANDOM);
//			put(SV_NAME_FOLLOWUP, BD_ACTION_REMINDER_FOLLOWUP);
//		}
//	};

	public final static HashMap<String, String> BD_REMINDER_MAP = new HashMap<String, String>(){
		{
			put(SV_NAME_MORNING, 	BD_ACTION_REMINDER_MORNING);
			put(SV_NAME_RANDOM, 	BD_ACTION_REMINDER_RANDOM);
			put(SV_NAME_FOLLOWUP, 	BD_ACTION_REMINDER_FOLLOWUP);
			put(SV_NAME_DRINKING, 	BD_ACTION_REMINDER_DRINKING);// these three for manually
			put(SV_NAME_MOOD, 		BD_ACTION_REMINDER_MOOD);
			put(SV_NAME_CRAVING, 	BD_ACTION_REMINDER_CRAVING);
		}
	};


	/*	validate*/

/*	Craving Study*/
	public final static String VALIDATE_ADDRESS = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/Crt2/validateUserDec.php";
	public final static String WRITE_ARRAY_TO_FILE = 		"http://dslsrv8.cs.missouri.edu/~hw85f/Server/Crt2/writeArrayToFile.php";
	public final static String WRITE_ARRAY_TO_FILE_DEC = 	"http://dslsrv8.cs.missouri.edu/~hw85f/Server/Crt2/writeArrayToFileDec.php";
	public final static String COMPLIANCE_ADDRESS = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/Crt2/complianceDec.php";
	public final static String STUDY_DAY_MODIFY_ADDRESS = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/Crt2/changeStudyWeekDec.php";

/*	EMA-STL Study*/
//	public final static String VALIDATE_ADDRESS = 			"http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtEMA/validateUser.php";
//	public final static String WRITE_ARRAY_TO_FILE = 		"http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtEMA/writeArrayToFile.php";
//	public final static String WRITE_ARRAY_TO_FILE_DEC = 	"http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtEMA/writeArrayToFileDec.php";
//	public final static String COMPLIANCE_ADDRESS = 		"http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtEMA/compliance.php";
//	public final static String STUDY_DAY_MODIFY_ADDRESS = 	"http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtEMA/changeStudyWeek.php";

/*	NIMH Emotion Study*/
//	public final static String VALIDATE_ADDRESS = 			"http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtNIMH/validateUser.php";
//	public final static String WRITE_ARRAY_TO_FILE = 		"http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtNIMH/writeArrayToFile.php";
//	public final static String WRITE_ARRAY_TO_FILE_DEC = 	"http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtNIMH/writeArrayToFileDec.php";
//	public final static String COMPLIANCE_ADDRESS = 		"http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtNIMH/compliance.php";
//	public final static String STUDY_DAY_MODIFY_ADDRESS = 	"http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtNIMH/changeStudyWeek.php";

//	public final static String UPLOAD_ADDRESS = WRITE_ARRAY_TO_FILE;
	public final static String UPLOAD_ADDRESS = WRITE_ARRAY_TO_FILE_DEC;
	public final static SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
	public final static boolean WRITE_RAW = !RELEASE;


	static boolean debug_system = true;
	static boolean debug = true;
	static boolean debugB = true;


	//shared values
	public static PublicKey publicKey = null;

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//static Functions()                                                                                          //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static SharedPreferences getSP(Context context, String name){
		SharedPreferences shp = context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
		return shp;
	}


/*	schedule		*/
/*	re-schedule		*/
	/* startup */
	public static void scheduleAll(Context context){
		Intent startScheduler = new Intent(Utilities.BD_ACTION_SCHEDULE_ALL);
		startScheduler.putExtra(Utilities.SV_NAME, Utilities.SV_NAME_MORNING);//useless
		context.sendBroadcast(startScheduler);

		// deal with suspension after reboot
		SharedPreferences shp = Utilities.getSP(context, Utilities.SP_SURVEY);

		if (shp.getBoolean(Utilities.SP_KEY_SURVEY_SUSPENSION, false)) {

			// last suspension timestamp
			Calendar c = Calendar.getInstance();
			SharedPreferences sp = context.getSharedPreferences(Utilities.SP_LOGIN, Context.MODE_PRIVATE);
			long lastStartTime = sp.getLong(Utilities.SP_KEY_SUSPENSION_TS,	c.getTimeInMillis());
			int choice = sp.getInt(SP_KEY_SUSPENSION_CHOICE, 0);

			AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			Intent breakIntent = new Intent(Utilities.BD_ACTION_SUSPENSION);
			breakIntent.putExtra(Utilities.SV_NAME, Utilities.SV_NAME_RANDOM);// useless
			PendingIntent breakPi = PendingIntent.getBroadcast(context, 0, breakIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
			am.setExact(AlarmManager.RTC_WAKEUP, lastStartTime + choice * SUSPENSION_INTERVAL_IN_SECOND * 1000,	breakPi);

		}
	}

	public static void scheduleDaemon(Context context){
		Intent i = new Intent(Utilities.BD_ACTION_DAEMON);
		i.putExtra(Utilities.BD_ACTION_DAEMON_FUNC, 0);
		context.sendBroadcast(i);
	}


	/* random */
	public static void scheduleRandomSurvey(Context context, boolean startFromNoon, boolean autoTriggered) {

		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 23);//23
		c.set(Calendar.MINUTE, 59);//59

		long base = Calendar.getInstance().getTimeInMillis();
		if (startFromNoon) {
			Calendar b = Calendar.getInstance();
			b.set(Calendar.HOUR_OF_DAY, 12);
			b.set(Calendar.MINUTE, 0);
			b.set(Calendar.SECOND, 0);
			base = b.getTimeInMillis();
		}
		long peak = c.getTimeInMillis();

		long unit = (peak-base)/(MAX_TRIGGER_RANDOM+1);//7
		long r_unit = (peak-base)/((MAX_TRIGGER_RANDOM+1)*3);//21

		String random_schedule = new String();

		for(int i=1;i<MAX_TRIGGER_RANDOM+1;i++){//7
			random_schedule = random_schedule + (base+unit*i+(new Random().nextInt((int) (2*r_unit))-r_unit)+",");
		}


		//write to file random schedule
		//6
		String strArr[] = random_schedule.split(",");

		if(strArr.length != 1){
			int i =0;
			for(String str: strArr){
				Calendar c2 = Calendar.getInstance();
				c2.setTimeInMillis(Long.parseLong(str));
				Log.d("Random Schedule ", "each item is "+str+" "+c2.get(Calendar.HOUR_OF_DAY)+":"+c2.get(Calendar.MINUTE));

				strArr[i] = sdf.format(c2.getTime());
				i++;
			}

			try {
				//craving gonna be different
				writeEventToFile(context, (autoTriggered ? CODE_SCHEDULE_AUTOMATIC : CODE_SCHEDULE_MANUALLY),
						strArr[0], strArr[1], strArr[2], strArr[3], strArr[4], strArr[5]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		Utilities.getSP(context, Utilities.SP_RANDOM_TIME).edit().putString(Utilities.SP_KEY_RANDOM_TIME_SET, random_schedule).commit();

		Utilities.getSP(context, Utilities.SP_SURVEY).edit().putInt(Utilities.SP_KEY_SURVEY_TRIGGER_SEQ_RANDOM, 0).commit();

		Intent scheduleIntent = new Intent(Utilities.BD_ACTION_SCHEDULE_RANDOM);
		scheduleIntent.putExtra(Utilities.SV_NAME, Utilities.SV_NAME_RANDOM);
		if (!startFromNoon) {
			context.sendBroadcast(scheduleIntent);
		}
	}


	public static void triggerRandom(Context context, int seq){
		Utilities.getSP(context, Utilities.SP_SURVEY).edit().putInt(Utilities.SP_KEY_SURVEY_TRIGGER_SEQ_RANDOM, seq).commit();

		Intent scheduleIntent = new Intent(Utilities.BD_ACTION_SCHEDULE_RANDOM);
		scheduleIntent.putExtra(Utilities.SV_NAME, Utilities.SV_NAME_RANDOM);
		scheduleIntent.putExtra(SP_KEY_SURVEY_REMINDER_CANCEL, true);
		context.sendBroadcast(scheduleIntent);
	}


	public static void reScheduleRandom(Context context){
		Calendar c = Calendar.getInstance();

		String strRandom[] = getSP(context, SP_RANDOM_TIME).getString(SP_KEY_RANDOM_TIME_SET, "").split(",");
		if(strRandom.length != 1){
			int i = 0;
			for(String str: strRandom){
				Calendar s = Calendar.getInstance();
				s.setTimeInMillis(Long.parseLong(str));
				if(s.after(c)){
					triggerRandom(context, i);
					Log.d("~~~~~~~~~~~~~~~~~~~~~~~~~~~", "i is "+i);
					break;
				}
				i++;
			}
		}
	}


	/* drinking */
	public static void triggerDrinkingFollowup(Context context){

		Utilities.getSP(context, Utilities.SP_SURVEY).edit().putInt(Utilities.SP_KEY_SURVEY_TRIGGER_SEQ_FOLLOWUP, 0).commit();

		Intent scheduleIntent = new Intent(Utilities.BD_ACTION_SCHEDULE_FOLLOWUP);
		scheduleIntent.putExtra(Utilities.SV_NAME, Utilities.SV_NAME_FOLLOWUP);
		scheduleIntent.putExtra(SP_KEY_SURVEY_REMINDER_CANCEL, true);
		context.sendBroadcast(scheduleIntent);
	}


	/* morning */
	public static void scheduleMorningSurvey(Context context, int hour, int minute){
		Calendar c = getMorningCal(hour, minute);

		getSP(context, SP_BED_TIME).edit().putLong(SP_KEY_BED_TIME_LONG, c.getTimeInMillis()).commit();


		Intent scheduleIntent = new Intent(Utilities.BD_ACTION_SCHEDULE_MORNING);
		scheduleIntent.putExtra(Utilities.SV_NAME, Utilities.SV_NAME_MORNING);
		context.sendBroadcast(scheduleIntent);
	}

//	public static void bedtimeComplete(Context context){
//		scheduleMorningSurvey(context, 12, 20);
//	}

	public static void reScheduleMorningSurvey(Context context){

		//noon
		Calendar n = Calendar.getInstance();
		n.set(Calendar.HOUR_OF_DAY, 12);
		n.set(Calendar.MINUTE, 0);
		n.set(Calendar.SECOND, 0);

		//default time to 12:00 at noon
//		Calendar d = getMorningCal(defHour, defMinute);
		Calendar d = getDefaultMorningCal(context);

    	long defTime = d.getTimeInMillis();

    	//current
		Calendar c = Calendar.getInstance();

		//morning
		Calendar m = Calendar.getInstance();
		m.setTimeInMillis(getSP(context, SP_BED_TIME).getLong(SP_KEY_BED_TIME_LONG, defTime));

		//set morning & schedule random
		if(c.after(n)){
//			morningComplete(context);

			//write complete time
			getSP(context, SP_BED_TIME).edit().putLong(SP_KEY_MORNING_COMPLETE_TIME, Calendar.getInstance().getTimeInMillis()).commit();

			if (isRandomScheduled(context)) {
				//reschedule today's random
				reScheduleRandom(context);
				writeReboot();
			}
			else {
				//schedule random
				scheduleRandomSurvey(context, true, true);
				reScheduleRandom(context);
			}

			//instead, we do this as a new way:
//			scheduleRandomSurvey(context, true, true);
//			reScheduleRandom(context);

		}

		//before previous set morning time & before noon
		else if(c.before(m)){

			scheduleMorningSurvey(context, m.get(Calendar.HOUR_OF_DAY), m.get(Calendar.MINUTE));

//			Intent scheduleIntent = new Intent(Utilities.BD_ACTION_SCHEDULE_MORNING);
//			scheduleIntent.putExtra(Utilities.SV_NAME, Utilities.SV_NAME_MORNING);
//			context.sendBroadcast(scheduleIntent);
		}

		//after morning time but earlier than noon
		else{
			//do nothing
		}
	}


	private static void writeReboot() {
		// TODO Auto-generated method stub

	}

	private static boolean isRandomScheduled(Context context){
		//random
		String strRandom[] = getSP(context, SP_RANDOM_TIME).getString(SP_KEY_RANDOM_TIME_SET, "").split(",");
		if(strRandom.length != 1){
			Calendar c = Calendar.getInstance();
			Calendar r = Calendar.getInstance();
			r.setTimeInMillis(Long.parseLong(strRandom[0]));

			//if is today, return true
			if(c.get(Calendar.DAY_OF_YEAR) == r.get(Calendar.DAY_OF_YEAR)){
				return true;
			}else{
				return false;
			}
		}
		else{
			return false;
		}
	}

	public static boolean completedMorningToday(Context context){
		long time = getSP(context, SP_BED_TIME).getLong(SP_KEY_MORNING_COMPLETE_TIME, 0);
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		int day = c.get(Calendar.DAY_OF_YEAR);

		int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

		if(day == today) {
			return true;
		}
		return false;
	}


	public static void morningComplete(Context context, boolean autoTriggered) {
		// TODO Auto-generated method stub

		//write complete time
		getSP(context, SP_BED_TIME).edit().putLong(SP_KEY_MORNING_COMPLETE_TIME, Calendar.getInstance().getTimeInMillis()).commit();
		//update study day
		updateStudyDay();
		//schedule random survey
//		scheduleRandomSurvey(context);

		if(isRandomScheduled(context)){
			//reschedule today's random
			reScheduleRandom(context);
		}
		else{
			//schedule random
			scheduleRandomSurvey(context, false, autoTriggered);
		}

		//cancel existing if any
		cancelMorning(context);

//		//write to file random schedule
//		//6
//		String strRandom[] = getSP(context, SP_RANDOM_TIME).getString(SP_KEY_RANDOM_TIME_SET, "").split(",");
//
//		if(strRandom.length != 1){
//			int i = 0;
//			for(String s: strRandom){
//				Calendar tempCal = Calendar.getInstance();
//				tempCal.setTimeInMillis(Long.parseLong(s));
//				strRandom[i] = sdf.format(tempCal.getTime());
//				i++;
//			}
//
//			try {
//				//craving gonna be different
//				writeEventToFile(context, 10, strRandom[0], strRandom[1], strRandom[2], strRandom[3], strRandom[4], strRandom[5]);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		else{
//		}

		//start loction
		context.sendBroadcast(new Intent(LocationUtilities.ACTION_START_LOCATION));
	}

	private static void updateStudyDay() {
		// TODO Auto-generated method stub
		//useless
	}

	//work with after 12:00 without doing morning survey
//	public static void morningSet(Context context){
//		Calendar c = Calendar.getInstance();
//		c.set(Calendar.DAY_OF_YEAR, Calendar.getInstance().get(Calendar.DAY_OF_YEAR));
//		c.set(Calendar.HOUR_OF_DAY, 12);
//		c.set(Calendar.MINUTE, 20);//MAX_REMINDER*REMINDER_IN_SECONDS/60 + COMPLETE_SURVEY_IN_SECONDS/60);
//
//		getSP(context, SP_BED_TIME).edit().putLong(SP_KEY_MORNING_COMPLETE_TIME, c.getTimeInMillis()).commit();
//	}

	//work with after bedtime setting
	public static void morningReset(Context context){
		Calendar c = Calendar.getInstance();
//		c.setTimeInMillis(getSP(context, SP_BED_TIME).getLong(SP_KEY_MORNING_COMPLETE_TIME, c.getTimeInMillis()));
		c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR)-1);
		getSP(context, SP_BED_TIME).edit().putLong(SP_KEY_MORNING_COMPLETE_TIME, c.getTimeInMillis()).commit();
	}


	public static void bedtimeComplete(Context context, int hour, int minute){
		//set flag for bedtime, press-in survey should be blocked
		morningReset(context);

		//reset random and survey
		cancelSchedule(context);

		//schedule for next morning
		scheduleMorningSurvey(context, hour, minute);
	}


/*	canceler*/
	public static void cancelReminder(Context context){

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		SharedPreferences shp = getSP(context, SP_SURVEY);
		for(String surveyName: BD_REMINDER_MAP.keySet()){
			Intent itReminder = new Intent(Utilities.BD_REMINDER_MAP.get(surveyName));
	    	itReminder.putExtra(Utilities.SV_NAME, surveyName);
			PendingIntent piReminder = PendingIntent.getBroadcast(context, 0, itReminder, Intent.FLAG_ACTIVITY_NEW_TASK);

			//set undergoing and send reminder broadcast // an other way
			//shp.edit().putBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, true).commit();
			//am.set(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), piReminder);

			//set reminder seq to 0
			shp.edit().putInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, 0).commit();
			shp.edit().putBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false).commit();

			shp.edit().putString(Utilities.SP_KEY_SURVEY_UNDERREMINDERING, "").commit();
			am.cancel(piReminder);
		}
	}

	public static void cancelTrigger(Context context){
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		SharedPreferences shp = getSP(context, SP_SURVEY);
		for(String surveyName: BD_TRIGGER_MAP.keySet()){
			Intent itSchedule = new Intent(Utilities.BD_TRIGGER_MAP.get(surveyName));
	    	itSchedule.putExtra(Utilities.SV_NAME, surveyName);
	    	PendingIntent piSchedule = PendingIntent.getBroadcast(context, 0, itSchedule, Intent.FLAG_ACTIVITY_NEW_TASK);

	    	am.cancel(piSchedule);
	    	String triggerSeq = Utilities.SP_KEY_TRIGGER_SEQ_MAP.get(surveyName);
			shp.edit().putInt(triggerSeq, 0).commit();
		}

		getSP(context, SP_RANDOM_TIME).edit().remove(SP_KEY_DRINKING_TIME_SET).commit();
	}

	public static void cancelSchedule(Context context){
		cancelReminder(context);
		cancelTrigger(context); //based on new requirement

		getSP(context, SP_RANDOM_TIME).edit().remove(SP_KEY_RANDOM_TIME_SET).commit();
		getSP(context, SP_SURVEY).edit().clear().commit();
		Log.d("sa======================", "clear");
	}

	public static void cancelMorning(Context context){
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent itTrigger = new Intent(Utilities.BD_TRIGGER_MAP.get(Utilities.SV_NAME_MORNING));
		itTrigger.putExtra(Utilities.SV_NAME, Utilities.SV_NAME_MORNING);
		PendingIntent piTrigger = PendingIntent.getBroadcast(context, 0, itTrigger, Intent.FLAG_ACTIVITY_NEW_TASK);
		am.cancel(piTrigger);
	}


	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//getters()                                                                                                   //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public static void Log_sys(String s1, String s2){
		if(debug_system) {
			Log.d(s1,s2);
		}
	}

	public static void Log(String s1, String s2){
		if(debug) {
			Log.d(s1,s2);
		}
	}

	public static void LogB(String s1, String s2){
		if(debugB) {
			Log.d(s1,s2);
		}
	}

	public static String getPWD(Context context){// need modify
		SharedPreferences shp = context.getSharedPreferences(SP_LOGIN, Context.MODE_PRIVATE);
//	    ID = shp.getString(AdminManageActivity.ASID, "");
	    String PWD = shp.getString(Utilities.SP_KEY_LOGIN_USERPWD, "");
	    return PWD;
	}

	public static String getTimeFromLong(long l){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(l);

		Calendar t = Calendar.getInstance();
		t.set(Calendar.HOUR_OF_DAY, 0);
		t.set(Calendar.MINUTE, 1);
		t.set(Calendar.SECOND, 0);

		if(c.after(t)){
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMinimumIntegerDigits(2);

			return nf.format(c.get(Calendar.HOUR_OF_DAY))+":"+nf.format(c.get(Calendar.MINUTE));
		}
		else{
			return TIME_NONE;
		}
	}


	public static String getScheduleForToady(Context context){
		//morning
		long morning = getSP(context, SP_BED_TIME).getLong(SP_KEY_BED_TIME_LONG, -1);

		//follow-ups
		long followup = getSP(context, SP_RANDOM_TIME).getLong(SP_KEY_DRINKING_TIME_SET, -1);
		String follow = "";
		if(followup != -1){
			for(int i=1;i<=MAX_TRIGGER_FOLLOWUP;i++){
				if(getTimeFromLong(followup+FOLLOWUP_IN_SECONDS*1000*i).equals(TIME_NONE)){
					follow = TIME_NONE;
					break;
				}
				follow += getTimeFromLong(followup+FOLLOWUP_IN_SECONDS*1000*i);
				follow += ", ";
			}
		}
		else{
			follow = TIME_NONE;
		}

		//random
		String strRandom[] = getSP(context, SP_RANDOM_TIME).getString(SP_KEY_RANDOM_TIME_SET, "").split(",");
		String random = "";
		if(strRandom.length != 1){
			for(String s: strRandom){
				if(getTimeFromLong(Long.parseLong(s)).equals(TIME_NONE)){
					random = TIME_NONE;
					break;
				}
				random += getTimeFromLong(Long.parseLong(s));
				random += ", ";
			}
		}
		else{
			random = TIME_NONE;
		}

		String str =
				"\nStudy Day: "+getStudyDay(context) +
				(!getSP(context, Utilities.SP_SURVEY).getBoolean(Utilities.SP_KEY_SURVEY_SUSPENSION, false)?"\n":"\nUnder suspension.\n") +
				(RELEASE? "" :
				"\nMorning  survey at: " + (morning == -1 ? TIME_NONE : getTimeFromLong(morning))+
				"\nFollowup survey at: " + follow +
				"\nRandom  survey at: "+random);

		return str;
	}


	public static int getStudyDay(Context context){
		String startStr = getSP(context, SP_LOGIN).getString(SP_KEY_LOGIN_STUDY_STARTTIME, "");
		if(!startStr.equals("")){
			long start = Long.parseLong(startStr);
			long current = Calendar.getInstance().getTimeInMillis();

			Calendar s = Calendar.getInstance();
			s.setTimeInMillis(start);
			s.set(Calendar.HOUR_OF_DAY, 3);
			s.set(Calendar.MINUTE, 0);
			s.set(Calendar.SECOND, 0);
			s.set(Calendar.MILLISECOND, 0);

			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(current);
//			c.set(Calendar.HOUR_OF_DAY, 12);
			c.set(Calendar.MINUTE, 0);
			c.set(Calendar.SECOND, 0);
			c.set(Calendar.MILLISECOND, 0);

			start = s.getTimeInMillis();
			current = c.getTimeInMillis();

			return (int) ((current - start) / (24*60*60*1000));
		}else{
			return -1;
		}
	}


	public static long getDayLong(){
		return 24*60*60*1000;
	}


	public static String getMorningTimeWithFlag(Context context){

		long time = getSP(context, SP_BED_TIME).getLong(SP_KEY_BED_TIME_LONG, -1);

		Calendar m = Calendar.getInstance();
		Calendar t = Calendar.getInstance();
		t.setTimeInMillis(time);
		//is for tomorrow?
		if(t.after(m)){
			Log.d("what am pm ", "am_ps is "+t.get(Calendar.HOUR_OF_DAY)+":"+t.get(Calendar.MINUTE)+" "+t.get(Calendar.AM_PM));
			NumberFormat nf = NumberFormat.getInstance();
			nf.setMinimumIntegerDigits(2);
			return nf.format(t.get(Calendar.HOUR_OF_DAY))+":"+nf.format(t.get(Calendar.MINUTE))+" "+(t.get(Calendar.AM_PM) == 0?"a.m.":"p.m.");
		}

		return TIME_NONE;
	}


	public static Calendar getDefaultMorningCal(Context context){

		SharedPreferences sp = getSP(context, SP_BED_TIME);
		int hour = defHour;
		int minute = defMinute;

		boolean setDefault = (sp.getInt(Utilities.SP_KEY_BED_TIME_HOUR, -1) == -1?false:true);
		if(setDefault){
			hour = sp.getInt(Utilities.SP_KEY_BED_TIME_HOUR, -1);
			minute = sp.getInt(Utilities.SP_KEY_BED_TIME_MINUTE, -1);
		}

		return getMorningCal(hour, minute);
	}

	public static Calendar getMorningCal(int hour, int minute){
		Calendar c = Calendar.getInstance();
		if(c.get(Calendar.HOUR_OF_DAY) > 3){
			//next day
			c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR)+1);
		}
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);

		return c;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//pin check                                                                                                   //
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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
//		        	bedAlertDialog();
		        	dialog.cancel();
	        	}
	        	else {
	        		//New AlertDialog to show instruction.
	        		new AlertDialog.Builder(context)
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



	public static void writeLocationToFile(Location location) throws IOException{

		String toWrite;
		Calendar cal=Calendar.getInstance();

		toWrite = String.valueOf(cal.getTime())+","+
			location.getLatitude()+","+location.getLongitude()+","+
			location.getAccuracy()+","+location.getProvider()+","+getNameFromType(ActivityRecognitionService.currentUserActivity);


		String userID = LocationBroadcast.ID;
		//filename
		Calendar cl=Calendar.getInstance();
		SimpleDateFormat curFormater = new SimpleDateFormat("MMMMM_dd");
		String dateObj =curFormater.format(cl.getTime());

		StringBuilder prefix_sb = new StringBuilder(Utilities.PREFIX_LEN);
		String prefix = "locations." + userID + "." + dateObj;
		prefix_sb.append(prefix);

		for (int i = prefix.length(); i <= Utilities.PREFIX_LEN; i++) {
			prefix_sb.append(" ");
		}


		//danick
		String toWriteArr = null;
		try {
			toWriteArr = encryption(prefix_sb.toString() + toWrite);
			if(WRITE_RAW){
				writeToFile("Location."+userID+"."+dateObj+".txt", toWrite);
			}else{
				writeToFileEnc("Location." + userID + "." + dateObj + ".txt", toWriteArr);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		//Ricky
		TransmitData transmitData=new TransmitData();
		transmitData.execute(toWriteArr);

	}


	//upload
	public static void writeEventToFile(Context context, int type, String scheduleTS, String r1, String r2, String r3, String startTS, String endTS) throws IOException{

		Calendar endCal = Calendar.getInstance();

		String userID = Utilities.getSP(context, Utilities.SP_LOGIN).getString(Utilities.SP_KEY_LOGIN_USERID, "0000");
		int studyDay = Utilities.getStudyDay(context);


		StringBuilder sb = new StringBuilder(100);

//		Calendar c = Calendar.getInstance();
//		c.setTimeInMillis(time);
		sb.append(endCal.getTime().toString());
		sb.append(",");

		sb.append(userID+","+studyDay+","+type+","+scheduleTS+","+r1+","+r2+","+r3+","+startTS+","+endTS+",");
//		sb.append("\n");

		Calendar cl = Calendar.getInstance();
		SimpleDateFormat curFormater = new SimpleDateFormat("MMMMM_dd");
		String dateObj = curFormater.format(cl.getTime());

		StringBuilder prefix_sb = new StringBuilder(Utilities.PREFIX_LEN);
		String prefix = "Excel." + userID + "." + dateObj;
		prefix_sb.append(prefix);

		for (int i = prefix.length(); i <= Utilities.PREFIX_LEN; i++) {
			prefix_sb.append(" ");
		}

		/************************************************************************
		 * Chen
		 *
		 * Data encryption
		 * Stringbuilder sb -> String ensb
		 */
		String ensb = null;
		try {
			ensb = encryption_withKey(context, prefix_sb.toString() + sb.toString());
			if(WRITE_RAW){
				writeToFile("Event.txt", sb.toString());
			}else{
				writeToFileEnc("Event.txt", ensb);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Ricky 2013/12/09
		TransmitData transmitData=new TransmitData();
		transmitData.execute(ensb);

	}

	//Chen
	public static String encryption_withKey(Context context, String string) throws Exception {
		// TODO Auto-generated method stub

		//generate symmetric key
		KeyGenerator keygt = KeyGenerator.getInstance("AES");
		keygt.init(128);
		SecretKey symkey =keygt.generateKey();

		//get it encoded
		byte[] aes_ba = symkey.getEncoded();

		//create cipher
		SecretKeySpec skeySpec = new SecretKeySpec(aes_ba, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE,skeySpec);

        //encryption
        byte [] EncSymbyteArray =cipher.doFinal(string.getBytes());

        //encrypt symKey with PublicKey
//        Key pubKey = getPublicKey();

//        Key pubKey = publicKey;

        InputStream is = context.getResources().openRawResource(R.raw.publickey);
		ObjectInputStream ois = new ObjectInputStream(is);

		BigInteger m = (BigInteger)ois.readObject();
		BigInteger e = (BigInteger)ois.readObject();
	    RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);


	    KeyFactory fact = KeyFactory.getInstance("RSA", "BC");
	    PublicKey pubKey = fact.generatePublic(keySpec);


        //RSA cipher
        Cipher cipherAsm = Cipher.getInstance("RSA", "BC");
        cipherAsm.init(Cipher.ENCRYPT_MODE, pubKey);

        //RSA encryption
        byte [] asymEncsymKey = cipherAsm.doFinal(aes_ba);

//	        File f3 = new File(BASE_PATH,"enc.txt");
//	        File f3key = new File(BASE_PATH,"enckey.txt");
//	        File f3file = new File(BASE_PATH,"encfile.txt");
//	        writeToFile2(f3,f3key,f3file, asymEncsymKey, EncSymbyteArray);

        //byte != new String
        //return new String(byteMerger(asymEncsymKey, EncSymbyteArray));
        return Base64.encodeToString(byteMerger(asymEncsymKey, EncSymbyteArray),Base64.DEFAULT);

	}

	//Chen
	public static String encryption(String string) throws Exception {
		// TODO Auto-generated method stub

		//generate symmetric key
		KeyGenerator keygt = KeyGenerator.getInstance("AES");
		keygt.init(128);
		SecretKey symkey =keygt.generateKey();

		//get it encoded
		byte[] aes_ba = symkey.getEncoded();
//		for (byte b : aes_ba) {
//			Log.d("---------------------", "" + b);
//		}

		//create cipher
		SecretKeySpec skeySpec = new SecretKeySpec(aes_ba, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE,skeySpec);

        //encryption
        byte [] EncSymbyteArray =cipher.doFinal(string.getBytes());

        //encrypt symKey with PublicKey
//        Key pubKey = getPublicKey();

        Key pubKey = publicKey;

        //RSA cipher
        Cipher cipherAsm = Cipher.getInstance("RSA", "BC");
        cipherAsm.init(Cipher.ENCRYPT_MODE, pubKey);

        //RSA encryption
        byte [] asymEncsymKey = cipherAsm.doFinal(aes_ba);

//	        File f3 = new File(BASE_PATH,"enc.txt");
//	        File f3key = new File(BASE_PATH,"enckey.txt");
//	        File f3file = new File(BASE_PATH,"encfile.txt");
//	        writeToFile2(f3,f3key,f3file, asymEncsymKey, EncSymbyteArray);

        //byte != new String
        //return new String(byteMerger(asymEncsymKey, EncSymbyteArray));
        return Base64.encodeToString(byteMerger(asymEncsymKey, EncSymbyteArray),Base64.DEFAULT);

	}

	public static byte[] byteMerger(byte[] byte_1, byte[] byte_2){
	    byte[] byte_3 = new byte[byte_1.length+byte_2.length];
	    System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
	    System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
	    return byte_3;
	}


	public static void writeToFile(String fileName, String toWrite) throws IOException{
		File dir =new File(PHONE_BASE_PATH);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		File f = new File(PHONE_BASE_PATH,fileName);
		FileWriter fw = new FileWriter(f, true);
		fw.write(toWrite+'\n');
        fw.flush();
		fw.close();
		f = null;
	}

	public static void writeToFileEnc(String fileName, String toWrite) throws IOException{
		Utilities.Log("write to file", "enc");
		File dir =new File(PHONE_BASE_PATH);
		if(!dir.exists()) {
			dir.mkdirs();
		}
		File f = new File(PHONE_BASE_PATH,fileName);
		FileWriter fw = new FileWriter(f, true);
		fw.write(toWrite);
        fw.flush();
		fw.close();
		f = null;
	}


	private static String getNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";

        }
        return "unknown";
    }


	static class TransmitData extends AsyncTask<String,Void, Boolean>
	{

		@Override
		protected Boolean doInBackground(String... strings) {
			// TODO Auto-generated method stub

			String data = strings[0];
			//			 String fileName=strings[0];
			//	         String dataToSend=strings[1];
//	         if(checkDataConnectivity())
	        	 if(true)
	 		{

	        Log.d("((((((((((((((((((((((((", ""+Thread.currentThread().getId());
	         HttpPost request = new HttpPost(UPLOAD_ADDRESS);
	         List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("data", data));

				//	         //file_name
				//	         params.add(new BasicNameValuePair("file_name",fileName));
				//	         //data
				//	         params.add(new BasicNameValuePair("data",dataToSend));
	         try {

	             request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
	             HttpResponse response = new DefaultHttpClient().execute(request);
	             if(response.getStatusLine().getStatusCode() == 200){
	                 String result = EntityUtils.toString(response.getEntity());
	                 Log.d("Sensor Data Point Info",result);
	                // Log.d("Wrist Sensor Data Point Info","Data Point Successfully Uploaded!");
	             }
	             return true;
	         }
	         catch (Exception e)
	         {
	             e.printStackTrace();
	             return false;
	         }
	 	  }

	     else
	     {
	     	Log.d("Sensor Data Point Info","No Network Connection:Data Point was not uploaded");
	     	return false;
	      }

		}

	}





}
