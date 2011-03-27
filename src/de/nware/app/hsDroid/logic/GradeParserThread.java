/**
 * 
 */
package de.nware.app.hsDroid.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.CookieSpecBase;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import de.nware.app.hsDroid.data.Exam;
import de.nware.app.hsDroid.data.StaticSessionData;

/**
 * @author Oliver Eichner
 * 
 */
public class GradeParserThread extends Thread {
	private ArrayList<Exam> examsTest;

	public final static byte STATE_NOT_STARTED = 0;
	public final static byte STATE_RUNNING = 1;
	public final static byte STATE_DONE = 2;

	private byte mThreadStatus = STATE_NOT_STARTED;

	private boolean mStoppingThread;

	public final static int MESSAGE_COMPLETE = 0;
	public final static int MESSAGE_ERROR = 1;
	public final static int MESSAGE_PROGRESS_FETCH = 2;
	public final static int MESSAGE_PROGRESS_PARSE = 3;
	public final static int MESSAGE_PROGRESS_CLEANUP = 4;

	public Handler handlerOfCaller;

	/**
	 * 
	 */
	public GradeParserThread(Handler nHandler) {
		this.handlerOfCaller = nHandler;
		this.examsTest = new ArrayList<Exam>();

	}

	public ArrayList<Exam> getExamsList() {
		return this.examsTest;
	}

	@Override
	public void run() {
		mThreadStatus = STATE_RUNNING;
		// FIXME asi key könnte man auch mit get in den header einbauen bzw alle
		// gets...

		// FIXME link zuu statisch.. geht warscheinlich nur für bachelor..testen

		// progressHandler.sendMessage(progressHandler.obtainMessage(1));
		final String notenSpiegelURL = "https://qis2.hs-karlsruhe.de/qisserver/rds?state=notenspiegelStudent&next=list.vm&nextdir=qispos/notenspiegel/student&createInfos=Y&struct=auswahlBaum&nodeID=auswahlBaum%7Cabschluss%3Aabschl%3D58%2Cstgnr%3D1&expand=1&asi="
				+ StaticSessionData.asiKey + "#auswahlBaum%7Cabschluss%3Aabschl%3D58%2Cstgnr%3D1";

		Message fetchMessage = handlerOfCaller.obtainMessage();
		fetchMessage.what = MESSAGE_PROGRESS_FETCH;
		handlerOfCaller.sendMessage(fetchMessage);
		HttpResponse response;
		HttpEntity entity;
		DefaultHttpClient client = new DefaultHttpClient();

		try {

			HttpPost httpPost = new HttpPost(notenSpiegelURL);
			CookieSpecBase cookieSpecBase = new BrowserCompatSpec();

			List<Header> cookieHeader = cookieSpecBase.formatCookies(StaticSessionData.cookies);
			httpPost.setHeader(cookieHeader.get(0));

			response = client.execute(httpPost);
			entity = response.getEntity();

			Message cleanUpMessage = handlerOfCaller.obtainMessage();
			cleanUpMessage.what = MESSAGE_PROGRESS_CLEANUP;
			handlerOfCaller.sendMessage(cleanUpMessage);
			InputStream is = entity.getContent();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is), 4096);
			String line;
			Boolean record = false;
			StringBuilder sb = new StringBuilder();
			while ((line = rd.readLine()) != null) {
				// möglichkeit den thread hier zu stoppen... sinnvoll?
				if (mStoppingThread) {
					break;
				}

				if (!record && line.contains("<table border=\"0\">")) {
					record = true;
				}
				if (record && line.contains("</table>")) {
					line = line.replaceAll("&nbsp;", "");
					line.trim();
					sb.append(line);
					// System.out.println("last line: " + line);
					record = false;
					break;
				}
				if (record) {
					// alle nicht anzeigbaren zeichen entfernen (\n,\t,\s...)
					line = line.trim();

					// alle html leerzeichen müssen raus, da der xml reader nix
					// mit anfangen kann
					line = line.replaceAll("&nbsp;", "");

					// da die <img ..> tags nicht xml like "well formed" sind,
					// muss man sie ein bissel anpassen ;)
					if (line.contains("<img")) {
						// Log.d("grade parser", line);
						line = line.substring(0, line.indexOf(">") + 1) + "</a>";
					}
					sb.append(line);
					// System.out.println("line: " + line);
				}
			}
			if (entity != null)
				entity.consumeContent();
			is.close();
			String htmlContentString = sb.toString();

