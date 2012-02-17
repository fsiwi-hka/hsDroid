package de.nware.app.hsDroid.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.nware.app.hsDroid.R;
import de.nware.app.hsDroid.data.StaticSessionData;

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
 * Erweiterung der standard {@link Activity}
 * 
 * @author Oliver Eichner
 * 
 */
public class nActivity extends Activity {

	private boolean customTitleSupported;
	private ProgressBar titleProgressBar = null;
	private Toast toast = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		if (StaticSessionData.sPreferences == null) {
			StaticSessionData.getSharedPrefs(this);
		}
		// CustomTitle setzen
		// customTitle(getText(R.string.app_name).toString(),
		// getText(R.string.app_version).toString());

		super.onCreate(savedInstanceState);
	}

	public void customTitle(String activityTitle) {
		if (activityTitle.length() > 20)
			activityTitle = activityTitle.substring(0, 20);
		// set up custom title
		if (customTitleSupported) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.titlebar);
			TextView titleTvActivityTitle = (TextView) findViewById(R.id.titleTvActivityTitle);
			titleTvActivityTitle.setText(activityTitle);
			// ProgressBar titleProgressBar;
			titleProgressBar = (ProgressBar) findViewById(R.id.leadProgressBar);
			// hide the progress bar if it is not needed
			hideTitleProgress();
		}
	}

	public void showTitleProgress() {
		if (titleProgressBar != null) {
			titleProgressBar.setVisibility(ProgressBar.VISIBLE);
		}
	}

	public void hideTitleProgress() {
		if (titleProgressBar != null) {
			titleProgressBar.setVisibility(ProgressBar.GONE);
		}
	}

	public void showToast(String text) {
		// XXX Test, wenn toast schon angezeigt wird
		// löschen und neu anzeigen..
		if (toast != null) {
			toast.setText(text);
			toast.show();
		} else {
			toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
			toast.show();
		}

	}
}
