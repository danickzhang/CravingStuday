package edu.missouri.niaaa.craving.survey.question;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import edu.missouri.niaaa.craving.survey.category.Answer;
import edu.missouri.niaaa.craving.survey.category.QuestionType;
import edu.missouri.niaaa.craving.survey.category.SurveyQuestion;

public class CheckQuestion extends SurveyQuestion{

/*	field*/
	boolean answered;
	String skipTo;

/*	constuctor*/
	public CheckQuestion(String id){
		this.questionId = id;
		this.questionType = QuestionType.CHECKBOX;
	}

/*	function*/
	@Override
	public LinearLayout prepareLayout(Context c) {

		//LinearLayout
		LinearLayout layout = new LinearLayout(c);
		layout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
		layout.setOrientation(LinearLayout.VERTICAL);

		//question layout
		LinearLayout.LayoutParams layoutq = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutq.setMargins(10, 10, 0, 0);

		TextView questionText = new TextView(c);
		questionText.setText(getQuestion().replace("|", "\n"));
		//questionText.setTextAppearance(c, R.attr.textAppearanceLarge);
		questionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
		questionText.setLines(3);
		questionText.setLayoutParams(layoutq);

		layout.addView(questionText);

		LinearLayout A_layout = new LinearLayout(c);
		A_layout.setOrientation(LinearLayout.VERTICAL);
		A_layout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

		for(Answer ans: this.answers){
			CheckBox check = new CheckBox(c);
			check.setText(ans.getAnswerText());
			// different text size based on 1.how many checkboxes 2.how long each of the checkbox
			// size<9 -> 25; size>9 -> 17; size==9 -> 20 but if single length>25 chars -> 16
			int size = (this.answers.size()<9? 25: (this.answers.size()>9? 17: (ans.getAnswerText().length()<25? 20 : 16)));
			check.setTextSize(TypedValue.COMPLEX_UNIT_DIP,size);//textSize
			//temp.setGravity(Gravity.TOP);
			//temp.setHeight(60);
			check.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT,1));

			answerViews.put(check, ans);
			check.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
					Answer a = answerViews.get(buttonView);
//					Log.d("final check", "answer text is "+a.getAnswerText()+" "+"answer getskip is "+a.getSkip());
					skipTo = a.getSkip();//should put into if(isChecked)

					if(isChecked){
						a.setSelected(true);
						if(a.checkClear()){
							for(Map.Entry<View, Answer> entry: answerViews.entrySet()){
								if(!entry.getValue().equals(a)){
									((CheckBox)entry.getKey()).setChecked(false);
									((CheckBox)entry.getKey()).setEnabled(false);
									entry.getValue().setSelected(false);
								}
							}
						}
					}
					else{
						a.setSelected(false);
						if(a.checkClear()){
							for(Map.Entry<View, Answer> entry: answerViews.entrySet()){
								((CheckBox)entry.getKey()).setEnabled(true);
							}
						}
					}
				}
			});

			//recheck the ones that had been checked before
			for(Map.Entry<View, Answer> entry: answerViews.entrySet()){
				if(entry.getValue().equals(ans) && entry.getValue().isSelected()){
					check.setChecked(true);
				}
			}

//			LinearLayout.LayoutParams layouta = new LinearLayout.LayoutParams(
//					 LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//			layouta.setMargins(10, 10, 0, 0);
//			temp.setLayoutParams(layouta);

			A_layout.addView(check);
		}
		layout.addView(A_layout);

		return layout;
	}


	@Override
	public boolean validateSubmit() {
		boolean b = false;
		for(Answer answer: answers){
			b = b | answer.isSelected();
		}
		return b;
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
//		answers = null;
		for(Answer answer: answers){
			answer.setSelected(false);
		}
		answered = false;
		return true;
	}
}
