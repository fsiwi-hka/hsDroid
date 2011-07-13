package de.nware.app.hsDroid.provider;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.nware.app.hsDroid.data.ExamInfo;

// TODO: Auto-generated Javadoc
/**
 * Der/Die/Das Class ExamInfoParser.
 */
public class ExamInfoParser extends DefaultHandler {

	/** Der/Die/Das fetch. */
	Boolean fetch = false;

	/** Der/Die/Das wait for td. */
	Boolean waitForTd = false;

	/** Der/Die/Das tr count. */
	int trCount = 0;

	/** Der/Die/Das td count. */
	int tdCount = 0; // 0-7

	/** Der/Die/Das sehr gut amount. */
	private String sehrGutAmount;

	/** Der/Die/Das gut amount. */
	private String gutAmount;

	/** Der/Die/Das befriedigend amount. */
	private String befriedigendAmount;

	/** Der/Die/Das ausreichend amount. */
	private String ausreichendAmount;

	/** Der/Die/Das nicht ausreichend amount. */
	private String nichtAusreichendAmount;

	/** Der/Die/Das average. */
	private String average;

	/** Der/Die/Das exam info. */
	private ExamInfo examInfo;

	/**
	 * Gibt exam infos.
	 * 
	 * @return Der/Die/das exam infos
	 */
	public ExamInfo getExamInfos() {
		return examInfo;
	}

	/**
	 * Reset exam info vars.
	 */
	private void resetExamInfoVars() {
		this.sehrGutAmount = "";
		this.gutAmount = "";
		this.befriedigendAmount = "";
		this.ausreichendAmount = "";
		this.befriedigendAmount = "";
		this.nichtAusreichendAmount = "";
		this.average = "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String,
	 * java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String,
	 * java.lang.String, java.lang.String)
	 */
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char ch[], int start, int length) {
		try {
			super.characters(ch, start, length);
		} catch (SAXException e) {
			e.printStackTrace();
			throw new RuntimeException("error reading chars in parser. " + e.getMessage());
		}
		String text = new String(ch, start, length);
		// FIXME test
		text = text.trim();
		if (fetch) {
			switch (tdCount) {
			case 0:
				// Log.d("first:" + trCount + ":", text);
				switch (trCount) {
				case 10:
					average += text;
					break;

				default:
					break;
				}

				break;
			case 1:
				// XXX +=text.. wegen zeilenumbrüchen im html code..
				// Log.d("second:" + trCount + ":", text);
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
				// Log.d("parser default", text + " element:" + tdCount);
				break;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
	 */
	public void startDocument() throws SAXException {
		super.startDocument();
		resetExamInfoVars();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	 */
	public void endDocument() throws SAXException {
		super.endDocument();
		examInfo = new ExamInfo(sehrGutAmount, gutAmount, befriedigendAmount, ausreichendAmount,
				nichtAusreichendAmount, average);
		// examInfo.setSehrGutAmount(sehrGutAmount);
		// examInfo.setGutAmount(gutAmount);
		// examInfo.setBefriedigendAmount(befriedigendAmount);
		// examInfo.setAusreichendAmount(ausreichendAmount);
		// examInfo.setNichtAusreichendAmount(nichtAusreichendAmount);
		// examInfo.setAverage(average);
	}
}
