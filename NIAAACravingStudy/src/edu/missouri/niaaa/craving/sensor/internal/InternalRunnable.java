package edu.missouri.niaaa.craving.sensor.internal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
//Ricky 2013/12/09
import android.os.AsyncTask;
import android.util.Log;
import edu.missouri.niaaa.craving.Utilities;
import edu.missouri.niaaa.craving.services.SensorLocationService;

public class InternalRunnable implements Runnable, SensorEventListener {// haven't been used for now

	final static String TAG = "InternalRunnalbe";
	private SensorManager mSensorManager;
	private static InternalRunnable _instanceInternal = null;
	int SensorType;
	int SamplingRate;
	static int Count=0;
	static String Temp=null;
	String identifier;
	List<String> dataPoints=new ArrayList<String>();
	Calendar c=Calendar.getInstance();
	SimpleDateFormat curFormater;
	private List<Double> AccList = Collections.synchronizedList(new ArrayList<Double>());
    private double avgAcc = 0;

	/**
	 * Singleton Class
	 *
	 * @author Ricky
	 * @param sensorManager
	 * @param sensorType
	 * @param samplingRate
	 * @param uniqueIdentifier
	 * @return
	 */
	public static InternalRunnable getInstance(SensorManager sensorManager,
			int sensorType, int samplingRate, String uniqueIdentifier) {
		if (_instanceInternal == null) {
			_instanceInternal = new InternalRunnable(sensorManager, sensorType,
					samplingRate, uniqueIdentifier);
		}
		return _instanceInternal;
	}
	public InternalRunnable(SensorManager sensorManager,int sensorType,int samplingRate,String uniqueIdentifier)
	{
	    mSensorManager = sensorManager;
		SensorType=sensorType;
		SamplingRate=samplingRate;
		identifier=uniqueIdentifier;
	}


	@Override
	public void run()
	{  // TODO Auto-generated method stub
		Log.d("Sensor Number",String.valueOf(SensorType));
		setup(SensorType,SamplingRate);
	}

	public void setup(int sensorType, int samplingRate)
	{
		// TODO Auto-generated method stub
		mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(sensorType),samplingRate);
	}



	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	public String getDate()
	{
		curFormater = new SimpleDateFormat("MMMMM_dd");
   		String dateObj =curFormater.format(c.getTime());
   		return dateObj;
	}

	public String getTimeStamp()
	{
		Calendar cal=Calendar.getInstance();
   		cal.setTimeZone(TimeZone.getTimeZone("US/Central"));
		return String.valueOf(cal.getTime());
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// TODO Auto-generated method stub
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			// log.d("Type detected: ACC Sensor");
			double ResultAcc = getOverallAcc(event.values[0], event.values[1],
					event.values[2]);
			if (compressAccelerometerData(ResultAcc)) {
				Log.d(TAG, "get avg Acc data");
				DecimalFormat df = new DecimalFormat("#0.00000");
				String avgAccStr = df.format(avgAcc);
				Log.d(TAG, avgAccStr);
				String Accelerometer_Values = getTimeStamp() + ","
						+ String.valueOf(avgAccStr);
				dataPoints.add(Accelerometer_Values + ";");
				String file_name = "Accelerometer." + identifier + "." + getDate();

				String encDataToWrite = null;
				try {
					if (Utilities.WRITE_RAW) {
						Utilities.writeToFile(file_name, Accelerometer_Values);
					} else {
						encDataToWrite = Utilities.encryption(Accelerometer_Values);
						Utilities.writeToFileEnc(file_name, encDataToWrite);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.d(TAG, "dataPoints'size: " + dataPoints.size());
				if (dataPoints.size() == 57) {

					StringBuilder prefix_sb = new StringBuilder(Utilities.PREFIX_LEN);
					String prefix = file_name;
					prefix_sb.append(prefix);

					for (int i = prefix.length(); i <= Utilities.PREFIX_LEN; i++) {
						prefix_sb.append(" ");
					}

					List<String> subList = dataPoints.subList(0, 56);
					String data = subList.toString();
					String formattedData = data.replaceAll("[\\[\\]]", "");
					String enformattedData = null;
					try {
						enformattedData = Utilities.encryption(prefix_sb.toString() + formattedData);
					} catch (Exception e) {
						e.printStackTrace();
					}

					TransmitData transmitData = new TransmitData();
					transmitData.execute(enformattedData);

					Log.d(TAG, "Accelerometer Data Point Sent " + enformattedData);
					subList.clear();
					subList = null;
				}
			}
		} else if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
	            //TODO: get values

   				String LightIntensity= getTimeStamp()+","+event.values[0];
	        	String file_name="LightSensor."+identifier+"."+getDate()+".txt";
	            File f = new File(Utilities.PHONE_BASE_PATH,file_name);
	            /*
                dataPoints.add(LightIntensity+";");
	            if(dataPoints.size()==80)
	            {
	            	    List<String> subList = dataPoints.subList(0,41);
	     	            String data=subList.toString();
	     	            String formattedData=data.replaceAll("[\\[\\]]","");
	     	            //Ricky 2013/12/09
	     	            //sendDatatoServer("LightSensor."+identifier+"."+getDate(),formattedData);
	     	            TransmitData transmitData=new TransmitData();
	     	            transmitData.execute("LightSensor."+identifier+"."+getDate(),formattedData);
	     	            subList.clear();
	     	     }
	     	     */
	    		try {
					writeToFile(f,LightIntensity);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	       }

	private double getOverallAcc(float xACC, float yACC, float zACC) {
		return Math.sqrt(xACC * xACC + yACC * yACC + zACC * zACC)
				- SensorManager.STANDARD_GRAVITY;
	}

	protected static void writeToFile(File f, String toWrite) throws IOException{
		FileWriter fw = new FileWriter(f, true);
		fw.write(toWrite+'\n');
        fw.flush();
		fw.close();
	}



	protected static boolean checkDataConnectivity() {
		boolean value=SensorLocationService.checkDataConnectivity();
		return value;
	}

	public void stop()
	{
		mSensorManager.unregisterListener(this);
		_instanceInternal = null;
	}


	//Ricky 2013/12/09
	private class TransmitData extends AsyncTask<String,Void, Boolean>
	{

		@Override
		protected Boolean doInBackground(String... strings) {
			// TODO Auto-generated method stub
			String data = strings[0];

			//			 String fileName=strings[0];
			//	         String dataToSend=strings[1];
	         if(checkDataConnectivity())
	 		{
	         HttpPost request = new HttpPost(Utilities.UPLOAD_ADDRESS);
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


	/**
	 * @author Ricky
	 * @param rawAccelerometerData Resultant Value of three axis
	 * @return True/False
	 */
	private Boolean compressAccelerometerData(Double rawAccelerometerData){
		synchronized (AccList) {
			if (AccList.size() <= InternalRunnableUtilities.Accelerometer.ACC_INTERVAL_MAX_POINT_VAL) {
				AccList.add(rawAccelerometerData);
				return false;
			} else {
			avgAcc = 0;
			for (int i=0;i<AccList.size();i++){
				avgAcc +=AccList.get(i);
			}
			avgAcc /= AccList.size();
			AccList.clear();
			AccList.add(rawAccelerometerData);
			return true;
		}
	}
	}

}

class InternalRunnableUtilities {
	public class Accelerometer {
		public final static int ACC_INTERVAL_MAX_POINT_VAL = 250;
	}
}
