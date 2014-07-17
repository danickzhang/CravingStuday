package edu.missouri.niaaa.craving.survey.question;

import java.util.ArrayList;
import java.util.Map;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
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

	boolean answered;
	String skipTo;
	
	public CheckQuestion(String id){
		this.questionId = id;
		this.questionType = QuestionType.CHECKBOX;
	}
	
	
	public LinearLayout prepareLayout(Context c) {
		
		LinearLayout layout = new LinearLayout(c);
		layout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
		layout.setOrientation(LinearLayout.VERTICAL);
		
		TextView questionText = new TextView(c);
		questionText.setText(getQuestion().replace("|", "\n"));
		//questionText.setTextAppearance(c, R.attr.textAppearanceLarge);
		questionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
		questionText.setLines(3);

//		LinearLayout.LayoutParams layoutq = new LinearLayout.LayoutParams(
//				 LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//		layoutq.setMargins(10, 10, 0, 0);
//		
//		
//		questionText.setLayoutParams(layoutq);		
		
		layout.addView(questionText);
		
		LinearLayout A_layout = new LinearLayout(c);
		A_layout.setOrientation(LinearLayout.VERTICAL);
		A_layout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
		
		for(Answer ans: this.answers){
			CheckBox temp = new CheckBox(c);
			temp.setText(ans.getValue());
			temp.setTextSize(TypedValue.COMPLEX_UNIT_DIP,15);
			//temp.setGravity(Gravity.TOP);
			//temp.setHeight(60);
			temp.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT,1));
			
			for(Map.Entry<View, Answer> entry: answerViews.entrySet()){
				if(entry.getValue().equals(ans) && entry.getValue().isSelected()){
					temp.setChecked(true);
				}
			}

			answerViews.put(temp, ans);
			temp.setOnCheckedChangeListener(new OnCheckedChangeListener(){
				
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					Answer a = answerViews.get(buttonView);
					skipTo = a.getSkip();
					if(isChecked){
						a.setSelected(true);
						if(a.checkClear()){
							for(Map.Entry<View, Answer> entry: answerViews.entrySet()){
								if(!entry.getValue().equals(a)){
									((CheckBox)entry.getKey()).setChecked(false);
									entry.getValue().setSelected(false);
								}
							}
							for(Map.Entry<View, Answer> entry: answerViews.entrySet()){
								if(!entry.getValue().equals(a)){
									((CheckBox)entry.getKey()).setEnabled(false);
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
			
//			LinearLayout.LayoutParams layouta = new LinearLayout.LayoutParams(
//					 LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//			layouta.setMargins(10, 10, 0, 0);
//			temp.setLayoutParams(layouta);
			A_layout.addView(temp);
		}
		layout.addView(A_layout);
		
		return layout;
	}

	
	public boolean validateSubmit() {
		boolean b = false;
		for(Answer answer: answers){
			b = b | answer.isSelected();
		}
		return b;
	}
	
	
	public ArrayList<String> getSelectedAnswers(){
		ArrayList<String> temp = new ArrayList<String>();
		for(Answer answer: answers){
			if(answer.isSelected())
				temp.add(answer.getId());
		}
		return temp;
	}
	
	public String getSkip(){
		return skipTo;
	}
}
