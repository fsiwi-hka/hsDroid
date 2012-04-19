package de.nware.app.hsDroid.provider;

import android.net.Uri;
import android.provider.BaseColumns;

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
 * 
 * @author Oliver Eichner
 * 
 */
public class onlineService2Data {
	public static final Uri baseUri = Uri.parse("content://"
			+ onlineService2Provider.AUTHORITY);

	public static final String EXAMS_TABLE_NAME = "exams";
	public static final String EXAM_INFOS_NAME = "examinfo";
	public static final String CERTIFICATIONS_NAME = "downloads";

	public static final String EXAMS_UPDATE_NAME = "examsupd";

	public static final class ExamsCol implements BaseColumns {

		public static final Uri CONTENT_URI = Uri.withAppendedPath(baseUri,
				EXAMS_TABLE_NAME);
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
		public static final String ADMITTED = "admitted";
		public static final String ATTEMPTS = "attempts";
		public static final String GRADE = "grade";
		public static final String LINKID = "linkid";
		public static final String STUDIENGANG = "studiengang";
	}

	public static final class ExamInfos implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.withAppendedPath(baseUri,
				EXAM_INFOS_NAME);
		public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd.hsdroid.examinfo";
		public static final String SEHRGUT = "sg";
		public static final String GUT = "g";
		public static final String BEFRIEDIGEND = "b";
		public static final String AUSREICHEND = "a";
		public static final String NICHTAUSREICHEND = "na";
		public static final String ATTENDEES = "att";
		public static final String AVERAGE = "avg";
	}

	public static final class CertificationsCol implements BaseColumns {

		public static final Uri CONTENT_URI = Uri.withAppendedPath(baseUri,
				CERTIFICATIONS_NAME);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.hsdroid.certifications";
		public static final String TITLE = "title";
		public static final String LINK = "link";
	}

	public static final class ExamsUpdateCol implements BaseColumns {

		public static final Uri CONTENT_URI = Uri.withAppendedPath(baseUri,
				EXAMS_UPDATE_NAME);
		public static final String CONTENT_TYPE = "vnd.android.cursor.item/vnd.hsdroid.examsupdate";
		public static final String AMOUNT = "amount";
		public static final String NEWEXAMS = "newExams";
	}
}
