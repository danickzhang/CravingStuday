package edu.missouri.niaaa.craving.survey.category;

public interface Answer {

	public void setAnswerText(String answerText);

	public void setClear(boolean clear);
	public void setExtraInput(boolean extraInput);

	public void setSurveyTrigger(String name);

	public void setSelected(boolean selected);
	public void setSkip(String id);

	public void setOption(String opt);

	public String getId();
	public String getAnswerText();
	public String getAnswerInput();

	public boolean checkClear();
	public boolean getExtraInput();

	public String getTriggerFile();
	public boolean hasSurveyTrigger();

	public boolean isSelected();
	public String getSkip();

	public String getOption();
	public boolean hasOption();


	public boolean equals(Answer ans);
}
