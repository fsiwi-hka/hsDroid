package de.nware.app.hsDroid.ui;

import de.nware.app.hsDroid.R;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Helper which Creates an About Dialog
 * 
 * @author Oliver Eichner
 * 
 */
public class AboutDialog {

	public AboutDialog(Context context) {
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.about);
		dialog.setTitle(context.getString(R.string.menu_about));
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);

		TextView aboutTitle = (TextView) dialog.findViewById(R.id.about_title);
		aboutTitle.setText(context.getString(R.string.app_name) + " - " + context.getString(R.string.app_version));
		aboutTitle.setShadowLayer(1, 1, 1, Color.GRAY);

		TextView aboutSubTitle = (TextView) dialog.findViewById(R.id.about_SubTitle);
		aboutSubTitle.setText(context.getString(R.string.about_subtitle));
		aboutSubTitle.setShadowLayer(1, 1, 1, Color.GRAY);

		TextView mainText = (TextView) dialog.findViewById(R.id.about_maintext);
		mainText.setText(context.getString(R.string.about_maintext));

		ImageView img = (ImageView) dialog.findViewById(R.id.about_image);
		img.setImageResource(R.drawable.icon);

		Button button = (Button) dialog.findViewById(R.id.about_cancel);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
			}

		});

		dialog.show();
	}
}
