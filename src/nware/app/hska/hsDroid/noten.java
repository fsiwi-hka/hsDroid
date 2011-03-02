package nware.app.hska.hsDroid;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import java.io.IOException;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;

import javax.xml.parsers.SAXParserFactory;

import nware.app.hska.hsDroid.R;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.CookieSpecBase;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class noten extends Activity {

	private static final String UPDATE_URL = "https://qis2.hs-karlsruhe.de/qisserver/rds?state=user&type=1&category=auth.login&startpage=portal.vm&breadCrumbSource=portal";
	List<Cookie> cookies;
	DefaultHttpClient client;
	private String asiKey;

	//storage public static, damit sie aus anderen activities verfügbar ist
	public static ExamStorage examStorage;

	public ProgressDialog progressDialog;
	private EditText UserEditText;
	private EditText PassEditText;
	private CheckBox LoginCheckBox;
	private Context mainContext;

	private boolean checkBoxChecked;
	private SharedPreferences notenapp_preferences;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mainContext = this;
		progressDialog = new ProgressDialog(this);
		progressDialog.setMessage(this.getString(R.string.progress_loading));
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(false);

		UserEditText = (EditText) findViewById(R.id.username);
		PassEditText = (EditText) findViewById(R.id.password);

		LoginCheckBox = (CheckBox) findViewById(R.id.login_checkBox);

		notenapp_preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String savedUser = notenapp_preferences.getString("UserSave", "");
		String savedPass = notenapp_preferences.getString("PassSave", "");
		checkBoxChecked = notenapp_preferences.getBoolean("CheckBox", false);
		LoginCheckBox.setChecked(checkBoxChecked);

		if (checkBoxChecked && !savedUser.equals("")) {
			UserEditText.setText(savedUser);
			PassEditText.setText(savedPass);
		}

		Button button = (Button) findViewById(R.id.login);

		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				String username = UserEditText.getText().toString().trim();
				String password = PassEditText.getText().toString();

				// FIXME zu unsicher.. wird alles im plaintext gespeichert..
				// eventuell sqlite mit encryption..
				// speichern von user und passwort
				SharedPreferences.Editor editor = notenapp_preferences.edit();
				if (LoginCheckBox.isChecked()) {

					editor.putString("UserSave", username);
					editor.putString("PassSave", password);
					editor.putBoolean("CheckBox", true);
					editor.commit(); // Very important
				} else {

					editor.remove("UserSave");
					editor.remove("PassSave");
					editor.remove("CheckBox");
					editor.commit(); // Very important
				}

				if (username.length() == 0) {
					createDialog(mainContext.getString(R.string.error),
							mainContext.getString(R.string.error_name_missing));
					return;
				} else
				// FIXME bessere RegExp
				if (!username.matches("^[a-z]{4}[0-9]{4}")) {
					createDialog(mainContext.getString(R.string.error),
							mainContext
									.getString(R.string.error_name_incorrect));
					return;
				} else

				if (password.length() == 0) {
					createDialog(mainContext.getString(R.string.error),
							mainContext
									.getString(R.string.error_password_missing));
					return;
				} else {
					progressDialog.show();

					doLogin(username, password);
				}
			}
		});

		button = (Button) findViewById(R.id.cancel);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				quit(false, null);
			}
		});
	}

	/**
	 * ProgresDialog Handler
	 */
	Handler progressHandle = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			String message = mainContext.getString(R.string.progress_loading);
			switch (msg.what) {
			case 1:
				message = mainContext.getString(R.string.progress_login);
				break;
			case 2:
				message = mainContext.getString(R.string.progress_webcheck);
				break;
			case 3:
				message = mainContext.getString(R.string.progress_cookie);
				break;
			case 4:
				message = mainContext.getString(R.string.progress_noten);
				break;
			case 5:
				message = mainContext.getString(R.string.progress_notenprepare);
			default:
				break;
			}
			progressDialog.setMessage(message);
		}
	};

	private void quit(boolean success, Intent i) {
		setResult((success) ? -1 : 0, i);
		finish();
	}

	/**
	 * Creates {@link AlertDialog}
	 * @param title {@link String} dialog title
	 * @param text {@link String} dialog text
	 */
	private void createDialog(String title, String text) {
		AlertDialog ad = new AlertDialog.Builder(this)
				.setPositiveButton(this.getString(R.string.error_ok), null)
				.setTitle(title).setMessage(text).create();
		ad.show();
	}

	// POST
	// /qisserver/rds?state=user&type=1&category=auth.login&startpage=portal.vm&breadCrumbSource=portal
	// asdf=mami0011&fdsa=secretpw&submit=Anmelden


	/**
	 * Login into qis2 server 
	 * @param login {@link String} Username
	 * @param pass {@link String} Password
	 */
	private void doLogin(final String login, final String pass) {
		final String pw = pass;
		Thread t = new Thread() {
			public void run() {
				
				client = new DefaultHttpClient();

				HttpResponse response;
				HttpEntity entity;
				try {
					progressHandle.sendMessage(progressHandle.obtainMessage(1));
					
					//Post daten zusammen bauen
					HttpPost post = new HttpPost(UPDATE_URL);
					List<NameValuePair> nvps = new ArrayList<NameValuePair>();
					nvps.add(new BasicNameValuePair("asdf", login));
					nvps.add(new BasicNameValuePair("fdsa", pw));
					nvps.add(new BasicNameValuePair("submit", "Anmelden"));
					post.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
					
					//http anfrage starten
					response = client.execute(post);
					
					
					entity = response.getEntity();
					InputStream is = entity.getContent();
					BufferedReader rd = new BufferedReader(
							new InputStreamReader(is), 4096);
					String line;
					int count = 0;
					progressHandle.sendMessage(progressHandle.obtainMessage(2));
					//response auswerten
					while ((line = rd.readLine()) != null) {
						// TODO check login success
						loginStringTest(line, count);

						if (line.contains("asi=")) {

							int begin = line.indexOf("asi=");
							asiKey = line.substring(begin + 4, begin + 24);
							break;
						}
						count++;
					}
					rd.close();
					is.close();

					progressHandle.sendMessage(progressHandle.obtainMessage(3));
					cookies = client.getCookieStore().getCookies();

					progressHandle.sendMessage(progressHandle.obtainMessage(4));
					getNotenspiegel();
					
					//progress dialog schließen
					progressDialog.dismiss();
					//start activity "NotenViewer"
					Intent i = new Intent(mainContext, NotenViewer.class);
					startActivity(i);

					if (entity != null)
						entity.consumeContent();
				} catch (Exception e) {
					progressDialog.dismiss();
					createDialog(
							mainContext.getString(R.string.error_couldnt_connect),

							e.getMessage());
				}
			}
		};
		t.start();
	}

	/**
	 * Test for Login response Line
	 * @param line {@link String} with a line from the login response
	 * @param count {@link Integer} line count
	 * @throws HSLoginException
	 */
	private void loginStringTest(String line, int count)
			throws HSLoginException {

		if (count < 10) { // sollte inerhalb der ersten 10 zeilen stehen..
			if (line.contains("System nicht verf")) {
				throw new HSLoginException(
						mainContext.getString(R.string.error_site_down));
			}
		}
		// A</u>nmelden
		// if (line.contains("Anmeldung fehlgeschlagen")) {
		if (count < 50 && count > 30) {
			if (line.contains("A</u>nmelden")) {
				throw new HSLoginException(
						mainContext.getString(R.string.error_login_failed));
			}
		}
	}

	//TODO auslagern in noten view
	private void getNotenspiegel() {
		// FIXME asi key könnte man auch mit get in den header einbauen bzw alle gets...
		String notenSpiegelURL = "https://qis2.hs-karlsruhe.de/qisserver/rds?state=notenspiegelStudent&next=list.vm&nextdir=qispos/notenspiegel/student&createInfos=Y&struct=auswahlBaum&nodeID=auswahlBaum%7Cabschluss%3Aabschl%3D58%2Cstgnr%3D1&expand=1&asi="
				+ asiKey + "#auswahlBaum%7Cabschluss%3Aabschl%3D58%2Cstgnr%3D1";

		HttpResponse response;
		HttpEntity entity;
		
		try {
			
			client = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(notenSpiegelURL);
			CookieSpecBase cookieSpecBase = new BrowserCompatSpec();

			List<Header> cookieHeader = cookieSpecBase.formatCookies(cookies);
			
			httpPost.setHeader(cookieHeader.get(0));
			
			response = client.execute(httpPost);
			entity = response.getEntity();
			InputStream is = entity.getContent();

			BufferedReader rd = new BufferedReader(new InputStreamReader(is),
					4096);
			String line;

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
			progressHandle.sendMessage(progressHandle.obtainMessage(5));
			read(htmlContentString);

		} catch (ClientProtocolException e) {
			Log.d("Notenspiegel::client exception:", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("Notenspiegel::io exception:", e.getMessage());
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
			Log.d("read:ParserConfException:", e.getMessage());
			e.printStackTrace();
		} catch (SAXException e) {
			Log.d("read:SAXException:", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.d("read:IOException:", e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * SAX2 event Handler for "Notenspiegel" html page
	 * @author Oliver Eichner
	 *
	 */
	private class LoginContentHandler extends DefaultHandler {

		Boolean fetch = false;
		Boolean waitForTd = false;
		int elementCount = 0; // 0-7
		private String pruefungsNr;
		private String pruefungsText;
		private String semester;
		private String pruefungsDatum;
		private String note;
		private boolean status;
		private String vermerk;
		private int versuch;

		private void resetLectureVars() {
			this.pruefungsNr = "";
			this.pruefungsText = "";
			this.semester = "";
			this.pruefungsDatum = "";
			this.note = "";
			this.status = false;
			this.vermerk = "";
			this.versuch = 0;
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

				examStorage.appendFach(pruefungsNr, pruefungsText, semester,
						pruefungsDatum, note, status, vermerk, versuch);

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
					pruefungsNr += text;
					// System.out.println("pnr  ["+pruefungsNr+"]");

					break;
				case 1:
					// Log.d("PruefName:", text);
					pruefungsText += text;
					// System.out.println("ptxt  ["+pruefungsText+"]");

					break;
				case 2:
					// Log.d("Semester:", text);
					semester += text;
					break;
				case 3:
					// Log.d("Datum:", text);
					pruefungsDatum += text;
					// SimpleDateFormat sdfToDate = new SimpleDateFormat(
					// "dd.MM.yyyy");
					// try {
					// pruefungsDatum = sdfToDate.parse(text);
					// } catch (ParseException e) {
					// // Log.d("read:: date parser: ", e.getMessage());
					// e.printStackTrace();
					// }
					break;
				case 4:
					// Log.d("Note:", text);
					note += text;
					break;
				case 5:
					// Log.d("Status:", text);
					if (text.equals("bestanden")) {
						status = true;
					}
					break;
				case 6:
					// Log.d("Vermerk:", text);
					vermerk += text;
					break;
				case 7:
					// Log.d("Versuch:", text);
					versuch = Integer.valueOf(text);
					break;

				default:
					break;
				}
			}

		}

		public void startDocument() throws SAXException {
			super.startDocument();
			examStorage = new ExamStorage();
			resetLectureVars();
		}

		public void endDocument() throws SAXException {
			super.endDocument();
			//progressDialog.dismiss();

			// TODO aus dem handler rausholen
//			Intent i = new Intent(mainContext, NotenViewer.class);
//			startActivity(i);
		}
	}
}
