package de.nware.app.hsDroid.ui;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import de.nware.app.hsDroid.R;
import de.nware.app.hsDroid.provider.onlineService2Data.ExamInfos;

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
 * Dialog für die Notenverteilung
 * 
 * @author Oliver Eichner
 * 
 */
public class ExamInfoDialog {

	public ExamInfoDialog(Context context, String name, String nr, String sem, Cursor exInfoCursor) {
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.exam_info_dialog);
		dialog.setTitle("Notenverteilung");

		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);

		TextView aboutTitle = (TextView) dialog.findViewById(R.id.examinfo_title);
		aboutTitle.setText(name);
		TextView aboutSubTitle = (TextView) dialog.findViewById(R.id.examinfo_SubTitle);
		aboutSubTitle.setText(nr + " - " + sem);

		TextView titleSG = (TextView) dialog.findViewById(R.id.examInfoTextViewSG1);
		titleSG.setText("(1,0 - 1,3)");
		TextView gradeSG = (TextView) dialog.findViewById(R.id.examInfoTextViewSG2);
		String sgText = exInfoCursor.getString(exInfoCursor.getColumnIndexOrThrow(ExamInfos.SEHRGUT));
		gradeSG.setText(sgText);

		TextView titleG = (TextView) dialog.findViewById(R.id.examInfoTextViewG1);
		titleG.setText("(1,7 - 2,3)");
		TextView gradeG = (TextView) dialog.findViewById(R.id.examInfoTextViewG2);
		String gText = exInfoCursor.getString(exInfoCursor.getColumnIndexOrThrow(ExamInfos.GUT));
		gradeG.setText(gText);

		TextView titleB = (TextView) dialog.findViewById(R.id.examInfoTextViewB1);
		titleB.setText("(2,7 - 3,3)");
		TextView gradeB = (TextView) dialog.findViewById(R.id.examInfoTextViewB2);
		String bText = exInfoCursor.getString(exInfoCursor.getColumnIndexOrThrow(ExamInfos.BEFRIEDIGEND));
		gradeB.setText(bText);

		TextView titleA = (TextView) dialog.findViewById(R.id.examInfoTextViewA1);
		titleA.setText("(3,7 - 4,0)");
		TextView gradeA = (TextView) dialog.findViewById(R.id.examInfoTextViewA2);
		String aText = exInfoCursor.getString(exInfoCursor.getColumnIndexOrThrow(ExamInfos.AUSREICHEND));
		gradeA.setText(aText);

		TextView titleN = (TextView) dialog.findViewById(R.id.examInfoTextViewN1);
		titleN.setText("(4,7 - 5,0)");
		TextView gradeN = (TextView) dialog.findViewById(R.id.examInfoTextViewN2);
		String naText = exInfoCursor.getString(exInfoCursor.getColumnIndexOrThrow(ExamInfos.NICHTAUSREICHEND));
		gradeN.setText(naText);

		TextView average = (TextView) dialog.findViewById(R.id.examinfoAverage);
		String avgText = exInfoCursor.getString(exInfoCursor.getColumnIndexOrThrow(ExamInfos.AVERAGE));
		average.setText(avgText);

		Button button = (Button) dialog.findViewById(R.id.examinfo_cancel);
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dialog.cancel();
			}

		});

		dialog.show();
	}
}
