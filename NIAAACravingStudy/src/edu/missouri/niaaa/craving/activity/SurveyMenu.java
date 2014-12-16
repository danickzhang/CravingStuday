package edu.missouri.niaaa.craving.activity;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.InputSource;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.missouri.niaaa.craving.R;
import edu.missouri.niaaa.craving.Utilities;
import edu.missouri.niaaa.craving.survey.SurveyInfo;
import edu.missouri.niaaa.craving.survey.XMLConfigParser;
import edu.missouri.niaaa.craving.survey.XMLSurveyActivity;

public class SurveyMenu extends Activity {


	String TAG = "XML SurveyMenu";
	List<SurveyInfo> surveys;
	HashMap<View, SurveyInfo> buttonMap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

//		ScrollView scrollView = new ScrollView(this);
		LinearLayout linearLayout = new LinearLayout(this);
		//linearLayout.addView(new Button(this));
		linearLayout.setOrientation(LinearLayout.VERTICAL);
//		scrollView.addView(linearLayout);

		//surveys = new ArrayList<SurveyInfo>();
		buttonMap = new HashMap<View, SurveyInfo>();

		XMLConfigParser configParser = new XMLConfigParser();

		//Try to read surveys from give file
		try {
			surveys = configParser.parseQuestion(new InputSource(getAssets().open("config.xml")));
		} catch (IOException e) {
			e.printStackTrace();
		}

		if(surveys == null){
			Toast.makeText(this, "Invalid configuration file", Toast.LENGTH_LONG).show();
			Utilities.Log_sys(TAG,"No surveys in config.xml");
			finish();
		}
		else{
			setTitle(R.string.survey_menu_title);
			TextView tv = new TextView(this);
			tv.setText(R.string.survey_menu_select);
			linearLayout.addView(tv);
			for(SurveyInfo survey: surveys){
				Utilities.Log(TAG, survey.getDisplayName());
				Button b = new Button(this);
				b.setText(survey.getDisplayName());
				b.setPadding(0, 30, 0, 30);
				linearLayout.addView(b);

				b.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						final SurveyInfo temp = buttonMap.get(v);
						Utilities.Log(TAG, temp.getDisplayName());
						Utilities.Log(TAG, temp.getDisplayName()+" "+temp.getFileName()+" "+temp.getName());

						// Morning Report
						// 1. only once per study day
						// 2. should be done before noon
						if(temp.getDisplayName().equals(getResources().getString(R.string.morning_report_name))){

							Calendar mT = Calendar.getInstance();
							Calendar noonT = Calendar.getInstance();
							noonT.set(Calendar.HOUR_OF_DAY, 12);
							noonT.set(Calendar.MINUTE, 20);
							noonT.set(Calendar.SECOND, 0);

							Calendar threeT = Calendar.getInstance();
							threeT.set(Calendar.HOUR_OF_DAY, 3);
							threeT.set(Calendar.MINUTE, 0);
							threeT.set(Calendar.SECOND, 0);

							if(Utilities.completedMorningToday(SurveyMenu.this)){
								Alert(R.string.morning_report_title,R.string.morning_report_msg);
							}
							else if(mT.after(noonT)){
								Alert(R.string.morning_report_title2,R.string.morning_report_msg2);
							}
							else if(mT.before(threeT)){
								Alert(R.string.morning_report_title3, R.string.morning_report_msg3);
							}
							else {
								launchSurvey(temp.getName());
							}
						}

						//Confirm Initial Drinking
						else if (temp.getDisplayName().equals(getResources().getString(R.string.initial_drink_name))){
							SharedPreferences shp = Utilities.getSP(SurveyMenu.this, Utilities.SP_SURVEY);
							if (shp.getBoolean(Utilities.SP_KEY_SURVEY_UNDERDRINKING, false)) {
								Alert(R.string.morning_report_title5, R.string.morning_report_msg5);
							} else {
								Dialog alertDialog = new AlertDialog.Builder(SurveyMenu.this)
								.setCancelable(true)
								.setTitle(R.string.first_drink_title)
								.setMessage(R.string.first_drink_msg)
								.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub
										launchSurvey(temp.getName());
									}
								})
								.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog, int which) {
										// TODO Auto-generated method stub

											}
								})
								.create();
								alertDialog.show();
							}
						}

						else {
							launchSurvey(temp.getName());
						}
					}
				});

				buttonMap.put(b, survey);
			}
		}

		setContentView(linearLayout);
	}

	protected void Alert(int title, int msg) {
		// TODO Auto-generated method stub
		Dialog alertDialog = new AlertDialog.Builder(SurveyMenu.this)
		.setCancelable(true)
		.setTitle(title)
		.setMessage(msg)
		.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

			}
		})
		.create();
		alertDialog.show();
	}


	private void launchSurvey(String Name){
		Intent launchIntent = new Intent(getApplicationContext(), XMLSurveyActivity.class);
		launchIntent.putExtra(Utilities.SV_NAME, Name);
//		if (surveyName.equalsIgnoreCase("RANDOM_ASSESSMENT"))
//			launchIntent.putExtra("random_sequence", randomSeq);
		startActivityForResult(launchIntent, 0);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		switch(requestCode){
		case 0:
			if(resultCode == 1){
				Toast.makeText(this, R.string.survey_timeout, Toast.LENGTH_LONG).show();
				Toast.makeText(getApplicationContext(), "3", Toast.LENGTH_LONG).show();
			}
			else if(resultCode == 2){
				Toast.makeText(this, R.string.morning_report_unfinished, Toast.LENGTH_LONG).show();
			}
			else if(resultCode == 3){
//				Toast.makeText(this, "morning complete", Toast.LENGTH_LONG).show();
//				new AlertDialog.Builder(this)
//			    .setTitle(R.string.morning_report_title4)
//			    .setMessage(R.string.morning_report_msg4)
//			    .setCancelable(false)
//			    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
//			        @Override
//			        public void onClick(DialogInterface dialog, int which) {
//			        	dialog.cancel();
//			        }
//			    })
//			    .create().show();
			}else{

			}

			break;
		default:

			break;
		}

	}

}
