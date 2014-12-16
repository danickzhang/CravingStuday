package edu.missouri.niaaa.craving.survey;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import edu.missouri.niaaa.craving.R;
import edu.missouri.niaaa.craving.Utilities;
import edu.missouri.niaaa.craving.survey.category.Answer;
import edu.missouri.niaaa.craving.survey.category.Category;
import edu.missouri.niaaa.craving.survey.category.Question;
import edu.missouri.niaaa.craving.survey.category.RandomCategory;

public class XMLSurveyActivity extends Activity {

	String TAG = "XML Survey Activity~~~~";

	//Layout question will be displayed on
    LinearLayout surveyLayout;
  	//Tell the parser which survey to use
    String surveyName;
    String surveyFile;
    boolean autoTriggered = false;
    //Button used to submit each question
    Button submitButton;
    Button backButton;
    //a serializable in an intent
    LinkedHashMap<String, List<String>> answerMap;
    //List of read categories
  	ArrayList<Category> cats = null;
    //Current category
  	Category currentCategory;
  	//Current question
  	Question currentQuestion;
  	//Will be set if a question needs to skip others
    boolean hasSkip = false;
    String skipFrom = null;
    //Category position in arraylist
  	int categoryNum;

  	SoundPool soundp;
	private HashMap<Integer, Integer> soundsMap;
	int soundDelay = 1000;
	Timer t;
	int streamID;
	String surveyTitle;
	String dialogTitle;

	Dialog pinDialog;
	Dialog reDialog;
	SharedPreferences shp;
	boolean underManuallyGoing = false;

	Calendar startCal;
	WakeLock wl;

	static Context context;

	private int randomSeq = -1; //haidong


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Utilities.Log_sys(TAG, "onCreate");

		context = this;

