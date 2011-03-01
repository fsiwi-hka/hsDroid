package nware.app.hska.noten;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Komplexer Datentyp zum Speichern einzelner Prüfungen
 * @author Oliver Eichner
 * @version 0.1
 */
public class Exam implements Parcelable {

	private String examNr="";
	private String examName="";
	private String semester="";
	private String examDate="";
	private String mark="";
	private boolean passed =false;
	private String notation="";
	private int attempts=0;

	/**
	 * Konstruktor für {@link Parcel}
	 */
	public Exam(Parcel in) {
		readFromParcel(in);
	}

	/**
	 * Konstruktor zum erstellen einer Prüfung
	 * @param examNr Die Prüfungs Nummer
	 * @param examName Der name der Prüfung
	 * @param semester Das Semester in dem geschrieben wurde
	 * @param examDate Das Prüfungs Datum
	 * @param mark Die erzielte Note
	 * @param passed Bestanden Status
	 * @param notation Vermerk zur Prüfung
	 * @param attempts Versuche bis jetzt
	 */
	public Exam(String examNr, String examName, String semester,
			String examDate, String mark, boolean passed, String notation,
			int attempts) {
		super();
		this.examNr = examNr;
		this.examName = examName;
		this.semester = semester;
		this.examDate = examDate;
		this.mark = mark;
		this.passed = passed;
		this.notation = notation;
		this.attempts = attempts;
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

	public String getMark() {
		return mark;
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

	//methoden für Parcel
	
	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(examNr);
		dest.writeString(examName);
		dest.writeString(semester);
		dest.writeString(mark);
		dest.writeInt(passed ? 1 : 0);
		dest.writeString(notation);
		dest.writeInt(attempts);

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
		this.mark = in.readString();
		this.passed = in.readInt() == 1 ? true : false;
		this.notation = in.readString();
		this.attempts = in.readInt();
	}

}
