package edu.missouri.niaaa.craving.survey.category;

import java.util.ArrayList;
import java.util.List;


public class SurveyCategory implements Category{

	protected ArrayList<Question> questions;
	protected int nextQuestionNumber = 0;
	protected String questionDesc;

	public SurveyCategory(){
		questions = new ArrayList<Question>();
	}

	public SurveyCategory(String questionDesc){
		this.questionDesc = questionDesc;
		questions = new ArrayList<Question>();
	}

	public SurveyCategory(String questionDesc, ArrayList<Question> questions){
		this.questionDesc = questionDesc;
		this.questions = new ArrayList<Question>();
		addQuestions(questions);
	}

	public SurveyCategory(String questionDesc, Question[] questions){
		this.questionDesc = questionDesc;
		this.questions = new ArrayList<Question>();
		addQuestions(questions);
	}

	@Override
	public Question nextQuestion(){
//		Utilities.Log("~~~~~~~~~~~~~~~~~~~~f", "index "+nextQuestionNumber);
		if((nextQuestionNumber) >= questions.size()){
			return null;
		}
//		Utilities.Log("~~~~~~~~~~~~~~~~~~~~", "index ");
		//get starts from 0, get current then ++
		return questions.get(nextQuestionNumber++);
	}


	@Override
	public Question lastQuestion(){
//		Utilities.Log("~~~~~~~~~~~~~~~~~~~~p", "index "+nextQuestionNumber);
		if(nextQuestionNumber == 0) {
			return null;
		} else {
			//			Utilities.Log("~~~~~~~~~~~~~~~~~~~~pp", "q "+questions.get(nextQuestionNumber-1).getId());
			return questions.get(--nextQuestionNumber);
		}
	}


	@Override
	public Question getQuestion(int index){
		if(index >= questions.size()){
			return null;
		}
		return questions.get(index);
	}


	@Override
	public void addQuestion(Question question){
		questions.add(question);
	}


	@Override
	public void addQuestions(ArrayList<Question> newQuestions){
		questions.addAll(newQuestions);
	}


	@Override
	public void addQuestions(Question[] newQuestions){
		for(Question q: newQuestions){
			questions.add(q);
		}
	}


	@Override
	public String getQuestionDesc(){
		return questionDesc;
	}


	@Override
	public void setQuestionDesc(String desc){
		this.questionDesc = desc;
	}


	@Override
	public int totalQuestions() {
		return questions.size();
	}


	@Override
	public int currentQuestionIndex() {
		return nextQuestionNumber;
	}


	@Override
	public List<Question> getQuestions(){
		return questions;
	}

}
