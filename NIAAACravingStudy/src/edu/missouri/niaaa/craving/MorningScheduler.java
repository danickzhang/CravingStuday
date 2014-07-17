package edu.missouri.niaaa.craving;

import java.text.NumberFormat;
import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.TimePicker.OnTimeChangedListener;

public class MorningScheduler extends Activity {
	
	String TAG = "Morning Scheduler";
	
	TimePicker timePicker;
	Button setPicker;
	Button backButton;
	
	int hour = 12;
	int minute = 0;
	
	SharedPreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.morning_scheduler_layout);
		
		sp = getSharedPreferences(Utilities.SP_BED_TIME, MODE_PRIVATE);
		
		timePicker = (TimePicker) findViewById(R.id.morning_picker);
		setPicker = (Button) findViewById(R.id.btnSchedule);
		backButton = (Button) findViewById(R.id.btnReturn);
		
//		timePicker.setIs24HourView(true)
		timePicker.setCurrentHour(12);
		timePicker.setCurrentMinute(0);
		
		
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
				
				Calendar c = Calendar.getInstance();
				if(c.get(Calendar.HOUR_OF_DAY) > 3){
					//next day
					c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR)+1);
				}
				c.set(Calendar.HOUR_OF_DAY, hour);
				c.set(Calendar.MINUTE, minute);
				
				Editor editor = sp.edit();

//				editor.putStringSet(Utilities.SP_BED_TIME, arg1);
				editor.putInt(Utilities.SP_KEY_BED_TIME_HOUR, hour);
				editor.putInt(Utilities.SP_KEY_BED_TIME_MINUTE, minute);
				editor.putLong(Utilities.SP_KEY_BED_TIME_LONG, c.getTimeInMillis());
				editor.commit();

				Intent scheduleIntent = new Intent(Utilities.BD_ACTION_SCHEDULE_MORNING);
				scheduleIntent.putExtra(Utilities.SV_NAME, Utilities.SV_NAME_MORNING);
				getApplicationContext().sendBroadcast(scheduleIntent);
				
				NumberFormat nf = NumberFormat.getInstance();
				nf.setMinimumIntegerDigits(2);
				
				Toast.makeText(getApplicationContext(),"Set wake-up time at "+hour+":"+nf.format(minute),Toast.LENGTH_LONG).show();
				
				finish();
			}});
		
		backButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}});
	}

	
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
}
