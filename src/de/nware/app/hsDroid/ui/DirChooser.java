package de.nware.app.hsDroid.ui;

import static de.nware.app.hsDroid.data.StaticSessionData.sPreferences;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;

import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import de.nware.app.hsDroid.R;

/**
 * Primitiver FolderManager :P
 * 
 * @author Oliver Eichner
 * 
 */
public class DirChooser extends nListActivity {
	// private static final String TAG = "hsDroid-DirChooser";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dir_chooser_list);
		customTitle(getString(R.string.dirchooser_label));
		if (savedInstanceState != null) {

		}

		sPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		prepareDirectoryList();

	}

	private void prepareDirectoryList() {
		final ArrayList<File> sdDirs = new ArrayList<File>();
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
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
			getListView().setAdapter(
					new ArrayAdapter<File>(getApplicationContext(),
							R.layout.dir_chooser_item, sdDirs));
			getListView().setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int pos, long id) {
					showToast(sdDirs.get(pos).getAbsolutePath());
					// hasSubDirs(sdDirs.get(pos));
					Editor ed = sPreferences.edit();
					ed.putString("downloadPathPref", sdDirs.get(pos)
							.getAbsolutePath());
					ed.commit();
					finish();
				}
			});
		} else {
			// TODO String exportieren
			showToast(getString(R.string.error_nosdcard));
		}
	}

	// private boolean hasSubDirs(File file) {
	// for (File childDir : file.listFiles()) {
	// if (childDir.isDirectory()) {
	// Log.d(TAG, "Pfad :" + file.getAbsolutePath() +
	// " hat unterverzeichnisse");
	// return true;
	// }
	// }
	// return false;
	// }
}
