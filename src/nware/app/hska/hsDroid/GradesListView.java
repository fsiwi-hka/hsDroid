package nware.app.hska.hsDroid;

import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

/**
 * {@link ListActivity} zum anzeigen der Prüfungen
 * 
 * @author Oliver Eichner
 * 
 */
public class GradesListView extends ListActivity {

	private static Boolean fistStart = true;

	private ExamAdapter m_examAdapter;

	private ArrayList<Exam> examsTest;

	private GradeParserThread mGradeParserThread = null;

	private ProgressDialog mProgressDialog = null;
	private static final int DIALOG_PROGRESS = 1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		System.out.println("test onCreate");

		// TODO //FIXME asikey und cookies darf nicht leer sein!!!!!!
		// StaticSessionData.gParser = new GradeParserThread();
		if (fistStart) {
			showDialog(DIALOG_PROGRESS);
			mGradeParserThread = new GradeParserThread(mProgressHandle);
			mGradeParserThread.start();
		}

		this.examsTest = new ArrayList<Exam>();
		// this.examsTest = StaticSessionData.gParser.getExamsList();
		// layout festlegen

		setContentView(R.layout.grade_list_view);

		this.m_examAdapter = new ExamAdapter(GradesListView.this, R.layout.grade_row_item, this.examsTest);
		m_examAdapter.notifyDataSetInvalidated();

		setListAdapter(this.m_examAdapter);

