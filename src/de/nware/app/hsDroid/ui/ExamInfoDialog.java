package de.nware.app.hsDroid.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import de.nware.app.hsDroid.R;
import de.nware.app.hsDroid.data.ExamInfo;

public class ExamInfoDialog {

	public ExamInfoDialog(Context context, ExamInfo exInfo) {
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.exam_info_dialog);
		dialog.setTitle("Notenverteilung");
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);

		TextView aboutTitle = (TextView) dialog.findViewById(R.id.examinfo_title);
		aboutTitle.setText(exInfo.getExam().getExamName());
		TextView aboutSubTitle = (TextView) dialog.findViewById(R.id.examinfo_SubTitle);
		aboutSubTitle.setText(exInfo.getExam().getExamNr() + " - " + exInfo.getExam().getSemester());

		TextView titleSG = (TextView) dialog.findViewById(R.id.examInfoTextViewSG1);
		titleSG.setText("(1,0 - 1,3)");
		TextView gradeSG = (TextView) dialog.findViewById(R.id.examInfoTextViewSG2);
		gradeSG.setText(exInfo.getSehrGutAmount());

		TextView titleG = (TextView) dialog.findViewById(R.id.examInfoTextViewG1);
		titleG.setText("(1,7 - 2,3)");
		TextView gradeG = (TextView) dialog.findViewById(R.id.examInfoTextViewG2);
		gradeG.setText(exInfo.getGutAmount());

		TextView titleB = (TextView) dialog.findViewById(R.id.examInfoTextViewB1);
		titleB.setText("(2,7 - 3,3)");
		TextView gradeB = (TextView) dialog.findViewById(R.id.examInfoTextViewB2);
		gradeB.setText(exInfo.getBefriedigendAmount());

		TextView titleA = (TextView) dialog.findViewById(R.id.examInfoTextViewA1);
		titleA.setText("(3,7 - 4,0)");
		TextView gradeA = (TextView) dialog.findViewById(R.id.examInfoTextViewA2);
		gradeA.setText(exInfo.getAusreichendAmount());

		TextView titleN = (TextView) dialog.findViewById(R.id.examInfoTextViewN1);
		titleN.setText("(4,7 - 5,0)");
		TextView gradeN = (TextView) dialog.findViewById(R.id.examInfoTextViewN2);
		gradeN.setText(exInfo.getNichtAusreichendAmount());

		TextView average = (TextView) dialog.findViewById(R.id.examinfoAverage);
		average.setText(exInfo.getAverage());

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
