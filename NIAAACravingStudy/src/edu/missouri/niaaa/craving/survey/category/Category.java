package edu.missouri.niaaa.craving.survey.category;

import java.util.ArrayList;
import java.util.List;



public interface Category {

	public Question nextQuestion();

	public Question lastQuestion();

	public Question getQuestion(int index);

	public void addQuestion(Question question);

	public void addQuestions(ArrayList<Question> newQuestions);

	public void addQuestions(Question[] newQuestions);

	//Ricky 2013/12/10 Add
//	public String getCurrentQuestionDesc();

	public String getQuestionDesc();

	public void setQuestionDesc(String desc);

	public int totalQuestions();

	public int currentQuestionIndex();

	public List<Question> getQuestions();
}
