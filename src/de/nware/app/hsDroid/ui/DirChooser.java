package de.nware.app.hsDroid.ui;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import de.nware.app.hsDroid.R;

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
 * Primitiver FolderManager :P
 * 
 * @author Oliver Eichner
 * 
 */
public class DirChooser extends nListActivity {
	private static final String TAG = "hsDroid-DirChooser";
	private SharedPreferences mPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dir_chooser_list);
		customTitle(getString(R.string.dirchooser_label));
		if (savedInstanceState != null) {

		}

		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		prepareDirectoryList();

	}

	private void prepareDirectoryList() {
		final ArrayList<File> sdDirs = new ArrayList<File>();
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			File extDir = Environment.getExternalStorageDirectory();
			// Log.d(TAG, "absolutePath:" + extDir.getAbsolutePath());
			// Log.d(TAG, "canonicalPath:" + extDir.getAbsolutePath());
			FileFilter dirFilter = new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					return pathname.isDirectory();
				}
			};
			for (File file : extDir.listFiles(dirFilter)) {
				// Log.d(TAG, "SD Dir:" + file.getName());
				if (!file.isHidden()) {
					sdDirs.add(file);
				}

			}
			// Sortiere Liste
			Collections.sort(sdDirs);
			// Setze Liste in ListView
			getListView()
					.setAdapter(new ArrayAdapter<File>(getApplicationContext(), R.layout.dir_chooser_item, sdDirs));
			getListView().setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
					showToast(sdDirs.get(pos).getAbsolutePath());
					// hasSubDirs(sdDirs.get(pos));
					Editor ed = mPreferences.edit();
					ed.putString("downloadPathPref", sdDirs.get(pos).getAbsolutePath());
					ed.commit();
					finish();
				}
			});
		} else {
			// TODO String exportieren
			showToast(getString(R.string.error_nosdcard));
		}
	}

	private boolean hasSubDirs(File file) {
		for (File childDir : file.listFiles()) {
			if (childDir.isDirectory()) {
				Log.d(TAG, "Pfad :" + file.getAbsolutePath() + " hat unterverzeichnisse");
				return true;
			}
		}
		return false;
	}
}
