package edu.missouri.niaaa.craving;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.InputSource;

import edu.missouri.niaaa.craving.survey.SurveyInfo;
import edu.missouri.niaaa.craving.survey.XMLConfigParser;
import edu.missouri.niaaa.craving.survey.XMLSurveyActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
			setTitle("Self-Assessment Survey Menu");
			TextView tv = new TextView(this);
			tv.setText("Select a survey");
			linearLayout.addView(tv);
			for(SurveyInfo survey: surveys){
				Utilities.Log(TAG, survey.getDisplayName());
				Button b = new Button(this);
				b.setText(survey.getDisplayName());
				b.setPadding(0, 30, 0, 30);
				linearLayout.addView(b);
				
				b.setOnClickListener(new OnClickListener(){
					
					public void onClick(View v) {
						final SurveyInfo temp = buttonMap.get(v);
						Utilities.Log(TAG, temp.getDisplayName());
						Utilities.Log(TAG, temp.getDisplayName()+" "+temp.getFileName()+" "+temp.getName());
						
						// Morning Report
						// 1. should be done before noon
						// 2. only once per study day
						if(temp.getDisplayName().equals(getResources().getString(R.string.morning_report_name))){
							Calendar mT = Calendar.getInstance();
							Calendar noonT = Calendar.getInstance();
							noonT.set(Calendar.HOUR_OF_DAY, 12);
							noonT.set(Calendar.MINUTE, 0);
							noonT.set(Calendar.SECOND, 0);
							if (mT.after(noonT)){
								Alert(R.string.morning_report_title,R.string.morning_report_msg);
							}
							else if(false){//once per day
								
								Alert(R.string.morning_report_title2,R.string.morning_report_msg2);
							}
							else {
								launchSurvey(temp.getFileName(),temp.getName());
							}
						}
						
						//Confirm Initial Drinking
						else if (temp.getDisplayName().equals(getResources().getString(R.string.initial_drink_name))){
							Dialog alertDialog = new AlertDialog.Builder(SurveyMenu.this)
							.setCancelable(true)
							.setTitle(R.string.first_drink_title)
							.setMessage(R.string.first_drink_msg)
							.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() { 

								@Override 
								public void onClick(DialogInterface dialog, int which) { 
									// TODO Auto-generated method stub  
									launchSurvey(temp.getFileName(),temp.getName());
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
						
						else {
							launchSurvey(temp.getFileName(),temp.getName());
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

	
	private void launchSurvey(String FileName, String Name){
		/*Intent launchIntent = new Intent(getApplicationContext(), SurveyPinCheck.class);
		launchIntent.putExtra("survey_file", FileName);
		launchIntent.putExtra("survey_name", Name);
		startActivity(launchIntent);*/
		
		Intent launchIntent = new Intent(getApplicationContext(), XMLSurveyActivity.class);
		launchIntent.putExtra("survey_file", FileName);
		launchIntent.putExtra("survey_name", Name);
//		if (surveyName.equalsIgnoreCase("RANDOM_ASSESSMENT"))
//			launchIntent.putExtra("random_sequence", randomSeq);
		startActivity(launchIntent);
	}
	
}
