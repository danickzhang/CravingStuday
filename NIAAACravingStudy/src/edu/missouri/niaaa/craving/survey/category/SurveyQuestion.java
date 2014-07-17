package edu.missouri.niaaa.craving.survey.category;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;


public abstract class SurveyQuestion implements Question {

	protected ArrayList<Answer> answers = new ArrayList<Answer>();
	protected HashMap<View, Answer> answerViews = new HashMap<View, Answer>();
	protected String questionText;
	protected String questionId;
	protected QuestionType questionType;
	
	
	public String getQuestion() {
		return questionText;
	}

	
	public void setQuestion(String questionText) {
		this.questionText = questionText;
		
	}

	
	public void addAnswer(Answer answer) {
		this.answers.add(answer);
	}

	
	public void addAnswers(ArrayList<Answer> answers) {
		this.answers.addAll(answers);
	}

	
	public void addAnswers(Answer[] answers) {
		for(Answer a: answers){
			this.answers.add(a);
		}
	}

	
	public ArrayList<Answer> getAnswers() {
		return answers;
	}

	
	public void setQuestionType(QuestionType type) {
		this.questionType = type;
	}

	
	public QuestionType getQuestionType() {
		return questionType;
	}

	
	public abstract LinearLayout prepareLayout(Context c);

	
	public abstract boolean validateSubmit();
	
	
	public abstract String getSkip();
	
	
	public String getId(){
		return questionId;
	}

}
