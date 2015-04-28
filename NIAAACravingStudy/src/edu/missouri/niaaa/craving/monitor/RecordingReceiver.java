package edu.missouri.niaaa.craving.monitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import edu.missouri.niaaa.craving.Utilities;

public class RecordingReceiver extends BroadcastReceiver {
	private final String TAG = "Recording Receiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "received");

		if(MonitorUtilities.ID == null){
			MonitorUtilities.ID = Utilities.getSP(context, Utilities.SP_LOGIN).getString(Utilities.SP_KEY_LOGIN_USERID, "0000");
			Log.d(TAG, "MonitorUtilities.ID was null. Now it is: "+MonitorUtilities.ID);
		}

		String fileName = MonitorUtilities.RECORDING_CATEGORY + "." + MonitorUtilities.ID + "." + MonitorUtilities.getFileDate();
		// Need to be modified like the format after merging
		// String prefix =
		// RECORDING_FILENAME+"."+phoneID+"."+getFileDate
		String toWrite = prepareData(context, intent);

		try {
			Utilities.writeToFile(fileName + ".txt", toWrite);
			Log.d(TAG, "write to file");
		} catch (IOException e) {
			Log.d(TAG, "not write to file!!");
			e.printStackTrace();
		}

		String fileHead = getFileHead(fileName);
		// Log.d("RecordingReceiver", fileHead);
		String toSend = fileHead + toWrite;
		String enformattedData = null;
		try {
			enformattedData = Utilities.monitorEncryption(toSend, context);
		} catch (Exception e) {
			Log.d(TAG, "Utilties monitorEncryption failed!");
			e.printStackTrace();
		}

		 TransmitData transmitData = new TransmitData();
		 if (MonitorUtilities.checkNetwork(context)) {
			 transmitData.execute(enformattedData);
			/*try {
				boolean result = transmitData.execute(enformattedData).get();
				Log.d(TAG, "send to server: "+result);
			} catch (InterruptedException e) {
				Log.d(TAG, "send to server interrupted exception");
				e.printStackTrace();
			} catch (ExecutionException e) {
				Log.d(TAG, "send to server execution exception");
				e.printStackTrace();
			}*/
		 }

		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Intent it = new Intent(MonitorUtilities.ACTION_RECORD);
		PendingIntent piTrigger = PendingIntent.getBroadcast(context, 0, it, Intent.FLAG_ACTIVITY_NEW_TASK);

