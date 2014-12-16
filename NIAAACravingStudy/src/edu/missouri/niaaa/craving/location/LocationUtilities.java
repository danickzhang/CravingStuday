package edu.missouri.niaaa.craving.location;

import java.io.IOException;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import edu.missouri.niaaa.craving.Utilities;

public class LocationUtilities {

	public final static String TAG = "LocationUtilities";
	public final static int TIME_ACCURACY_IN_SECOND = 30;//30;
	public final static int DISTENCE_ACCURACY_IN_METER = 30;//30;

	public final static String BD_ACTION_SCHEDULE_LOCATION = "edu.missouri.niaaa.craving.SCHEDULE_LOCATION";
	public final static String ACTION_START_LOCATION = "edu.missouri.niaaa.craving.START_LOCATION";
	public final static String ACTION_STOP_LOCATION = "edu.missouri.niaaa.craving.STOP_LOCATION";


	public static Location mCurrentLocation = null;
//	public static int currentUserActivity = 9;

	static ActivityRecognitionScan activityRecognition;

	//	LocationManager locationM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


	public static void startGPS(Context context, LocationManager locationM){
		activityRecognition = new ActivityRecognitionScan(context);
		activityRecognition.startActivityRecognitionScan();

		requestLocation(locationM);
	}

	public static void stopGPS(Context context, LocationManager locationM){
		activityRecognition.stopActivityRecognitionScan();

		removeLocation(locationM);
	}


	public static void requestLocation(LocationManager locationM){
		locationM.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_ACCURACY_IN_SECOND*1000, DISTENCE_ACCURACY_IN_METER, locationListenerGps);
		Log.d(TAG, "request Location Updates");
	}

	public static void removeLocation(LocationManager locationM){
		locationM.removeUpdates(locationListenerGps);
		Log.d(TAG, "remove Location Updates");
	}

	private static LocationListener locationListenerGps = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {

			if (location != null) {

				Log.d("test gps", "gps location is not null "+location.getLatitude()+","+location.getLongitude()+","+
						location.getAccuracy()+","+location.getProvider());
				if(location.getAccuracy() <= 35)
				{
					if(isBetterLocation(location, mCurrentLocation))
					{
						mCurrentLocation = location;
						try {
							Log.d("test gps", "gps location");
							Utilities.writeLocationToFile(location);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}

		@Override
		public void onProviderDisabled(String provider) {
//			Toast.makeText(serviceContext, "GPS is not enabled", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onProviderEnabled(String provider) {
//			Toast.makeText(serviceContext, "GPS is enabled now", Toast.LENGTH_LONG).show();
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};


	private static boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > 30*1000;
		boolean isSignificantlyOlder = timeDelta < -30*1000;
		boolean isNewer = timeDelta > 0;

		// If it's been more than five minutes since the current location, use
		// the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than five minutes older, it must be
			// worse
		} else if (isSignificantlyOlder) {
			return false;
		}

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(),	currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and
		// accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate
				&& isFromSameProvider) {
			return true;
		}
		return false;
	}

	/** Checks whether two providers are the same */
	private static boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}

}
