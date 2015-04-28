package edu.missouri.niaaa.craving.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;

public class BatteryInfoBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {

			/* nick changed the default value for level from 0 to -1 on april 2nd 2015 */
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

			/* nick added the cast to float (float) because the percentage was not showing up right when phone was under 10% battery life  */
			float batteryPercent = level / (float) scale;
			MonitorUtilities.curBatt = String.valueOf(batteryPercent);
			// Log.d("BatteryInfoBroadcastReceiver", Utilities.curBatt);


			/* Nick added this to determine if the phone is charging or not
			 * and how the phone is charging
			 * 	on April 2nd 2015 for slu app
			 */

			//Are we charging ?
			int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
			boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL;

			MonitorUtilities.isCharging = String.valueOf(isCharging);

			// How are we charging?
			int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

			if(chargePlug == BatteryManager.BATTERY_PLUGGED_USB){
				MonitorUtilities.howCharging = "USB";
			}
			else if(chargePlug == BatteryManager.BATTERY_PLUGGED_AC){
				MonitorUtilities.howCharging = "AC Outlet";
			}
			else if(chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS){
				MonitorUtilities.howCharging = "Wireless Power Source";
			}
			else {
				MonitorUtilities.howCharging = "unknown";
			}
		}
	}
}
