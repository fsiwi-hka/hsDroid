/**
 * 
 */
package de.nware.app.hsDroid.logic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
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
import de.nware.app.hsDroid.data.ExamInfo;
import de.nware.app.hsDroid.data.StaticSessionData;

/**
 * @author Oliver Eichner
 * 
 */
public class ExamInfoParserThread extends Thread {
	// private ArrayList<Exam> examsTest;
	private ExamInfo examInfo;
	// private Exam exam;
	public final static byte THREAD_NOT_STARTED = 0;
	public final static byte THREAD_RUNNING = 1;
	public final static byte THREAD_DONE = 2;

	private byte mThreadStatus = THREAD_NOT_STARTED;

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
	public ExamInfoParserThread(Handler nHandler, Exam exam) {
		// TODO Auto-generated constructor stub

		this.handlerOfCaller = nHandler;
		this.examInfo = new ExamInfo(exam);
		// this.examsTest = new ArrayList<Exam>();

		// getGradesFromWeb();
	}

	public ExamInfo getExamInfo() {
		return this.examInfo;
	}

	@Override
	public void run() {
		mThreadStatus = THREAD_RUNNING;
		// FIXME asi key aus url könnte man auch mit get in den header einbauen.
		// bzw alle
		// gets...
		// progressHandler.sendMessage(progressHandler.obtainMessage(1));

		// final String notenSpiegelURL =
		// "https://qis2.hs-karlsruhe.de/qisserver/rds?state=notenspiegelStudent&next=list.vm&nextdir=qispos/notenspiegel/student&createInfos=Y&struct=auswahlBaum&nodeID=auswahlBaum%7Cabschluss%3Aabschl%3D58%2Cstgnr%3D1&expand=1&asi="
		// + StaticSessionData.asiKey +
		// "#auswahlBaum%7Cabschluss%3Aabschl%3D58%2Cstgnr%3D1";

		Message fetchMessage = handlerOfCaller.obtainMessage();
		fetchMessage.what = MESSAGE_PROGRESS_FETCH;
		handlerOfCaller.sendMessage(fetchMessage);
		HttpResponse response;
		HttpEntity entity;
		DefaultHttpClient client = new DefaultHttpClient();

		try {

			HttpPost httpPost = new HttpPost(examInfo.getExam().getInfoLink());
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

				if (!record && line.contains("<table border=\"0\" align=\"left\"  width=\"60%\">")) {
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
						Log.d("examInfo parser", line);
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
			mThreadStatus = THREAD_DONE;
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
			LoginContentHandler uch = new LoginContentHandler();
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
	private class LoginContentHandler extends DefaultHandler {

		Boolean fetch = false;
		Boolean waitForTd = false;
		int trCount = 0;
		int tdCount = 0; // 0-7
		private String sehrGutAmount;
		private String gutAmount;
		private String befriedigendAmount;
		private String ausreichendAmount;
		private String nichtAusreichendAmount;
		private String average;

		private void resetLectureVars() {
			this.sehrGutAmount = "";
			this.gutAmount = "";
			this.befriedigendAmount = "";
			this.ausreichendAmount = "";
			this.befriedigendAmount = "";
			this.nichtAusreichendAmount = "";
			this.average = "";
		}

		@Override
		public void startElement(String n, String l, String q, Attributes a) throws SAXException {
			super.startElement(n, l, q, a);
			// Log.d("hska saxparser start l:", l);
			if (l == "tr") {
				trCount++;
				waitForTd = true;
			}
			if (waitForTd && l == "th") {
				waitForTd = false;
			}
			if (fetch && l == "td") {
				tdCount++;
			}
			if (waitForTd && l == "td") {
				fetch = true;
				waitForTd = false;
			}

		}

		@Override
		public void endElement(String n, String l, String q) throws SAXException {
			super.endElement(n, l, q);
			if (l == "tr" && fetch == true) {

				// examsTest.add(new Exam(examNr, examName, semester, examDate,
				// grade, passed, notation, attempts,
				// infoLink));

				waitForTd = false;
				fetch = false;
				tdCount = 0;
				// resetLectureVars();

			}

		}

		@Override
		public void characters(char ch[], int start, int length) {
			try {
				super.characters(ch, start, length);
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new RuntimeException("error reading chars in parser. " + e.getMessage());
			}
			String text = new String(ch, start, length);
			// FIXME test
			text = text.trim();
			if (fetch) {
				switch (tdCount) {
				case 0:
					Log.d("first:" + trCount + ":", text);
					switch (trCount) {
					case 10:
						average += text;
						break;

					default:
						break;
					}

					break;
				case 1:
					// FIXME +=text.. wegen zeilenumbrüchen im html code..
					Log.d("second:" + trCount + ":", text);
					switch (trCount) {
					case 4:
						sehrGutAmount += text;
						break;
					case 5:
						gutAmount += text;
						break;
					case 6:
						befriedigendAmount += text;
						break;
					case 7:
						ausreichendAmount += text;
						break;
					case 8:
						nichtAusreichendAmount += text;
						break;
					case 10:
						average += text;
						break;
					default:
						break;
					}

					break;

				default:
					Log.d("parser default", text + " element:" + tdCount);
					break;
				}
			}

		}

		public void startDocument() throws SAXException {
			super.startDocument();
			resetLectureVars();
		}

		public void endDocument() throws SAXException {
			super.endDocument();
			examInfo.setSehrGutAmount(sehrGutAmount);
			examInfo.setGutAmount(gutAmount);
			examInfo.setBefriedigendAmount(befriedigendAmount);
			examInfo.setAusreichendAmount(ausreichendAmount);
			examInfo.setNichtAusreichendAmount(nichtAusreichendAmount);
			examInfo.setAverage(average);
		}
	}
}
