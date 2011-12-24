package de.nware.app.hsDroid.ui;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.nware.app.hsDroid.R;

public class nActivity extends Activity {

	private boolean customTitleSupported;
	private ProgressBar titleProgressBar = null;
	private Toast toast = null;
	private final String TAG = "nActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// customTitleSupported =
		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		// CustomTitle setzen
		// customTitle(getText(R.string.app_name).toString(),
		// getText(R.string.app_version).toString());

		ActionBar actionBar = getActionBar();
		actionBar.setHomeButtonEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		actionBar.setDisplayShowTitleEnabled(false);
		Tab tab = actionBar.newTab().setText(R.string.title_head_mark)
				.setTabListener(new TabListener<GradesFragment>(this, "artist", ArtistFragment.class));
		actionBar.addTab(tab);

		tab = actionBar.newTab().setText(R.string.title_Certifications)
				.setTabListener(new TabListener<CertFragment>(this, "album", AlbumFragment.class));
		actionBar.addTab(tab);

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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		Log.d(TAG, "unkown itemID: " + item.getItemId() + " title: " + item.getTitle());
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, Dashboard.class);
			// Falls Activity schon läuft, kein neue starten, sondern alle
			// Activities die darüber ligen schließen und wechseln.
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			Log.d(TAG, "unkown itemID: " + item.getItemId() + " title: " + item.getTitle());
			return super.onOptionsItemSelected(item);
		}
	}

	public static class TabListener<T extends Fragment> implements ActionBar.TabListener {

		private Fragment mFragment;
		private final Activity mActivity;
		private final String mTag;
		private final Class<T> mClass;

		/**
		 * Constructor used each time a new tab is created.
		 * 
		 * @param activity
		 *            The host Activity, used to instantiate the fragment
		 * @param tag
		 *            The identifier tag for the fragment
		 * @param clz
		 *            The fragment's Class, used to instantiate the fragment
		 */
		public TabListener(Activity activity, String tag, Class<T> clz) {
			mActivity = activity;
			mTag = tag;
			mClass = clz;
		}

		/* The following are each of the ActionBar.TabListener callbacks */

		public void onTabSelected(Tab tab, FragmentTransaction ft) {
			// Check if the fragment is already initialized
			if (mFragment == null) {
				// If not, instantiate and add it to the activity
				mFragment = Fragment.instantiate(mActivity, mClass.getName());
				ft.add(android.R.id.content, mFragment, mTag);
			} else {
				// If it exists, simply attach it in order to show it
				ft.attach(mFragment);
			}
		}

		public void onTabUnselected(Tab tab, FragmentTransaction ft) {
			if (mFragment != null) {
				// Detach the fragment, because another one is being attached
				ft.detach(mFragment);
			}
		}

		public void onTabReselected(Tab tab, FragmentTransaction ft) {
			// User selected the already selected tab. Usually do nothing.
		}

	}

}
