package de.nware.app.hsDroid.data;

/**
 *  This file is part of hsDroid.
 * 
 *  hsDroid is an Android App for students to view their grades from QIS Online Service 
 *  Copyright (C) 2011,2012  Oliver Eichner <n0izeland@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *  
 *  hsDroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  
 *  Diese Datei ist Teil von hsDroid.
 *  
 *  hsDroid ist Freie Software: Sie können es unter den Bedingungen
 *  der GNU General Public License, wie von der Free Software Foundation,
 *  Version 3 der Lizenz oder jeder späteren veröffentlichten Version, 
 *  weiterverbreiten und/oder modifizieren.
 *  
 *  hsDroid wird in der Hoffnung, dass es nützlich sein wird, aber
 *  OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
 *  Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
 *  Siehe die GNU General Public License für weitere Details.
 *  
 *  Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 *  Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */

/**
 * 
 * @author Oliver Eichner
 * 
 */
public class ExamInfo {

	// private Exam exam;
	private String sehrGutAmount;
	private String gutAmount;
	private String befriedigendAmount;
	private String ausreichendAmount;
	private String nichtAusreichendAmount;
	private String attendees;
	private String average;

	// public ExamInfo(Exam exam) {
	// this.exam = exam;
	// }

	public ExamInfo(String sehrGutAmount, String gutAmount,
			String befriedigendAmount, String ausreichendAmount,
			String nichtAusreichendAmount, String attendees, String average) {
		super();
		this.sehrGutAmount = sehrGutAmount;
		this.gutAmount = gutAmount;
		this.befriedigendAmount = befriedigendAmount;
		this.ausreichendAmount = ausreichendAmount;
		this.nichtAusreichendAmount = nichtAusreichendAmount;
		this.attendees = attendees;
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

	public String getAttendees() {
		return attendees;
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

	public void setAttendees(String attendees) {
		this.attendees = attendees;
	}

	public void setAverage(String average) {
		this.average = average;
	}
}
