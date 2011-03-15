package nware.app.hska.hsDroid;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ExamInfoDialog {

	public ExamInfoDialog(Context context, ExamInfo exInfo) {
		final Dialog dialog = new Dialog(context);
		dialog.setContentView(R.layout.exam_info_dialog);
		dialog.setTitle(exInfo.getExam().getExamName());
		dialog.setCancelable(true);
		dialog.setCanceledOnTouchOutside(true);

		TextView aboutTitle = (TextView) dialog.findViewById(R.id.examinfo_title);
		aboutTitle.setText("notenverteilung");

		TableLayout table = (TableLayout) dialog.findViewById(R.id.examinfo_tablelayout);

		TableRow rowSehrGut = (TableRow) dialog.findViewById(R.id.examInfoRow);
		TextView titleSG = (TextView) dialog.findViewById(R.id.examInfoTextView1);
		titleSG.setText("Sehr Gut");
		// rowSehrGut.addView(titleSG);
		TextView gradeSG = (TextView) dialog.findViewById(R.id.examInfoTextView1);
		titleSG.setText(exInfo.getSehrGutAmount());
		// rowSehrGut.addView(gradeSG);
		// table.addView(rowSehrGut);

		TableRow rowGut = (TableRow) dialog.findViewById(R.id.examInfoRow);
		TextView titleG = (TextView) dialog.findViewById(R.id.examInfoTextView1);
		titleSG.setText("Sehr Gut");
		// rowSehrGut.addView(titleG);
		TextView gradeG = (TextView) dialog.findViewById(R.id.examInfoTextView1);
		titleSG.setText(exInfo.getGutAmount());
		// rowSehrGut.addView(gradeG);
		// table.addView(rowGut);

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
