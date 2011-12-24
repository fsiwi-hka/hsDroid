package de.nware.app.hsDroid.ui;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import de.nware.app.hsDroid.R;
import de.nware.app.hsDroid.data.StaticSessionData;
import de.nware.app.hsDroid.provider.onlineService2Data.ExamsCol;

public class Preferences extends PreferenceActivity {
	private static final String TAG = "hsDroid-PreferenceActivity";
	private Toast toast;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar aBar = getActionBar();
		aBar.setHomeButtonEnabled(true);

		addPreferencesFromResource(R.xml.preferences);

		updateSummaries();

		CheckBoxPreference saveLoginDataPref = (CheckBoxPreference) findPreference("saveLoginDataPref");
		saveLoginDataPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				CheckBoxPreference cbr = (CheckBoxPreference) preference;
				Editor ed = preference.getEditor();
				if ((Boolean) newValue == false) {
					ed.remove(preference.getKey());
					CheckBoxPreference autoLogin = (CheckBoxPreference) findPreference("autoLoginPref");
					autoLogin.setChecked(false);
					ed.putBoolean("autoLoginPref", false);
					Log.d(TAG, "autologin:" + newValue);
				}
				ed.putBoolean(preference.getKey(), (Boolean) newValue);
				ed.commit();
				Log.d(TAG, "savePW:" + newValue);
				cbr.setChecked((Boolean) newValue);
				return (Boolean) newValue;
			}
		});

		Preference customPref = (Preference) findPreference("highlightColorPref");
		customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				showToast("Implement Me :)");
				// startColorPickerDialog();
				return true;
			}

		});

		Preference clearDBPref = (Preference) findPreference("clearDBPref");
		clearDBPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				showToast("Datenbank gelöscht.");
				clearDB();
				return true;
			}

		});

		Preference selectFolderPref = (Preference) findPreference("downloadPathPref");
		selectFolderPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			// TODO möglichkeit über OI Dateimanager, AndExplorer.. etc..
			// verzeichnis aufrufen
			public boolean onPreferenceClick(Preference preference) {

				// showToast("Implement me :)");
				// Log.d(TAG, "dl dir: " + Environment.DIRECTORY_DOWNLOADS);
				// Log.d(TAG, "ext DirStat: " +
				// Environment.getExternalStorageState());

				Intent intent = new Intent(getApplicationContext(), DirChooser.class);
				startActivity(intent);
				// Preference pref = (Preference)
				// findPreference("downloadPathPref");
				// pref.setSummary("Pfad: " +
				// pref.getSharedPreferences().getString(pref.getKey(), ""));
				return true;
			}

		});

		Preference delCookiePref = (Preference) findPreference("delCookiePref");
		delCookiePref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {

				if (StaticSessionData.cookies != null) {
					// FIXME .clear() geht nicht..
					// java.lang.UnsupportedOperationException warum??
					// StaticSessionData.cookies.clear();
					StaticSessionData.cookies = null;
					showToast("Cookie gelöscht");
				} else {
					Log.d(TAG, "Versuch nicht vorhandenes Cookie zu lösche. Na na na, das geht aber nich.");
					showToast("Kein Cookie vorhanden.");
				}

				return true;
			}

		});

		Preference delPref = (Preference) findPreference("delPref");
		delPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				Editor ed = preference.getEditor();
				ed.clear();
				ed.commit();
				showToast("Einstellunge zurückgesetzt.");
				// FIXME Invalidate View geht nich??
				// main updaten.. user und pw löschen..
				finish();
				return true;
			}

		});
	}

	private void clearDB() {
		Thread clrDB = new Thread() {
			public void run() {
				try {
					Looper.prepare();
					final ContentResolver resolver = getContentResolver();
					resolver.delete(ExamsCol.CONTENT_URI, null, null);

				} catch (Exception e) {
					e.printStackTrace();

				}
			}
		};
		clrDB.start();

	}

	private void showToast(String text) {
		// Toast erstellen oder ändern...
		if (toast != null) {
			toast.cancel();
			toast.setText(text);
			toast.show();
		} else {
			toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	private void startColorPickerDialog() {
		Dialog bla = new Dialog(this);
		// TODO implement color picker
		bla.setTitle("Implement Me");
		bla.setCancelable(true);
		bla.setCanceledOnTouchOutside(true);
		Log.d("prefs", "start dialog");
		bla.show();
		Log.d("prefs", "stop dialog");

	}

	private void updateSummaries() {
		Preference pref = (Preference) findPreference("downloadPathPref");
		pref.setSummary("Pfad: "
				+ pref.getSharedPreferences().getString(pref.getKey(),
						Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS));
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		updateSummaries();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, "unkown itemID: " + item.getItemId() + " title: " + item.getTitle());
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, Dashboard.class);
			// Falls Activity schon läuft, kein neue starten, sondern alle
			// Activities die darüber ligen schließen und wechseln.
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			Log.d(TAG, "unkown itemID: " + item.getItemId() + " title: " + item.getTitle());
			return super.onOptionsItemSelected(item);
		}
	}
}
