package de.nware.app.hsDroid.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
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
