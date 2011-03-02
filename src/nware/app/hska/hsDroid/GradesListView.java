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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

/**
 * {@link ListActivity} zum anzeigen der Prüfungen
 * 
 * @author Oliver Eichner
 * 
 */
public class GradesListView extends ListActivity {
	private boolean showAllExams = false;
	private ExamAdapter m_examAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// layout festlegen
		setContentView(R.layout.grade_list_view);

		this.m_examAdapter = new ExamAdapter(this, R.layout.grade_row_item,
				noten.examStorage.getList());
		setListAdapter(this.m_examAdapter);
		getListView().setTextFilterEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about:
			// TODO add about dialog
			Toast.makeText(this, "You pressed about!", Toast.LENGTH_LONG)
					.show();
			return true;
		case R.id.examViewAll:
			showAllExams = true;
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);

			m_examAdapter.getFilter().filter("");
			return true;
		case R.id.examViewOnlyLast:
			showAllExams = false;

			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			m_examAdapter.getFilter().filter(getLastExamSem());
			m_examAdapter.notifyDataSetChanged();
			m_examAdapter.notifyDataSetChanged();
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}

	}

	/**
	 * Gibt das aktuelle Semester zurück
	 * 
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
			// wenn schon januar is, dann -1
			if (month == 1) {
				year--;
			}
			semString = "SoSe " + year;
		}
		return semString;
	}

	/**
	 * Prüfungs {@link ArrayAdapter} für {@link ListActivity}
	 * 
	 * @author Oliver Eichner
	 * 
	 */
	private class ExamAdapter extends ArrayAdapter<Exam> implements Filterable {
		private ArrayList<Exam> exams;
		private final Object mLock = new Object(); // FIXME ??
		private ExamFilter mFilter;

		public ExamAdapter(Context context, int textViewResourceId,
				ArrayList<Exam> exams) {
			super(context, textViewResourceId, exams);
			this.exams = exams;

			// reihenfolge umkehren

			Collections.reverse(this.exams);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parents) {
			View v = convertView;
			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.grade_row_item, null);
			}

			Exam ex = exams.get(position);
			if (ex != null) {
				TextView exName = (TextView) v.findViewById(R.id.examName);
				TextView exNr = (TextView) v.findViewById(R.id.examNr);
				TextView exAtt = (TextView) v.findViewById(R.id.examAttempts);
				TextView exGrade = (TextView) v.findViewById(R.id.examGrade);
				if (exName != null) {
					exName.setText(ex.getExamName());
				}
				if (exNr != null) {
					exNr.setText(ex.getExamNr());
				}
				if (exAtt != null) {
					exAtt.setText(this.getContext().getString(
							R.string.grades_view_attempt)
							+ ex.getAttempts());
				}
				if (exGrade != null) {
					if (!ex.isPassed()) {
						if (ex.getAttempts() > 1) {
							exGrade.setBackgroundColor(Color.RED);
							exGrade.setTextColor(Color.BLACK);
						} else {
							exGrade.setTextColor(Color.rgb(0xc4, 0x3B, 0x3B)); // TODO
																				// helleres
																				// rot
							exGrade.setBackgroundColor(Color.BLACK);
						}
					} else {
						exGrade.setTextColor(Color.rgb(0x87, 0xeb, 0x0c)); // Color.rgb(0x87,0xeb,0x0c
						exGrade.setBackgroundColor(Color.BLACK);
						// TODO android grün ;)
						// http://www.perbang.dk/rgb/A4C639/
						// # C1FF00 # AEE500
						// gingerbread ähnlich, bissel heller.. BEEB0C
					}
					exGrade.setText(ex.getGrade());
				}

			}
			return v;
		}

		@Override
		public Filter getFilter() {
			if (mFilter == null) {
				mFilter = new ExamFilter();
			}
			return mFilter;

		}

		@Override
		public int getCount() {
			return exams.size();

		}

		private class ExamFilter extends Filter {
			protected FilterResults performFiltering(CharSequence prefix) {
				// result objekt
				FilterResults results = new FilterResults();

				// wenn adapter array leer, hole original
				if (exams == null) {
					synchronized (mLock) { // Notice the declaration above
						exams = new ArrayList<Exam>(
								noten.examStorage.getArrayList());
					}
				}

				// kein prefix, also ganzes array übernehmen
				if (prefix == null || prefix.length() == 0) {
					synchronized (mLock) {
						results.values = noten.examStorage.getArrayList();
						results.count = noten.examStorage.getArrayList().size();
					}
				} else {
					// lower case
					String prefixString = prefix.toString().toLowerCase();

					// array kopieren
					final ArrayList<Exam> items = exams;
					final int count = items.size();
					final ArrayList<Exam> newItems = new ArrayList<Exam>(count);

					for (int i = 0; i < count; i++) {
						final Exam item = items.get(i);
						final String itemName = item.getSemester()
								.toLowerCase();

						// semester muss übereinstimmen
						if (itemName.equals(prefixString)) {
							newItems.add(item);
						}
					}

					// Set and return
					results.values = newItems;
					results.count = newItems.size();
				}

				return results;
			}

			protected void publishResults(CharSequence prefix,
					FilterResults results) {
				// noinspection unchecked
				exams = (ArrayList<Exam>) results.values;
				// Let the adapter know about the updated list
				if (results.count > 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}
		}
	}

}
