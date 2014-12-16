package edu.missouri.niaaa.craving.location;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;


public class ActivityRecognitionService extends IntentService{

	private static final String TAG ="ActivityRecognitionService";
	public final static String BASE_PATH = "sdcard/TestResults/";
	ActivityRecognitionResult result;
	public static int currentUserActivity = 9;
	public static boolean IsRetrievingUpdates=false;
	public static boolean IsIntentSent = false;
	PendingIntent scheduleLocation;
	AlarmManager mAlarmManager;
	LocationManager mLocationManager;
	IntentFilter mIntentFilter;


	public ActivityRecognitionService() {
		super("ActivityRecognitionService");
		Log.d(TAG, "constructor");
	}

	/**
	* Google Play Services calls this once it has analysed the sensor data
	*/
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, "on handle intent");
	   if (ActivityRecognitionResult.hasResult(intent)) {
		   result=null;
		   result = ActivityRecognitionResult.extractResult(intent);
		   setCurrentUserActivity(result.getMostProbableActivity().getType(),result.getMostProbableActivity().getConfidence());
		   Log.d(TAG, "ActivityRecognitionResult: "+getNameFromType(result.getMostProbableActivity().getType()));
	   }
	}



	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

	}

	private String getNameFromType(int activityType) {
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

	public  void setCurrentUserActivity(int Activity,int Confidence){

		Log.d(TAG, "current user activity changed");
		switch (Activity) {
		case DetectedActivity.IN_VEHICLE:
		case DetectedActivity.ON_BICYCLE:
		case DetectedActivity.ON_FOOT:
			if(Confidence>=75)
			{
				currentUserActivity=Activity;
//	    		Intent i=new Intent("INTENT_ACTION_SCHEDULE_LOCATION");
//	    		i.putExtra("activity",currentUserActivity);
//				this.sendBroadcast(i);
				Log.d(TAG, "current user activity changed inner");
			}
		case DetectedActivity.TILTING:
		case DetectedActivity.STILL:
	    case DetectedActivity.UNKNOWN:
		default:
			  //stopLocationUpdates();
		}
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();

		Log.d(TAG, "gps reco service onCreate");
	}

}
