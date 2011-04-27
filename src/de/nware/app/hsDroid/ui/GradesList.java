package de.nware.app.hsDroid.ui;

import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import de.nware.app.hsDroid.R;
import de.nware.app.hsDroid.data.Exam;
import de.nware.app.hsDroid.data.ExamInfo;
import de.nware.app.hsDroid.logic.ExamInfoParserThread;
import de.nware.app.hsDroid.logic.GradeParserThread;

/**
 * {@link ListActivity} zum anzeigen der Prüfungen
 * 
 * @author Oliver Eichner
 * 
 */
public class GradesList extends ListActivity {

	private ExamAdapter m_examAdapter;
	private ListView lv;
	private ArrayList<Exam> examsTest;
	private ExamInfo currentEInfo;
	private GradeParserThread mGradeParserThread = null;
	private ExamInfoParserThread mExamInfoParserThread = null;

	private ProgressDialog mProgressDialog = null;

	private SharedPreferences mPreferences;

	private static final byte DIALOG_PROGRESS = 1;

	private static final String SORT_ALL = "";
	private static final String SORT_ALL_FAILED = "allfail";
	private static final String SORT_ACTUAL = "act";
	private static final String SORT_ACTUAL_FAILED = "actfail";
	private static String ACTUAL_SORT = SORT_ALL;

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("test onCreate");

		// einstellungne holen
		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		ACTUAL_SORT = getDefaultListSort();

		this.examsTest = new ArrayList<Exam>();
		if (savedInstanceState == null) {

			showDialog(DIALOG_PROGRESS);
			mGradeParserThread = new GradeParserThread(mProgressHandle);
			mGradeParserThread.start();

		} else {
			Log.d("hs-Droid:Grades ListeView", "saved instance not null!!!!");
			examsTest = (ArrayList<Exam>) savedInstanceState.get("exams_list");
			Log.d("hs-Droid:Grades ListeView", String.valueOf(examsTest.size()));
		}

		// layout festlegen
		this.m_examAdapter = new ExamAdapter(GradesList.this, R.layout.grade_row_item, this.examsTest);
		if (this.examsTest.size() == 0) {
			this.m_examAdapter.notifyDataSetInvalidated();
		} else {

			this.m_examAdapter.getFilter().filter(getDefaultListSort());
		}

