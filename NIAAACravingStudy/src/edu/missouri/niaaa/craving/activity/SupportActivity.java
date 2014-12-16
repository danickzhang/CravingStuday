package edu.missouri.niaaa.craving.activity;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;
import edu.missouri.niaaa.craving.R;
import edu.missouri.niaaa.craving.Utilities;
import edu.missouri.niaaa.mail.GmailSender;

public class SupportActivity extends Activity {

	String TAG = "Support Activity";

	RadioGroup rg;
	RadioButton rb;
	EditText et;
	Button btn_send;
	Button btn_back;

	String userID;
	String appVersion;
	String emailBody;
	String dateTime;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_support);

		rg = (RadioGroup) findViewById(R.id.support_set);
		et = (EditText) findViewById(R.id.support_edit);
		btn_send = (Button) findViewById(R.id.support_btn_send);
		btn_back = (Button) findViewById(R.id.support_btn_back);

		setListener();

		userID = Utilities.getSP(this, Utilities.SP_LOGIN).getString(Utilities.SP_KEY_LOGIN_USERID, "0000");
		try {
			PackageInfo pinfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
			appVersion = "ver." + pinfo.versionName + "." + pinfo.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		dateTime = Utilities.sdf.format(Calendar.getInstance().getTime());
		emailBody = "Technical Support Needed" + "\n"
				+ "User: " + userID + " running version on " + appVersion + " has reported on problem as follows:" + "\n\n"

				+ "UserID: " + userID + "\n"
				+ "AppVer: " + appVersion + "\n"
				+ "DateTime: " + dateTime + "\n";

	}

	private void setListener() {
		// TODO Auto-generated method stub

		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				// TODO Auto-generated method stub
				rb = (RadioButton) findViewById(arg0.getCheckedRadioButtonId());
				Log.d(TAG, "text is " + rb.getText().toString());
			}
		});

		btn_send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				if (rb == null || et == null) {

					new AlertDialog.Builder(SupportActivity.this)
							.setTitle(R.string.support_alert_title)
							.setPositiveButton(android.R.string.ok, null)
							.create()
							.show();
					return;
				}

				final String problems = rb.getText().toString();
				final String comments = et.getText().toString();
				final String title = "No not reply! BodySensorApp Tech Support - ID:" + userID + "@" + appVersion;
				final String body = emailBody
						+ "Category: " + problems + "\n"
						+ "Comments: " + "\n"
						+ "\"\n" +
						comments + "\n"
						+ "\"";

				Toast.makeText(SupportActivity.this, "Now sending... ", Toast.LENGTH_LONG).show();
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							GmailSender sender = new GmailSender("bodysensorapp@gmail.com", "xyz123ABC");
							sender.sendMail(
									title,
									body,
									"bodysensorapp@gmail.com",
									"bodysensorapp@gmail.com");
						} catch (Exception e) {
							Log.e("SendMail", e.getMessage(), e);
						}

						sendSMS("5732288570", "Someone needs tech support, please login public gmail account to see details. @" + dateTime);

					}
				}).start();

				finish();
			}
		});

		btn_back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				finish();
			}
		});

	}

	private void sendSMS(String phoneNumber, String message) {
		SmsManager sms = SmsManager.getDefault();
		sms.sendTextMessage(phoneNumber, null, message, null, null);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
