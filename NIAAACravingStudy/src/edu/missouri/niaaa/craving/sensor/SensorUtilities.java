package edu.missouri.niaaa.craving.sensor;


public class SensorUtilities {

	public final static String TAG = "Sensor Utilities";

	public static final int INTENT_CONNECT_SENSOR = 1;
	public static final int SENSOR_STATE_CHANGE = 2;
	public final static int INTENT_REQUEST_BLUETOOTH = 3;


/*	broadcast*/
	public static final String ACTION_CONNECT_SENSOR = "edu.missouri.niaaa.craving.CONNECT_SENSOR";
	public static final String ACTION_DISCONNECT_SENSOR = "edu.missouri.niaaa.craving.DISCONNECT_SENSOR";
	public static final String ACTION_LOST_CONNECTION_SOUND = "edu.missouri.niaaa.craving.LOST_CONNECTION_SOUND";

	public final static String KEY_ADDRESS = "KEY_ADDRESS";
	public final static String KEY_DEVICE_NAME = "KEY_DEVICE_NAME";

}
