package de.nware.app.hsDroid.provider;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.CookieSpecBase;
import org.xml.sax.SAXException;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.Xml;
import de.nware.app.hsDroid.data.Exam;
import de.nware.app.hsDroid.data.StaticSessionData;
import de.nware.app.hsDroid.provider.onlineService2Data.CertificationsCol;
import de.nware.app.hsDroid.provider.onlineService2Data.ExamsCol;
import de.nware.app.hsDroid.provider.onlineService2Data.ExamsUpdateCol;

public class onlineService2Provider extends ContentProvider {

	private static final String TAG = "OnlineServiceContentProvider";

	private static final String DATABASE_NAME = "hsdroid.db";

	private static final int VERSION = 1;

	public static final String AUTHORITY = "de.nware.app.hsDroid.provider.onlineService2Provider";

	private static final UriMatcher mUriMatcher;

	private static final int EXAMS = 1;
	private static final int EXAMS_UPDATE = 2;
	private static final int CERTIFICATIONS = 3;

	private static HashMap<String, String> examsProjectionMap;

	private static final String[] CERTIFICATIONS_COLUMNS = new String[] { BaseColumns._ID, CertificationsCol.TITLE,
			CertificationsCol.LINK };
	private static final String[] EXAMS_UPDATE_COLUMNS = new String[] { BaseColumns._ID, ExamsUpdateCol.AMOUNT,
			ExamsUpdateCol.NEWEXAMS };

	// HTTP gedöns
	final String urlBase = "https://qis2.hs-karlsruhe.de/qisserver/rds";
	// final String notenSpiegelURLTmpl =
	// "?state=notenspiegelStudent&next=list.vm&nextdir=qispos/notenspiegel/student&createInfos=Y&struct=auswahlBaum&nodeID=auswahlBaum|abschluss:abschl=58,stgnr=1&expand=1&asi=%s#auswahlBaum|abschluss:abschl=58,stgnr=1";

	final String certificationURLTmpl = "%s?state=qissosreports&amp;besch=%s&amp;next=wait.vm&amp;asi=%s";

	final String[] certificationType = { "stammdaten", "studbesch", "studbescheng", "bafoeg", "kvv", "studienzeit" };
	final String[] certificationName = { "Datenkontrollblatt", "Immatrikulationsbescheinigung",
			"Englische Immatrikulationsbescheinigung", "Bescheinigung nach § 9 BAföG", "KVV-Bescheinigung",
			"Studienzeitbescheinigung" };

	final String examInfoURLTmpl = "%s?state=notenspiegelStudent&amp;next=list.vm&amp;nextdir=qispos/notenspiegel/student&amp;createInfos=Y&amp;struct=abschluss&amp;nodeID=auswahlBaum%7Cabschluss%3Aabschl%3D58%2Cstgnr%3D1%7Cstudiengang%3Astg%3DIB%7CpruefungOnTop%3Alabnr%3D%s&amp;expand=0&amp;asi=%s";
	// baseurl/examID/asiKey
	// + StaticSessionData.asiKey;

	private static final String USER_AGENT = TAG + "/" + VERSION;

	/** Temporärer Buffer zum halten der HTTP Get Antwort. */
	private static byte[] mContentBuffer = new byte[1024];

	/** Der http client. */
	private final HttpClient mHttpClient = new DefaultHttpClient();

	// DB
	private class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// db.setLocale(Locale.getDefault());
			// db.setLockingEnabled(true);
			// db.setVersion(VERSION);

			Log.d(TAG, "create table");
			db.execSQL("CREATE TABLE " + onlineService2Data.EXAMS_TABLE_NAME + " (" + BaseColumns._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + ExamsCol.SEMESTER + " VARCHAR(255)," + ExamsCol.PASSED
					+ " INTEGER," + ExamsCol.EXAMNAME + " VARCHAR(255)," + ExamsCol.EXAMNR + " VARCHAR(255),"
					+ ExamsCol.EXAMDATE + " VARCHAR(255)," + ExamsCol.NOTATION + " VARCHAR(255)," + ExamsCol.ATTEMPTS
					+ " VARCHAR(255)," + ExamsCol.GRADE + " VARCHAR(255)," + ExamsCol.LINKID + " INTEGER" + ");");

