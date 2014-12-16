package edu.missouri.niaaa.craving.survey.question;

import java.util.ArrayList;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import edu.missouri.niaaa.craving.survey.category.Answer;
import edu.missouri.niaaa.craving.survey.category.QuestionType;
import edu.missouri.niaaa.craving.survey.category.SurveyQuestion;

public class RadioInputQuestion extends SurveyQuestion{

	String skipTo;
	String selectedText = "";
	Answer selectedAnswer;
	EditText editText;

	public RadioInputQuestion(String id){
		this.questionId = id;
		this.questionType = QuestionType.RADIOINPUT;
	}


	@Override
	public ArrayList<String> getSelectedAnswers() {
		ArrayList<String> temp = new ArrayList<String>();
		temp.add(selectedAnswer.getId()+"/"+selectedText);
		return temp;
	}


	@Override
	public LinearLayout prepareLayout(Context c) {

		LinearLayout layout = new LinearLayout(c);
		layout.setOrientation(LinearLayout.VERTICAL);

		TextView questionText = new TextView(c);
		questionText.setText(getQuestion().replace("|", "\n"));
		//questionText.setTextAppearance(c, R.attr.textAppearanceLarge);
		questionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,20);
		questionText.setLines(4);

		LinearLayout.LayoutParams layoutq = new LinearLayout.LayoutParams(
				 LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutq.setMargins(10, 15, 0, 0);


		questionText.setLayoutParams(layoutq);


		RadioGroup radioGroup = new RadioGroup(c);
		radioGroup.setOrientation(RadioGroup.VERTICAL);

		//RadioButton[] buttons = new RadioButton[this.answers.size()];

		editText = new EditText(c);
		editText.setEnabled(false);
		editText.addTextChangedListener(new TextWatcher(){

			@Override
			public void afterTextChanged(Editable arg0) {
				selectedText = arg0.toString();
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) { }
		});

		for(Answer ans: this.answers){
			RadioButton temp = new RadioButton(c);
			temp.setText(ans.getAnswerText());
			temp.setTextSize(TypedValue.COMPLEX_UNIT_DIP,15);


			LinearLayout.LayoutParams layouta = new LinearLayout.LayoutParams(
					 LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			layouta.setMargins(10, 20, 0, 0);
			temp.setLayoutParams(layouta);

			radioGroup.addView(temp);
			//temp.setText(ans.getAnswerText());

			answerViews.put(temp, ans);

			temp.setOnCheckedChangeListener(new OnCheckedChangeListener(){

				@Override
				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					if(isChecked){
						if(selectedAnswer != null) {
							selectedAnswer.setSelected(false);
						}
						Answer selected = answerViews.get(buttonView);
						selected.setSelected(true);
						selectedAnswer = selected;
						skipTo = selected.getSkip();
						if(selectedAnswer.getExtraInput()){
							editText.setEnabled(true);
						}
						else{
							editText.setEnabled(false);
						}
					}
				}
			});
		}

		LinearLayout.LayoutParams layoutp = new LinearLayout.LayoutParams(
				 LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutp.setMargins(0, 40, 0, 0);
		radioGroup.setLayoutParams(layoutp);

		layout.addView(questionText);
		layout.addView(radioGroup);
		layout.addView(editText);
		return layout;


	}


	@Override
	public String getSkip() {
		return skipTo;
	}

	@Override
	public boolean validateSubmit() {
		if(selectedAnswer == null) {
			return false;
		}
		boolean extraInput = selectedAnswer.getExtraInput();
		if((!extraInput) || (extraInput && selectedText.length() > 0)){
			return true;
		}
		return false;
	}

	@Override
	public boolean clearSelectedAnswers(){
//		answers = null;
//		answered = false;
		return true;
	}

}
