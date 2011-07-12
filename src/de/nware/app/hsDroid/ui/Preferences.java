package de.nware.app.hsDroid.ui;

import android.app.Dialog;
import android.content.ContentResolver;
import android.os.Bundle;
import android.os.Looper;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import de.nware.app.hsDroid.R;
import de.nware.app.hsDroid.provider.onlineService2Data.ExamsCol;

public class Preferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		Preference customPref = (Preference) findPreference("highlightColorPref");
		customPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				startDialog();
				return true;
			}

		});

		Preference clearDBPref = (Preference) findPreference("clearDBPref");
		clearDBPref.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			public boolean onPreferenceClick(Preference preference) {
				clearDB();
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

	private void startDialog() {
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
