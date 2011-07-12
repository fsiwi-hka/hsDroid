package de.nware.app.hsDroid.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import de.nware.app.hsDroid.R;

public class nActivity extends Activity {

	private boolean customTitleSupported;
	private ProgressBar titleProgressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

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
		Toast test = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
		test.show();
	}
}
