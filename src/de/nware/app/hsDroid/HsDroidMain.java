package de.nware.app.hsDroid;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import de.nware.app.hsDroid.data.StaticSessionData;
import de.nware.app.hsDroid.logic.LoginThread;
import de.nware.app.hsDroid.ui.AboutDialog;
import de.nware.app.hsDroid.ui.Dashboard;
import de.nware.app.hsDroid.ui.nActivity;

/**
 * 
 * @author Oliver Eichner
 * 
 */
public class HsDroidMain extends nActivity implements OnClickListener {

	private final static String TAG = "hsDroid-main";

	private LoginThread mLoginThread = null;

	private ProgressDialog mProgressDialog = null;
	private static final int DIALOG_PROGRESS = 1;
	private EditText mTextfieldUsername;
	private EditText mTextfieldPassword;
	private CheckBox mLoginCheckBox;

	private boolean savePassword = false;
	private SharedPreferences mSharedPreferences;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);
		customTitle("Anmeldung");

		mTextfieldUsername = (EditText) findViewById(R.id.username);
		mTextfieldPassword = (EditText) findViewById(R.id.password);

		mLoginCheckBox = (CheckBox) findViewById(R.id.login_checkBox);

		mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		String savedUser = mSharedPreferences.getString("UserSave", "");
		String savedPass = mSharedPreferences.getString("PassSave", "");
		savePassword = mSharedPreferences.getBoolean("saveLoginDataPref", false);

		mLoginCheckBox.setChecked(savePassword);

		if (savePassword && !savedUser.equals("")) {
			mTextfieldPassword.setText(savedPass);
		}

		mTextfieldUsername.setText(savedUser);

		final boolean autoLogin = mSharedPreferences.getBoolean("autoLoginPref", false);

		if (autoLogin && savePassword) {
			doLogin();
		} else if (autoLogin && !savePassword) {
			showToast("Autologin nur mit gespeichertem Passwort möglich");
		}

		mLoginCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences.Editor editor = mSharedPreferences.edit();
				if (mLoginCheckBox.isChecked()) {
					editor.putBoolean("saveLoginDataPref", true);
				} else {
					editor.putBoolean("saveLoginDataPref", false);
					if (mSharedPreferences.getBoolean("autoLoginPref", false)) {
						editor.putBoolean("autoLoginPref", false);
						showToast("Autologin wurde deaktiviert.");
					}

				}
				editor.commit();
			}
		});

		Button button = (Button) findViewById(R.id.login);

		button.setOnClickListener(this);
		// button.setOnClickListener(new View.OnClickListener() {
		// public void onClick(View v) {
		// onClick(v);
		// }
		// });

		// passwort actionGo
		mTextfieldPassword.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event != null && event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
					onClick(v);
					return true;
				}
				return false;
			}
		});

	}

	public void doLogin() {
		// Benutzernamen: nicht anzeigbare Zeichen entfernen, alles
		// klein schreiben
		// (leerzeichen killen und Buchstaben klein,
		// wegen leerzeichen bei android autovervollständigung beim tippen in
		// der textbox...
		String username = mTextfieldUsername.getText().toString().trim().toLowerCase();
		// Password: nicht anzeigbare Zeichen entfernen
		String password = mTextfieldPassword.getText().toString().trim();

		// Prüfeung falls sich ein anderer user anmeldet. damit nicht das
		// session cookie vom vorherigen user übernommen wird..
		if (!username.equals("") && !username.equals(mSharedPreferences.getString("UserSave", ""))) {
			if (StaticSessionData.cookies != null) {
				// StaticSessionData.cookies.clear();
				StaticSessionData.cookies = null;
			}
		}

		// prüfen ob Cookie Vorhanden und gültig ist..wenn ja, login
		// überspringen
		if (StaticSessionData.cookies != null && !StaticSessionData.cookies.isEmpty()
				&& StaticSessionData.isCookieValid()) {
			Log.d("hsDroidMain", "Cookie still valid!!");
			mProgressHandle.sendEmptyMessage(LoginThread.MESSAGE_COMPLETE);
			return;
		}

		if (username.length() == 0) {
			createDialog(getText(R.string.error), getText(R.string.error_name_missing));
			return;
		} else if (!username.matches("^[a-zA-Z]{4}(00|10){1}[0-9]{2}")) {
			createDialog(getText(R.string.error), getText(R.string.error_name_incorrect));
			return;
		} else

		if (password.length() == 0) {
			createDialog(getText(R.string.error), getText(R.string.error_password_missing));
			return;
		} else {

			// FIXME zu unsicher.. wird alles im plaintext gespeichert..
			// eventuell sqlite mit encryption..
			// speichern von user und passwort
			SharedPreferences.Editor editor = mSharedPreferences.edit();
			if (mLoginCheckBox.isChecked()) {
				editor.putString("PassSave", password);
				editor.putBoolean("saveLoginDataPref", true);
			} else {
				// editor.remove("UserSave");
				editor.remove("PassSave");
				editor.putBoolean("saveLoginDataPref", false);
			}
			editor.putString("UserSave", username);
			editor.commit(); // Very important

			showDialog(DIALOG_PROGRESS);
			mLoginThread = new LoginThread(mProgressHandle, username, password);
			mLoginThread.start();
			mLoginThread.login();

		}
	}

	/**
	 * ProgresDialog Handler für Login
	 */
	final Handler mProgressHandle = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LoginThread.MESSAGE_COMPLETE:

				if (mLoginThread != null) {
					mLoginThread.stopThread();
					mLoginThread = null;
				}

				removeDialog(DIALOG_PROGRESS);

				Intent i = new Intent(HsDroidMain.this, Dashboard.class);
				startActivity(i);
				break;
			case LoginThread.MESSAGE_ERROR:
				Log.d("handler login error", msg.getData().getString("Message"));

				removeDialog(DIALOG_PROGRESS);

				String errorMessage = msg.getData().getString("Message");

				if (errorMessage.equals(LoginThread.ERROR_MSG_SITE_MAINTENANCE)) {
					errorMessage = HsDroidMain.this.getString(R.string.error_site_down);
				} else if (errorMessage.equals(LoginThread.ERROR_MSG_LOGIN_FAILED)) {
					errorMessage = HsDroidMain.this.getString(R.string.error_login_failed);
				} else if (errorMessage.equals(LoginThread.ERROR_MSG_COOKIE_MISSING)) {
					errorMessage = HsDroidMain.this.getString(R.string.error_cookie_empty);
				}

				createDialog(HsDroidMain.this.getString(R.string.error_couldnt_connect), errorMessage);

				mLoginThread.stopThread();
				mLoginThread.kill();
				mLoginThread = null;
				break;
			case LoginThread.MESSAGE_PROGRESS_CONNECT:
				mProgressDialog.setMessage(HsDroidMain.this.getString(R.string.progress_connect));
				break;
			case LoginThread.MESSAGE_PROGRESS_PARSE:
				mProgressDialog.setMessage(HsDroidMain.this.getString(R.string.progress_parse));
				break;
			case LoginThread.MESSAGE_PROGRESS_COOKIE:
				mProgressDialog.setMessage(HsDroidMain.this.getString(R.string.progress_cookie));
				break;
			default:
				Log.d("progressHandler Main", "unknown message: " + msg.what);
				removeDialog(DIALOG_PROGRESS);

				mLoginThread.stopThread();
				mLoginThread = null;
				break;
			}
		}
	};

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus) {
			mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
			savePassword = mSharedPreferences.getBoolean("saveLoginDataPref", false);
			mLoginCheckBox.setChecked(savePassword);
		}
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setIndeterminate(true);
			mProgressDialog.setCancelable(false);
			return mProgressDialog;
		default:
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu_login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// case R.id.menu_preferences:
		// Intent settingsActivity = new Intent(getBaseContext(),
		// Preferences.class);
		// startActivity(settingsActivity);
		//
		// return true;
		case R.id.menu_about:
			aboutDialog();

			return true;
		default:
			System.out.println("id:" + item.getItemId() + " about: " + R.id.menu_about);
			return super.onOptionsItemSelected(item);
		}

	}

	public void aboutDialog() {
		new AboutDialog(this);
	}

	/**
	 * Creates an custom {@link AlertDialog}
	 * 
	 * @param title
	 *            {@link String} dialog title
	 * @param text
	 *            {@link String} dialog text
	 */
	private void createDialog(CharSequence title, CharSequence text) {
		AlertDialog ad = new AlertDialog.Builder(this).setPositiveButton(getText(R.string.error_ok), null)
				.setTitle(title).setMessage(text).create();
		ad.show();
	}

	@Override
	public void onClick(View v) {
		doLogin();
	}

}
