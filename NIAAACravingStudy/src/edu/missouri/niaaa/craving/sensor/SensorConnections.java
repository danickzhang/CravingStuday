package edu.missouri.niaaa.craving.sensor;



import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.equivital.sdk.connection.SemBluetoothConnection;

import edu.missouri.niaaa.craving.R;
import edu.missouri.niaaa.craving.services.SensorLocationService;


public class SensorConnections extends Activity {

	final static String TAG = "Sensor Connections";

	static TextView tvSensorStatus;
	ToggleButton tbBluetooth;
	Button   btnSensorConnect;
	Button   btnReset;

	BluetoothAdapter btAdapter=BluetoothAdapter.getDefaultAdapter();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_sensor_connect);

		tvSensorStatus=(TextView)findViewById(R.id.sensor_status_text);
		tbBluetooth = (ToggleButton) findViewById(R.id.btnBluetooth);
		btnSensorConnect=(Button)findViewById(R.id.btnSensorConnect);
		btnReset = (Button)findViewById(R.id.btnSensorReset);


		tvSensorStatus.setText(getSemState(SemBluetoothConnection.getState()));

		tbBluetooth.setChecked(getBTState());
		tbBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				// TODO Auto-generated method stub
		        if (isChecked){
		            turnOnBt();
		        }
		        else{
		        	btAdapter.disable();
		        }
			}

		});

		btnSensorConnect.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(btAdapter.isEnabled()){
					Intent i = new Intent(getApplicationContext(),SensorLocationService.class);
					startService(i);

					Intent serverIntent = new Intent(getApplicationContext(),DeviceListActivity.class);
		            startActivityForResult(serverIntent, SensorUtilities.INTENT_CONNECT_SENSOR);
	            }
				else{
					Toast.makeText(getApplicationContext(), R.string.bluetooth_needed, Toast.LENGTH_LONG).show();
				}
			}
        });

		btnReset.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				hardReset();
			}
        });

	}


	protected void hardReset(){
		btAdapter.disable();

		Intent i = new Intent(SensorUtilities.ACTION_DISCONNECT_SENSOR);
		this.sendBroadcast(i);

		tbBluetooth.setChecked(getBTState());

		Intent i2 = new Intent(this,SensorLocationService.class);
		stopService(i2);

		//wait some time
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		turnOnBt();
	}


	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + requestCode+" "+resultCode);

        switch (requestCode) {
        case SensorUtilities.INTENT_REQUEST_BLUETOOTH:
        	if(resultCode == Activity.RESULT_OK){
        		tbBluetooth.setChecked(true);
        	}
        	else{
        		tbBluetooth.setChecked(false);
        	}

        	break;

		case SensorUtilities.INTENT_CONNECT_SENSOR:
			if (resultCode == Activity.RESULT_OK){
				Log.d(TAG,"result ok");
				String address = data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				BluetoothDevice device = btAdapter.getRemoteDevice(address);

				Log.d(TAG,"device name "+device.getName());
				if(device.getName() != null && device.getName().startsWith("EQ")){
					Log.d(TAG,"device name "+address);
					Toast.makeText(getApplicationContext(), R.string.bluetooth_loop, Toast.LENGTH_LONG).show();

					Intent connectSensor = new Intent(SensorUtilities.ACTION_CONNECT_SENSOR);
					connectSensor.putExtra(SensorUtilities.KEY_ADDRESS,address);
					connectSensor.putExtra(SensorUtilities.KEY_DEVICE_NAME,device.getName());
					this.sendBroadcast(connectSensor);
				}
				else{
					Toast.makeText(getApplicationContext(), R.string.bluetooth_wrong_select, Toast.LENGTH_LONG).show();
				}
			}
			else{
				Toast.makeText(getApplicationContext(), R.string.bluetooth_no_select, Toast.LENGTH_LONG).show();
			}

		   break;

        }
    }


	public void turnOnBt() {
		// TODO Auto-generated method stub
		Intent Enable_Bluetooth=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(Enable_Bluetooth, SensorUtilities.INTENT_REQUEST_BLUETOOTH);
	}


    public static final Handler SensorHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
			Log.d(TAG, "sensor handler is message. what is " + msg.what);
        	if(msg.what == SensorUtilities.SENSOR_STATE_CHANGE){
        		tvSensorStatus.setText(getSemState(msg.arg1));
        	}

        }
    };

	private static String getSemState(int state){
		switch (state) {
		case SemBluetoothConnection.STATE_CONNECTED:
			Log.d("BluetoothState from Handler","Connected" );
			return "Connected";
		case SemBluetoothConnection.STATE_CONNECTING:
			return "Attempting to Connect...";
		case SemBluetoothConnection.STATE_LISTEN:
			return "Listening for a Connection...";
		case SemBluetoothConnection.STATE_NONE:
			return "Not Connected";
		case SemBluetoothConnection.STATE_RECONNECTING:
			return "Reconnecting...";
		default:
			return "An error has occured";
		}
	}

	private boolean getBTState(){
		if (btAdapter.isEnabled()) {
			return true;
		}
		else{
			return false;
		}
	}

}

