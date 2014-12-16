package edu.missouri.niaaa.craving.survey.question;

import java.util.ArrayList;
import java.util.Map;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.TypedValue;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import edu.missouri.niaaa.craving.survey.category.Answer;
import edu.missouri.niaaa.craving.survey.category.QuestionType;
import edu.missouri.niaaa.craving.survey.category.SurveyQuestion;

public class RadioQuestion extends SurveyQuestion {

/*	field*/
	boolean answered;
	String skipTo;

/*	constructor*/
	public RadioQuestion(String id){
		this.questionId = id;
		this.questionType = QuestionType.RADIO;
	}


/*	function*/
	@Override
	public LinearLayout prepareLayout(final Context c) {

		//Linearlayout
		LinearLayout layout = new LinearLayout(c);
		layout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
		layout.setOrientation(LinearLayout.VERTICAL);

		//question layout
		LinearLayout.LayoutParams QTextLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		QTextLayout.setMargins(10, 0, 0, 0);

		TextView questionText = new TextView(c);
		questionText.setText(getQuestion().replace("|", "\n"));
		//questionText.setTextAppearance(c, R.attr.textAppearanceLarge);
		questionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
		questionText.setLines(4);
		questionText.setLayoutParams(QTextLayout);


		//answer layout
		RadioGroup radioGroup = new RadioGroup(c);
		radioGroup.setOrientation(RadioGroup.VERTICAL);

		for(Answer ans: this.answers){
			LinearLayout.LayoutParams radioLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			radioLayout.setMargins(10, 0, 10, 0);

			RadioButton radio = new RadioButton(c);
			radio.setText(ans.getAnswerText());
			int size = (ans.getAnswerText().length()<35? 25 : 22);
			radio.setTextSize(TypedValue.COMPLEX_UNIT_DIP,size);
			radio.setLayoutParams(radioLayout);
			radio.setPadding(0, 10, 20, 10);

			radioGroup.addView(radio);

			answerViews.put(radio, ans);
			radio.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					Answer a = answerViews.get(buttonView);


					if(isChecked){
//						Log.d("final ", "answer text is "+a.getAnswerText()+" "+"answer getskip is "+a.getSkip());
						skipTo = a.getSkip();
						a.setSelected(true);
						for(Map.Entry<View, Answer> entry: answerViews.entrySet()){
							if(!entry.getValue().equals(a)){
								entry.getValue().setSelected(false);
							}
						}
					}
					else{
						a.setSelected(false);
					}

					answered = true;

					//dialog
					if(isChecked && a.hasOption()){
						new AlertDialog.Builder(c)
//					    .setTitle(R.string.bedtime_title)
					    .setMessage(a.getOption())
					    .setCancelable(false)
					    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
					        @Override
					        public void onClick(DialogInterface dialog, int which) {
					        	dialog.cancel();
					        }
					    })
					    .create().show();
					}
				}
			});

			//check the one that had been checked before
			if(ans.isSelected()){
				radio.setChecked(true);
			}
		}


		LinearLayout.LayoutParams RGroupLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		RGroupLayout.setMargins(0, 40, 0, 0);
		radioGroup.setLayoutParams(RGroupLayout);

		LinearLayout A_layout = new LinearLayout(c);
		A_layout.setOrientation(LinearLayout.VERTICAL);
		A_layout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
		A_layout.addView(radioGroup);

		layout.addView(questionText);
		layout.addView(A_layout);
		return layout;
	}


	@Override
	public boolean validateSubmit() {
		return answered;
	}

	@Override
	public String getSkip(){
		return skipTo;
	}

	@Override
	public ArrayList<String> getSelectedAnswers(){
		ArrayList<String> temp = new ArrayList<String>();
		for(Answer answer: answers){
			if(answer.isSelected()) {
				temp.add(answer.getId());
			}
		}
		return temp;
	}

	@Override
	public boolean clearSelectedAnswers(){
//		Log.d("final 3", "clear");
//		answers. = null;
		for(Answer answer: answers){
			answer.setSelected(false);
		}
		answered = false;
		return true;
	}
}
