package nware.app.hska.hsDroid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ListActivity;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.SimpleAdapter;

public class GradesListView extends ListActivity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//layout festlegen
		setContentView(R.layout.grade_list_view);

		// Liste für die Liste ;)
		List<Map<String, Object>> examList = new ArrayList<Map<String, Object>>();

		// Map für Prüfungen
		Map<String, Object> data;

		// füllen von data, einfügen von data in examList
		for (int index = noten.examStorage.getSize() - 1; index >= 0; index--) {
			data = new HashMap<String, Object>();

			try {
				data.put("exName", noten.examStorage.getExam(index)
						.getExamName());
				data.put("exNr", noten.examStorage.getExam(index).getExamNr());
				
				int versuch = noten.examStorage.getExam(index).getAttempts();
				if ( versuch > 1) {
					data.put("exAttempts", "Versuche:" + noten.examStorage.getExam(index)
							.getAttempts());
				} else {
					data.put("exAttempts", "Versuch:" + noten.examStorage.getExam(index)
							.getAttempts());
				}
				
				data.put("exGrade", noten.examStorage.getExam(index).getGrade());
				examList.add(data);
			}

			catch (Resources.NotFoundException nfe) {
				// ...
			}
		}
		
		 SimpleAdapter notes = new SimpleAdapter(
		            this,
		            examList,
		            R.layout.grade_row_item,
		            new String[] { "exName","exNr", "exAttempts", "exGrade"},
		            new int[] { R.id.examName, R.id.examNr, R.id.examAttempts, R.id.examGrade } );

		        setListAdapter(notes);


	}
}
