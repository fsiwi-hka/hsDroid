package nware.app.hska.hsDroid;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class HsDroidMain extends Activity {

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

		notenapp_preferences = PreferenceManager.getDefaultSharedPreferences(this);
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
				// Benutzernamen: nicht anzeigbare Zeichen entfernen, alles
				// klein schreiben
				// (leerzeichen killen und erster Buchstabe klein, wenn über
				// android autoverfolständigung eingefügt...)
				String username = UserEditText.getText().toString().trim().toLowerCase();
				// Password: nicht anzeigbare Zeichen entfernen
				String password = PassEditText.getText().toString().trim();

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

					createDialog(v.getContext().getString(R.string.error),
							v.getContext().getString(R.string.error_name_missing));
					return;
				} else
				// FIXME bessere RegExp
				if (!username.matches("^[a-zA-Z]{4}[0-9]{4}")) {
					createDialog(v.getContext().getString(R.string.error),
							v.getContext().getString(R.string.error_name_incorrect));
					return;
				} else

				if (password.length() == 0) {
					createDialog(v.getContext().getString(R.string.error),
							v.getContext().getString(R.string.error_password_missing));
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
			Toast.makeText(this, "You pressed preferences!", Toast.LENGTH_LONG).show();
			return true;
		case R.id.menu_about:
			// Toast.makeText(this, "You pressed about!", Toast.LENGTH_LONG)
			// .show();
			aboutDialog();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}

	}

	private void aboutDialog() {

		final Dialog dialog = new Dialog(HsDroidMain.this);
		dialog.setContentView(R.layout.about);
		dialog.setTitle(this.getString(R.string.menu_about));
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);

		TextView aboutTitle = (TextView) dialog.findViewById(R.id.about_title);
		aboutTitle.setText(this.getString(R.string.app_name) + " - " + this.getString(R.string.app_version));
		TextView aboutSubTitle = (TextView) dialog.findViewById(R.id.about_SubTitle);
		aboutSubTitle.setText(this.getString(R.string.about_subtitle));

		TextView mainText = (TextView) dialog.findViewById(R.id.about_maintext);
		mainText.setText(this.getString(R.string.about_maintext));
		// mainText.setMovementMethod(LinkMovementMethod.getInstance());

		// set up image view

		ImageView img = (ImageView) dialog.findViewById(R.id.about_image);

		img.setImageResource(R.drawable.icon);
		// set up button

		Button button = (Button) dialog.findViewById(R.id.about_cancel);

		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				dialog.cancel();

			}

		});

		// now that the dialog is set up, it's time to show it

		dialog.show();
	}

	/**
	 * ProgresDialog Handler
	 */
	Handler progressHandle = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			String message = HsDroidMain.this.getString(R.string.progress_loading);
			switch (msg.what) {
			case 1:
				message = HsDroidMain.this.getString(R.string.progress_login);
				break;
			case 2:
				message = HsDroidMain.this.getString(R.string.progress_webcheck);
				break;
			case 3:
				message = HsDroidMain.this.getString(R.string.progress_cookie);
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
		AlertDialog ad = new AlertDialog.Builder(this).setPositiveButton(this.getString(R.string.error_ok), null)
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
					Looper.prepare();
					// Post Daten zusammen bauen
					HttpPost post = new HttpPost(UPDATE_URL);
					List<NameValuePair> nvps = new ArrayList<NameValuePair>();

					// bessere namen als "asdf" und "fdsa" für die post daten
					// sind ihnen beim basteln der Seite nicht eingefallen xD
					// ganz nach dem motto "security by obscurity" ;)
					nvps.add(new BasicNameValuePair("asdf", login));
					nvps.add(new BasicNameValuePair("fdsa", pw));
					nvps.add(new BasicNameValuePair("submit", "Anmelden"));
					post.setHeader("Content-Type", "application/x-www-form-urlencoded");
					post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));

					// http Anfrage starten
					response = client.execute(post);

					entity = response.getEntity();
					InputStream is = entity.getContent();
					BufferedReader rd = new BufferedReader(new InputStreamReader(is), 4096);
					String line;
					int count = 0;
					progressHandle.sendMessage(progressHandle.obtainMessage(2));
					// response auswerten
					while ((line = rd.readLine()) != null) {
						// TODO check login success
						loginStringTest(line, count);

						if (line.contains("asi=")) {
							// wenn ein asi Key gefunden wird, kann man davon
							// ausgehen, dass man angemeldet is ;)
							int begin = line.indexOf("asi=");
							asiKey = line.substring(begin + 4, begin + 24);
							break;
						}
						count++;
					}
					rd.close();
					is.close();

					progressHandle.sendMessage(progressHandle.obtainMessage(3));
					HsDroidMain.cookies = client.getCookieStore().getCookies();
					// cookies darf nicht leer sein
					if (cookies.size() == 0)
						throw new HSLoginException(HsDroidMain.this.getString(R.string.error_cookie_empty));

					// progress dialog schließen
					progressDialog.dismiss();

					// start activity "NotenViewer"
					Intent i = new Intent(HsDroidMain.this, GradesListView.class);
					i.putExtra("asiKey", asiKey);
					startActivity(i);

					if (entity != null)
						entity.consumeContent();
				} catch (Exception e) {
					progressDialog.dismiss();
					createDialog(HsDroidMain.this.getString(R.string.error_couldnt_connect),

					e.getMessage());
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Looper.loop();
			}
		};
		t.start();
	}

	/**
	 * Quick'n'Dirty Test for Login response Lines
	 * 
	 * @param line
	 *            {@link String} with a line from the login response
	 * @param count
	 *            {@link Integer} line count
	 * @throws HSLoginException
	 */
	private void loginStringTest(String line, int count) throws HSLoginException {
		// TODO geht bestimmt schöner, aber funktioniert ;)

		if (count < 10) { // sollte innerhalb der ersten 10 Zeilen stehen..
			if (line.contains("System nicht verf")) {// blöööde Umlaute...
				throw new HSLoginException(this.getString(R.string.error_site_down));
			}
		}
		// A</u>nmelden
		// if (line.contains("Anmeldung fehlgeschlagen")) {
		if (count < 50 && count > 30) {
			if (line.contains("A</u>nmelden")) {
				throw new HSLoginException(this.getString(R.string.error_login_failed));
			}
		}
	}

}
