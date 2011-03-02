package nware.app.hska.hsDroid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * {@link ListActivity} zum anzeigen der Prüfungen
 * @author Oliver Eichner
 *
 */
public class GradesListView extends ListActivity {
	
	private ExamAdapter m_examAdapter;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// layout festlegen
		setContentView(R.layout.grade_list_view);

		// Liste für die Liste ;)
		List<Map<String, Object>> examList = new ArrayList<Map<String, Object>>();

		// Map für Prüfungen
		Map<String, Object> data;

		// füllen von data, einfügen von data in examList
		for (int index = noten.examStorage.getSize() - 1; index >= 0; index--) {
			data = new HashMap<String, Object>();

			try {
				data.put("exName", noten.examStorage.getExam(index)
						.getExamName());
				data.put("exNr", noten.examStorage.getExam(index).getExamNr());

				int versuch = noten.examStorage.getExam(index).getAttempts();
				if (versuch > 1) {
					data.put("exAttempts", "Versuche:"
							+ noten.examStorage.getExam(index).getAttempts());
				} else {
					data.put("exAttempts", "Versuch:"
							+ noten.examStorage.getExam(index).getAttempts());
				}

				data.put("exGrade", noten.examStorage.getExam(index).getGrade());
				examList.add(data);
			}

			catch (Resources.NotFoundException e) {
				Log.e("ExamAdapter:: Not Found Exception:", e.getLocalizedMessage());
				// ...
			}
		}

		this.m_examAdapter = new ExamAdapter(this, R.layout.grade_row_item, noten.examStorage.getList());
        setListAdapter(this.m_examAdapter);

	}

	/**
	 * Gibt das aktuelle Semester zurück
	 * @return ein {@link String} ("WiSe XX/XX" oder "SoSe XX")
	 */
	private String getLastExamSem() {
		String semString = "";
		Date dt = new Date();
		int year = dt.getYear() - 100;
		int month = dt.getMonth() + 1;
		if (month < 7 && month > 1) { // zwischen jan und jul ws anzeigen 
			semString = "WiSe " + (year - 1) + "/" + year;
		} else {// ansonsten ss
			//wenn schon januar is, dann -1
			if (month == 1){
				year--;
			}
			semString = "SoSe " + year;
		}
		return semString;
	}
	
	/**
	 * Prüfungs {@link ArrayAdapter} für {@link ListActivity}
	 * @author Oliver Eichner
	 *
	 */
	private class ExamAdapter extends ArrayAdapter<Exam> {
		private ArrayList<Exam> exams;
		public ExamAdapter(Context context, int textViewResourceId,
				ArrayList<Exam> exams) {
			super(context, textViewResourceId, exams);
			this.exams=exams;
			//reihenfolge umkehren
			Collections.reverse(this.exams);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parents){
			View v = convertView;
			if (v == null){
				LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.grade_row_item, null);
			}

			Exam ex = exams.get(position);
			if( ex != null){
				TextView exName = (TextView) v.findViewById(R.id.examName);
				TextView exNr = (TextView) v.findViewById(R.id.examNr);
				TextView exAtt = (TextView) v.findViewById(R.id.examAttempts);
				TextView exGrade = (TextView) v.findViewById(R.id.examGrade);
				if (exName != null){
					exName.setText(ex.getExamName());
				}
				if (exNr != null){
					exNr.setText(ex.getExamNr());
				}
				if (exAtt != null){
					exAtt.setText(this.getContext().getString(R.string.grades_view_attempt) +ex.getAttempts()); 
				}
				if (exGrade != null){
					if (!ex.isPassed()) {
						if (ex.getAttempts() >1) {
							exGrade.setBackgroundColor(Color.RED);
							exGrade.setTextColor(Color.BLACK);
						} else {
						exGrade.setTextColor(Color.rgb(0xc4, 0x3B, 0x3B)); //TODO helleres rot
						exGrade.setBackgroundColor(Color.BLACK);
						}
					} else {
						exGrade.setTextColor(Color.rgb(0x87,0xeb,0x0c)); //Color.rgb(0x87,0xeb,0x0c
						exGrade.setBackgroundColor(Color.BLACK);
						//TODO android grün ;)
						//http://www.perbang.dk/rgb/A4C639/
						//# C1FF00 # AEE500
						// gingerbread ähnlich, bissel heller.. BEEB0C 
					}
					exGrade.setText(ex.getGrade());
				}
				
				
			}
			return v;
		}

	}
}
