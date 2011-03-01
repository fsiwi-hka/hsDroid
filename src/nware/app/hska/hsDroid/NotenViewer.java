package nware.app.hska.hsDroid;

import java.util.Date;

import nware.app.hska.hsDroid.R;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Activity zum ziehen und anzeigen der Noten
 * @author Oliver Eichner
 * @version 0.1
 * 
 */
public class NotenViewer extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ScrollView scrollView = new ScrollView(this);

		TableLayout table = new TableLayout(this);
		table.setStretchAllColumns(true);
		table.setShrinkAllColumns(true);

		//String für das aktuelle Semester
		String sem = getActualSem();
		
		// Titel
		TableRow rowTitle = new TableRow(this);
		rowTitle.setGravity(Gravity.CENTER_HORIZONTAL);

		// title column/row
		TextView title = new TextView(this);
		title.setText(this.getString(R.string.title_view_marks)+" - "+sem);

		title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
		title.setGravity(Gravity.CENTER);
		title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

		TableRow.LayoutParams params = new TableRow.LayoutParams();
		params.span = 2;

		rowTitle.addView(title, params);
		table.addView(rowTitle);

		// table head row
		TableRow tableHeadRow = new TableRow(this);

		TextView headExamName = new TextView(this);
		headExamName.setText(this.getString(R.string.title_head_exam));
		headExamName.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

		TextView headMark = new TextView(this);
		headMark.setText(this.getString(R.string.title_head_mark));
		headMark.setTypeface(Typeface.DEFAULT, Typeface.BOLD);

		tableHeadRow.addView(headExamName);
		tableHeadRow.addView(headMark);

		table.addView(tableHeadRow);

		// Noten rows
		int examStoreSize = noten.examStorage.getSize();

		TableRow[] examRows = new TableRow[examStoreSize];
		TextView[] examName = new TextView[examStoreSize];
		TextView[] examMark = new TextView[examStoreSize];

		
		// for (int i = 0; i < examStoreSize; i++) {
		for (int i = examStoreSize-1; i >=0; i--) {
			if (noten.examStorage.getExam(i) != null
					&& noten.examStorage.getExam(i).getExamName() != null) {
				examRows[i] = new TableRow(this);

				// examRows[i].setBackgroundColor(Color.BLUE);
				examName[i] = new TextView(this);
				// System.out.println("lectureID:"+i);
				// lectureNamen[i].setText(lecStore.getLecture(i).getPruefungsText());
				examName[i].setText(noten.examStorage.getExam(i).getExamName());
				examName[i].setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
				// examName[i].setPadding(2, 2, 0, 2);
				// examName[i].setBackgroundColor(Color.GREEN);

				examMark[i] = new TextView(this);
				// lectureNote[i].setText(lecStore.getLecture(i).getNote());
				examMark[i].setMinimumWidth(100);
				examMark[i].setText(noten.examStorage.getExam(i).getMark());
				examMark[i].setTypeface(Typeface.DEFAULT, Typeface.NORMAL);
				// examMark[i].set Padding(0, 2, 2, 2);
				// examMark[i].setBackgroundColor(Color.GREEN);
				if (noten.examStorage.getExam(i).getSemester().equals(sem)) {
					examRows[i].setBackgroundColor(Color.rgb(0x97, 0xff, 0x97));

					examName[i].setTextColor(Color.BLACK);
					examMark[i].setTextColor(Color.BLACK);
				}
				if (!noten.examStorage.getExam(i).isPassed()) {

					examRows[i].setBackgroundColor(Color.rgb(0xc4, 0x3b, 0x3b));

					examName[i].setTextColor(Color.BLACK);
					examMark[i].setTextColor(Color.BLACK);
				}
				examRows[i].addView(examName[i]);
				examRows[i].addView(examMark[i]);

				table.addView(examRows[i]);
			} else {
				Log.d("VIEW ERROR", String.valueOf(i));
			}
		}

		scrollView.addView(table);
		setContentView(scrollView);
	}

	/**
	 * Gibt das aktuelle Semester zurück
	 * @return ein {@link String} ("WiSe XX/XX" oder "SoSe XX")
	 */
	private String getActualSem() {
		String semString = "";
		Date dt = new Date();
		int year = dt.getYear() - 100;
		int month = dt.getMonth() + 1;
		if (month < 4 || month > 9) {
			semString = "WiSe " + (year - 1) + "/" + year;
		} else {
			semString = "WiSe " + year;
		}
		return semString;
	}
}
