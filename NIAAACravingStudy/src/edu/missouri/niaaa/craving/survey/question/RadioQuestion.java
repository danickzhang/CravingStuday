package edu.missouri.niaaa.craving.survey.question;

import java.util.ArrayList;
import java.util.List;

import edu.missouri.niaaa.craving.Utilities;
import edu.missouri.niaaa.craving.survey.XMLSurveyActivity;
import edu.missouri.niaaa.craving.survey.category.Answer;
import edu.missouri.niaaa.craving.survey.category.QuestionType;
import edu.missouri.niaaa.craving.survey.category.SurveyQuestion;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout.LayoutParams;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class RadioQuestion extends SurveyQuestion {
	
	boolean answered;
	String skipTo;
	Answer selectedAnswer;
	Context broadcastContext;
	public boolean has = false;
	
	public RadioQuestion(String id){
		this.questionId = id;
		this.questionType = QuestionType.RADIO;
	}
	
	
	public LinearLayout prepareLayout(Context c) {
		broadcastContext = c;
		LinearLayout layout = new LinearLayout(c);
		layout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
		layout.setOrientation(LinearLayout.VERTICAL);
		
		TextView questionText = new TextView(c);
		questionText.setText(getQuestion().replace("|", "\n"));
		//questionText.setTextAppearance(c, R.attr.textAppearanceLarge);
		questionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
		questionText.setLines(4);
		
		LinearLayout.LayoutParams layoutq = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutq.setMargins(10, 15, 0, 0);
		
		
		questionText.setLayoutParams(layoutq);

		
		RadioGroup radioGroup = new RadioGroup(c);
		radioGroup.setOrientation(RadioGroup.VERTICAL);
		
		List<Integer> answersTemp = new ArrayList<Integer>(); //Added by Haidong
		
		for(Answer ans: this.answers){
			RadioButton temp = new RadioButton(c);
			
			temp.setText(ans.getValue());
			temp.setTextSize(TypedValue.COMPLEX_UNIT_DIP,25);
			ans.checkClear();
			
			
			LinearLayout.LayoutParams layouta = new LinearLayout.LayoutParams(
					 LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			layouta.setMargins(10, 20, 0, 0);
			temp.setLayoutParams(layouta);
			
			radioGroup.addView(temp);

			answersTemp.add(temp.getId());//Added by Haidong
			
			//if(ans.isSelected()){
			//	temp.setChecked(true);
			//	temp.setSelected(true);
			//}

			answerViews.put(temp, ans);
			
			
			temp.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					if(isChecked){
						if(selectedAnswer != null) 
							selectedAnswer.setSelected(false);
						Answer selected = answerViews.get(buttonView);
						//selected.setSelected(true);
						selectedAnswer = selected;
						skipTo = selected.getSkip();
						Utilities.Log("~~~~~~~answered", "true");
						answered = true;
					}
				}
			});
		}
		
//------------------Haidong-------------------------
		if(this.getSelectedAnswers() != null){
			
			Log.d("this get selected answers",this.getSelectedAnswers().get(0));
			Log.d("answers temp",""+answersTemp.get(0));

			if(this.getSelectedAnswers().get(0).equals("y")){
				radioGroup.check(answersTemp.get(0));
			}
			if(this.getSelectedAnswers().get(0).equals("n")){
				radioGroup.check(answersTemp.get(1));
			}	
			if( (!this.getSelectedAnswers().get(0).equals("y")) && (!this.getSelectedAnswers().get(0).equals("n"))){
				radioGroup.check(answersTemp.get(Integer.parseInt(this.getSelectedAnswers().get(0))-1));
			}

		}
//-----------------------Haidong end------------------		
		
		LinearLayout.LayoutParams layoutp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutp.setMargins(0, 40, 0, 0);
		radioGroup.setLayoutParams(layoutp);
		
		LinearLayout A_layout = new LinearLayout(c);
		A_layout.setOrientation(LinearLayout.VERTICAL);
		A_layout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
		A_layout.addView(radioGroup);
		
		layout.addView(questionText);
		layout.addView(A_layout);
		return layout;
	}

	
	public boolean validateSubmit() {
		return answered;
	}

	public String getSkip(){
		return skipTo;
	}
	
	
	public ArrayList<String> getSelectedAnswers(){
		ArrayList<String> temp = new ArrayList<String>();
		if(selectedAnswer != null){
			temp.add(selectedAnswer.getId());
			
			if(selectedAnswer.hasSurveyTrigger()){
				long[] times = selectedAnswer.getTriggerTimes();
				String triggerName = selectedAnswer.getTriggerName();
				String triggerFile = selectedAnswer.getTriggerFile();
				Log.d("RADIO QUESTION","Times: "+times.length);
				int counter = 0;
				for(long time: times){
					Log.d("RadioQuestion","Time: "+time);
//					triggerSurvey(time, triggerName, triggerFile, counter++);
				}
				
				Log.d("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww", " contains trigger");
				XMLSurveyActivity.triggerFollowup = true;
				
			}
			broadcastContext = null;
			return temp;
		}
		return null;
	}
	
//	private void triggerSurvey(long time, String triggerFile, 
//			String triggerName, int counter){
//		Log.d("RADIO QUESTION","Triggering survey number: "+counter);
//		AlarmManager manager = 
//				(AlarmManager) broadcastContext.getSystemService(Context.ALARM_SERVICE);
//		Intent broadcast = new Intent(SensorService.ACTION_SCHEDULE_SURVEY);
//		broadcast.putExtra("doing", "trigger");
//		broadcast.putExtra(Answer.TRIGGER_NAME, triggerName);
//		broadcast.putExtra(Answer.TRIGGER_FILE, triggerFile);
//		broadcast.putExtra(Answer.TRIGGER_TIME, time);
//		broadcast.putExtra("id"+counter, counter);
//		PendingIntent temp = PendingIntent.getBroadcast(
//				broadcastContext, counter, broadcast, PendingIntent.FLAG_ONE_SHOT);
//		manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//				SystemClock.elapsedRealtime()+time, temp);
//
//	}
}
