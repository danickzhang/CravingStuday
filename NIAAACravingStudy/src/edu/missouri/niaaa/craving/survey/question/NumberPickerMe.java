package edu.missouri.niaaa.craving.survey.question;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

public class NumberPickerMe extends NumberPicker {

public NumberPickerMe(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

//	public NumberPickerMe(Context context, AttributeSet attrs) {
//	     super(context, attrs);
//	   }

	   @Override
	   public void addView(View child) {
	     super.addView(child);
	     updateView(child);
	   }

	   @Override
	   public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
	     super.addView(child, index, params);
	     updateView(child);
	   }

	   @Override
	   public void addView(View child, android.view.ViewGroup.LayoutParams params) {
	     super.addView(child, params);
	     updateView(child);
	   }

	   private void updateView(View view) {
	     if(view instanceof EditText){
	       ((EditText) view).setTextSize(30);
//	       ((EditText) view).setTextColor(Color.parseColor("#ffffff"));
	     }
	   }
}
