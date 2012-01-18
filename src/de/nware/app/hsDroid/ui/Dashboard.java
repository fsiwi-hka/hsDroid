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

public class Dashboard extends nActivity {
	private static final String TAG = "hsDroid-Dashboard";

	private GridView dashboard;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard_grid);
		customTitle("Menü");

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
			Log.d("Main menu:", "about");
			new AboutDialog(this);

			return true;
		default:
			Log.d("Main menu:", "default");
			System.out.println("id:" + item.getItemId() + " about: " + R.id.menu_about);
			return super.onOptionsItemSelected(item);
		}

	}
}
