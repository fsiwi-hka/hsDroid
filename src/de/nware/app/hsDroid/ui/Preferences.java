package de.nware.app.hsDroid.ui;

import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.widget.Toast;
import de.nware.app.hsDroid.R;
import de.nware.app.hsDroid.data.StaticSessionData;
import de.nware.app.hsDroid.provider.onlineService2Data.ExamsCol;

/**
 *  This file is part of hsDroid.
 * 
 *  hsDroid is an Android App for students to view their grades from QIS Online Service 
 *  Copyright (C) 2011,2012  Oliver Eichner <n0izeland@gmail.com>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *  
 *  hsDroid is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  
 *  Diese Datei ist Teil von hsDroid.
 *  
 *  hsDroid ist Freie Software: Sie können es unter den Bedingungen
 *  der GNU General Public License, wie von der Free Software Foundation,
 *  Version 3 der Lizenz oder jeder späteren veröffentlichten Version, 
 *  weiterverbreiten und/oder modifizieren.
 *  
 *  hsDroid wird in der Hoffnung, dass es nützlich sein wird, aber
 *  OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
 *  Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
 *  Siehe die GNU General Public License für weitere Details.
 *  
 *  Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 *  Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 */

/**
 * Einstellungen für hsDroid
 * 
 * @author Oliver Eichner
 * 
 */
public class Preferences extends PreferenceActivity {
	private static final String TAG = "hsDroid-PreferenceActivity";
	private Toast toast;

	private static final int RET_DIRNAME = 10;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (resultCode) {
		case RESULT_OK:
			switch (requestCode) {
			case RET_DIRNAME:
				SharedPreferences preferences = PreferenceManager
						.getDefaultSharedPreferences(this);
				SharedPreferences.Editor ed = preferences.edit();
				ed.putString("downloadPathPref", data.getDataString()
						.substring(7, data.getDataString().length()));
				ed.commit();

				break;

			default:
				break;
			}
			break;

		default:
			// Log.d(TAG, "other res: " + resultCode);
			break;
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	// TODO Funktion isIntentAvailable() wo anders hinpacken...
	/**
	 * Quelle:
	 * http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html
	 * 
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * 
	 * @param context
	 *            The application's environment.
	 * @param action
	 *            The Intent action to check for availability.
	 * 
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean isIntentAvailable(Context context, String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		return list.size() > 0;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferencescreen);

		updateSummaries();

		CheckBoxPreference saveLoginDataPref = (CheckBoxPreference) findPreference(getString(R.string.Preference_SaveLoginData));
		saveLoginDataPref
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference preference,
							Object newValue) {
						CheckBoxPreference cbr = (CheckBoxPreference) preference;
						Editor ed = preference.getEditor();
						if ((Boolean) newValue == false) {
							ed.remove(preference.getKey());
							CheckBoxPreference autoLogin = (CheckBoxPreference) findPreference(getString(R.string.Preference_AutoLogin));
							autoLogin.setChecked(false);
							ed.putBoolean(
									getString(R.string.Preference_AutoLogin),
									false);
							// Log.d(TAG, "autologin:" + newValue);
						}
						ed.putBoolean(preference.getKey(), (Boolean) newValue);
						ed.commit();
						// Log.d(TAG, "savePW:" + newValue);
						cbr.setChecked((Boolean) newValue);
						return (Boolean) newValue;
					}
				});

		Preference customPref = (Preference) findPreference(getString(R.string.Preference_HighlightColor));
		customPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						showToast("Implement Me :)");
						// startColorPickerDialog();
						return true;
					}

				});

		// Datenbank Löschen
		Preference clearDBPref = (Preference) findPreference("clearDBPref");
		clearDBPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						showToast(getString(R.string.text_delDBCompleted));
						clearDB();
						return true;
					}

				});

		Preference selectFolderPref = (Preference) findPreference(getString(R.string.Preference_DownloadPath));
		selectFolderPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {
					// TODO möglichkeit über AndExplorer.. etc..
					// verzeichnis aufrufen

					public boolean onPreferenceClick(Preference preference) {

						// http://android-developers.blogspot.com/2009/01/can-i-use-this-intent.html

						if (isIntentAvailable(preference.getContext(),
								"org.openintents.action.PICK_DIRECTORY")) {
							Intent dirIntent = new Intent(
									"org.openintents.action.PICK_DIRECTORY");
							startActivityForResult(dirIntent, RET_DIRNAME);
						} else {
							Intent intent = new Intent(getApplicationContext(),
									DirChooser.class);
							startActivity(intent);
						}
						return true;
					}

				});

		Preference delCookiePref = (Preference) findPreference("delCookiePref");
		delCookiePref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {

						if (StaticSessionData.cookies != null) {
							// XXX .clear() geht nicht..
							// java.lang.UnsupportedOperationException warum??
							// StaticSessionData.cookies.clear();
							StaticSessionData.cookies = null;
							showToast("Cookie gelöscht");
						} else {
							Log.d(TAG,
									"Versuch nicht vorhandenes Cookie zu lösche. So geht das aber nich ;).");
							showToast(getString(R.string.error_cookie_empty));
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

				// Defaults laden
				StaticSessionData
						.resetSharedPreferences(getApplicationContext());

				// Delete Cookie
				StaticSessionData.cookies = null;

				showToast(getString(R.string.text_resetSettingsComplete));
				// FIXME Invalidate View geht nich??
				// main updaten.. user und pw löschen..
				// finish();
				return true;
			}

		});

		// EditTextfeld dees ConnectionTimeout sind nur Zahlen..
		EditTextPreference pref = (EditTextPreference) findPreference(getText(R.string.Preference_ConnectionTimeout));
		pref.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
		pref.getEditText().setSelectAllOnFocus(true);
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
			toast = Toast.makeText(getApplicationContext(), text,
					Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	// private void startColorPickerDialog() {
	// Dialog bla = new Dialog(this);
	// // TODO implement color picker
	// bla.setTitle("Implement Me");
	// bla.setCancelable(true);
	// bla.setCanceledOnTouchOutside(true);
	// // Log.d("prefs", "start dialog");
	// bla.show();
	// // Log.d("prefs", "stop dialog");
	//
	// }

	private void updateSummaries() {
		Preference pref = (Preference) findPreference(getString(R.string.Preference_DownloadPath));
		pref.setSummary(getString(R.string.text_path)
				+ " "
				+ pref.getSharedPreferences().getString(
						pref.getKey(),
						Environment.getExternalStorageDirectory() + "/"
								+ Environment.DIRECTORY_DOWNLOADS));
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateSummaries();
	}
}
