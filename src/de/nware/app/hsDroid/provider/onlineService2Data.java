package de.nware.app.hsDroid.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class onlineService2Data {
	public static final Uri baseUri = Uri.parse("content://" + onlineService2Provider.AUTHORITY);

	public static final String EXAMS_TABLE_NAME = "exams";
	public static final String EXAM_INFOS_NAME = "examinfo";
	public static final String CERTIFICATIONS_NAME = "downloads";

	public static final String EXAMS_UPDATE_NAME = "examsupd";

	public static final class ExamsCol implements BaseColumns {

		public static final Uri CONTENT_URI = Uri.withAppendedPath(baseUri, EXAMS_TABLE_NAME);
		// public static final Uri CONTENT_URI = Uri.withAppendedPath(baseUri,
		// "exams");
		public static final String _ID = "_id";
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.hsdroid.exams";
		public static final String SEMESTER = "semester";
		public static final String PASSED = "passed";
		public static final String EXAMNAME = "examname";
		public static final String EXAMNR = "examnr";
		public static final String EXAMDATE = "examdate";
		public static final String NOTATION = "notation";
		public static final String ATTEMPTS = "attempts";
		public static final String GRADE = "grade";
		public static final String LINKID = "linkid";
	}

	public static final class ExamInfos implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(baseUri, EXAM_INFOS_NAME);
		public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd.hsdroid.examinfo";
		public static final String SEHRGUT = "sg";
		public static final String GUT = "g";
		public static final String BEFRIEDIGEND = "b";
		public static final String AUSREICHEND = "a";
		public static final String NICHTAUSREICHEND = "na";
		public static final String AVERAGE = "avg";
	}

	public static final class CertificationsCol implements BaseColumns {

		public static final Uri CONTENT_URI = Uri.withAppendedPath(baseUri, CERTIFICATIONS_NAME);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.hsdroid.certifications";
		public static final String TITLE = "title";
		public static final String LINK = "link";
	}

	public static final class ExamsUpdateCol implements BaseColumns {

		public static final Uri CONTENT_URI = Uri.withAppendedPath(baseUri, EXAMS_UPDATE_NAME);
		public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd.hsdroid.examsupdate";
		public static final String AMOUNT = "amount";
		public static final String NEWEXAMS = "newExams";
	}
}
