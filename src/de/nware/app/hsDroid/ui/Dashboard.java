package de.nware.app.hsDroid.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import de.nware.app.hsDroid.R;

public class Dashboard extends nActivity {
	private static final String TAG = "hsDroid-Dashboard";

	private GridView dashboard;

	private final int MENU_GRADES = 0;
	private final int MENU_CERTIFICATIONS = 1;
	private final int MENU_SETTINGS = 5;

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

				// switch (position) {
				// case MENU_GRADES:
				// Log.d(TAG, "prepare launch");
				// intent = new Intent(getBaseContext(), GradesList.class);
				// startActivityForResult(intent, 1);
				// Log.d(TAG, "prepare launched");
				// break;
				// case MENU_CERTIFICATIONS:
				// intent = new Intent(getBaseContext(), Certifications.class);
				// startActivityForResult(intent, 1);
				// break;
				// case MENU_SETTINGS:
				//
				// break;
				//
				// default:
				// break;
				// }
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

				ImageView imageView = (ImageView) view.findViewById(R.id.dash_item_icon);
				imageView.setImageResource(mDashIcon[position]);
			} else {
				view = convertView;
			}
			return view;
		}

		private Integer[] mDashIcon = { R.drawable.view, R.drawable.refresh, R.drawable.preferences };
		private String[] mDashText = { "Notenspiegel", "Bescheinigungen", "Einstellungen" };
		private Class[] mDashClass = { GradesList.class, Certifications.class, Preferences.class };

	}
}
