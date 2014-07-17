package edu.missouri.niaaa.craving.survey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;

import edu.missouri.niaaa.craving.R;
import edu.missouri.niaaa.craving.SurveyBroadcast;
import edu.missouri.niaaa.craving.Utilities;
import edu.missouri.niaaa.craving.survey.category.Category;
import edu.missouri.niaaa.craving.survey.category.Question;
import edu.missouri.niaaa.craving.survey.category.RandomCategory;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class XMLSurveyActivity extends Activity {

	String TAG = "XML Survey Activity~~~~";
	
	//Layout question will be displayed on
    LinearLayout surveyLayout;
  	//Tell the parser which survey to use
    String surveyName;
    String surveyFile;
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
    boolean skipTo = false;
    String skipFrom = null;
    //Category position in arraylist
  	int categoryNum;
  	
  	SoundPool sp;
	private HashMap<Integer, Integer> soundsMap;
	Dialog alertDialog;
	Dialog alertDialog2;
	
	MediaPlayer mp;
	
	
	SharedPreferences sharedp;
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Utilities.Log_sys(TAG, "onCreate");
		
		sp = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
		soundsMap = new HashMap<Integer, Integer>();
		soundsMap.put(1, sp.load(this, R.raw.alarm_sound, 0));
		
		
        Intent it = new Intent("sounds_alarm");
        //sendBroadcast(it);
        
		if(getIntent().getBooleanExtra(Utilities.SP_KEY_SURVEY_REMINDER_LAST, false)){
			finish();
		}else{
	        mp = MediaPlayer.create(this, R.raw.alarm_sound);
	        mp.start();
		}
        
		
		setContentView(R.layout.survey_layout);
		setListeners();
		
		//Initialize map that will pass questions and answers to service
        answerMap = new LinkedHashMap<String, List<String>>();
        //Tell the parser which survey to use		
      	surveyName = getIntent().getStringExtra("survey_name");
      	surveyFile = getIntent().getStringExtra("survey_file");
      	Utilities.Log(TAG, "survey file is "+surveyFile);
      	
      	if(surveyFile.equals(Utilities.SV_FILE_MORNING)){
      		
      	}
      	
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
		
		for(Category ca :cats){
			Utilities.Log(TAG, "category is "+ca.getQuestionDesc());
			Utilities.Log(TAG, "category contains questions "+ca.totalQuestions());
			for(Question q: ca.getQuestions()){
				Utilities.Log(TAG, "question id "+q.getId());
			}
		}
			
		
		//Survey doesn't contain any categories
		if(cats == null){
			//surveyComplete();
		}
		//Survey contain categories
		else{
			//Set current category to the first category
			currentCategory = cats.get(0);
			//Setup the layout
			ViewGroup vg = setupLayout(nextQuestion());
			if(vg != null)
				setContentView(vg);
		}
		
		
		alertDialog = new AlertDialog.Builder(this)
		.setCancelable(false)
		.setTitle("verify")
		.setMessage("input pin")
		.setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() { 

			@Override 
			public void onClick(DialogInterface dialog, int which) { 
				// TODO Auto-generated method stub  
//				alertDialog2.show();
				
				Utilities.getSP(XMLSurveyActivity.this, Utilities.SP_SURVEY).edit().putBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, true).commit();
				
				Intent it = new Intent(Utilities.BD_ACTION_REMINDER_SURVEY);
				it.putExtra(Utilities.SV_NAME, surveyName);

				XMLSurveyActivity.this.sendBroadcast(it);
			} 
		})
		.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				// TODO Auto-generated method stub
				Utilities.getSP(XMLSurveyActivity.this, Utilities.SP_SURVEY).edit().putBoolean(Utilities.SP_KEY_SURVEY_UNDERGOING, true).commit();
				finish();
			}
		})
		.create();
		
		alertDialog2 = new AlertDialog.Builder(this)
		.setCancelable(false)
		.setTitle("Wrong")
		.setMessage("input again?")
		.setCancelable(false)
		.setPositiveButton("OK", new DialogInterface.OnClickListener() { 

			@Override 
			public void onClick(DialogInterface dialog, int which) { 
				// TODO Auto-generated method stub  
//				alertDialog.show();
			} 
		})
		.create();
		
		
		alertDialog.show();
		
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		Utilities.Log(TAG, "on new intent");
		
		if(intent.getBooleanExtra(Utilities.SP_KEY_SURVEY_REMINDER_LAST, false)){
			finish();
		}else{
		

		mp = MediaPlayer.create(this, R.raw.alarm_sound);
        mp.start();
		}
	}

    private Dialog UserPinDialog(Context context, final String ID) {  
        LayoutInflater inflater = LayoutInflater.from(this);
        final View textEntryView = inflater.inflate(R.layout.pin_input, null);
        
        TextView alert_text = (TextView) textEntryView.findViewById(R.id.pin_text);
        alert_text.setText("Please input 4-digit PIN for User: "+ID);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);  
        builder.setCancelable(false);
        builder.setTitle("Set User PIN");  
        //builder.setMessage("Please input 4-digit PIN for User: "+ID);
        builder.setView(textEntryView);  
        builder.setPositiveButton("OK",  
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	
                    	//check networking
         		        
                    }  
                });  
        builder.setNegativeButton("Cancel",  
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {  
                    	
                    }  
                });  
        return builder.create();  
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
    protected LinearLayout nextQuestion(){
    	Utilities.Log("~~~~~~~~~~~~~~~~~~~~n", "");
    	Question temp = null;
    	boolean done = false;
    	boolean allowSkip = false;
    	if(currentQuestion != null && !skipTo)
    		skipFrom = currentQuestion.getId();
    	do{
    		if(temp != null)
    			answerMap.put(temp.getId(), null);
    		//Simplest case: category has the next question
    		temp = currentCategory.followingQuestion();

    		//Category is out of questions, try to move to next category
    		if(temp == null && (++categoryNum < cats.size())){
    			/* Advance the category.  Loop will get the question
    			 * on next iteration.
    			 */
    			currentCategory = cats.get(categoryNum);
    			if(currentCategory instanceof RandomCategory &&
    					currentQuestion.getSkip() != null){
    				//Check if skip is in category
    				RandomCategory tempCat = (RandomCategory) currentCategory;
    				if(tempCat.containsQuestion(currentQuestion.getSkip())){
    					allowSkip = skipTo = true;
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
    			(currentQuestion != null && currentQuestion.getSkip() != null &&
    			!(currentQuestion.getSkip().equals(temp.getId()) || allowSkip))	);
		/*if(currentQuestion != null){
			answerMap.put(currentQuestion.getId(), currentQuestion.getSelectedAnswers());
		}*/
    	if(done){
    		//surveyComplete();
    		return null;
    	}
    	else{
    		currentQuestion = temp;
    		Utilities.Log("~~~~~~~~~~~~~~~~~~~~n", currentQuestion.getId());
    		return currentQuestion.prepareLayout(this);
    	}
    }
    
    protected LinearLayout lastQuestion(){
    	Question temp = null;
    	
    	while(temp == null){
    		Utilities.Log("~~~~~~~~~while", "0 skipfrom "+skipFrom+"skipTo "+skipTo);
    		temp = currentCategory.previousQuestion();
    		//Log.d(TAG,"Trying to get previous question");
    		/*
    		 * If temp is null, this category is out of questions,
    		 * we need to go back to the previous category if it exists.
    		 */
    		if(temp == null){
    			Utilities.Log("~~~~~~~~~", "1");
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
    			Utilities.Log("~~~~~~~~~", "2 "+temp.getId()+" "+temp.validateSubmit());
    			temp = null;
    		}
    		
    		if(temp != null && skipTo && !temp.getId().equals(skipFrom)){
    			Utilities.Log("~~~~~~~~~", "3 skipfrom"+skipFrom);
    			temp = null;
    		}
    		else if(temp != null && skipTo){
    			Utilities.Log("~~~~~~~~~", "4");
    			skipTo = false;
    			skipFrom = null;
    		}
    		//Else: valid question, it will be returned.
    	}
    	currentQuestion = temp;
    	Utilities.Log("~~~~~~~~~~~~~~~~~~~~l", currentQuestion.getId());
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
					ViewGroup vg = setupLayout(nextQuestion());
					if(vg != null){
						setContentView(vg);
					}
					backButton.setText(R.string.btn_previous);
				}
				
			}
		});
        
        backButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				
				ViewGroup vg = setupLayout(lastQuestion());
				if(vg != null)
					setContentView(vg);
			}
		});
        
        
	}


