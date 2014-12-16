package edu.missouri.niaaa.craving.survey.question;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import android.widget.TextView;
import edu.missouri.niaaa.craving.survey.category.QuestionType;
import edu.missouri.niaaa.craving.survey.category.SurveyQuestion;

public class NumberQuestion extends SurveyQuestion {

	TextView counterText;
	boolean answered = false;
	int result = -1;
	String item = "number(s)";
	int min = 0;
	int max = 1;

	public NumberQuestion(String id){
		this.questionId = id;
		this.questionType = QuestionType.NUMBER;
	}


	@Override
	public LinearLayout prepareLayout(Context c) {
		this.item = this.answers.get(0).getAnswerText();
		this.min = Integer.parseInt(this.answers.get(1).getAnswerText());
		this.max = Integer.parseInt(this.answers.get(2).getAnswerText());
		if(result == -1) {
			result = this.min;
		}

		LinearLayout layout = new LinearLayout(c);
		layout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT));
		layout.setOrientation(LinearLayout.VERTICAL);

		LinearLayout A_layout = new LinearLayout(c);
		A_layout.setOrientation(LinearLayout.VERTICAL);
		A_layout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));

		TextView questionText = new TextView(c);
		questionText.setText(getQuestion().replace("|", "\n"));
		questionText.setTextAppearance(c, android.R.attr.textAppearanceLarge);
		questionText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,22);
		questionText.setLines(4);

		counterText = new TextView(c);
		counterText.setText(result + " " + item);
		counterText.setTextSize(TypedValue.COMPLEX_UNIT_DIP,22);

		LinearLayout.LayoutParams layoutt = new LinearLayout.LayoutParams(
				 LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		layoutt.setMargins(10,15,10,0);

		questionText.setLayoutParams(layoutt);
		counterText.setLayoutParams(layoutt);


		final NumberPickerMe np = new NumberPickerMe(c);
		np.setLayoutParams(layoutt);
		np.setMaxValue(max);
		np.setMinValue(min);
		np.setValue(result);

		answered = true;
		np.setOnValueChangedListener(new OnValueChangeListener(){

			@Override
			public void onValueChange(NumberPicker picker, int oldVal,
					int newVal) {
				// TODO Auto-generated method stub
				Log.d("_________", "number picker");
				result = newVal;
				np.setValue(result);
				counterText.setText(result + " " + item);
			}


		});

//		SeekBar sb = new SeekBar(c);
//		sb.setMax(15);
//		sb.setProgress(result);
//		sb.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
//			public void onProgressChanged(SeekBar seekBar, int progress,
//					boolean fromUser) {
//				if(fromUser){
//					result = progress;
//					counterText.setText(progress + " drinks");
//					answered = true;
//				}
//			}
//			public void onStartTrackingTouch(SeekBar seekBar) {		}
//			public void onStopTrackingTouch(SeekBar seekBar)  {		}
//		});

		layout.addView(questionText);

		A_layout.addView(counterText);
		A_layout.addView(np);
		layout.addView(A_layout);

//		layout.addView(counterText);
//		layout.addView(sb);
//		layout.addView(np);

		return layout;
	}


	@Override
	public boolean validateSubmit() {
		if(answered && result >= 0) {
			return true;
		}
		return false;
	}

	@Override
	public String getSkip(){
		return null;
	}


	@Override
	public ArrayList<String> getSelectedAnswers(){
		if(!validateSubmit()){
			ArrayList<String> temp = new ArrayList<String>();
			temp.add(Integer.valueOf(-1).toString());
			return temp;
		}
		ArrayList<String> temp = new ArrayList<String>();
		temp.add(Integer.valueOf(result).toString());
		return temp;
	}

	@Override
	public boolean clearSelectedAnswers(){
//		Log.d("final 3", "clear");
//		answers = null;
		result = -1;
		answered = false;
		return true;
	}
}
