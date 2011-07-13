package de.nware.app.hsDroid.ui;

import android.app.Dialog;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.Looper;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
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
		addPreferencesFromResource(R.xml.preferences);

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
}