		am.setExact(AlarmManager.RTC_WAKEUP, getNextLongTime(), piTrigger);
	}

	private long getNextLongTime() {
		Calendar s = Calendar.getInstance();
		s.add(Calendar.MINUTE, 5);
		// s.add(Calendar.SECOND, 30);
		return s.getTimeInMillis();
	}

	private String prepareData(Context context, Intent intent) {
		String connectionState = MonitorUtilities.getConnectionState(context);

		/* Added by Nick on April 2nd 2015 for SLU app */
		String gpsMode = MonitorUtilities.checkGpsMode(context);
		String activeNetwork = MonitorUtilities.checkActiveNetwork(context);
		String airplaneMode = MonitorUtilities.checkAirplaneMode(context);
		String mobileConnectionState = MonitorUtilities.getMobileConnectionState(context);
		String wifiConnectionState = MonitorUtilities.getWifiConnectionState(context);
		String bluetoothConnectionState = MonitorUtilities.getBluetoothConnectionState(context);
		/* this is bluetooth for device connection */
		String bluetoothSupported = MonitorUtilities.checkIfBluetoothIsSupported();
		String bluetoothOn = MonitorUtilities.checkIfBluetoothIsOn();
		/*String bluetoothBonded = MonitorUtilities.checkIfBluetoothIsBonded();
		String bluetoothConnected = MonitorUtilities.checkIfBluetoothIsConnected();*/

		//String activeNetwork2 = MonitorUtilities.checkActiveNetwork2(context);

		String textForGPSAccuracy;

		if(MonitorUtilities.gpsAccuracy.equals("unknown")) {
			textForGPSAccuracy = "";
		} else {
			textForGPSAccuracy = " meters";
		}


		return MonitorUtilities.getCurrentTimeStamp() + MonitorUtilities.LINEBREAK
				/* Added by nick for slu app on april 2nd 2015 */
				+ MonitorUtilities.LINEBREAK
				+ "Is Phone Charging? -> " + MonitorUtilities.isCharging + MonitorUtilities.LINEBREAK
				+ "  Charging By: " + MonitorUtilities.howCharging + MonitorUtilities.LINEBREAK
				/*+ "  Charging By USB: " + MonitorUtilities.usbCharge + MonitorUtilities.LINEBREAK
				+ "  Charging By AC Outlet: " + MonitorUtilities.acCharge + MonitorUtilities.LINEBREAK*/
				/* Nick end */
				+ "Battery Level: " + MonitorUtilities.curBatt + MonitorUtilities.LINEBREAK
				+ "Network Connection Status: " + connectionState + MonitorUtilities.LINEBREAK
				/* added by nick for slu app on april 2nd 2015 */
				+ "  " + mobileConnectionState + MonitorUtilities.LINEBREAK
				+ "  " + wifiConnectionState + MonitorUtilities.LINEBREAK
				+ "  " + bluetoothConnectionState + MonitorUtilities.LINEBREAK
				+ "  " + activeNetwork + MonitorUtilities.LINEBREAK
				//+ "  " + activeNetwork2 + MonitorUtilities.LINEBREAK
				+ gpsMode + MonitorUtilities.LINEBREAK
				+ "  Is There an Active GPS Signal? -> " + MonitorUtilities.activeGPS + MonitorUtilities.LINEBREAK
				+ "  GPS Provider: " + MonitorUtilities.gpsProvider + MonitorUtilities.LINEBREAK
				+ "  GPS Accuracy: " + MonitorUtilities.gpsAccuracy + textForGPSAccuracy + MonitorUtilities.LINEBREAK
				+ "  Is the GPS Accuracy Good? -> " + MonitorUtilities.gpsAccuracyGood + MonitorUtilities.LINEBREAK
				+ "  Is There a Better GPS Signal Available? -> " + MonitorUtilities.betterGpsLocation + MonitorUtilities.LINEBREAK
				+ bluetoothSupported + MonitorUtilities.LINEBREAK
				+ "  " + bluetoothOn + MonitorUtilities.LINEBREAK
				/*+ "  " + bluetoothBonded + MonitorUtilities.LINEBREAK
				+ "  " + bluetoothConnected + MonitorUtilities.LINEBREAK*/
				+ airplaneMode + MonitorUtilities.LINEBREAK
				/* Nick end */
				+ MonitorUtilities.SPLIT;
	}

	private String getFileHead(String fileName) {
		StringBuilder prefix_sb = new StringBuilder(Utilities.PREFIX_LEN);
		prefix_sb.append(fileName);

		for (int i = fileName.length(); i <= Utilities.PREFIX_LEN; i++) {
			prefix_sb.append(" ");
		}
		return prefix_sb.toString();
	}

	private class TransmitData extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... strings) {
			String data = strings[0];
			// String fileName = strings[0];
			// String dataToSend = strings[1];

			HttpPost request = new HttpPost(Utilities.UPLOAD_ADDRESS);
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("data", data));
			// // file_name
			// params.add(new BasicNameValuePair("file_name", fileName));
			// // data
			// params.add(new BasicNameValuePair("data", dataToSend));
			try {
				request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				HttpResponse response = new DefaultHttpClient().execute(request);
				Log.d("Sensor Data Point Info", String.valueOf(response.getStatusLine().getStatusCode()));
				if(response.getStatusLine().getStatusCode() == 200){
					Log.d(TAG, "send to server");
				}
				return true;
			} catch (Exception e){
				e.printStackTrace();
				Log.d(TAG, "not send to server!!");
				return false;
			}
		}
	}
}
