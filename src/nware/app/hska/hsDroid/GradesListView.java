package nware.app.hska.hsDroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.CookieSpecBase;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

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
import android.widget.Toast;

/**
 * {@link ListActivity} zum anzeigen der Prüfungen
 * 
 * @author Oliver Eichner
 * 
 */
public class GradesListView extends ListActivity implements Runnable {

	private ExamAdapter m_examAdapter;

	// storage public static, damit sie aus anderen activities verfügbar ist
	private String asiKey;
	// private static ExamStorage examStorage;
	private ArrayList<Exam> examsTest;

	public ProgressDialog progressDialog;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			asiKey = extras.getString("asiKey");
		}
		// progressDialog = new ProgressDialog(this);
		// progressDialog.setMessage(this.getString(R.string.progress_loading));
		// progressDialog.setIndeterminate(true);
		// progressDialog.setCancelable(false);
		getMarks();

		// layout festlegen
		setContentView(R.layout.grade_list_view);

		this.examsTest = new ArrayList<Exam>();
		this.m_examAdapter = new ExamAdapter(GradesListView.this,
				R.layout.grade_row_item, this.examsTest);
		m_examAdapter.notifyDataSetInvalidated();
		setListAdapter(this.m_examAdapter);

	}

	public void getMarks() {

		// new ProgressDialog(this);
		progressDialog = ProgressDialog.show(this, "",
				this.getString(R.string.progress_loading));

		Thread thread = new Thread(this);
		thread.start();
	}

	public void run() {
		// progressHandler.sendMessage(progressHandler.obtainMessage(1));
		progressHandler.sendEmptyMessage(1);
		getGradesFromWeb();
		progressHandler.sendEmptyMessage(0);
	}

	private Handler progressHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String message = "";
			switch (msg.what) {
			case 0:
				m_examAdapter.getFilter().filter("actualexam");
				progressDialog.dismiss();
				return;
			case 1:
				message = GradesListView.this
						.getString(R.string.progress_notenfetch);
				break;
			case 2:
				message = GradesListView.this
						.getString(R.string.progress_notencleanup);
				break;
			case 3:
				message = GradesListView.this
						.getString(R.string.progress_notenparse);
			default:
				break;
			}
			progressDialog.setMessage(message);

		}
	};

	/**
	 * Optionsmenü
	 */
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
		case R.id.view_menu_about:
			// TODO add about dialog
			Toast.makeText(this, "You pressed about!", Toast.LENGTH_LONG)
					.show();
			return true;
		case R.id.view_menu_refresh:
			// TODO add about dialog
			getMarks();
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
		return false;
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

		public ExamAdapter(Context context, int textViewResourceId,
				ArrayList<Exam> nExams) {
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
				if (exName != null) {
					exName.setText(ex.getExamName());
				}
				if (exNr != null) {
					exNr.setText(ex.getExamNr());
				}
				if (exAtt != null && ex.getAttempts() != 0) {
					exAtt.setText(this.getContext().getString(
							R.string.grades_view_attempt)
							+ ex.getAttempts());
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
			protected void publishResults(CharSequence prefix,
					FilterResults results) {
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

	private void getGradesFromWeb() {
		// FIXME asi key könnte man auch mit get in den header einbauen bzw alle
		// gets...
		progressHandler.sendMessage(progressHandler.obtainMessage(1));
		String notenSpiegelURL = "https://qis2.hs-karlsruhe.de/qisserver/rds?state=notenspiegelStudent&next=list.vm&nextdir=qispos/notenspiegel/student&createInfos=Y&struct=auswahlBaum&nodeID=auswahlBaum%7Cabschluss%3Aabschl%3D58%2Cstgnr%3D1&expand=1&asi="
				+ asiKey + "#auswahlBaum%7Cabschluss%3Aabschl%3D58%2Cstgnr%3D1";

		HttpResponse response;
		HttpEntity entity;

		try {

			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(notenSpiegelURL);
			CookieSpecBase cookieSpecBase = new BrowserCompatSpec();

			List<Header> cookieHeader = cookieSpecBase
					.formatCookies(noten.cookies);

			httpPost.setHeader(cookieHeader.get(0));

			response = client.execute(httpPost);
			entity = response.getEntity();
			InputStream is = entity.getContent();

			BufferedReader rd = new BufferedReader(new InputStreamReader(is),
					4096);
			String line;
			progressHandler.sendMessage(progressHandler.obtainMessage(2));
			Boolean record = false;
			StringBuilder sb = new StringBuilder();
			while ((line = rd.readLine()) != null) {
				if (!record && line.contains("<table border=\"0\">")) {
					record = true;
				}
				if (record && line.contains("</table>")) {
					sb.append(line);
					// System.out.println("last line: " + line);
					record = false;
					break;
				}
				if (record) {
					// alle nicht anzeigbaren zeichen entfernen (\n,\t,\s...)
					line = line.trim();

					// alle html leerzeichen müssen raus, da der xml reader nix
					// mit anfangen kann
					line = line.replaceAll("&nbsp;", "");

					// da die <img ..> tags nicht xml like "well formed" sind,
					// muss man sie ein bissel anpassen ;)
					if (line.contains("<img")) {
						line = line.substring(0, line.indexOf(">") + 1)
								+ "</a>";
					}
					sb.append(line);
					// System.out.println("line: " + line);
				}
			}
			if (entity != null)
				entity.consumeContent();
			is.close();
			String htmlContentString = sb.toString();

			rd.close();
			progressHandler.sendMessage(progressHandler.obtainMessage(3));
			read(htmlContentString);

		} catch (ClientProtocolException e) {
			Log.e("Notenspiegel::client exception:", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("Notenspiegel::io exception:", e.getMessage());
			e.printStackTrace();
		}

	}

	private void read(String test) {
		SAXParser sp;
		try {
			sp = SAXParserFactory.newInstance().newSAXParser();
			XMLReader xr = sp.getXMLReader();
			LoginContentHandler uch = new LoginContentHandler();
			xr.setContentHandler(uch);

			xr.parse(new InputSource(new StringReader(test)));
		} catch (ParserConfigurationException e) {
			Log.e("read:ParserConfException:", e.getMessage());
			e.printStackTrace();
		} catch (SAXException e) {
			Log.e("read:SAXException:", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("read:IOException:", e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * SAX2 event Handler for "Notenspiegel" html page
	 * 
	 * @author Oliver Eichner
	 * 
	 */
	private class LoginContentHandler extends DefaultHandler {

		Boolean fetch = false;
		Boolean waitForTd = false;
		int elementCount = 0; // 0-7
		private String examNr;
		private String examName;
		private String semester;
		private String examDate;
		private String grade;
		private boolean passed;
		private String notation;
		private int attempts;

		private void resetLectureVars() {
			this.examNr = "";
			this.examName = "";
			this.semester = "";
			this.examDate = "";
			this.grade = "";
			this.passed = false;
			this.notation = "";
			this.attempts = 0;
		}

		@Override
		public void startElement(String n, String l, String q, Attributes a)
				throws SAXException {
			super.startElement(n, l, q, a);
			// Log.d("hska saxparser start l:", l);
			if (l == "tr") {
				waitForTd = true;
			}
			if (waitForTd && l == "th") {
				waitForTd = false;
			}
			if (fetch && l == "td") {
				elementCount++;
			}
			if (waitForTd && l == "td") {
				fetch = true;
				waitForTd = false;
			}

		}

		@Override
		public void endElement(String n, String l, String q)
				throws SAXException {
			super.endElement(n, l, q);
			if (l == "tr" && fetch == true) {

				examsTest.add(new Exam(examNr, examName, semester, examDate,
						grade, passed, notation, attempts));

				waitForTd = false;
				fetch = false;
				elementCount = 0;
				resetLectureVars();

			}

		}

		@Override
		public void characters(char ch[], int start, int length) {
			try {
				super.characters(ch, start, length);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String text = new String(ch, start, length);
			// FIXME test
			text = text.trim();
			if (fetch) {
				switch (elementCount) {
				case 0:
					// Log.d("PruefNr:", text);
					examNr += text;
					// System.out.println("pnr  ["+examNr+"]");

					break;
				case 1:
					// Log.d("PruefName:", text);
					examName += text;
					// System.out.println("ptxt  ["+examName+"]");

					break;
				case 2:
					// Log.d("Semester:", text);
					semester += text;
					break;
				case 3:
					// Log.d("Datum:", text);
					examDate += text;
					// SimpleDateFormat sdfToDate = new SimpleDateFormat(
					// "dd.MM.yyyy");
					// try {
					// examDate = sdfToDate.parse(text);
					// } catch (ParseException e) {
					// // Log.d("read:: date parser: ", e.getMessage());
					// e.printStackTrace();
					// }
					break;
				case 4:
					// Log.d("Note:", text);
					grade += text;
					break;
				case 5:
					// Log.d("Status:", text);
					if (text.equals("bestanden")) {
						passed = true;
					}
					break;
				case 6:
					// Log.d("Vermerk:", text);
					notation += text;
					break;
				case 7:
					// Log.d("Versuch:", text);
					attempts = Integer.valueOf(text);
					break;

				default:
					break;
				}
			}

		}

		public void startDocument() throws SAXException {
			super.startDocument();
			examsTest = new ArrayList<Exam>();
			resetLectureVars();
		}

		public void endDocument() throws SAXException {
			super.endDocument();
			// array umdrehen
			Collections.reverse(examsTest);
		}
	}

}