//	protected LinearLayout setupLayout(LinearLayout layout){
//    	/* Didn't get a layout from nextQuestion(),
//    	 * error (shouldn't be possible) or survey complete,
//    	 * either way finish safely.
//    	 */
//    	if(layout == null){
//    		surveyComplete();
//    		return null;
//    	}
//    	else{
//			//Setup LinearLayout
//    		LinearLayout sv = new LinearLayout(getApplicationContext());
//			//Remove submit button from its parent so we can reuse it
//			if(submitButton.getParent() != null){
//				((ViewGroup)submitButton.getParent()).removeView(submitButton);
//			}
//			if(backButton.getParent() != null){
//				((ViewGroup)backButton.getParent()).removeView(backButton);
//			}
//			//Add submit button to layout
//			
//			//LinearLayout.LayoutParams keepFull = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
//			
//			RelativeLayout.LayoutParams keepBTTM = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
//			keepBTTM.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//			
//			//sv.setLayoutParams(keepFull);
//			//layout.setLayoutParams(keepFull);
//			
//			RelativeLayout rela = new RelativeLayout(getApplicationContext());
//			//rela.setLayoutParams(keepFull);
//						
//			LinearLayout buttonCTN = new LinearLayout(getApplicationContext());
//			buttonCTN.setOrientation(LinearLayout.VERTICAL);
//			buttonCTN.setLayoutParams(keepBTTM);
//			
//			buttonCTN.addView(submitButton);
//			buttonCTN.addView(backButton);
//
//			rela.addView(buttonCTN);
//			layout.addView(rela);
//			
//			//layout.addView(submitButton);
//			//layout.addView(backButton);
//			//Add layout to scroll view in case it's too long
//			sv.addView(layout);
//			//Display scroll view
//			setContentView(sv);
//			return sv;
//    	}
//    }

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
    	
    	//Fill answer map for when it is passed to service
    	for(Category cat: cats){
    		for(Question question: cat.getQuestions()){
    			answerMap.put(question.getId(), question.getSelectedAnswers());
    			//Here to target the first question of Drinking Follow-up
    			if (cat.getQuestionDesc().equals("Drinking Follow-up")&&question.getId().equals("q611")){
    				//dAns is the answer of drink numbers user reported
    				String dAns = question.getSelectedAnswers().get(0);
    				if (!dAns.equals("0")){
//    					Intent drinkFollowUpScheduler = new Intent(SensorService.ACTION_DRINK_FOLLOWUP);
//    					getApplicationContext().sendBroadcast(drinkFollowUpScheduler);
    				}
    				//Log.d(TAG,dAns);
    				
    			}
    		}
    	}
		//answerMap.put(currentQuestion.getId(), currentQuestion.getSelectedAnswers());

    	
    	//Send to service
