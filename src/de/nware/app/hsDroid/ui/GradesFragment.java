package de.nware.app.hsDroid.ui;

import android.app.ListFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import de.nware.app.hsDroid.R;
import de.nware.app.hsDroid.provider.onlineService2Data.ExamsCol;
import de.nware.app.hsDroid.ui.GradesList.ExamDBAdapter;

public class GradesFragment extends ListFragment {
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		String[] links = getResources().getStringArray(R.array.tut_links);

		String content = links[position];
		Intent showContent = new Intent(getActivity().getApplicationContext(), GradesList.class);
		showContent.setData(Uri.parse(content));
		startActivity(showContent);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final String[] from = new String[] { ExamsCol.EXAMNAME, ExamsCol.EXAMNR, ExamsCol.ATTEMPTS, ExamsCol.GRADE };
		final int[] to = new int[] { R.id.examName, R.id.examNr, R.id.examGrade, R.id.examAttempts, R.id.examGrade };
		ExamDBAdapter mExamAdapter = new ExamDBAdapter(null, R.layout.grade_row_item, cursor, from, to);
		setListAdapter(mExamAdapter);

	}
}