		lv = getListView();
		lv.setAdapter(this.m_examAdapter);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// url muss vorhandensein
				if (!GradesList.this.m_examAdapter.getItem(position).getInfoLink().equals("")) {

					showDialog(DIALOG_PROGRESS);
					mExamInfoParserThread = new ExamInfoParserThread(mProgressHandle, GradesList.this.m_examAdapter
							.getItem(position));
					mExamInfoParserThread.start();
				} else {
					Log.d("list onClick", "keine url. todo: alertDialog");
				}

			}
		});

	}

	// helper, get rid of....
	private String getDefaultListSort() {

		String prefView = mPreferences.getString("defaultViewPref", "1");
		if (prefView.isEmpty()) {
			prefView = "1";
		}
		switch (Integer.valueOf(prefView)) {
		case 0:
			return SORT_ALL;
		case 1:
			return SORT_ACTUAL;
		case 2:
			return SORT_ACTUAL_FAILED;
		case 3:
			return SORT_ALL_FAILED;

		default:
			return prefView;
		}
	}

	/**
	 * ProgresDialog Handler
	 */
	final Handler mProgressHandle = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Log.d("handler msg.what:", String.valueOf(msg.what));

			switch (msg.what) {
			case GradeParserThread.MESSAGE_COMPLETE:
				Log.d("handler", "Login_complete");
				if (mGradeParserThread != null) {
					GradesList.this.examsTest = mGradeParserThread.getExamsList();
					mGradeParserThread = null;
					GradesList.this.m_examAdapter.getFilter().filter(getDefaultListSort());
				}

				if (mExamInfoParserThread != null) {
					currentEInfo = mExamInfoParserThread.getExamInfo();
					mExamInfoParserThread = null;
					new ExamInfoDialog(GradesList.this, currentEInfo);

				}

				dismissDialog(DIALOG_PROGRESS);

				break;
			case GradeParserThread.MESSAGE_ERROR:
				dismissDialog(DIALOG_PROGRESS);
				Log.d("handler login error", msg.getData().getString("Message"));
				createDialog(GradesList.this.getString(R.string.error_couldnt_connect),
						msg.getData().getString("Message"));
				// XXX alert dialog auch mit showDialog???

				mGradeParserThread.stopThread();
				mGradeParserThread = null;
				break;
			case GradeParserThread.MESSAGE_PROGRESS_FETCH:
				mProgressDialog.setMessage(GradesList.this.getString(R.string.progress_loading));
				break;
			case GradeParserThread.MESSAGE_PROGRESS_PARSE:
				mProgressDialog.setMessage(GradesList.this.getString(R.string.progress_parse));
				break;
			case GradeParserThread.MESSAGE_PROGRESS_CLEANUP:
				mProgressDialog.setMessage(GradesList.this.getString(R.string.progress_notencleanup));
				break;
			default:
				Log.d("onCreate should not happen", String.valueOf(mGradeParserThread.getStatus()));
				dismissDialog(DIALOG_PROGRESS);
				// Get rid of the sending thread
				mGradeParserThread.stopThread();
				mGradeParserThread = null;
				break;
			}
		}
	};

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			// mProgressDialog.setMessage(this.getString(R.string.progress_connect));
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCancelable(false);
			return mProgressDialog;

		default:
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void onRestoreInstanceState(Bundle outState) {
		super.onRestoreInstanceState(outState);

		examsTest = (ArrayList<Exam>) outState.get("exams_list");
		this.m_examAdapter.getFilter().filter(GradesList.ACTUAL_SORT);
		// FIXME nicht default , alte sortierung.!!

		System.out.println("test onRestoreInstanceState");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putParcelableArrayList("exams_list", examsTest);
		System.out.println("test onSaveInstanceState");

	}

	@Override
	protected void onPause() {
		super.onPause();
		System.out.println("test onPause");
		// fistStart = false;

	}

	@Override
	protected void onResume() {
		super.onResume();
		ACTUAL_SORT = getDefaultListSort();
		if (examsTest == null || examsTest.size() == 0) {
			System.out.println("test onResume:empty");
		} else {
			System.out.println("test onResume:data found");
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus) {
			this.m_examAdapter.getFilter().filter(ACTUAL_SORT);
		}
	}

	/**
	 * Optionsmenü
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.grade_menu, menu);
		return true;
	}

	/**
	 * Optionsmenü Callback
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			new AboutDialog(this);
			return true;
		case R.id.view_menu_refresh:
			showDialog(DIALOG_PROGRESS);
			mGradeParserThread = new GradeParserThread(mProgressHandle);
			mGradeParserThread.start();
			return true;
		case R.id.view_menu_preferences:
			Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
			startActivity(settingsActivity);
			// update??
			mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			// checkBoxChecked =
			// mPreferences.getBoolean("SaveLoginToggle", false);
			ACTUAL_SORT = getDefaultListSort();
			return true;
		case R.id.view_submenu_examViewAll:
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);

			ACTUAL_SORT = SORT_ALL;
			m_examAdapter.getFilter().filter(SORT_ALL);
			return true;
		case R.id.view_submenu_examViewOnlyLast:
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			ACTUAL_SORT = SORT_ACTUAL;
			m_examAdapter.getFilter().filter(SORT_ACTUAL);
			return true;
		case R.id.view_submenu_examViewOnlyLastFailed:
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);

			ACTUAL_SORT = SORT_ACTUAL_FAILED;
			m_examAdapter.getFilter().filter(SORT_ACTUAL_FAILED);
			return true;
		default:
			Log.d("GradeView menu:", "default");
			return super.onOptionsItemSelected(item);
		}

	}

	private void createDialog(String title, String text) {
		AlertDialog ad = new AlertDialog.Builder(this).setPositiveButton(this.getString(R.string.error_ok), null)
				.setTitle(title).setMessage(text).create();
		ad.show();
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
		if (month > 1 && month < 7) { // zwischen jan und jul ws anzeigen
			semString = "WiSe " + (year - 1) + "/" + year;
		} else {// ansonsten ss
			// wenn januar is, dann jahr-1
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
		private ArrayList<Exam> examsList;
		private final Object mLock = new Object();
		private ExamFilter mFilter;

		public ExamAdapter(Context context, int textViewResourceId, ArrayList<Exam> nExams) {
			super(context, textViewResourceId, nExams);
			this.examsList = nExams;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parents) {
			View v = convertView;

			if (v == null) {
				LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = vi.inflate(R.layout.grade_row_item, null);
			}

			Exam ex = this.examsList.get(position);
			if (ex != null) {
				TextView exName = (TextView) v.findViewById(R.id.examName);
				TextView exNr = (TextView) v.findViewById(R.id.examNr);
				TextView exAtt = (TextView) v.findViewById(R.id.examAttempts);
				TextView exGrade = (TextView) v.findViewById(R.id.examGrade);
				TextView exSemester = (TextView) v.findViewById(R.id.examSemester);
				if (exName != null) {
					exName.setText(ex.getExamName());
					if (isActualExam(ex) && mPreferences.getBoolean("highlightActualExamsPref", false)) {
						exName.setShadowLayer(3, 0, 0, Color.GREEN);
					} else {
						exName.setShadowLayer(0, 0, 0, 0);
					}
				}
				if (exNr != null) {
					exNr.setText(ex.getExamNr());
				}
				if (exSemester != null) {
					exSemester.setText(this.getContext().getString(R.string.grades_view_semester) + ex.getSemester());
				}
				if (exAtt != null && ex.getAttempts() != 0) {
					exAtt.setText(this.getContext().getString(R.string.grades_view_attempt) + ex.getAttempts());
				}
				if (exGrade != null) {
					if (!ex.isPassed()) {
						// FIXME wenn möglich.. farben gedöns is ziemlich tricky
						// wegen "recycler" von ListActivity
						if (ex.getAttempts() > 1) {
							exGrade.setTextColor(Color.BLACK);
							exGrade.setBackgroundColor(Color.RED);
							exGrade.setCompoundDrawablePadding(2);

						} else {
							exGrade.setTextColor(Color.RED);
							exGrade.setBackgroundColor(Color.TRANSPARENT);
						}
					} else {
						exGrade.setTextColor(Color.rgb(0x87, 0xeb, 0x0c));

						exGrade.setBackgroundColor(Color.TRANSPARENT);
					}
					if (ex.getGrade() != "") {
						exGrade.setText(ex.getGrade());
					} else {
						if (ex.isPassed())
							exGrade.setText("BE");
						else
							exGrade.setText("NB");
					}
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
			if (this.examsList == null) {
				return 0;
			}
			return this.examsList.size();

		}

		@Override
		public Exam getItem(int position) {
			return this.examsList.get(position);

		}

		private class ExamFilter extends Filter {

			protected FilterResults performFiltering(CharSequence prefix) {
				// result objekt
				FilterResults results = new FilterResults();

				if (prefix == null || prefix.length() == 0 || prefix == SORT_ALL) {
					synchronized (mLock) {
						results.values = examsTest;
						results.count = examsTest.size();
					}
				} else {

					// array kopieren
					final ArrayList<Exam> items = examsTest;
					final int count = items.size();
					final ArrayList<Exam> newItems = new ArrayList<Exam>(count);

					if (prefix.equals(SORT_ACTUAL_FAILED)) {
						for (int i = 0; i < count; i++) {
							final Exam item = items.get(i);

							// semester muss übereinstimmen und nicht bestanden
							if (isActualExam(item) && !item.isPassed()) {
								newItems.add(item);
							}
						}
					} else if (prefix.equals(SORT_ALL_FAILED)) {
						for (int i = 0; i < count; i++) {
							final Exam item = items.get(i);

							// nicht bestanden
							if (!item.isPassed()) {
								newItems.add(item);
							}
						}
					} else if (prefix.equals(SORT_ACTUAL)) {
						for (int i = 0; i < count; i++) {
							final Exam item = items.get(i);
							// semester muss übereinstimmen
							if (isActualExam(item)) {
								newItems.add(item);
							}
						}
					}
					// Set and return
					results.values = newItems;
					results.count = newItems.size();
				}

				return results;
			}

			@SuppressWarnings("unchecked")
			protected void publishResults(CharSequence prefix, FilterResults results) {
				// noinspection unchecked
				examsList = (ArrayList<Exam>) results.values;
				// Let the adapter know about the updated list
				if (results.count > 0) {
					notifyDataSetChanged();
				} else {
					notifyDataSetInvalidated();
				}
			}
		}
	}

	/**
	 * 
	 * @param nExam
	 * @return
	 */
	private boolean isActualExam(Exam nExam) {
		return nExam.getSemester().equals(getLastExamSem());
	}

}
