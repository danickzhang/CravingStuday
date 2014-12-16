package edu.missouri.niaaa.craving.location;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import edu.missouri.niaaa.craving.Utilities;

public class LocationBroadcast extends BroadcastReceiver {

	String TAG = "Location Broadcast";
	public static LocationManager locationM;
	public static String ID;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub


		String action = intent.getAction();
		locationM = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		ID = Utilities.getSP(context, Utilities.SP_LOGIN).getString(Utilities.SP_KEY_LOGIN_USERID, "0000");

		if(action.equals(LocationUtilities.ACTION_START_LOCATION)){
			Utilities.Log(TAG, "location recording start");
			LocationUtilities.requestLocation(locationM);

			/*acquire wake lock*/
		}

		else if(action.equals(LocationUtilities.ACTION_STOP_LOCATION)){
			Utilities.Log(TAG, "location recording stop");
			LocationUtilities.removeLocation(locationM);

			/*release wake lock*/
		}
	}

}
