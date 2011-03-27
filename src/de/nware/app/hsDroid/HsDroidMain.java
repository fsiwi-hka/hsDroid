package de.nware.app.hsDroid;

import android.app.Activity;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import de.nware.app.hsDroid.data.StaticSessionData;
import de.nware.app.hsDroid.logic.LoginThread;
import de.nware.app.hsDroid.ui.AboutDialog;
import de.nware.app.hsDroid.ui.GradesList;
import de.nware.app.hsDroid.ui.Preferences;

/**
 * 
 * @author Oliver Eichner
 * 
 */
public class HsDroidMain extends Activity {

	private LoginThread mLoginThread = null;

	private ProgressDialog mProgressDialog = null;
	private static final int DIALOG_PROGRESS = 1;
	private EditText UserEditText;
	private EditText PassEditText;
	private CheckBox LoginCheckBox;

	private boolean checkBoxChecked = false;
	private SharedPreferences notenapp_preferences;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		UserEditText = (EditText) findViewById(R.id.username);
		PassEditText = (EditText) findViewById(R.id.password);

		LoginCheckBox = (CheckBox) findViewById(R.id.login_checkBox);
		notenapp_preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String savedUser = notenapp_preferences.getString("UserSave", "");
		String savedPass = notenapp_preferences.getString("PassSave", "");
		checkBoxChecked = notenapp_preferences.getBoolean("saveLoginDataPref", false);

		LoginCheckBox.setChecked(checkBoxChecked);

		if (checkBoxChecked && !savedUser.equals("")) {
			UserEditText.setText(savedUser);
			PassEditText.setText(savedPass);
		}
		boolean autoLogin = notenapp_preferences.getBoolean("autoLoginPref", false);
		if (autoLogin) {
			doLogin(getCurrentFocus());
		}

		LoginCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences.Editor editor = notenapp_preferences.edit();
				if (LoginCheckBox.isChecked()) {
					editor.putBoolean("saveLoginDataPref", true);
				} else {
					editor.putBoolean("saveLoginDataPref", false);
				}
				editor.commit();
			}
		});

		Button button = (Button) findViewById(R.id.login);

		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				doLogin(v);
			}
		});

	}

	private void doLogin(View v) {
		// Benutzernamen: nicht anzeigbare Zeichen entfernen, alles
		// klein schreiben
		// (leerzeichen killen und Buchstaben klein,
		// wegen leerzeichen bei android autovervollständigung beim tippen in
		// der textbox...
		String username = UserEditText.getText().toString().trim().toLowerCase();
		// Password: nicht anzeigbare Zeichen entfernen
		String password = PassEditText.getText().toString().trim();
		if (StaticSessionData.cookies != null) {
			mProgressHandle.sendEmptyMessage(LoginThread.MESSAGE_COMPLETE);
			return;
		}
		// FIXME zu unsicher.. wird alles im plaintext gespeichert..
		// eventuell sqlite mit encryption..
		// speichern von user und passwort
		SharedPreferences.Editor editor = notenapp_preferences.edit();
		if (LoginCheckBox.isChecked()) {
			editor.putString("UserSave", username);
			editor.putString("PassSave", password);
			editor.putBoolean("saveLoginDataPref", true);
		} else {
			editor.remove("UserSave");
			editor.remove("PassSave");
			editor.putBoolean("saveLoginDataPref", false);
		}
		editor.commit(); // Very important

		if (username.length() == 0) {
			createDialog(v.getContext().getString(R.string.error), v.getContext()
					.getString(R.string.error_name_missing));
			return;
		} else
		// FIXME bessere RegExp
		if (!username.matches("^[a-zA-Z]{4}(00|10){1}[0-9]{2}")) {
			createDialog(v.getContext().getString(R.string.error),
					v.getContext().getString(R.string.error_name_incorrect));
			return;
		} else

		if (password.length() == 0) {
			createDialog(v.getContext().getString(R.string.error),
					v.getContext().getString(R.string.error_password_missing));
			return;
		} else {
			showDialog(DIALOG_PROGRESS);
			mLoginThread = new LoginThread(mProgressHandle, username, password);
			mLoginThread.start();
			mLoginThread.login();

		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus) {
			notenapp_preferences = PreferenceManager.getDefaultSharedPreferences(this);
			checkBoxChecked = notenapp_preferences.getBoolean("saveLoginDataPref", false);
			LoginCheckBox.setChecked(checkBoxChecked);
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
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
			startActivity(settingsActivity);
			// notenapp_preferences =
			// PreferenceManager.getDefaultSharedPreferences(this);
			// checkBoxChecked =
			// notenapp_preferences.getBoolean("SaveLoginToggle", false);

			return true;
		case R.id.menu_about:
			Log.d("Main menu:", "about");
			aboutDialog();

			return true;
		default:
			Log.d("Main menu:", "default");
			System.out.println("id:" + item.getItemId() + " about: " + R.id.menu_about);
			return super.onOptionsItemSelected(item);
		}

	}

	public void aboutDialog() {
		new AboutDialog(this);
	}

	/**
	 * ProgresDialog Handler
	 */
	final Handler mProgressHandle = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			Log.d("handler msg.what:", String.valueOf(msg.what));
			switch (msg.what) {
			case LoginThread.MESSAGE_COMPLETE:
				Log.d("handler", "Login_complete");
				if (mLoginThread != null) {
					mLoginThread.stopThread();
					mLoginThread.kill();
				}
				mLoginThread = null;
				removeDialog(DIALOG_PROGRESS);
				Intent i = new Intent(HsDroidMain.this, GradesList.class);
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
				Log.d("progressHandler Main", "unknown message");
				removeDialog(DIALOG_PROGRESS);

				mLoginThread.stopThread();
				mLoginThread = null;
				break;
			}
		}
	};

	// private void quit(boolean success, Intent i) {
	// setResult((success) ? -1 : 0, i);
	// finish();
	// }

	/**
	 * Creates an custom {@link AlertDialog}
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

}