			rd.close();
			// progressHandler.sendMessage(progressHandler.obtainMessage(3));
			Message parseMessage = handlerOfCaller.obtainMessage();
			parseMessage.what = MESSAGE_PROGRESS_PARSE;
			handlerOfCaller.sendMessage(parseMessage);
			read(htmlContentString);
			mThreadStatus = STATE_DONE;
			Message oMessage = handlerOfCaller.obtainMessage();
			oMessage.what = MESSAGE_COMPLETE;
			handlerOfCaller.sendMessage(oMessage);

		} catch (ClientProtocolException e) {
			Log.e("Notenspiegel::client exception:", e.getMessage());
			e.printStackTrace();
			Message oMessage = handlerOfCaller.obtainMessage();
			Bundle oBundle = new Bundle();
			String strMessage = e.getMessage();
			oBundle.putString("Message", strMessage);
			oMessage.setData(oBundle);
			oMessage.what = MESSAGE_ERROR;
			handlerOfCaller.sendMessage(oMessage);
		} catch (IOException e) {
			Log.e("Notenspiegel::io exception:", e.getMessage());
			e.printStackTrace();
			Message oMessage = handlerOfCaller.obtainMessage();
			Bundle oBundle = new Bundle();
			String strMessage = e.getMessage();
			oBundle.putString("Message", strMessage);
			oMessage.setData(oBundle);
			oMessage.what = MESSAGE_ERROR;
			handlerOfCaller.sendMessage(oMessage);
		}

	}

	public void stopThread() {
		this.mStoppingThread = true;
	}

	public byte getStatus() {
		return this.mThreadStatus;
	}

	private void read(String test) {
		SAXParser sp;
		try {
			sp = SAXParserFactory.newInstance().newSAXParser();
			XMLReader xr = sp.getXMLReader();
			try {
				xr.setFeature("http://xml.org/sax/features/validation", true);
			} catch (SAXException e) {
				System.err.println("Cannot activate validation.");
			}

			gpContentHandler uch = new gpContentHandler();
			xr.setContentHandler(uch);

			xr.parse(new InputSource(new StringReader(test)));
		} catch (ParserConfigurationException e) {
			Log.e("read:ParserConfException:", e.getMessage());
			e.printStackTrace();
		} catch (SAXException e) {
			Log.e("read:SAXException:", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("read:IOException:", e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * SAX2 event Handler for "Notenspiegel" html page
	 * 
	 * @author Oliver Eichner
	 * 
	 */
	private class gpContentHandler extends DefaultHandler {

		Boolean fetch = false;
		Boolean waitForTd = false;
		int elementCount = 0; // 0-7
		private String examNr;
		private String examName;
		private String semester;
		private String examDate;
		private String grade;
		private boolean passed;
		private String notation;
		private int attempts;
		private String infoLink;

		private void resetLectureVars() {
			this.examNr = "";
			this.examName = "";
			this.semester = "";
			this.examDate = "";
			this.grade = "";
			this.passed = false;
			this.notation = "";
			this.attempts = 0;
			this.infoLink = "";
		}

		@Override
		public void startElement(String n, String l, String q, Attributes a) throws SAXException {
			super.startElement(n, l, q, a);
			// Log.d("hska saxparser start l:", l);
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
				this.infoLink = a.getValue("href"); // kann leer sein!!
			}

		}

		@Override
		public void endElement(String n, String l, String q) throws SAXException {
			super.endElement(n, l, q);
			if (l == "tr" && fetch == true) {

				examsTest.add(new Exam(examNr, examName, semester, examDate, grade, passed, notation, attempts,
						infoLink));

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
				// XXX string auslagern
			}
			String text = new String(ch, start, length);

			text = text.trim();
			if (fetch) {
				switch (elementCount) {
				case 0:
					// Log.d("PruefNr:", text);
					examNr += text;
					// System.out.println("pnr  ["+examNr+"]");

					break;
				case 1:
					// Log.d("PruefName:", text);
					examName += text;
					// System.out.println("ptxt  ["+examName+"]");

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
					if (text.equals("bestanden")) {
						passed = true;
					}
					break;
				case 6:
					// Log.d("Vermerk:", text);
					notation += text;
					break;
				case 7:
					// Log.d("Versuch:", text);
					attempts = Integer.valueOf(text);
					break;

				default:
					Log.d("parser default", text + " element:" + elementCount);
					break;
				}
			}

		}

		public void startDocument() throws SAXException {
			super.startDocument();
			examsTest = new ArrayList<Exam>();
			resetLectureVars();
		}

		public void endDocument() throws SAXException {
			super.endDocument();
			// array umdrehen
			Collections.reverse(examsTest);
		}
	}
}
