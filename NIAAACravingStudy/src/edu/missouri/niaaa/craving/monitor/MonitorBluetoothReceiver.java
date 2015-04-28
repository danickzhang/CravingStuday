package edu.missouri.niaaa.craving.monitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import edu.missouri.niaaa.craving.Utilities;


public class MonitorBluetoothReceiver extends BroadcastReceiver {
	private final String TAG = "Monitor Bluetooth Receiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Received");

		String action = intent.getAction();
		Log.d(TAG, "Action: " + action);

		if(action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)){
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			Log.d(TAG, "Bluetooth Device Connected to Device Name: " + device.getName());
			String message = "Active Bluetooth has just been CONNECTED to the Device Named '"+device.getName()+"' !!";
			writeAndSend(message, context);
		}
		if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)){
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			Log.d(TAG, "Bluetooth Device Disconnected from Device Name: " + device.getName());
			String message = "Active Bluetooth has just been DISCONNECTED from the Device Named '"+device.getName()+"' !!";
			writeAndSend(message, context);
		}
		/*if(action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)){
			Log.d(TAG, "User has requested a disconnection from bluetooth device");
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			Log.d(TAG, "Device Name: " + device.getName());
			String send = "Active Bluetooth has just been REQUESTED by USER for DISCONNECTION from the Device Named '"+device.getName()+"' !!";
			boolean result = writeAndSend(send, context);
			Log.d(TAG, "send to server: "+result);
		}*/
		if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
			Log.d(TAG, "Entered Action Bond State Changed");
			BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			String deviceName = device.getName();

			/*Action Bond State Changed gets called a lot and it would send info
			 * besides the ones checked in the if statements. so in order to only send
			 * the info we want, I put this here and when the if statements execute
			 * then it will set this to true, cause the data to be written to file
			 * and sent to server
			 */
			boolean sendData = false;

			String message = "Bluetooth Pairing State";

			//the first parameter is the integer you would like to get
			//the second parameter is the default integer if there is no value
			final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
			final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);
			Log.d(TAG, "bond state: "+state);
			Log.d(TAG, "previous bond state: "+prevState);

			if(state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING){
				Log.d(TAG, "Bluetooth Bond State: CONNECTED; To Device Named: "+deviceName);
				sendData = true;
				message += " has just been CONNECTED to";
			}
			if(state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED){
				Log.d(TAG, "Bluetooth Bond State: DISCONNECTED; To Device Named: "+deviceName);
				sendData = true;
				message += " has just been DISCONNECTED from";
			}
			if(state == BluetoothDevice.BOND_BONDING && prevState == BluetoothDevice.BOND_NONE){
				Log.d(TAG, "Bluetooth Bond State: CONNECTING; To Device Named"+deviceName);
				sendData = true;
				message += " is CONNECTING to";
			}

			message += " the Device Named '"+deviceName+"' !!";

			if(sendData){
				writeAndSend(message, context);
			}
		}
		if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
			Log.d(TAG, "entered action state changed");
			int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
			int prevState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1);
			Log.d(TAG, "bluetooth state: " + state);
			Log.d(TAG, "bluetooth previous state: " + prevState);

			String message = "Bluetooth";

			switch(state){
			case BluetoothAdapter.STATE_OFF:
				Log.d(TAG, "bluetooth adapter state off");
				message += "'s Current State: OFF";
				break;
			case BluetoothAdapter.STATE_TURNING_ON:
				Log.d(TAG, "bluetooth adapter state turing on");
				message += " is TURNING ON and was activated by the user !!";
				break;
			case BluetoothAdapter.STATE_ON:
				Log.d(TAG, "bluetooth adapter state on");
				message += "'s Current State: ON";
				break;
			case BluetoothAdapter.STATE_TURNING_OFF:
				Log.d(TAG, "bluetooth adapter state turning off");
				message += " is TURNING OFF and was activated by the user !!";
				break;
			}

			writeAndSend(message, context);
		}
	}

	private void writeAndSend(String data, Context context){
		if(MonitorUtilities.ID == null){
			MonitorUtilities.ID = Utilities.getSP(context, Utilities.SP_LOGIN).getString(Utilities.SP_KEY_LOGIN_USERID, "0000");
			Log.d(TAG, "MonitorUtilities.ID was null. Now it is: "+MonitorUtilities.ID);
		}

		String fileName = MonitorUtilities.RECORDING_CATEGORY + "." + MonitorUtilities.ID + "." + MonitorUtilities.getFileDate();
		String toWrite = MonitorUtilities.getCurrentTimeStamp() + MonitorUtilities.LINEBREAK + data
				+ MonitorUtilities.LINEBREAK + MonitorUtilities.SPLIT;

		try {
			Utilities.writeToFile(fileName + ".txt", toWrite);
			Log.d(TAG, "write to file");
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(TAG, "not write to file!!");
		}

		String fileHead = getFileHead(fileName);
		// Log.d("RecordingReceiver", fileHead);
		String toSend = fileHead + toWrite;
		String enformattedData = null;
		try {
			enformattedData = Utilities.monitorEncryption(toSend, context);
		} catch (Exception e) {
			Log.d(TAG, "utilities monitorEncryption failed!!");
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
				Log.d(TAG, "did not send to server!");
				e.printStackTrace();
				return false;
			}
		}
	}
}
