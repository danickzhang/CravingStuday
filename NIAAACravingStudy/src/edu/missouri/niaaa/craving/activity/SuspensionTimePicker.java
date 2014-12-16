package edu.missouri.niaaa.craving.activity;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import edu.missouri.niaaa.craving.R;
import edu.missouri.niaaa.craving.Utilities;

public class SuspensionTimePicker extends Activity {

	String TAG = "Suspension time picker";
//	String[] display = {"  15 minutes  ","  30 minutes  ","  45 minutes  ","  60 minutes  ","  1 hour & 15 minutes  ","  1 & half hour  ","  1 hour & 45 minutes  ","  2 hours  "};
	int seq = 0;
	int interval = Utilities.SUSPENSION_INTERVAL_IN_SECOND;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_suspension_picker);
		final SharedPreferences sp = getSharedPreferences(Utilities.SP_LOGIN, Context.MODE_PRIVATE);
		if(!sp.contains(Utilities.SP_KEY_SUSPENSION_TS)){
			sp.edit().putLong(Utilities.SP_KEY_SUSPENSION_TS, Calendar.getInstance().getTimeInMillis()).commit();
		}

		NumberPicker np = (NumberPicker) findViewById(R.id.suspension_picker);
		Button setPicker = (Button) findViewById(R.id.btnSuspension);
		Button backButton = (Button) findViewById(R.id.btnReturn);

		np.setMinValue(0);
		np.setMaxValue(Utilities.SUSPENSION_DISPLAY.length-1);
		np.setDisplayedValues(Utilities.SUSPENSION_DISPLAY);

		np.setOnValueChangedListener(new OnValueChangeListener(){

			@Override
			public void onValueChange(NumberPicker picker, int oldValue, int newValue) {
				// TODO Auto-generated method stub
				Utilities.Log(TAG, "selection is "+seq+" and item is "+Utilities.SUSPENSION_DISPLAY[seq]);

				seq = newValue;
			}});

		setPicker.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

//				section_6.setText("Break Suspension");
				Utilities.getSP(SuspensionTimePicker.this, Utilities.SP_SURVEY).edit().putBoolean(Utilities.SP_KEY_SURVEY_SUSPENSION, true).commit();
				sp.edit().putInt(Utilities.SP_KEY_SUSPENSION_CHOICE, seq + 1).commit();

				//set suspension alarm
				AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

				Intent breakIntent = new Intent(Utilities.BD_ACTION_SUSPENSION);
				breakIntent.putExtra(Utilities.SV_NAME, Utilities.SV_NAME_RANDOM);//useless
    			PendingIntent breakPi = PendingIntent.getBroadcast(getApplicationContext(), 0, breakIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
//				getApplicationContext().sendBroadcast(breakIntent);

    			am.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis()+(seq+1)*interval*1000, breakPi);

    			//close volume
    			AudioManager audiom = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
            	audiom.setStreamVolume(AudioManager.STREAM_MUSIC, 3, AudioManager.FLAG_PLAY_SOUND);

    			//set result and finish
				setResult(1);// set text to break suspension
				finish();
			}
		});

		backButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
