package de.nware.app.hsDroid.ui;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import de.nware.app.hsDroid.R;

public class Preferences extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		// Get the custom preference
		// Preference customPref = (Preference) findPreference("customPref");
		// customPref.setOnPreferenceClickListener(new
		// OnPreferenceClickListener() {
		//
		// public boolean onPreferenceClick(Preference preference) {
		// SharedPreferences customSharedPreference =
		// getSharedPreferences("hsDroid", Activity.MODE_PRIVATE);
		// SharedPreferences.Editor editor = customSharedPreference.edit();
		// editor.putString("myCustomPref", "The preference has been clicked");
		// editor.commit();
		// return true;
		// }
		//
		// });
	}
}
