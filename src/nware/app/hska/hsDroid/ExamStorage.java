package nware.app.hska.hsDroid;

import nware.app.hska.hsDroid.Exam;

import java.util.ArrayList;

import android.R.integer;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Speicher zur Verwaltung von {@link Exam} Objekten
 * @author Oliver Eichner
 * @version 0.1
 */
public class ExamStorage implements Parcelable {

	/**
	 * {@link ArrayList} zum halten der Objekte
	 */
	private ArrayList<Exam> exams;

	/**
	 * Standart Konstruktor
	 */
	public ExamStorage() {
		exams = new ArrayList<Exam>();
	}

	/**
	 * Konstruktor für {@link Parcel}
	 * @param in {@link Parcel}
	 */
	public ExamStorage(Parcel in) {
		
		readFromParcel(in);
	}

	/**
	 * Gibt eine Liste mit {@link Exam} Objekten zurück
	 * @return {@link ArrayList} vom Typ {@link Exam} 
	 */
	public ArrayList<Exam> getArrayList(){
		return exams;
	}
	
	/**
	 * Fügt eine Prüfung hinzu.
	 * @param examNr {@link String} mit der Prüfungsnummer
	 * @param examName {@link String} mit dem Prüfungsnamen
	 * @param semester {@link String} mit dem Prüfungs Semester
	 * @param examDate {@link String} mit dem Prüfungsdatum
	 * @param grade {@link String} mit der erzielten Note
	 * @param passed {@link Boolean} Bestanden Status
	 * @param notation {@link String} Vermerk
	 * @param attempts {@link integer} bisher benötigte Versuche
	 */
	public void appendFach(String examNr, String examName,
			String semester, String examDate, String grade,
			Boolean passed, String notation, int attempts) {
		Exam tmpExam = new Exam(examNr, examName, semester,
				examDate, grade, passed, notation, attempts);
		exams.add(tmpExam);
	}

	/**
	 * Leert den {@link Exam} Speicher
	 */
	public void clear() {
		exams.clear();
	}

	/**
	 * Gibt die Anzahl der Prüfungen zurück
	 * @return
	 */
	public int getSize() {
		return exams.size();
	}

	/**
	 * Gibt eine Prüfung anhand eines Indexes zurück
	 * @param index {@link integer} Index der gewünschten Prüfung
	 * @return
	 */
	public Exam getExam(int index) {
		return exams.get(index);
	}

	// Parcel methoden
	public static final Parcelable.Creator<ExamStorage> CREATOR = new Parcelable.Creator<ExamStorage>() {

		public ExamStorage createFromParcel(Parcel in) {
			return new ExamStorage(in);
		}

		public ExamStorage[] newArray(int size) {
			return new ExamStorage[size];
		}

	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeTypedList(exams);
	}
	
	public void readFromParcel(Parcel in){
		this.exams = new ArrayList<Exam>();
		in.readTypedList(exams, Exam.CREATOR);
	}

}
