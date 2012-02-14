package de.nware.app.hsDroid.data;

import android.os.Parcel;
import android.os.Parcelable;

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
 * Komplexer Datentyp zum Speichern einzelner Prüfungen.
 * 
 * {@link Parcelable}
 * 
 * @author Oliver Eichner
 * @version 0.2
 */
public class Exam implements Parcelable {

	private String examNr = "";
	private String examName = "";
	private String semester = "";
	private String examDate = "";
	private String grade = "";
	private boolean passed = false;
	private String notation = "";
	private int attempts = 0;
	private String infoLink = "";
	private int infoID = 0;
	private String studiengang = "";

	/**
	 * Konstruktor für {@link Parcel}
	 */
	public Exam(Parcel in) {
		readFromParcel(in);
	}

	/**
	 * Konstruktor zum erstellen einer Prüfung
	 * 
	 * @param examNr
	 *            Die Prüfungs Nummer
	 * @param examName
	 *            Der name der Prüfung
	 * @param semester
	 *            Das Semester in dem geschrieben wurde
	 * @param examDate
	 *            Das Prüfungs Datum
	 * @param grade
	 *            Die erzielte Note
	 * @param passed
	 *            Bestanden Status
	 * @param notation
	 *            Vermerk zur Prüfung
	 * @param attempts
	 *            Versuche bis jetzt
	 * @param infoID
	 *            LinkID der Notenverteilung
	 */
	public Exam(String examNr, String examName, String semester, String examDate, String grade, boolean passed,
			String notation, int attempts, int infoID, String studiengang) {
		super();
		this.examNr = examNr;
		this.examName = examName;
		this.semester = semester;
		this.examDate = examDate;
		this.grade = grade;
		this.passed = passed;
		this.notation = notation;
		this.attempts = attempts;
		this.infoID = infoID;
		this.studiengang = studiengang;
	}

	public String getExamNr() {
		return examNr;
	}

	public String getExamName() {
		return examName;
	}

	public String getSemester() {
		return semester;
	}

	public String getExamDate() {
		return examDate;
	}

	public String getGrade() {
		return grade;
	}

	public boolean isPassed() {
		return passed;
	}

	public String getNotation() {
		return notation;
	}

	public int getAttempts() {
		return attempts;
	}

	public int getInfoID() {
		return infoID;
	}

	public String getStudiengang() {
		return studiengang;
	}

	// methoden für Parcel

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(examNr);
		dest.writeString(examName);
		dest.writeString(semester);
		dest.writeString(grade);
		dest.writeInt(passed ? 1 : 0);
		dest.writeString(notation);
		dest.writeInt(attempts);
		dest.writeString(infoLink);
		dest.writeInt(infoID);
		dest.writeString(studiengang);

	}

	public static final Creator<Exam> CREATOR = new Creator<Exam>() {

		public Exam createFromParcel(Parcel source) {
			return new Exam(source);
		}

		public Exam[] newArray(int size) {
			return new Exam[size];
		}
	};

	private void readFromParcel(Parcel in) {
		this.examNr = in.readString();
		this.examName = in.readString();
		this.semester = in.readString();
		this.examDate = in.readString();
		this.grade = in.readString();
		this.passed = in.readInt() == 1 ? true : false;
		this.notation = in.readString();
		this.attempts = in.readInt();
		this.infoLink = in.readString();
		this.infoID = in.readInt();
		this.studiengang = in.readString();
	}

}
