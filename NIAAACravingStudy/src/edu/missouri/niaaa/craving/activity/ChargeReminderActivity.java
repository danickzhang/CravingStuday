package edu.missouri.niaaa.craving.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import edu.missouri.niaaa.craving.R;

public class ChargeReminderActivity extends Activity {

	MediaPlayer myPlayer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("test", "onCreate chargerActivity");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_charge_reminder);

		myPlayer = new MediaPlayer();
		myPlayer = MediaPlayer.create(this, R.raw.alarm_sound);
		myPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
		myPlayer.setLooping(true);

		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(5000);

		myPlayer.start();

		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(R.string.charge_reminder_alert_title);
		builder.setMessage(R.string.charge_reminder_alert_message);
		builder.setCancelable(false);
		builder.setNeutralButton(R.string.charge_reminder_alert_button, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				myPlayer.stop();
				dialog.cancel();
				ChargeReminderActivity.this.finish();
			}
		});

		AlertDialog alert = builder.create();
		alert.show();

		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment())
					.commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.charge_reminder, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_charge_reminder, container, false);
			return rootView;
		}
	}

}