//    	Intent surveyResultsIntent = new Intent();
//    	surveyResultsIntent.setAction(INTENT_ACTION_SURVEY_RESULTS);
//    	surveyResultsIntent.putExtra(INTENT_EXTRA_SURVEY_NAME, surveyName);
//    	surveyResultsIntent.putExtra(INTENT_EXTRA_SURVEY_RESULTS, answerMap);
//    	surveyResultsIntent.putExtra(INTENT_EXTRA_COMPLETION_TIME, System.currentTimeMillis());
//    	if (surveyName.equalsIgnoreCase("RANDOM_ASSESSMENT")){
//    		randomSeq = getIntent().getIntExtra("random_sequence", 0);
//    		surveyResultsIntent.putExtra("random_sequence",randomSeq);
//    		Log.d("wtest","random's seq in SurveyActivity: "+randomSeq);
//    	}
//    	this.sendBroadcast(surveyResultsIntent);    	
    	//Alert user
    	Toast.makeText(this, "Survey Complete.", Toast.LENGTH_LONG).show();
    	
//    	if(surveyName.equalsIgnoreCase("DRINKING_FOLLOWUP") && surveyFile.equalsIgnoreCase("DrinkingFollowup.xml")){
//    		SensorService.drinkUpFlag = false;
//    	}
//    	cancelAllTimerTask();
//    	String EndLog = Calendar.getInstance().getTime().toString()+", "+surveyName+" survey is completed.";
//		TransmitData completeSurveyData=new TransmitData();
//		completeSurveyData.execute("EventSurvey."+String.valueOf(ID),EndLog);
    	/* Finish, this call is asynchronous, so handle that when
    	 * views need to be changed...
    	 */
    	finish();
    }











	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		Utilities.Log_sys(TAG, "On back pressed");
	}
	
//	=========================================================================================================================
//	=========================================================================================================================
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Utilities.Log_sys(TAG, "onDestroy");
		alertDialog.dismiss();
		alertDialog2.dismiss();
		
		if(mp != null){
			mp.stop();
			mp.release();
			mp = null;
		}
		
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Utilities.Log_sys(TAG, "onPause");
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
	
}
