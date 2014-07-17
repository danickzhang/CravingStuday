package edu.missouri.niaaa.craving;

import java.util.Calendar;
import java.util.HashMap;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class Utilities {
	
/*	survey type*/
	public final static String SV_FILE = "survey_file";
	public final static String SV_NAME = "survey_name";
	
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
	
	public final static HashMap<String, Integer> MAX_TRIGGER_MAP = new HashMap<String, Integer>(){
		{
			put(SV_NAME_MORNING, MAX_TRIGGER_MORNING);
			put(SV_NAME_RANDOM, MAX_TRIGGER_RANDOM);
			put(SV_NAME_FOLLOWUP, MAX_TRIGGER_FOLLOWUP);
		}
	};
	
	public final static int REMINDER_IN_SECONDS = 5;
	public final static int COMPLETE_SURVEY_IN_SECONDS = REMINDER_IN_SECONDS;
	public final static int FOLLOWUP_IN_SECONDS = 30;
	
/*	shared preferences*/
	public final static String SP_BED_TIME = "edu.missouri.niaaa.craving.BEDTIME_INFO";
	public final static String SP_RANDOM_TIME = "edu.missouri.niaaa.craving.RANDOM_TIME_INFO";
	public final static String SP_SURVEY = "edu.missouri.niaaa.craving.SURVEY";
	public final static String SP_LOGIN = "edu.missouri.niaaa.craving.LOGIN";
	
/*	shared preferences keys*/
	public final static String SP_KEY_BED_TIME_HOUR = "BEDTIME_INFO_HOUR";
	public final static String SP_KEY_BED_TIME_MINUTE = "BEDTIME_INFO_MINUTE";
	public final static String SP_KEY_BED_TIME_LONG = "BEDTIME_INTO_LONG";
	public final static String SP_KEY_RANDOM_TIME_SET = "RANDOM_TIME_SET";
	
	public final static String SP_KEY_SURVEY_REMINDER_SEQ = "SURVEY_REMINDER_SEQ";
	public final static String SP_KEY_SURVEY_UNDERGOING = "SURVEY_UNDERGOING";
	public final static String SP_KEY_SURVEY_REMINDER_LAST = "SURVEY_REMINDER_LAST";
	public final static String SP_KEY_SURVEY_UNDERREMINDERING = "SURVEY_UNDER_REMINDERING";
	
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
	
/*	broadcast*/
	public final static String BD_ACTION_SCHEDULE_ALL = "edu.missouri.niaaa.craving.ACTION_SCHEDULE_ALL";
	public final static String BD_ACTION_REMINDER_SURVEY = "edu.missouri.niaaa.craving.REMINDER";
	
	public final static String BD_ACTION_SCHEDULE_MORNING = "edu.missouri.niaaa.craving.SCHEDULE_MORNING";
	public final static String BD_ACTION_TRIGGER_MORNING = "edu.missouri.niaaa.craving.TRIGGER_MORNING";
//	public final static String BD_ACTION_REMINDER_MORNING = "edu.missouri.niaaa.craving.REMINDER_MORNING";
	
	public final static String BD_ACTION_SCHEDULE_RANDOM = "edu.missouri.niaaa.craving.SCHEDULE_RANDOM";
	public final static String BD_ACTION_TRIGGER_RANDOM = "edu.missouri.niaaa.craving.TRIGGER_RANDOM";
//	public final static String BD_ACTION_REMINDER_RANDOM = "edu.missouri.niaaa.craving.REMINDER_RANDOM";
	
	public final static String BD_ACTION_SCHEDULE_FOLLOWUP = "edu.missouri.niaaa.craving.SCHEDUL_FOLLOWUP";
	public final static String BD_ACTION_TRIGGER_FOLLOWUP = "edu.missouri.niaaa.craving.TRIGGER_FOLLOWUP";
//	public final static String BD_ACTION_REMINDER_FOLLOWUP = "edu.missouri.niaaa.craving.REMINDER_FOLLOWUP";
	
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
	

	
	static boolean debug_system = true;
	static boolean debug = true;
	
	public static SharedPreferences getSP(Context context, String name){
		SharedPreferences shp = context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
		return shp;
	}
	
	
	private void put(String svNameMorning, int maxTriggerMorning) {
		// TODO Auto-generated method stub
		
	}


	public static void Log_sys(String s1, String s2){
		if(debug_system)
			Log.d(s1,s2);
	}
	
	public static void Log(String s1, String s2){
		if(debug)
			Log.d(s1,s2);
	}
	
	public static String getTimeFromLong(long l){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(l);
		return c.get(Calendar.HOUR_OF_DAY)+":"+c.get(Calendar.MINUTE);
	}
	
}
