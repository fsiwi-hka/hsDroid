package de.nware.app.hsDroid.provider;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;
import de.nware.app.hsDroid.data.Exam;

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
 * SAX2 event Handler for "Notenspiegel" html page
 * 
 * @author Oliver Eichner
 * 
 */
class ExamParser extends DefaultHandler {
	private static final String TAG = "ExamParser";

	private Boolean fetch = false;
	private Boolean waitForTd = false;
	private int elementCount = 0;

	private String examNr;
	private String examName;
	private String semester;
	private String examDate;
	private String grade;
	private boolean passed;
	private String admitted;
	private String notation;
	private int attempts;
	private int infoID;
	private String studiengang;

	ArrayList<Exam> lecList;

	public ArrayList<Exam> getExams() {
		return lecList;
	}

	private void resetLectureVars() {
		this.examNr = "";
		this.examName = "";
		this.semester = "";
		this.examDate = "";
		this.grade = "";
		this.passed = false;
		this.admitted = "";
		this.notation = "";
		this.attempts = 0;
		this.infoID = 0;
		this.studiengang = "";
	}

	@Override
	public void startElement(String n, String l, String q, Attributes a)
			throws SAXException {
		super.startElement(n, l, q, a);
		// Log.d(TAG, l);
		if (l == "tr") {
			waitForTd = true;
		}
		if (waitForTd && l == "th") {
			waitForTd = false;
		}
		if (fetch && l == "td") {
			elementCount++;
		}
		if (waitForTd && l == "td") {
			fetch = true;
			waitForTd = false;
		}

		if (l == "a") {
			String infoLink = a.getValue("href"); // kann leer sein!!
			// https://qis2.hs-karlsruhe.de/qisserver/rds?state=notenspiegelStudent&amp;next=list.vm&amp;nextdir=qispos/notenspiegel/student&amp;createInfos=Y&amp;struct=abschluss&amp;nodeID=auswahlBaum%7Cabschluss%3Aabschl%3D58%2Cstgnr%3D1%7Cstudiengang%3Astg%3DIB%7CpruefungOnTop%3Alabnr%3D2792169&amp;expand=0&amp;asi=oq.nE$uDmdlbVCRpLPX.

			String searchStringA = "%3Astg%3D";
			int stringLengthA = infoLink.indexOf(searchStringA) + 9;
			studiengang = infoLink.substring(stringLengthA, stringLengthA + 9);
			int stringLengthB = studiengang.indexOf("%7C");
			studiengang = studiengang.substring(0, stringLengthB);
			// Log.d(TAG, "Studiengang: " + studiengang);

			String searchString = "labnr%3D";
			int stringLength = infoLink.indexOf(searchString) + 8;
			infoID = Integer.valueOf(infoLink.substring(stringLength,
					stringLength + 7));
			// Log.d(TAG, "infoLink: " + infoLink);
			// Log.d(TAG, "infoID: " + infoID);
		}

	}

	@Override
	public void endElement(String n, String l, String q) throws SAXException {
		super.endElement(n, l, q);
		if (l == "tr" && fetch == true) {
			// Log.d(TAG, "infoID: " + infoID);
			lecList.add(new Exam(examNr, examName, semester, examDate, grade,
					passed, admitted, notation, attempts, infoID, studiengang));

			waitForTd = false;
			fetch = false;
			elementCount = 0;
			resetLectureVars();

		}

	}

	@Override
	public void characters(char ch[], int start, int length) {
		try {
			super.characters(ch, start, length);
		} catch (SAXException e) {
			e.printStackTrace();
		}
		String text = new String(ch, start, length);

		text = text.trim();
		if (fetch) {
			switch (elementCount) {
			case 0:
				// Log.d("PruefNr:", text);
				examNr += text;
				break;
			case 1:
				// Log.d("PruefName:", text);
				examName += text;
				break;
			case 2:
				// Log.d("Semester:", text);
				semester += text;
				break;
			case 3:
				// Log.d("Datum:", text);
				examDate += text;
				// SimpleDateFormat sdfToDate = new SimpleDateFormat(
				// "dd.MM.yyyy");
				// try {
				// examDate = sdfToDate.parse(text);
				// } catch (ParseException e) {
				// // Log.d("read:: date parser: ", e.getMessage());
				// e.printStackTrace();
				// }
				break;
			case 4:
				// Log.d("Note:", text);
				grade += text;
				break;
			case 5:
				// Log.d("Status:", text);
				// XXX ugly
				if (text.equals("bestanden")) {
					passed = true;
				}
				break;
			case 6:
				// Log.d("Vermerk:", text);
				notation += text;
				break;
			case 7:
				// Log.d("Anerkannt:", text);
				admitted += text;
				break;
			case 8:
				// Log.d("Versuch:", text);
				attempts = Integer.valueOf(text);
				break;

			default:
				Log.d(TAG, text + " element:" + elementCount);
				break;
			}
		}

	}

	public void startDocument() throws SAXException {
		super.startDocument();
		lecList = new ArrayList<Exam>();
		resetLectureVars();
	}

	public void endDocument() throws SAXException {
		super.endDocument();
	}
}