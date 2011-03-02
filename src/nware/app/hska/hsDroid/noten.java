package nware.app.hska.hsDroid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class noten extends Activity {

	private static final String UPDATE_URL = "https://qis2.hs-karlsruhe.de/qisserver/rds?state=user&type=1&category=auth.login&startpage=portal.vm&breadCrumbSource=portal";
	public static List<Cookie> cookies;
	DefaultHttpClient client;
	private String asiKey;
	boolean loggedIn = false;

	public ProgressDialog progressDialog;
	private EditText UserEditText;
	private EditText PassEditText;
	private CheckBox LoginCheckBox;
	// private Context mainContext;

	private boolean checkBoxChecked;
	private SharedPreferences notenapp_preferences;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// mainContext = this;
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

					createDialog(v.getContext().getString(R.string.error), v
							.getContext()
							.getString(R.string.error_name_missing));
					return;
				} else
				// FIXME bessere RegExp
				if (!username.matches("^[a-z]{4}[0-9]{4}")) {
					createDialog(
							v.getContext().getString(R.string.error),
							v.getContext().getString(
									R.string.error_name_incorrect));
					return;
				} else

				if (password.length() == 0) {
					createDialog(
							v.getContext().getString(R.string.error),
							v.getContext().getString(
									R.string.error_password_missing));
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			Toast.makeText(this, "You pressed preferences!", Toast.LENGTH_LONG)
					.show();
			return true;
		case R.id.menu_about:
			Toast.makeText(this, "You pressed about!", Toast.LENGTH_LONG)
					.show();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	/**
	 * ProgresDialog Handler
	 */
	Handler progressHandle = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			String message = noten.this.getString(R.string.progress_loading);
			switch (msg.what) {
			case 1:
				message = noten.this.getString(R.string.progress_login);
				break;
			case 2:
				message = noten.this.getString(R.string.progress_webcheck);
				break;
			case 3:
				message = noten.this.getString(R.string.progress_cookie);
				break;
			case 4:
				message = noten.this.getString(R.string.progress_noten);
				break;
			case 5:
				message = noten.this.getString(R.string.progress_notenprepare);
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
	 * 
	 * @param title
	 *            {@link String} dialog title
	 * @param text
	 *            {@link String} dialog text
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
	 * 
	 * @param login
	 *            {@link String} Username
	 * @param pass
	 *            {@link String} Password
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

					// Post daten zusammen bauen
					HttpPost post = new HttpPost(UPDATE_URL);
					List<NameValuePair> nvps = new ArrayList<NameValuePair>();
					nvps.add(new BasicNameValuePair("asdf", login));
					nvps.add(new BasicNameValuePair("fdsa", pw));
					nvps.add(new BasicNameValuePair("submit", "Anmelden"));
					post.setHeader("Content-Type",
							"application/x-www-form-urlencoded");
					post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

					// http anfrage starten
					response = client.execute(post);

					entity = response.getEntity();
					InputStream is = entity.getContent();
					BufferedReader rd = new BufferedReader(
							new InputStreamReader(is), 4096);
					String line;
					int count = 0;
					progressHandle.sendMessage(progressHandle.obtainMessage(2));
					// response auswerten
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
					noten.cookies = client.getCookieStore().getCookies();

					loggedIn = true;

					// progress dialog schließen
					progressDialog.dismiss();
					// start activity "NotenViewer"
					// finalize();
					Intent i = new Intent(noten.this, GradesListView.class);
					i.putExtra("asiKey", asiKey);
					startActivity(i);

					if (entity != null)
						entity.consumeContent();
				} catch (Exception e) {
					progressDialog.dismiss();
					createDialog(
							noten.this.getString(R.string.error_couldnt_connect),

							e.getMessage());
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		t.start();
	}

	/**
	 * Test for Login response Line
	 * 
	 * @param line
	 *            {@link String} with a line from the login response
	 * @param count
	 *            {@link Integer} line count
	 * @throws HSLoginException
	 */
	private void loginStringTest(String line, int count)
			throws HSLoginException {

		if (count < 10) { // sollte inerhalb der ersten 10 zeilen stehen..
			if (line.contains("System nicht verf")) {
				throw new HSLoginException(
						this.getString(R.string.error_site_down));
			}
		}
		// A</u>nmelden
		// if (line.contains("Anmeldung fehlgeschlagen")) {
		if (count < 50 && count > 30) {
			if (line.contains("A</u>nmelden")) {
				throw new HSLoginException(
						this.getString(R.string.error_login_failed));
			}
		}
	}

}
