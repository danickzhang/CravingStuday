package edu.missouri.niaaa.craving.survey.category;

import android.util.Log;

public class SurveyAnswer implements Answer, Cloneable {

	//constructor param
	protected String answerId;
	protected String answerText;
	protected String answerInput;
	
	//action param
	protected boolean clearOthers = false;
	protected boolean extraInput = false;
	
	//trigger param
	protected String triggerFile = null;
	protected boolean hasTrigger = false;
	
	// param
	protected boolean selected = false;
	protected String skipId;
	protected String option;
	protected boolean hasOption = false;
	
	
/*	constructor*/
	public SurveyAnswer(String id){
		this.answerId = id;
	}
	
	public SurveyAnswer(String id, String triggerFile){
		this(id);
		this.triggerFile = triggerFile;
		this.hasTrigger = true;
		
	}
	
	public SurveyAnswer(String id, String value, String answerText){
		this.answerId = id;
		this.answerText = value;
		this.answerInput = answerText;
	}
	
	
	
/*	setter*/
	public void setAnswerText(String answerText){
		//this.answerText = answerText;
		this.answerText = answerText;
	}
	
	//action
	public void setClear(boolean clear){
		this.clearOthers = clear;
	}
	
	public void setExtraInput(boolean extraInput){
		this.extraInput = extraInput;
	}
	
	//trigger
	public void setSurveyTrigger(String name) {
		this.triggerFile = name;
	}
	

	//
	public void setSelected(boolean selected){
		this.selected = selected;
	}
	
	public void setSkip(String id) {
		this.skipId = id;
	}
	
	public void setOption(String opt){
		this.option = opt;
		hasOption = true;
	}
	
	
	
/*	getter*/
	public String getId(){
		return this.answerId;
	}
	
	public String getAnswerText(){
		return this.answerText;
	}
	
	public String getAnswerInput(){
		return this.answerInput;
	}
	
	//action
	public boolean checkClear(){
		return clearOthers;
	}
	
	public boolean getExtraInput(){
		return this.extraInput;
	}
	
	
	//trigger
	public String getTriggerFile() {
		return this.triggerFile;
	}
	
	public boolean hasSurveyTrigger(){
		return hasTrigger;
	}
	
	
	//
	public boolean isSelected(){
		return selected;
	}
	
	public String getSkip() {
		return skipId;
	}
	
	public String getOption(){
		return option;
	}
	
	public boolean hasOption(){
		return hasOption;
	}
	

	
/*	function*/
	public boolean equals(Answer answer){
		if(answer == null) return false;
		if(this.getId().equals(answer.getId()) &&
				this.getAnswerText().equals(answer.getAnswerText()))
			return true;
		return false;		
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}

	
}
