package de.nware.app.hsDroid.data;

public class ExamInfo {

	// private Exam exam;
	private String sehrGutAmount;
	private String gutAmount;
	private String befriedigendAmount;
	private String ausreichendAmount;
	private String nichtAusreichendAmount;
	private String average;

	// public ExamInfo(Exam exam) {
	// this.exam = exam;
	// }

	public ExamInfo(String sehrGutAmount, String gutAmount, String befriedigendAmount, String ausreichendAmount,
			String nichtAusreichendAmount, String average) {
		super();
		this.sehrGutAmount = sehrGutAmount;
		this.gutAmount = gutAmount;
		this.befriedigendAmount = befriedigendAmount;
		this.ausreichendAmount = ausreichendAmount;
		this.nichtAusreichendAmount = nichtAusreichendAmount;
		this.average = average;
	}

	public String getSehrGutAmount() {
		return sehrGutAmount;
	}

	public String getGutAmount() {
		return gutAmount;
	}

	public String getBefriedigendAmount() {
		return befriedigendAmount;
	}

	public String getAusreichendAmount() {
		return ausreichendAmount;
	}

	public String getNichtAusreichendAmount() {
		return nichtAusreichendAmount;
	}

	public String getAverage() {
		return average;
	}

	public void setSehrGutAmount(String sehrGutAmount) {
		this.sehrGutAmount = sehrGutAmount;
	}

	public void setGutAmount(String gutAmount) {
		this.gutAmount = gutAmount;
	}

	public void setBefriedigendAmount(String befriedigendAmount) {
		this.befriedigendAmount = befriedigendAmount;
	}

	public void setAusreichendAmount(String ausreichendAmount) {
		this.ausreichendAmount = ausreichendAmount;
	}

	public void setNichtAusreichendAmount(String nichtAusreichendAmount) {
		this.nichtAusreichendAmount = nichtAusreichendAmount;
	}

	public void setAverage(String average) {
		this.average = average;
	}
}
