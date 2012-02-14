package de.nware.app.hsDroid.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
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
 * Hauptmenü Activity
 * 
 * @author Oliver Eichner
 * 
 */
public class Dashboard extends nActivity {
	private static final String TAG = "hsDroid-Dashboard";

	private GridView dashboard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard_grid);
		customTitle(getString(R.string.dashboard_label));

		dashboard = (GridView) findViewById(R.id.dashboard_gridview);
		dashboard.setAdapter(new DashboardAdapter(this));

		dashboard.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long arg3) {
				Log.d(TAG, "pos: " + position);

				Intent intent;
				intent = new Intent(getBaseContext(), (Class<?>) adapter.getItemAtPosition(position));
				startActivityForResult(intent, 1);
			}

		});

	}

	public class DashboardAdapter extends BaseAdapter {
		Context mContext;

		public DashboardAdapter(Context context) {
			this.mContext = context;
		}

		@Override
		public int getCount() {
			return mDashIcon.length;
		}

		@Override
		public Object getItem(int position) {
			// gibt Class object zurück
			return mDashClass[position];
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if (convertView == null) {
				LayoutInflater li = getLayoutInflater();
				view = li.inflate(R.layout.dashboard_item, null);
				TextView tv = (TextView) view.findViewById(R.id.dash_item_text);
				tv.setText(mDashText[position]);
				tv.setCompoundDrawablesWithIntrinsicBounds(0, (int) mDashIcon[position], 0, 0);
			} else {
				view = convertView;
			}
			return view;
		}

		private Integer[] mDashIcon = { R.drawable.ic_launcher_notenspiegel, R.drawable.ic_launcher_paper2,
				R.drawable.ic_launcher_preferences };
		private String[] mDashText = { "Notenspiegel", "Bescheinigungen", "Einstellungen" };

		@SuppressWarnings("rawtypes")
		private Class[] mDashClass = { GradesList.class, Certifications.class, Preferences.class };

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

			return true;
		case R.id.menu_about:
			new AboutDialog(this);

			return true;
		default:
			System.out.println("id:" + item.getItemId() + " about: " + R.id.menu_about);
			return super.onOptionsItemSelected(item);
		}

	}
}
