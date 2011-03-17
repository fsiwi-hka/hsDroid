package nware.app.hska.hsDroid;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

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
		titleSG.setText("Sehr Gut");
		TextView gradeSG = (TextView) dialog.findViewById(R.id.examInfoTextViewSG2);
		gradeSG.setText(exInfo.getSehrGutAmount());

		TextView titleG = (TextView) dialog.findViewById(R.id.examInfoTextViewG1);
		titleG.setText("Gut");
		TextView gradeG = (TextView) dialog.findViewById(R.id.examInfoTextViewG2);
		gradeG.setText(exInfo.getGutAmount());

		TextView titleB = (TextView) dialog.findViewById(R.id.examInfoTextViewB1);
		titleB.setText("Befriedigend");
		TextView gradeB = (TextView) dialog.findViewById(R.id.examInfoTextViewB2);
		gradeB.setText(exInfo.getBefriedigendAmount());

		TextView titleA = (TextView) dialog.findViewById(R.id.examInfoTextViewA1);
		titleA.setText("Ausreichend");
		TextView gradeA = (TextView) dialog.findViewById(R.id.examInfoTextViewA2);
		gradeA.setText(exInfo.getAusreichendAmount());

		TextView titleN = (TextView) dialog.findViewById(R.id.examInfoTextViewN1);
		titleN.setText("Nicht Ausreichend");
		TextView gradeN = (TextView) dialog.findViewById(R.id.examInfoTextViewN2);
		gradeN.setText(exInfo.getNichtAusreichendAmount().trim()); // FIXME null
																	// text

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