		shp = Utilities.getSP(this, Utilities.SP_SURVEY);
		surveyTitle = surveyName = getIntent().getStringExtra(Utilities.SV_NAME);
		dialogTitle = getString(R.string.pin_title);
		autoTriggered = getIntent().getBooleanExtra(Utilities.SV_AUTO_TRIGGERED, false);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "Gank");


		soundp = new SoundPool(2, AudioManager.STREAM_MUSIC, 100);
		soundsMap = new HashMap<Integer, Integer>();
		soundsMap.put(1, soundp.load(this, R.raw.alarm_sound, 1));
		t=new Timer();


		Log.d("ssssssssss", "reminder last is "+getIntent().getBooleanExtra(Utilities.SV_REMINDER_LAST, false));
		if(getIntent().getBooleanExtra(Utilities.SV_REMINDER_LAST, false)){
			Toast.makeText(getApplicationContext(), R.string.survey_timeout, Toast.LENGTH_LONG).show();
			Toast.makeText(getApplicationContext(), "2", Toast.LENGTH_LONG).show();

			String[] reminder = getReminderTimeStamp(context);
			try {
				String seq = "";
				int surSeq = shp.getInt(Utilities.SP_KEY_TRIGGER_SEQ_MAP.get(surveyName), -1);
				if (surSeq == 0) {
					surSeq = Utilities.MAX_TRIGGER_MAP.get(surveyName);
				}
				if (surveyName.equals(Utilities.SV_NAME_RANDOM)) {
					seq = "," + surSeq;
				}

				Utilities.writeEventToFile(context, getSurveyType(), getScheduleTimeStamp(),
						reminder[0], reminder[1], reminder[2],
						"", Utilities.sdf.format(Calendar.getInstance().getTime()) + seq);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//set result
			setResult(1);
			finish();
		}
		else if(autoTriggered){
			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			am.setStreamVolume(AudioManager.STREAM_MUSIC, Utilities.VOLUME, AudioManager.FLAG_PLAY_SOUND);

			acquireLock();

			prepareSound();

			Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	        v.vibrate(1000);

	        //prepare seq title and reminder title
	        Log.d("!!!!!!!!!!!!!!!!", ""+surveyTitle);
	        if(Utilities.SP_KEY_TRIGGER_SEQ_MAP.get(surveyName) != null){
	        	int i = shp.getInt(Utilities.SP_KEY_TRIGGER_SEQ_MAP.get(surveyName), 0);

	        	randomSeq = i; //haidong
				switch(i){
				case 0:
					surveyTitle = num2seq(Utilities.MAX_TRIGGER_MAP.get(surveyName))+surveyName;
					break;
				default:
					surveyTitle = num2seq(i)+surveyName;
				}
				Log.d("!!!!!!!!!!!!!!!!2", ""+surveyTitle);
				dialogTitle = getDialogTitle();

				//haidong
				if(surveyName.equals("RANDOM_ASSESSMENT")){
					Log.d("!!!!!!!!!!!!!!!!hhhhhhhhhhhh", "i="+i+" randomSeq="+randomSeq);

					String rsID = String.valueOf(randomSeq);
					Calendar rsT = Calendar.getInstance();
					String rsDate = (rsT.get(Calendar.MONTH)+1)+"/"+rsT.get(Calendar.DAY_OF_MONTH)+"/"+rsT.get(Calendar.YEAR);
					String uID = Utilities.getSP(this, Utilities.SP_LOGIN).getString(Utilities.SP_KEY_LOGIN_USERID, "0000");

					String data = null;
					try {
						data = Utilities.encryption(uID + "," + rsDate + "," + rsID + "," + "trigger");
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					ComplianceSignal triggerSignal = new ComplianceSignal();
					triggerSignal.execute(data);

				}
			}


		}else{
			//manually click in
			//excepts
			//1. manually do morning survey
			//2. after bedreport && before 3am can do initial drinking !!
			Log.d("6666666666666666666", "before 3 is "+ (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)<3));
			if(!Utilities.completedMorningToday(this) && !surveyName.equals(Utilities.SV_NAME_MORNING) && !surveyName.equals(Utilities.SV_NAME_DRINKING)
					&& !surveyName.equals(Utilities.SV_NAME_MOOD)  && !surveyName.equals(Utilities.SV_NAME_CRAVING)
					){
				//give an alert to show morning survey should be done first today.
				//or after 12:00 pm automatically cancel the restrict
				//except morning survey

				setResult(2);
				finish();
			}

			else if(surveyName.equals(Utilities.SV_NAME_DRINKING) &&
					Calendar.getInstance().get(Calendar.HOUR_OF_DAY)>=3 && Calendar.getInstance().get(Calendar.HOUR_OF_DAY)<12 && !Utilities.completedMorningToday(this)
					){

				setResult(2);
				finish();
			}

//			if(!Utilities.completedMorningToday(this) && !surveyName.equals(Utilities.SV_NAME_MORNING) && !surveyName.equals(Utilities.SV_NAME_DRINKING)
//					&& Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 3
//					&& Calendar.getInstance().getTimeInMillis() <
//							Utilities.getSP(XMLSurveyActivity.this, Utilities.SP_BED_TIME).getLong(Utilities.SP_KEY_BED_TIME_LONG, -1)
//							){//problem
//				//same as above
//				setResult(2);
//				finish();
//			}

//			if((!Utilities.completedMorningToday(this) && !surveyName.equals(Utilities.SV_NAME_MORNING) && !surveyName.equals(Utilities.SV_NAME_DRINKING)) ||
//					(!Utilities.completedMorningToday(this)
//					&& surveyName.equals(Utilities.SV_NAME_DRINKING)
//					&& Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 3
//					&& Calendar.getInstance().getTimeInMillis() <
//							Utilities.getSP(XMLSurveyActivity.this, Utilities.SP_BED_TIME).getLong(Utilities.SP_KEY_BED_TIME_LONG, -1)
//					)
//			){//problem
//				//same as above
//				setResult(2);
//				finish();
//			}
		}

		if(surveyName.equals(Utilities.SV_NAME_MORNING)){

		}

      	setTitle(Utilities.RELEASE? surveyName: surveyTitle);




		setContentView(R.layout.survey_layout);
		setListeners();

		//Initialize map that will pass questions and answers to service
        answerMap = new LinkedHashMap<String, List<String>>();
        //Tell the parser which survey to use
      	surveyFile = Utilities.SV_MAP.get(surveyName);

      	Utilities.Log(TAG, "survey file is "+surveyFile);

      	//Setup XML parser
      	XMLParser parser = new XMLParser();

		//Open the specified survey
		try {
			/* .parseQuestion takes an input source to the assets file,
			 * a context in case there are external files, a boolean for
			 * allowing external files, and a baseid that will be appended
			 * to question ids.  If boolean is false, no context is needed.
			 */
			cats = parser.parseQuestion(new InputSource(getAssets().open(surveyFile)),this,true,"");
		} catch (IOException e) {
			e.printStackTrace();
		}

//		Log.d("---------------^^^^^^^^______________", "start");
//		for(Category ca :cats){
//			Utilities.Log(TAG, "category is "+ca.getQuestionDesc());
//			Utilities.Log(TAG, "category contains questions "+ca.totalQuestions());
//			for(Question q: ca.getQuestions()){
//				Utilities.Log(TAG, "question id "+q.getId());
//				for(Answer a: q.getAnswers()){
//					Utilities.Log(TAG, "contains trigger "+a.hasSurveyTrigger()+" is selected "+a.isSelected()+" answer skipto "+a.getSkip());
//				}
//			}
//		}


		//Survey doesn't contain any categories
		if(cats == null){
			//surveyComplete();
		}
		//Survey contain categories
		else{
			//Set current category to the first category
			currentCategory = cats.get(0);
			//Setup the layout
			ViewGroup vg = setupLayout(nextQuestionLayout());
			if(vg != null) {
				setContentView(vg);
			}
		}


		pinDialog = PinCheckDialog(this);
		reDialog = retryDialog();
		pinDialog.show();

	}


	private void acquireLock() {
		// TODO Auto-generated method stub
		wl.acquire();
	}


	private String getDialogTitle(){

		int seq = shp.getInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, 1);
		Editor ed = Utilities.getSP(context, Utilities.SP_REMINDER_INFO).edit();
		String ts = Utilities.sdf.format(Calendar.getInstance().getTime());

        //write reminder timestamp to shared preferences
        if(seq == 3){
        	ed.putString(Utilities.SP_KEY_REMINDER_INFO_3, ts).commit();
        }
        else if(seq == 2){
        	ed.putString(Utilities.SP_KEY_REMINDER_INFO_2, ts).commit();
        	ed.putString(Utilities.SP_KEY_REMINDER_INFO_3, "").commit();
        }else{
        	ed.putString(Utilities.SP_KEY_REMINDER_INFO_1, ts).commit();
        	ed.putString(Utilities.SP_KEY_REMINDER_INFO_2, "").commit();
        	ed.putString(Utilities.SP_KEY_REMINDER_INFO_3, "").commit();
        }

		if(Utilities.RELEASE) {
			return getString(R.string.pin_title);
		}
		return getString(R.string.pin_title) + " for reminder "+ seq;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		Utilities.Log(TAG, "on new intent");

		//autoTriggered = getIntent().getBooleanExtra(Utilities.SV_AUTO_TRIGGERED, false);

		if(intent.getBooleanExtra(Utilities.SV_REMINDER_LAST, false)){
			Toast.makeText(getApplicationContext(), R.string.survey_timeout, Toast.LENGTH_LONG).show();
			Toast.makeText(getApplicationContext(), "1", Toast.LENGTH_LONG).show();

			String surName = intent.getStringExtra(Utilities.SV_NAME);
			SharedPreferences surShp = Utilities.getSP(this, Utilities.SP_SURVEY);

			String[] reminder = getReminderTimeStamp(context);
			try {
				String seq = "";
				int surSeq = surShp.getInt(Utilities.SP_KEY_TRIGGER_SEQ_MAP.get(surName), -1);
				if (surSeq == 0) {
					surSeq = Utilities.MAX_TRIGGER_MAP.get(surName);
				}
				if (surName.equals(Utilities.SV_NAME_RANDOM)) {
					seq = "," + surSeq;
				}

				Utilities.writeEventToFile(context, getSurveyType(), getScheduleTimeStamp(),
						reminder[0], reminder[1], reminder[2],
						"", Utilities.sdf.format(Calendar.getInstance().getTime()) + seq);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			finish();
		}

		else if(underManuallyGoing){

			Toast.makeText(getApplicationContext(), "manually one block the auto one!", Toast.LENGTH_LONG).show();
			Toast.makeText(getApplicationContext(), "4", Toast.LENGTH_LONG).show();

			String surName = intent.getStringExtra(Utilities.SV_NAME);
			SharedPreferences surShp = Utilities.getSP(this, Utilities.SP_SURVEY);

			if (!surName.equals(Utilities.SV_NAME_MORNING)) {
				try {
					// for under doing some survey MANUALLY, the new one will be skipped
					// Random
					// Drinking follow-ups

					String seq = "";
					int surSeq = surShp.getInt(Utilities.SP_KEY_TRIGGER_SEQ_MAP.get(surName), -1);
					if (surSeq == 0) {
						surSeq = Utilities.MAX_TRIGGER_MAP.get(surName);
					}
					if (surName.equals(Utilities.SV_NAME_RANDOM)) {
						seq = "," + surSeq;
					}

					Utilities.writeEventToFile(context, (surName.equals(Utilities.SV_NAME_RANDOM) ? Utilities.CODE_SKIP_BLOCK_SURVEY_RANDOM : Utilities.CODE_SKIP_BLOCK_SURVEY_DRINKING),
							"", "", "", "",
							"", Utilities.sdf.format(Calendar.getInstance().getTime()) + seq);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		else{//auto triggered
			acquireLock();

			playSound();

			shp = Utilities.getSP(this, Utilities.SP_SURVEY);
			dialogTitle = getDialogTitle();
			if(pinDialog.isShowing()) {
				pinDialog.dismiss();
			}
			pinDialog = PinCheckDialog(this);
			pinDialog.show();
		}

	}


	private String num2seq(int num){
		String seq = "";
		switch(num){
		case 1:
			seq = "1st ";
			break;
		case 2:
			seq = "2nd ";
			break;
		case 3:
			seq = "3rd ";
			break;
		default:
			seq = ""+num+"th ";
		}
		return seq;
	}

	private Dialog retryDialog(){

		return new AlertDialog.Builder(this)
		.setCancelable(false)
		.setTitle(R.string.pin_title_wrong)
		.setMessage(R.string.pin_message_wrong)
		.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

				pinDialog.show();
				dialog.cancel();

				Log.d("fffffffffffff", ""+Utilities.getSP(XMLSurveyActivity.this, Utilities.SP_SURVEY).getBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false) + " "+
						Utilities.getSP(XMLSurveyActivity.this, Utilities.SP_SURVEY).getString(Utilities.SP_KEY_SURVEY_UNDERREMINDERING, "nothing")+ " "+
						Utilities.getSP(XMLSurveyActivity.this, Utilities.SP_SURVEY).getInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, -1)
						);
			}
		})
		.create();
	}


	private Dialog PinCheckDialog(final Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		final View DialogView = inflater.inflate(R.layout.pin_input, null);
		TextView pinText = (TextView) DialogView.findViewById(R.id.pin_text);
		pinText.setText(R.string.pin_message);
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setCancelable(false);
		builder.setTitle(dialogTitle);
		builder.setView(DialogView);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {

				EditText pinEdite = (EditText) DialogView.findViewById(R.id.pin_edit);
				String pinStr = pinEdite.getText().toString();
				Utilities.Log("Pin Dialog", "pin String is "+pinStr);

				if (pinStr.equals(Utilities.getPWD(context))){
					Log.d("test", "b r o a d is "+ surveyName+" "+Utilities.BD_REMINDER_MAP.get(surveyName));
					soundp.stop(streamID);

					startCal = Calendar.getInstance();

					if(autoTriggered){
						//undergoing
						shp.edit().putBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, true).commit();

						//underreminder
//						shp.edit().putString(Utilities.SP_KEY_SURVEY_UNDERREMINDERING, surveyFile).commit();

						//notify broadcast to set timeout
						Intent it = new Intent(Utilities.BD_REMINDER_MAP.get(surveyName));
						it.putExtra(Utilities.SV_NAME, surveyName);
						XMLSurveyActivity.this.sendBroadcast(it);
					}
					else{
						//under manual
//						shp.edit().putBoolean("undermangoing", true).commit();
						underManuallyGoing = true;

					}

					dialog.cancel();
	        	}
	        	else {
	        		dialog.cancel();
	        		reDialog.show();
	        	}
	        	dialog.cancel();

			}
		});

		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
		    @Override
			public void onClick(DialogInterface dialog, int whichButton) {

		    	soundp.stop(streamID);
				finish();
		    }
		});

		return builder.create();
	}



	private void playSound(){
//		this.setVolumeControlStream(AudioManager.STREAM_ALARM);
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		am.setStreamVolume(AudioManager.STREAM_MUSIC, Utilities.VOLUME, AudioManager.FLAG_PLAY_SOUND);

		t.schedule(new StartSound(),soundDelay);

		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(1000);
	}


    private class StartSound extends TimerTask {
    	@Override
    	public void run(){

    		streamID = soundp.play(soundsMap.get(1), 1, 1, 1, 0, 1); // craving should be different
    	}
    }

	private void prepareSound(){
		soundp.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

			@Override
			public void onLoadComplete(SoundPool arg0, int arg1, int arg2) {
				// TODO Auto-generated method stub
				t.schedule(new StartSound(),soundDelay);
			}
		});
	}




