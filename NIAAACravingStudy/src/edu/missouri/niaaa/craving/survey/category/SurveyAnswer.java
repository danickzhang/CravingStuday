package edu.missouri.niaaa.craving.survey.category;


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
	@Override
	public void setAnswerText(String answerText){
		//this.answerText = answerText;
		this.answerText = answerText;
	}

	//action
	@Override
	public void setClear(boolean clear){
		this.clearOthers = clear;
	}

	@Override
	public void setExtraInput(boolean extraInput){
		this.extraInput = extraInput;
	}

	//trigger
	@Override
	public void setSurveyTrigger(String name) {
		this.triggerFile = name;
	}


	//
	@Override
	public void setSelected(boolean selected){
		this.selected = selected;
	}

	@Override
	public void setSkip(String id) {
		this.skipId = id;
	}

	@Override
	public void setOption(String opt){
		this.option = opt;
		hasOption = true;
	}



/*	getter*/
	@Override
	public String getId(){
		return this.answerId;
	}

	@Override
	public String getAnswerText(){
		return this.answerText;
	}

	@Override
	public String getAnswerInput(){
		return this.answerInput;
	}

	//action
	@Override
	public boolean checkClear(){
		return clearOthers;
	}

	@Override
	public boolean getExtraInput(){
		return this.extraInput;
	}


	//trigger
	@Override
	public String getTriggerFile() {
		return this.triggerFile;
	}

	@Override
	public boolean hasSurveyTrigger(){
		return hasTrigger;
	}


	//
	@Override
	public boolean isSelected(){
		return selected;
	}

	@Override
	public String getSkip() {
		return skipId;
	}

	@Override
	public String getOption(){
		return option;
	}

	@Override
	public boolean hasOption(){
		return hasOption;
	}



/*	function*/
	@Override
	public boolean equals(Answer answer){
		if(answer == null) {
			return false;
		}
		if(this.getId().equals(answer.getId()) &&
				this.getAnswerText().equals(answer.getAnswerText())) {
			return true;
		}
		return false;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		return super.clone();
	}


}
