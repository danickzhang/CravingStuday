package edu.missouri.niaaa.craving;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class StartupIntentReceiver extends BroadcastReceiver {

	private String action="android.intent.action.MAIN";
	private String category="android.intent.category.LAUNCHER";
	private final int DELAY_TIME = 30*1000;

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		final Context t = context;
		Intent s = new Intent(context, MainActivity.class);
		s.setAction(action);
		s.addCategory(category);
		s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(s);
		Handler h = new Handler();
		h.postDelayed(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Utilities.scheduleAll(t);
//				Intent startScheduler = new Intent(Utilities.BD_ACTION_SCHEDULE_ALL);
//				startScheduler.putExtra(Utilities.SV_NAME, Utilities.SV_NAME_MORNING);//useless
//				t.sendBroadcast(startScheduler);

				Utilities.scheduleDaemon(t);
//				Intent i = new Intent(Utilities.BD_ACTION_DAEMON);
//				i.putExtra(Utilities.BD_ACTION_DAEMON_FUNC, 0);
//				t.sendBroadcast(i);
			}

		}, DELAY_TIME);
	}

}
