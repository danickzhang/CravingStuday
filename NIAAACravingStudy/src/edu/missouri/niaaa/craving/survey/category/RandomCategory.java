package edu.missouri.niaaa.craving.survey.category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;



public class RandomCategory extends SurveyCategory {

	public RandomCategory (){
		super();
	}

	@Override
	public void addQuestion(Question question) {
		super.addQuestion(question);
		if(nextQuestionNumber == 0) {
			Collections.shuffle(this.questions, new Random(System.currentTimeMillis()));
		}
	}

	@Override
	public void addQuestions(ArrayList<Question> newQuestions){
		super.addQuestions(newQuestions);
		if(nextQuestionNumber == 0) {
			Collections.shuffle(this.questions, new Random(System.currentTimeMillis()));
		}
	}

	public void addQuestions(SurveyQuestion[] newQuestions){
		super.addQuestions(newQuestions);
		if(nextQuestionNumber == 0) {
			Collections.shuffle(this.questions, new Random(System.currentTimeMillis()));
		}
	}

	public boolean containsQuestion(String questionId){
		for(Question question: this.questions){
			if(question.getId().equals(questionId)) {
				return true;
			}
		}
		return false;
	}
}
