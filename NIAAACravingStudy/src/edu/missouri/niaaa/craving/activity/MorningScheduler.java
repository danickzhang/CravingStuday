package edu.missouri.niaaa.craving.activity;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;
import edu.missouri.niaaa.craving.R;
import edu.missouri.niaaa.craving.Utilities;

public class MorningScheduler extends Activity {

	String TAG = "Morning Scheduler";

	TextView timeText;
	CheckBox timeBox;
	TimePicker timePicker;
	Button setPicker;
	Button backButton;

	int hour = Utilities.defHour;
	int minute = Utilities.defMinute;

	SharedPreferences sp;
	Calendar startBedReportCal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_morning_scheduler);
		startBedReportCal = Calendar.getInstance();

		sp = getSharedPreferences(Utilities.SP_BED_TIME, MODE_PRIVATE);
		boolean setDefault = (sp.getInt(Utilities.SP_KEY_BED_TIME_HOUR, -1) == -1?false:true);
		if(setDefault){
			hour = sp.getInt(Utilities.SP_KEY_BED_TIME_HOUR, -1);
			minute = sp.getInt(Utilities.SP_KEY_BED_TIME_MINUTE, -1);
		}

		timeText = (TextView) findViewById(R.id.morning_text);
		timeBox = (CheckBox) findViewById(R.id.morning_box);
		timePicker = (TimePicker) findViewById(R.id.morning_picker);
		setPicker = (Button) findViewById(R.id.btnSchedule);
		backButton = (Button) findViewById(R.id.btnReturn);

		timeText.setText(Utilities.getMorningTimeWithFlag(this));

		timeBox.setChecked(setDefault);
		timeBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if(arg1){
					Log.d("test", "checked");
					timePicker.setEnabled(false);
				}
				else{
					Log.d("test", "unchecked");
					timePicker.setEnabled(true);
				}
			}});


		timePicker.setEnabled(!setDefault);
//		timePicker.setIs24HourView(true)
		timePicker.setCurrentHour(hour);
		timePicker.setCurrentMinute(minute);
		timePicker.setOnTimeChangedListener(new OnTimeChangedListener(){

			@Override
			public void onTimeChanged(TimePicker arg0, int arg1, int arg2) {
				// TODO Auto-generated method stub

				Utilities.Log(TAG, "on time changed listener");

				hour = arg1;
				minute = arg2;

			}});

		setPicker.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				Utilities.Log(TAG, ""+hour+":"+minute);

				if(hour >= 3 && hour <12 || (hour == 12 && minute == 0)){
//				if(true){

					setAsDefault();
					Utilities.bedtimeComplete(MorningScheduler.this, hour, minute);//as following

//					//set flag for bedtime, press-in survey should be blocked
//					Utilities.morningReset(MorningScheduler.this);
//
//					//cancel all the running survey
//					Utilities.cancelSchedule(MorningScheduler.this);
//
//					//schedule for next morning
//					Utilities.scheduleMorningSurvey(MorningScheduler.this, hour, minute);
//
//					//next midnight
//					Intent i = new Intent(Utilities.BD_ACTION_DAEMON);
//					i.putExtra(Utilities.BD_ACTION_DAEMON_FUNC, -3);
//					sendBroadcast(i);

					NumberFormat nf = NumberFormat.getInstance();
					nf.setMinimumIntegerDigits(2);

					Toast.makeText(getApplicationContext(), getString(R.string.bedtime_set)+" "+nf.format(hour)+":"+nf.format(minute),Toast.LENGTH_LONG).show();
					nf = null;

					try {
						Utilities.writeEventToFile(MorningScheduler.this, Utilities.CODE_BEDTIME,
								Utilities.sdf.format(Utilities.getMorningCal(hour, minute).getTime()), "", "", "",
								Utilities.sdf.format(startBedReportCal.getTime()),
								Utilities.sdf.format(Calendar.getInstance().getTime()));
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					finish();
				}
				else{
					Toast.makeText(getApplicationContext(),R.string.bedtime_alert,Toast.LENGTH_LONG).show();
				}
			}});

		backButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}});
	}

	private void setAsDefault(){
		if(timeBox.isChecked()){
			sp.edit().putInt(Utilities.SP_KEY_BED_TIME_HOUR, hour).commit();
			sp.edit().putInt(Utilities.SP_KEY_BED_TIME_MINUTE, minute).commit();
		}
		else{
			sp.edit().putInt(Utilities.SP_KEY_BED_TIME_HOUR, -1).commit();
			sp.edit().putInt(Utilities.SP_KEY_BED_TIME_MINUTE, -1).commit();
		}
	}


	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