			Log.d(TAG, "create table done");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Datenbank Uupgrade von Version " + oldVersion + " zu " + newVersion
					+ ". Alle alten Daten gehen verloren");
			db.execSQL("DROP TABLE IF EXISTS " + onlineService2Data.EXAMS_TABLE_NAME);
			onCreate(db);

		}

	}

	private DatabaseHelper mOpenHelper;

	static {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(AUTHORITY, onlineService2Data.EXAMS_TABLE_NAME, EXAMS);
		mUriMatcher.addURI(AUTHORITY, onlineService2Data.CERTIFICATIONS_TABLE_NAME, CERTIFICATIONS);
		mUriMatcher.addURI(AUTHORITY, onlineService2Data.EXAMS_UPDATE_TABLE_NAME, EXAMS_UPDATE);

		examsProjectionMap = new HashMap<String, String>();
		examsProjectionMap.put(onlineService2Data.ExamsCol.SEMESTER, onlineService2Data.ExamsCol.SEMESTER);
		examsProjectionMap.put(onlineService2Data.ExamsCol.PASSED, onlineService2Data.ExamsCol.PASSED);
		examsProjectionMap.put(onlineService2Data.ExamsCol.EXAMNAME, onlineService2Data.ExamsCol.EXAMNAME);
		examsProjectionMap.put(onlineService2Data.ExamsCol.EXAMNR, onlineService2Data.ExamsCol.EXAMNR);
		examsProjectionMap.put(onlineService2Data.ExamsCol.EXAMDATE, onlineService2Data.ExamsCol.EXAMDATE);
		examsProjectionMap.put(onlineService2Data.ExamsCol.NOTATION, onlineService2Data.ExamsCol.NOTATION);
		examsProjectionMap.put(onlineService2Data.ExamsCol.ATTEMPTS, onlineService2Data.ExamsCol.ATTEMPTS);
		examsProjectionMap.put(onlineService2Data.ExamsCol.GRADE, onlineService2Data.ExamsCol.GRADE);
		examsProjectionMap.put(onlineService2Data.ExamsCol.LINKID, onlineService2Data.ExamsCol.LINKID);
	}

	@Override
	public int delete(Uri uri, String whereClause, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (mUriMatcher.match(uri)) {
		case EXAMS:
			count = db.delete(onlineService2Data.EXAMS_TABLE_NAME, whereClause, whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unbekannte URI " + uri);
		}

		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (mUriMatcher.match(uri)) {
		case EXAMS:
			return ExamsCol.CONTENT_TYPE;

		case CERTIFICATIONS:
			return CertificationsCol.CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unbekannte URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (mUriMatcher.match(uri) != EXAMS) {
			throw new IllegalArgumentException("Unbekannte URI " + uri);
		}

		ContentValues contentValues;
		if (initialValues != null) {
			contentValues = new ContentValues(initialValues);
		} else {
			contentValues = new ContentValues();
		}
		if (mOpenHelper == null) {
			Log.d(TAG, "mOpenHelper NULL");
		}
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowID = db.insert(onlineService2Data.EXAMS_TABLE_NAME, ExamsCol.EXAMNAME, contentValues);
		if (rowID > 0) {
			Uri examsUri = ContentUris.withAppendedId(ExamsCol.CONTENT_URI, rowID);
			getContext().getContentResolver().notifyChange(examsUri, null); // Observer?
			return examsUri;
		}
		throw new SQLException("Konnte row nicht zu " + uri + " hinzufügen");
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		Cursor cursor = null;
		switch (mUriMatcher.match(uri)) {
		case EXAMS:
			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			qb.setTables(onlineService2Data.EXAMS_TABLE_NAME);
			qb.setProjectionMap(examsProjectionMap);
			SQLiteDatabase db = mOpenHelper.getReadableDatabase();
			cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(getContext().getContentResolver(), uri);
			break;
		case EXAMS_UPDATE:
			MatrixCursor cur = new MatrixCursor(EXAMS_UPDATE_COLUMNS);
			Integer[] columnValues = updateGrades();
			cur.addRow(new Object[] { 0, columnValues[0], columnValues[1] });
			return cur;
		case CERTIFICATIONS:
			cursor = getCertifications();
			break;
		default:
			throw new IllegalArgumentException("Unbekannte URI " + uri);
		}

		return cursor;
	}

	private Cursor getCertifications() {

		final MatrixCursor cursor = new MatrixCursor(CERTIFICATIONS_COLUMNS);
		int count = 0;
		for (String certType : certificationType) {
			String downloadUrl = String.format(certificationURLTmpl, urlBase, certType, StaticSessionData.asiKey);
			cursor.addRow(new Object[] { count, certificationName[count], downloadUrl });
			count++;
		}

		return cursor;

	}

	@Override
	public int update(Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (mUriMatcher.match(uri)) {
		case EXAMS:
			count = db.update(onlineService2Data.EXAMS_TABLE_NAME, values, whereClause, whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unbekannte URI " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null); // Observer?
		return count;
	}

	/**
	 * Stellt HTTP Anfrage und liefert deren Antwort zurück.
	 * 
	 * @param url
	 *            die formatierte URL
	 * @return die HTML/XML Antwort
	 */
	private synchronized String getResponse(String url) {

		Log.d(TAG, "URL: " + url);
		final HttpPost httpPost = new HttpPost(url);
		httpPost.addHeader("User-Agent", USER_AGENT);
		CookieSpecBase cookieSpecBase = new BrowserCompatSpec();
		List<Header> cookieHeader = cookieSpecBase.formatCookies(StaticSessionData.cookies);
		httpPost.setHeader(cookieHeader.get(0));

		try {
			final HttpResponse response = mHttpClient.execute(httpPost);

			// Prüfen ob HTTP Antwort ok ist.
			final StatusLine status = response.getStatusLine();
			if (status.getStatusCode() != HttpStatus.SC_OK) {
				throw new RuntimeException("Ungültige Antwort vom Server: " + status.toString());
			}

			// Hole Content Stream
			final HttpEntity entity = response.getEntity();

			// content typ.
			final Header contentType = entity.getContentType();

			// content.
			final InputStream inputStream = entity.getContent();
			final ByteArrayOutputStream content = new ByteArrayOutputStream();
			Log.d("SMWData", "http read content: " + contentType.getValue());
			// response lesen in ByteArrayOutputStream.
			int readBytes = 0;
			while ((readBytes = inputStream.read(mContentBuffer)) != -1) {
				content.write(mContentBuffer, 0, readBytes);
			}

			return new String(content.toByteArray());

		} catch (IOException e) {
			Log.d(TAG, e.getMessage());
			throw new RuntimeException("Verbindung fehlgeschlagen: " + e.getMessage(), e);
		}

	}

	public boolean examExists(String examnr, String examdate) {
		SQLiteDatabase mDb = mOpenHelper.getReadableDatabase();
		Cursor cursor = mDb.rawQuery("select 1 from " + onlineService2Data.EXAMS_TABLE_NAME + " where "
				+ onlineService2Data.ExamsCol.EXAMNR + "=? AND " + onlineService2Data.ExamsCol.EXAMDATE + "=?",
				new String[] { examnr, examdate });
		boolean exists = (cursor.getCount() > 0);
		cursor.close();
		return exists;
	}

	public Integer[] updateGrades() {
		// String url = String.format(notenSpiegelURLTmpl,
		// StaticSessionData.asiKey);
		// Log.d(TAG, "url: " + urlBase + URLEncoder.encode(url));
		final String notenSpiegelURLTmpl = urlBase
				+ "?state=notenspiegelStudent&next=list.vm&nextdir=qispos/notenspiegel/student&createInfos=Y&struct=auswahlBaum&nodeID=auswahlBaum%7Cabschluss%3Aabschl%3D58%2Cstgnr%3D1&expand=1&asi="
				+ StaticSessionData.asiKey + "#auswahlBaum%7Cabschluss%3Aabschl%3D58%2Cstgnr%3D1";

		String response = getResponse(notenSpiegelURLTmpl);
		BufferedReader rd = new BufferedReader(new StringReader(response));

		try {
			String line;
			Boolean record = false;
			StringBuilder sb = new StringBuilder();
			while ((line = rd.readLine()) != null) {
				if (!record && line.contains("<table border=\"0\">")) {
					record = true;
				}
				if (record && line.contains("</table>")) {
					line = line.replaceAll("&nbsp;", "");
					line.trim();
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
						// Log.d("grade parser", line);
						line = line.substring(0, line.indexOf(">") + 1) + "</a>";
					}
					sb.append(line);
					// System.out.println("line: " + line);
				}
			}
			rd.close();
			String htmlContentString = sb.toString();
			return read(htmlContentString);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d(TAG, e.getMessage());
			e.printStackTrace();
		}
		return null;

	}

	private Integer[] read(String htmlContent) {
		Integer[] counter = { 0, 0 };
		try {

			ExamParser handler = new ExamParser();
			Xml.parse(htmlContent, handler);

			for (Exam iterable_element : handler.getExams()) {
				if (!examExists(iterable_element.getExamNr(), iterable_element.getExamDate())) {
					counter[1]++;
					Log.d(TAG, "exam: insert " + iterable_element.getExamName() + " into DB");
					ContentValues values = new ContentValues();
					values.put(onlineService2Data.ExamsCol.SEMESTER, iterable_element.getSemester());
					// values.put(onlineService2Data.ExamsCol.PASSED,
					// (iterable_element.isPassed() ? 1 : 0));
					values.put(onlineService2Data.ExamsCol.PASSED, iterable_element.isPassed());
					values.put(onlineService2Data.ExamsCol.EXAMNAME, iterable_element.getExamName());
					values.put(onlineService2Data.ExamsCol.EXAMNR, iterable_element.getExamNr());
					values.put(onlineService2Data.ExamsCol.EXAMDATE, iterable_element.getExamDate());
					values.put(onlineService2Data.ExamsCol.NOTATION, iterable_element.getNotation());
					values.put(onlineService2Data.ExamsCol.ATTEMPTS, iterable_element.getAttempts());
					values.put(onlineService2Data.ExamsCol.GRADE, iterable_element.getGrade());
					values.put(onlineService2Data.ExamsCol.LINKID, iterable_element.getInfoID());
					Log.d(TAG, "insert..");
					insert(onlineService2Data.ExamsCol.CONTENT_URI, values);
				} else {
					Log.d(TAG, "exam: " + iterable_element.getExamName() + " already in DB");
				}
				counter[0]++;
			}
		} catch (SAXException e) {
			Log.e("read:SAXException:", e.getMessage());
			e.printStackTrace();
		}
		return counter;
	}
}
