package edu.missouri.niaaa.craving.services;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import edu.missouri.niaaa.craving.R;
import edu.missouri.niaaa.craving.sensor.SensorUtilities;

public class NotificationSystem extends Activity {


	AlarmManager am;
	Intent itTrigger;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setFinishOnTouchOutside(false);
//		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setTitle("Notification");

		setContentView(R.layout.notification_layout);

		am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		itTrigger = new Intent(SensorUtilities.ACTION_LOST_CONNECTION_REMINDER);

		Button button = (Button) findViewById(R.id.notify_btn);

		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				//				cancelConnectionReminder(itTrigger, am);
				Intent i = new Intent(SensorUtilities.ACTION_DISCONNECT_SENSOR);
				sendBroadcast(i);
				finish();
			}
		});
	}

	protected void cancelConnectionReminder(Intent itTrigger, AlarmManager am) {
		// TODO Auto-generated method stub
		PendingIntent piTrigger = null;

		for (int i = 0; i < 3; i++) {
			piTrigger = PendingIntent.getBroadcast(this, i, itTrigger, Intent.FLAG_ACTIVITY_NEW_TASK);

			am.cancel(piTrigger);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub

		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}



}