		// lezten thread holen, wenn er noch nicht fertig ist
		if (getLastNonConfigurationInstance() != null) {

			mGradeParserThread = (GradeParserThread) getLastNonConfigurationInstance();
			mGradeParserThread.handlerOfCaller = mProgressHandle;

			// Prüfen ob der thread noch läuft
			switch (mGradeParserThread.getStatus()) {
			case GradeParserThread.STATE_RUNNING:
				// progress dialog wieder anzeigen
				showDialog(DIALOG_PROGRESS);
				break;
			case GradeParserThread.STATE_NOT_STARTED:
				// progress dialog schließen, falls er noch offen ist
				// FIXME prüfen ob dialog geöffnet ist!!
				dismissDialog(DIALOG_PROGRESS);
				break;
			// case GradeParserThread.STATE_ERROR:
			// // progress dialog schließen, falls er noch offen ist
			// dismissDialog(DIALOG_PROGRESS);
			// break;
			case GradeParserThread.STATE_DONE:
				GradesListView.this.examsTest = mGradeParserThread.getExamsList();
				mGradeParserThread = null;
				// progress dialog schließen, falls er noch offen ist
				dismissDialog(DIALOG_PROGRESS);
				// TODO daten aktualisieren
				// GradesListView.this.m_examAdapter.notifyDataSetChanged();
				this.m_examAdapter.getFilter().filter("actualexam");
				break;
			default:
				// sollte nicht vorkommen ;)
				Log.d("onCreate should not happen", String.valueOf(mGradeParserThread.getStatus()));

				dismissDialog(DIALOG_PROGRESS);
				// thread killen
				mGradeParserThread.stopThread();
				mGradeParserThread = null;
				break;
			}
		}

	}

	/**
	 * ProgresDialog Handler
	 */
	final Handler mProgressHandle = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Log.d("handler msg.what:", String.valueOf(msg.what));
			// String message =
			// HsDroidMain.this.getString(R.string.progress_loading);
			switch (msg.what) {
			case GradeParserThread.MESSAGE_COMPLETE:
				Log.d("handler", "Login_complete");
				GradesListView.this.examsTest = mGradeParserThread.getExamsList();
				mGradeParserThread = null;
				dismissDialog(DIALOG_PROGRESS);
				// TODO daten aktualisieren
				// GradesListView.this.m_examAdapter.notifyDataSetChanged();
				GradesListView.this.m_examAdapter.getFilter().filter("actualexam");

				// Intent i = new Intent(GradesListView.this,
				// GradesListView.class);
				// // i.putExtra("asiKey", asiKey);
				// startActivity(i);
				break;
			case GradeParserThread.MESSAGE_ERROR:
				dismissDialog(DIALOG_PROGRESS);
				Log.d("handler login error", msg.getData().getString("Message"));
				createDialog(GradesListView.this.getString(R.string.error_couldnt_connect),
						msg.getData().getString("Message"));
				// TODO alert dialog auch mit showDialog???

				mGradeParserThread.stopThread();
				mGradeParserThread = null;
				break;
			case GradeParserThread.MESSAGE_PROGRESS_FETCH:
				mProgressDialog.setMessage(GradesListView.this.getString(R.string.progress_notenfetch));
				break;
			case GradeParserThread.MESSAGE_PROGRESS_PARSE:
				mProgressDialog.setMessage(GradesListView.this.getString(R.string.progress_parse));
				break;
			case GradeParserThread.MESSAGE_PROGRESS_CLEANUP:
				mProgressDialog.setMessage(GradesListView.this.getString(R.string.progress_notencleanup));
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
	public Object onRetainNonConfigurationInstance() {
		removeDialog(DIALOG_PROGRESS);
		// prüfen on der login thread noch läuft
		if (mGradeParserThread != null) {
			// referenz zur aktivity entfernen (memory leak)
			mGradeParserThread.handlerOfCaller = null;
			// instanz die erhalten werden soll zurückgeben
			return (mGradeParserThread);
		}
		return super.onRetainNonConfigurationInstance();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			// mProgressDialog.setMessage(this.getString(R.string.progress_connect));
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCancelable(false);
			return mProgressDialog;
			// progressDialog.setProgressStyle() //TODO in sdk nachschauen was
			// es noch für optionen gibt
		default:
			return null;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		System.out.println("test onSaveInstanceState");

	}

	@Override
	protected void onPause() {
		super.onPause();
		System.out.println("test onPause");
		fistStart = false;

	}

	@Override
	protected void onResume() {

		super.onResume();
		if (examsTest == null || examsTest.size() == 0) {
			System.out.println("test onResume:empty");
		} else {
			System.out.println("test onResume:data found");
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
			Log.d("OptonItemSelect", "refresh.. not implemented");
			showDialog(DIALOG_PROGRESS);
			mGradeParserThread = new GradeParserThread(mProgressHandle);
			mGradeParserThread.start();
			// StaticSessionData.gParser.refreshList();
			return true;
		case R.id.view_submenu_examViewAll:
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);

			m_examAdapter.getFilter().filter("");
			return true;
		case R.id.view_submenu_examViewOnlyLast:
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);
			m_examAdapter.getFilter().filter("actualexam");
			return true;
		case R.id.view_submenu_examViewOnlyLastFailed:
			if (item.isChecked())
				item.setChecked(false);
			else
				item.setChecked(true);

			m_examAdapter.getFilter().filter("failed");
			return true;
		}
		Log.d("GradeView menu:", "default");
		return super.onOptionsItemSelected(item);
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
		private ArrayList<Exam> examsList;
		private final Object mLock = new Object(); // FIXME ??
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
						// wegen des "recycler" von ListActivity
						if (ex.getAttempts() > 1) {
							exGrade.setBackgroundColor(Color.RED);
							exGrade.setTextColor(Color.BLACK);
						} else {
							exGrade.setTextColor(Color.RED); // TODO helleres
																// rot
							exGrade.setBackgroundColor(Color.TRANSPARENT);
						}
					} else {
						exGrade.setTextColor(Color.rgb(0x87, 0xeb, 0x0c)); // Color.rgb(0x87,0xeb,0x0c
						exGrade.setBackgroundColor(Color.TRANSPARENT);
						// TODO android grün ;)
						// http://www.perbang.dk/rgb/A4C639/
						// # C1FF00 # AEE500
						// gingerbread ähnlich, bissel heller.. BEEB0C
					}
					if (ex.getGrade() != "") {
						exGrade.setText(ex.getGrade());
					} else {
						if (ex.isPassed())
							exGrade.setText("B");
						else
							exGrade.setText("N");
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

		private class ExamFilter extends Filter {
			protected FilterResults performFiltering(CharSequence prefix) {
				// result objekt
				FilterResults results = new FilterResults();

				// FIXME ...funzt nich..//wenn adapter array leer, hole original
				// if (examsList == null) {
				// synchronized (mLock) { // Notice the declaration above
				// examsList = new ArrayList<Exam>(examsTest);
				// }
				// }

				// kein prefix, also ganzes (altes) array übernehmen

				// FIXME ??
				if (prefix == null || prefix.length() == 0 || prefix == "") {
					synchronized (mLock) {
						results.values = examsTest;
						results.count = examsTest.size();
					}
				} else {

					// array kopieren
					final ArrayList<Exam> items = examsTest;
					final int count = items.size();
					final ArrayList<Exam> newItems = new ArrayList<Exam>(count);

					if (prefix.equals("failed")) {
						for (int i = 0; i < count; i++) {
							final Exam item = items.get(i);

							// semester muss übereinstimmen und nicht bestanden
							if (isActualExam(item) && !item.isPassed()) {
								newItems.add(item);
							}
						}
					} else if (prefix.equals("actualexam")) {
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
