package de.nware.app.hsDroid.logic;

public class HSLoginException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HSLoginException() {

	}

	public HSLoginException(int i) {
		this(getString(i));
	}

	final static public String getString(int i) {
		switch (i) {
		case 0:

			break;
		case 1:
		default:
			break;
		}
		return "---..--";

	}

	public HSLoginException(String s) {
		super(s);

	}
}