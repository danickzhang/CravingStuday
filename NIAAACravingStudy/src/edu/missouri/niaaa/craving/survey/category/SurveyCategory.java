package edu.missouri.niaaa.craving.survey.category;

import java.util.ArrayList;
import java.util.List;

import edu.missouri.niaaa.craving.Utilities;


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
	
	public Question nextQuestion(){
		Utilities.Log("~~~~~~~~~~~~~~~~~~~~f", "index "+nextQuestionNumber);
		if((nextQuestionNumber) >= questions.size()){
			return null;
		}
		Utilities.Log("~~~~~~~~~~~~~~~~~~~~", "index ");
		//get starts from 0, get current then ++
		return questions.get(nextQuestionNumber++);
	}
	
	
	public Question lastQuestion(){
		Utilities.Log("~~~~~~~~~~~~~~~~~~~~p", "index "+nextQuestionNumber);
		if(nextQuestionNumber == 0)
			return null;
		else
			Utilities.Log("~~~~~~~~~~~~~~~~~~~~pp", "q "+questions.get(nextQuestionNumber-1).getId());
			return questions.get(--nextQuestionNumber);
	}
	
	
	public Question getQuestion(int index){
		if(index >= questions.size()){
			return null;
		}
		return questions.get(index);
	}
	
	
	public void addQuestion(Question question){
		questions.add(question);
	}
	
	
	public void addQuestions(ArrayList<Question> newQuestions){
		questions.addAll(newQuestions);
	}
	
	
	public void addQuestions(Question[] newQuestions){
		for(Question q: newQuestions){
			questions.add(q);
		}
	}
	
	
	public String getQuestionDesc(){
		return questionDesc;
	}
	
	
	public void setQuestionDesc(String desc){
		this.questionDesc = desc;
	}

	
	public int totalQuestions() {
		return questions.size();
	}

	
	public int currentQuestionIndex() {
		return nextQuestionNumber;
	}
	
	
	public List<Question> getQuestions(){
		return questions;
	}
	
}