//	protected LinearLayout nextQuestion(){
//		Utilities.Log("~~~~~~~~~~~~~~~~~~~~n", "start");
//		Question temp = null;
//		boolean done = false;
//
////		if(currentQuestion != null)
////			skipFrom = currentQuestion.getId();
//
//		do{
////			if(temp != null)
////    			answerMap.put(temp.getId(), null);
//
//    		//Simplest case: category has the next question
//    		temp = currentCategory.followingQuestion();
//
//    		//Category is out of questions, try to move to next category
//    		if(temp == null && (++categoryNum < cats.size())){
//    			Utilities.Log("~~~~~~~~~!!~~~~~~~~~", "category is out of questions");
//    			/* Advance the category.  Loop will get the question
//    			 * on next iteration.
//    			 */
//    			currentCategory = cats.get(categoryNum);
//    		}
//
//    		//Out of categories, survey must be done
//    		else if(temp == null){
//    			Utilities.Log("~~~~~~~~~!!~~~~~~~~~", "survey complete");
//    			//Log.d("XMLActivity","Should be done...");
//    			done = true;
//    			break;
//    			//surveyComplete();
//    		}
//
//    		else{
//    			Utilities.Log("~~~~~~~~~!!~~~~~~~~~", "get into this situation");
//    		}
//
//
//		}
//		while(temp == null);
//
//		if(done){
//    		//surveyComplete();
//    		return null;
//    	}
//    	else{
//    		currentQuestion = temp;
//    		Utilities.Log("~~~~~~~~~~~~~~~~~~~~n", currentQuestion.getId());
//    		return currentQuestion.prepareLayout(this);
//    	}
//
//	}
//
//	protected LinearLayout lastQuestion(){
//		Question temp = null;
//
//    	while(temp == null)
//    	{
//    		temp = currentCategory.previousQuestion();
//
//    		// out of current category, need to go previous category if any
//    		if(temp == null){
//    			Utilities.Log("~~~~~~~~~~~~~~~~~~~~l", "out of current category");
//
//    			//
//    			if(categoryNum - 1 >= 0){
//    				Utilities.Log("~~~~~~~~~~~~~~~~~~~~l", "have previous category");
//    				categoryNum--;
//    				currentCategory = cats.get(categoryNum);
//    				temp = null;
//    			}
//
//    			//First question in first category, return currentQuestion
//    			else{
//    				Utilities.Log("~~~~~~~~~~~~~~~~~~~~l", "in the very beginning");
//    				backButton.setText(R.string.btn_cancel);
//    				temp = currentQuestion;
//    			}
//    		}
//    		else{
//
//    			Utilities.Log("~~~~~~~~~~~~~~~~~~~~l", "else");
//    		}
//
//    	}
//    	currentQuestion = temp;
//    	Utilities.Log("~~~~~~~~~~~~~~~~~~~~l", currentQuestion.getId());
//    	return currentQuestion.prepareLayout(this);
//
//	}


	//Get the next question to be displayed
    protected LinearLayout nextQuestionLayout(){
//		Utilities.Log("~~~~~~~~~~~~~~~~~~~~next", "currentQ" + (currentQuestion != null ? currentQuestion.getSelectedAnswers().get(0) + currentQuestion.getSkip() : "null"));

    	Question temp = null;
    	boolean done = false;
    	boolean allowSkip = false;

    	if(currentQuestion != null && !hasSkip) {
			skipFrom = currentQuestion.getId();
		}


    	do{
    		if(temp != null) {
				answerMap.put(temp.getId(), null);
			}

    		//Simplest case: category has the next question
    		temp = currentCategory.nextQuestion();


    		//Category is out of questions, try to move to next category
    		if(temp == null && (++categoryNum < cats.size())){
    			/* Advance the category.  Loop will get the question
    			 * on next iteration.
    			 */
    			currentCategory = cats.get(categoryNum);
    			if(currentCategory instanceof RandomCategory && currentQuestion.getSkip() != null){
    				//Check if skip is in category
    				RandomCategory tempCat = (RandomCategory) currentCategory;
    				if(tempCat.containsQuestion(currentQuestion.getSkip())){
    					allowSkip = true;
    				}

    			}
    		}



    		//Out of categories, survey must be done
    		else if(temp == null){
    			//Log.d("XMLActivity","Should be done...");
    			done = true;
    			break;
    			//surveyComplete();
    		}

    	}while(temp == null ||
    			(currentQuestion != null && currentQuestion.getSkip() != null && !(currentQuestion.getSkip().equals(temp.getId()) || allowSkip)
    			&& ( !currentQuestion.getId().equals(temp.getId()) && temp.clearSelectedAnswers())
    			)
    		  );
		/*if(currentQuestion != null){
			answerMap.put(currentQuestion.getId(), currentQuestion.getSelectedAnswers());
		}*/

    	if(done){
    		//surveyComplete();
    		return null;
    	}
    	else{
    		currentQuestion = temp;
//    		Utilities.Log("~~~~~~~~~~~~~~~~~~~~n", currentQuestion.getId());
    		return currentQuestion.prepareLayout(this);
    	}

    }

    protected LinearLayout lastQuestionLayout(){
//    	Utilities.Log("~~~~~~~~~~~~~~~~~~~~last", "skipFrom"+ skipFrom);
    	Question temp = null;

    	while(temp == null){
//    		Utilities.Log("~~~~~~~~~while", "0 skipfrom "+skipFrom+"skipTo "+skipTo);
    		temp = currentCategory.lastQuestion();
    		//Log.d(TAG,"Trying to get previous question");
    		/*
    		 * If temp is null, this category is out of questions,
    		 * we need to go back to the previous category if it exists.
    		 */
    		if(temp == null){
//    			Utilities.Log("~~~~~~~~~", "1");
    			//Log.d(TAG,"Temp is null, probably at begining of category");
    			/* Try to go back a category, get the question on
    			 * the next iteration.
    			 */
    			if(categoryNum - 1 >= 0){
    				//Log.d(TAG,"Moving to previous category");
    				categoryNum--;
    				currentCategory = cats.get(categoryNum);
    			}
    			//First question in first category, return currentQuestion
    			else{
    				//Log.d(TAG,"No previous category, staying at current question");
    				backButton.setText(R.string.btn_cancel);
    				temp = currentQuestion;
    			}
    		}
    		/* A question with no answer must have been skipped,
    		 * skip it again.
    		 */
    		else if(temp != null && !temp.validateSubmit()){
    			//Log.d(TAG, "No answer, skipping question");
//    			Utilities.Log("~~~~~~~~~", "2 "+temp.getId()+" "+temp.validateSubmit());
    			temp = null;
    		}

    		if(temp != null && hasSkip && !temp.getId().equals(skipFrom)){
//    			Utilities.Log("~~~~~~~~~", "3 skipfrom"+skipFrom);
    			temp = null;
    		}
    		else if(temp != null && hasSkip){
//    			Utilities.Log("~~~~~~~~~", "4");
    			hasSkip = false;
    			skipFrom = null;
    		}
    		//Else: valid question, it will be returned.
    	}
    	currentQuestion = temp;
//    	Utilities.Log("~~~~~~~~~~~~~~~~~~~~l", currentQuestion.getId());

    	return currentQuestion.prepareLayout(this);
    }


	private void setListeners() {
		// TODO Auto-generated method stub

		/*
         * The same submit button is used for every question.
         * New buttons could be made for each question if
         * additional specific functionality is needed/
         */
        submitButton = new Button(this);
        backButton = new Button(this);
        submitButton.setText(R.string.btn_submit);
        backButton.setText(R.string.btn_cancel);

        submitButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if(currentQuestion.validateSubmit()){
					ViewGroup vg = setupLayout(nextQuestionLayout());
					if(vg != null){
						setContentView(vg);
					}
					backButton.setText(R.string.btn_previous);
				}

//				setTitle(surveyName);

			}
		});

        backButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

				ViewGroup vg = setupLayout(lastQuestionLayout());
				if(vg != null) {
					setContentView(vg);
				}

				if(backButton.getText().equals(XMLSurveyActivity.this.getString(R.string.btn_cancel))){
					onBackPressed();
				}

			}
		});


	}


	protected LinearLayout setupLayout(LinearLayout layout){
    	/* Didn't get a layout from nextQuestion(),
    	 * error (shouldn't be possible) or survey complete,
    	 * either way finish safely.
    	 */
    	if(layout == null){
    		surveyComplete();
    		return null;
    	}
    	else{
			//Setup LinearLayout
    		LinearLayout sv = new LinearLayout(getApplicationContext());
			//Remove submit button from its parent so we can reuse it
			if(submitButton.getParent() != null){
				((ViewGroup)submitButton.getParent()).removeView(submitButton);
			}
			if(backButton.getParent() != null){
				((ViewGroup)backButton.getParent()).removeView(backButton);
			}
			//Add submit button to layout

			LinearLayout.LayoutParams keepFull = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);

			RelativeLayout.LayoutParams keepBTTM = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
			keepBTTM.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

			//sv.setLayoutParams(keepFull);
			//layout.setLayoutParams(keepFull);

			LinearLayout rela = new LinearLayout(getApplicationContext());
			//rela.setLayoutParams(keepFull);

			LinearLayout buttonCTN = new LinearLayout(getApplicationContext());
			buttonCTN.setOrientation(LinearLayout.VERTICAL);
			buttonCTN.setLayoutParams(keepFull);

			buttonCTN.addView(submitButton);
			buttonCTN.addView(backButton);

			rela.addView(buttonCTN);
			layout.addView(rela);

			//layout.addView(submitButton);
			//layout.addView(backButton);
			//Add layout to scroll view in case it's too long
			sv.addView(layout);
			//Display scroll view
			setContentView(sv);
			return sv;
    	}
    }

	protected void surveyComplete(){

		boolean hasTrigger = false;
    	//Fill answer map for when it is passed to service
    	for(Category cat: cats){
//    		Utilities.Log(TAG, "category is "+cat.getQuestionDesc());
//    		Utilities.Log(TAG, "category contains questions "+cat.totalQuestions());
    		for(Question question: cat.getQuestions()){
//    			Utilities.Log(TAG, "question id "+question.getId());
    			answerMap.put(question.getId(), question.getSelectedAnswers());
    			//Here to target the first question of Drinking Follow-up
    			for(Answer answer: question.getAnswers()){
//    				Log.d("_________________________________","answer "+answer.getAnswerText()+" "+answer.getId()+" "+answer.hasSurveyTrigger());
//    				Utilities.Log(TAG, "contains trigger "+answer.hasSurveyTrigger()+" is selected "+answer.isSelected());
    				if(answer.isSelected() && answer.hasSurveyTrigger()){
    					hasTrigger = true;
//    					Log.d("_________________________________","has trigger");
    				}
    			}

//    			for(String answer: question.getSelectedAnswers()){
//    				Log.d("+++++++++++++++++++++++++++++","answer string "+answer);
//    			}
    		}
    	}
		//answerMap.put(currentQuestion.getId(), currentQuestion.getSelectedAnswers());

    	if(surveyName.equals(Utilities.SV_NAME_MORNING)){

    		//notify to set next day at noon (cancel today's Noon)
    		Intent i = new Intent(Utilities.BD_ACTION_DAEMON);
			i.putExtra(Utilities.BD_ACTION_DAEMON_FUNC, -1);
			sendBroadcast(i);

			Utilities.morningComplete(this, false);// as following
//    		//write complete time
//    		Utilities.getSP(this, Utilities.SP_BED_TIME).edit().putLong(Utilities.SP_KEY_MORNING_COMPLETE_TIME, Calendar.getInstance().getTimeInMillis()).commit();
//    		//update study day
//    		Utilities.updateStudyDay();
//    		//schedule random survey
//    		Utilities.scheduleRandomSurvey(this);

			// craving for those have sensor only
			setResult(3);

			new AlertDialog.Builder(this)
					.setTitle(R.string.morning_report_title4)
					.setMessage(R.string.morning_report_msg4)
					.setCancelable(false)
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							finish();
						}
					})
					.create().show();
    	}

    	//haidong
    	if(surveyName.equals(Utilities.SV_NAME_RANDOM)){

			String rsID = String.valueOf(randomSeq);
			Calendar rsT = Calendar.getInstance();
			String rsDate = (rsT.get(Calendar.MONTH)+1)+"/"+rsT.get(Calendar.DAY_OF_MONTH)+"/"+rsT.get(Calendar.YEAR);
			String uID = Utilities.getSP(this, Utilities.SP_LOGIN).getString(Utilities.SP_KEY_LOGIN_USERID, "0000");

			String data = null;
			try {
				data = Utilities.encryption(uID + "," + rsDate + "," + rsID + "," + "complete");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			ComplianceSignal completedSignal = new ComplianceSignal();
			completedSignal.execute(data);

    	}
    	//--

    	//schedule drinking follow-ups if current completion is "initial drinking" or "random survey" with condition
    	if(surveyName.equals(Utilities.SV_NAME_DRINKING) || hasTrigger){// and followup triggers followup
    		Utilities.triggerDrinkingFollowup(this);
			shp.edit().putBoolean(Utilities.SP_KEY_SURVEY_UNDERDRINKING, true).commit();
    	}

    	//notify broadcast to cancel timeout alarm
//    	Intent it = new Intent(Utilities.BD_REMINDER_MAP.get(surveyName));
//		it.putExtra(Utilities.SV_NAME, surveyName);
//		sendBroadcast(it);

		//recording
    	try {
			writeSurveyToFile(answerMap);

			if(autoTriggered) {
				Utilities.getSP(this, Utilities.SP_REMINDER_INFO).edit().clear().commit();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	Toast.makeText(this, R.string.survey_completed, Toast.LENGTH_LONG).show();

		//craving only
		if (!surveyName.equals(Utilities.SV_NAME_MORNING)) {
			finish();
		}
    }


	//java 1.6 style...
	private int getSurveyType(){
		if(surveyName.equals(Utilities.SV_NAME_MORNING)) {
			return Utilities.CODE_NAME_MORNING;
		} else if(surveyName.equals(Utilities.SV_NAME_DRINKING)) {
			return Utilities.CODE_NAME_DRINKING;
		} else if(surveyName.equals(Utilities.SV_NAME_MOOD)) {
			return Utilities.CODE_NAME_MOOD;
		} else if(surveyName.equals(Utilities.SV_NAME_CRAVING)) {
			return Utilities.CODE_NAME_CRAVING;
		} else if(surveyName.equals(Utilities.SV_NAME_RANDOM)) {
			return Utilities.CODE_NAME_RANDOM;
		} else if(surveyName.equals(Utilities.SV_NAME_FOLLOWUP)) {
			return Utilities.CODE_NAME_FOLLOW;
		} else {
			return -1;
		}
	}

	private String getScheduleTimeStamp(){

		long scheduleTimeStamp = 0;
		String scheduleTS = "";

		//default time to 12:00 at noon
//		Calendar d = Utilities.getMorningCal(Utilities.defHour, Utilities.defMinute);
		Calendar d = Utilities.getDefaultMorningCal(this);
    	long defTime = d.getTimeInMillis();

		//schedule timestamp for morning, random and followups
		if(autoTriggered){

			//sequence
			String triggerSeq = Utilities.SP_KEY_TRIGGER_SEQ_MAP.get(surveyName);
			int seq = shp.getInt(triggerSeq, 1);
			if(seq == 0) {
				seq = Utilities.MAX_TRIGGER_MAP.get(surveyName);
			}


			//for morning survey
        	if(surveyName.equals(Utilities.SV_NAME_MORNING)){
        		scheduleTimeStamp = Utilities.getSP(this, Utilities.SP_BED_TIME).getLong(Utilities.SP_KEY_BED_TIME_LONG, defTime);
        	}

    		//for random survey
        	else if(surveyName.equals(Utilities.SV_NAME_RANDOM)){
//		        		time = Calendar.getInstance().getTimeInMillis();
        		scheduleTimeStamp = Long.parseLong(Utilities.getSP(this, Utilities.SP_RANDOM_TIME).getString(Utilities.SP_KEY_RANDOM_TIME_SET, ""+scheduleTimeStamp).split(",")[seq-1]);
        	}

    		//for followup survey
        	else{
        		//followup setting time only works for schedule look-up
        		scheduleTimeStamp = Utilities.getSP(this, Utilities.SP_RANDOM_TIME).getLong(Utilities.SP_KEY_DRINKING_TIME_SET, scheduleTimeStamp) + (seq * Utilities.FOLLOWUP_IN_SECONDS*1000);
        	}
		}else{
			if(surveyName.equals(Utilities.SV_NAME_MORNING)){

//				return Utilities.sdf.format(Utilities.getDefaultMorningCal(this).getTimeInMillis());
				return Utilities.sdf.format(Utilities.getSP(this, Utilities.SP_BED_TIME).getLong(Utilities.SP_KEY_BED_TIME_LONG, defTime));
			} else {
				return scheduleTS;
			}

		}


		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(scheduleTimeStamp);
		return Utilities.sdf.format(c.getTime());
	}

	private String[] getReminderTimeStamp(Context context) {

		String[] value = { "", "", "" };
		if(autoTriggered){
			SharedPreferences sp = Utilities.getSP(context, Utilities.SP_REMINDER_INFO);
			value[0] = sp.getString(Utilities.SP_KEY_REMINDER_INFO_1, "");
			value[1] = sp.getString(Utilities.SP_KEY_REMINDER_INFO_2, "");
			value[2] = sp.getString(Utilities.SP_KEY_REMINDER_INFO_3, "");
			return value;
		} else {
			return value;
		}

	}

	protected void writeSurveyToFile(HashMap<String, List<String>> surveyData) throws IOException{

		Calendar endCal = Calendar.getInstance();

		String userID = Utilities.getSP(this, Utilities.SP_LOGIN).getString(Utilities.SP_KEY_LOGIN_USERID, "0000");
		int studyDay = Utilities.getStudyDay(this);
		int type = getSurveyType();

//		long scheduleTimeStamp = getScheduleTimeStamp();
		String scheduleTS = getScheduleTimeStamp();

		long startTimeStamp = startCal.getTimeInMillis();
		long endTimeStamp = endCal.getTimeInMillis();

		String startTS = Utilities.sdf.format(startCal.getTime());
		String endTS = Utilities.sdf.format(endCal.getTime());





		StringBuilder sb = new StringBuilder(100);

//		Calendar c = Calendar.getInstance();
//		c.setTimeInMillis(time);
		sb.append(endCal.getTime().toString());
		sb.append(",");

		String[] rem = getReminderTimeStamp(context);
		sb.append(userID + "," + studyDay + "," + type + "," + scheduleTS + "," + rem[0] + "," + rem[1] + "," + rem[2] + "," + startTS + "," + endTS + ",");

		List<String> sorted = new ArrayList<String>(surveyData.keySet());
		Collections.sort(sorted);

		for(int i = 0; i < sorted.size(); i++){
			String key = sorted.get(i);
			List<String> data = surveyData.get(key);
			sb.append(key+":");
			if(data == null || data.isEmpty()){
				sb.append("-1");
			}
			else{
				for(int j = 0; j < data.size(); j++){
					sb.append(data.get(j));
					if(i != data.size()-1) {
						sb.append("");
					}
				}
			}
			if(i != sorted.size()-1) {
				sb.append(",");
			}
		}

		//Ricky 2014/4/1
		//dealing with the random sequence
		if (surveyName.equals(Utilities.SV_NAME_RANDOM)) {
			//random sequence
			int i = shp.getInt(Utilities.SP_KEY_SURVEY_TRIGGER_SEQ_RANDOM, -1);
			sb.append(",seq:"+ (i==0? Utilities.MAX_TRIGGER_MAP.get(surveyName): i));
		}
//		sb.append("\n");



		//file name
			Calendar c=Calendar.getInstance();
			SimpleDateFormat curFormater = new SimpleDateFormat("MMMMM_dd");
			String dateObj =curFormater.format(c.getTime());
			String file_name=surveyName+"."+userID+"."+dateObj+".txt";

		StringBuilder prefix_sb = new StringBuilder(Utilities.PREFIX_LEN);
		String prefix = surveyName + "." + userID + "." + dateObj;
		prefix_sb.append(prefix);

		for (int i = prefix.length(); i <= Utilities.PREFIX_LEN; i++) {
			prefix_sb.append(" ");
		}

		/************************************************************************
		 * Chen
		 *
		 * Data encryption
		 * Stringbuilder sb -> String ensb
		 */
		String ensb = null;
		try {
			ensb = Utilities.encryption(prefix_sb.toString() + sb.toString());

			if(Utilities.WRITE_RAW) {
				Utilities.writeToFile(file_name, sb.toString());
			} else{
       	 		Utilities.writeToFileEnc(file_name, ensb);
       	 	}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


		//Ricky 2013/12/09
		TransmitData transmitData=new TransmitData();
		transmitData.execute(ensb);


		//for debug
		int i = shp.getInt(Utilities.SP_KEY_SURVEY_TRIGGER_SEQ_RANDOM, -1);
		Utilities.writeToFile("Event.txt",sb.substring(0, sb.indexOf("q"))+
				(surveyName.equals(Utilities.SV_NAME_RANDOM) ?  "seq:"+(i==0? Utilities.MAX_TRIGGER_MAP.get(surveyName): i) : ""));

	}



	private class TransmitData extends AsyncTask<String,Void, Boolean>
	{

		@Override
		protected Boolean doInBackground(String... strings) {
			// TODO Auto-generated method stub
			String data = strings[0];

			//			String fileName=strings[0];
			//	        String dataToSend=strings[1];
	        if(checkDataConnectivity())
	 		{

	        Log.d("((((((((((((((((((((((((", ""+Thread.currentThread().getId());
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
	     	Toast.makeText(XMLSurveyActivity.this, "@#$", Toast.LENGTH_LONG).show();
	     	return false;
	      }

		}

	}

	 public static boolean checkDataConnectivity() {
	    	ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo[] info = connectivity.getAllNetworkInfo();
				if (info != null) {
					for (int i = 0; i < info.length; i++) {
						if (info[i].getState() == NetworkInfo.State.CONNECTED) {
							return true;
						}
					}
				}
			}
			return false;
	}



//	=========================================================================================================================
//	=========================================================================================================================

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Utilities.Log_sys(TAG, "On back pressed");
		new AlertDialog.Builder(this)
		.setTitle(R.string.survey_cancel_title)
		.setMessage(R.string.survey_cancel_msg)
		.setCancelable(false)
		.setNegativeButton(android.R.string.cancel, null)
		.setPositiveButton(android.R.string.ok, new android.content.DialogInterface.OnClickListener() {

		    @Override
			public void onClick(DialogInterface arg0, int arg1) {

		    	String[] reminder = getReminderTimeStamp(context);
				try {
					String seq = "";
					int surSeq = shp.getInt(Utilities.SP_KEY_TRIGGER_SEQ_MAP.get(surveyName), -1);
					if (surSeq == 0) {
						surSeq = Utilities.MAX_TRIGGER_MAP.get(surveyName);
					}
					if (surveyName.equals(Utilities.SV_NAME_RANDOM)) {
						seq = "," + surSeq;
					}

					Utilities.writeEventToFile(context, getSurveyType(), getScheduleTimeStamp(),
							reminder[0], reminder[1], reminder[2],
							"", Utilities.sdf.format(Calendar.getInstance().getTime()) + seq);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		    	XMLSurveyActivity.super.onBackPressed();
		    }
		}).create().show();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Utilities.Log_sys(TAG, "onDestroy");

		soundp.stop(streamID);
		soundp.release();
		t=null;

		pinDialog.dismiss();
		reDialog.dismiss();
//		Log.d("ondestory undergooing", "shp "+Utilities.getSP(this, Utilities.SP_SURVEY).getBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false)+" "+
//		Utilities.getSP(this, Utilities.SP_SURVEY).getInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, -2));

		//!((max and false)  ||  (max+1 and true))
//		if(!(
//				(Utilities.getSP(this, Utilities.SP_SURVEY).getInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, Utilities.MAX_REMINDER) == Utilities.MAX_REMINDER &&
//						!Utilities.getSP(this, Utilities.SP_SURVEY).getBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false))
//				||
//				(Utilities.getSP(this, Utilities.SP_SURVEY).getInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, Utilities.MAX_REMINDER+1) == Utilities.MAX_REMINDER+1 &&
//						Utilities.getSP(this, Utilities.SP_SURVEY).getBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false))
//			)
//		){


		if(shp.getBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false)){
//			shp.edit().putBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, false).commit();
			shp.edit().putInt(Utilities.SP_KEY_SURVEY_REMINDER_SEQ, Utilities.MAX_REMINDER+2).commit();

	    	//notify broadcast to cancel timeout alarm
	    	Intent it = new Intent(Utilities.BD_REMINDER_MAP.get(surveyName));
			it.putExtra(Utilities.SV_NAME, surveyName);
			sendBroadcast(it);
		}
//		}

		else{
//			shp.edit().putBoolean("undermangoing", false).commit();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Utilities.Log_sys(TAG, "onPause");
		soundp.stop(streamID);
		if(wl.isHeld()) {
			wl.release();
		}
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Utilities.Log_sys(TAG, "onRestart");
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Utilities.Log_sys(TAG, "onResume");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Utilities.Log_sys(TAG, "onStart");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Utilities.Log_sys(TAG, "onStop");
	}

	//haidong from ricky
	private class ComplianceSignal extends AsyncTask<String,Void, Boolean>
	{

		@Override
		protected Boolean doInBackground(String... strings) {
			// TODO Auto-generated method stub
			String data = strings[0];
			//	         String UID = strings[0];
			//	         String Date = strings[1];
			//	         String RSID = strings[2];
			//	         String CMD = strings[3];
	         if(checkDataConnectivity())
	 		{
	         HttpPost request = new HttpPost(Utilities.COMPLIANCE_ADDRESS);
	         List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("data", data));
				//	         params.add(new BasicNameValuePair("category",CMD));
				//	         params.add(new BasicNameValuePair("UID",UID));
				//	         params.add(new BasicNameValuePair("Date",Date));
				//	         params.add(new BasicNameValuePair("RSID",RSID));
	         try {

	             request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
	             HttpResponse response = new DefaultHttpClient().execute(request);
	             if(response.getStatusLine().getStatusCode() == 200){
	                 String result = EntityUtils.toString(response.getEntity());
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
	     	return false;
	      }

		}

	}
}
