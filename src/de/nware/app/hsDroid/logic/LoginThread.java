package de.nware.app.hsDroid.logic;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import de.nware.app.hsDroid.data.StaticSessionData;

/**
 * Login Thread
 * 
 * @author Oliver Eichner
 * 
 */
public class LoginThread extends Thread {

	private static final String TAG = "LoginThread";
	private final static String UPDATE_URL = "https://qis2.hs-karlsruhe.de/qisserver/rds?state=user&type=1&category=auth.login&startpage=portal.vm&breadCrumbSource=portal";

	public final static byte STATE_NOT_STARTED = 0;
	public final static byte STATE_RUNNING = 1;
	public final static byte STATE_DONE = 2;
	public final static byte STATE_ERROR = 3;

	private final static int HANLDER_MSG_LOGIN = 1;
	private final static byte HANLDER_MSG_LOGOUT = 2;
	private final static byte HANLDER_MSG_KILL = 0;

	private byte mThreadStatus = STATE_NOT_STARTED;

	private boolean mStoppingThread;

	public final static int MESSAGE_COMPLETE = 0;
	public final static int MESSAGE_ERROR = 1;
	public final static int MESSAGE_PROGRESS_CONNECT = 2;
	public final static int MESSAGE_PROGRESS_PARSE = 3;
	public final static int MESSAGE_PROGRESS_COOKIE = 4;

	public final static String ERROR_MSG_LOGIN_FAILED = "loginFailed";
	public final static String ERROR_MSG_SITE_MAINTENANCE = "siteInMaintenance";
	public final static String ERROR_MSG_COOKIE_MISSING = "noCookie";

	private String password;
	private String username;

	public Handler mParentHandler;
	public Handler mThreadHandler;

	/**
	 * Login into qis2 server and save cookie and asiKey
	 * 
	 * @param nHandler
	 * @param nUsername
	 *            {@link String} Username
	 * @param nPassword
	 *            {@link String} Password
	 */
	public LoginThread(Handler nParentHandler, String nUsername, String nPassword) {
		this.mParentHandler = nParentHandler;
		this.username = nUsername;
		this.password = nPassword;
	}

	@Override
	public void run() {
		// flags setzen
		mStoppingThread = false;
		mThreadStatus = STATE_RUNNING;

		// prepare Looper
		Looper.prepare();

		mThreadHandler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HANLDER_MSG_LOGIN:
					try {
						doLogin();

					} catch (Exception e) {
						mThreadStatus = STATE_ERROR;
						Message oMessage = mParentHandler.obtainMessage();
						Bundle oBundle = new Bundle();
						String strMessage = e.getMessage();
						oBundle.putString("Message", strMessage);
						oMessage.setData(oBundle);
						oMessage.what = MESSAGE_ERROR;
						mParentHandler.sendMessage(oMessage);
					} catch (Throwable e) {
						Log.e("Login Handler", e.getMessage());
						e.printStackTrace();
					}
					break;

				case HANLDER_MSG_LOGOUT:
					try {
						doLogout();

					} catch (Exception e) {
						mThreadStatus = STATE_ERROR;
						Message oMessage = mParentHandler.obtainMessage();
						Bundle oBundle = new Bundle();
						String strMessage = e.getMessage();
						oBundle.putString("Message", strMessage);
						oMessage.setData(oBundle);
						oMessage.what = MESSAGE_ERROR;
						mParentHandler.sendMessage(oMessage);
					} catch (Throwable e) {
						Log.e("Login Handler", e.getMessage());
						e.printStackTrace();
					}
					break;
				case HANLDER_MSG_KILL:
					getLooper().quit();
					break;
				default:
					break;
				}
			}
		};

		Looper.loop();
	}

	public void stopThread() {
		// Looper.myLooper().quit();

		this.mStoppingThread = true;
		mThreadHandler.getLooper().quit();
	}

	public byte getStatus() {
		return this.mThreadStatus;
	}

	public boolean login() {
		// warten bis threadHandler bereit ist
		while (mThreadHandler == null) {
			// int count = 0;
			// FIXME geht erst im zweiten Durchlauf der Schleife????
			// Log.e("login", "handler empty: " + count);
			try {
				sleep(1);
				// count += 1;
			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}

		}
		return this.mThreadHandler.sendEmptyMessage(HANLDER_MSG_LOGIN);
	}

	public boolean logout() {
		return this.mThreadHandler.sendEmptyMessage(HANLDER_MSG_LOGOUT);
	}

	public boolean kill() {
		return this.mThreadHandler.sendEmptyMessage(HANLDER_MSG_KILL);
	}

	private void doLogout() throws Exception {
		// TODO!! abmelden implementieren
		// beim System abmelden
	}

	/**
	 * Login für Online Service2.
	 * 
	 * @throws Exception
	 */
	private void doLogin() throws Exception {
		DefaultHttpClient client = new DefaultHttpClient();

		HttpResponse response;
		HttpEntity entity;

		// progressHandle.sendMessage(progressHandle.obtainMessage(1));

		Message connectMessage = mParentHandler.obtainMessage();
		connectMessage.what = MESSAGE_PROGRESS_CONNECT;
		mParentHandler.sendMessage(connectMessage);
		// Post Daten zusammen bauen
		HttpPost post = new HttpPost(UPDATE_URL);
		List<NameValuePair> postData = new ArrayList<NameValuePair>();

		postData.add(new BasicNameValuePair("asdf", this.username));
		postData.add(new BasicNameValuePair("fdsa", this.password));
		postData.add(new BasicNameValuePair("submit", "Anmelden"));

		// header bauen
		post.setHeader("Content-Type", "application/x-www-form-urlencoded");
		post.setEntity(new UrlEncodedFormEntity(postData, HTTP.UTF_8));

		// http Anfrage starten
		response = client.execute(post);

		Message parseMessage = mParentHandler.obtainMessage();
		parseMessage.what = MESSAGE_PROGRESS_PARSE;

		// Send Message to mainThread
		mParentHandler.sendMessage(parseMessage);

		entity = response.getEntity();

		InputStream is = entity.getContent();

		BufferedReader rd = new BufferedReader(new InputStreamReader(is), 4096);

		String line;
		int count = 0;
		// response auswerten
		while ((line = rd.readLine()) != null) {

			// möglichkeit den thread hier zu stoppen... sinnvoll?
			if (mStoppingThread) {
				break;
			}
			// TODO check login success
			loginStringTest(line, count);

			// session id holen
			if (line.contains("asi=")) {
				// wenn eine session id gefunden wird, kann man davon
				// ausgehen, dass man angemeldet is ;)
				int begin = line.indexOf("asi=") + 4;
				StaticSessionData.asiKey = line.substring(begin, begin + 20);
				break;
			}
			count++;
		}
		rd.close();
		is.close();
		Message cookieMessage = mParentHandler.obtainMessage();
		cookieMessage.what = MESSAGE_PROGRESS_COOKIE;
		mParentHandler.sendMessage(cookieMessage);
		if (client.getCookieStore().getCookies().size() != 0) {
			// Speichere Login zeit
			StaticSessionData.cookieMillis = System.currentTimeMillis();

			StaticSessionData.cookies = client.getCookieStore().getCookies();
			// for (int i = 0; i < StaticSessionData.cookies.size(); i++) {
			// if (StaticSessionData.cookies.get(i) == null) {
			// Log.e("login", "cookie:" + i + "empty");
			// }
			//
			// Log.d("login: " + i + " domain", "" +
			// StaticSessionData.cookies.get(i).getDomain());
			// Log.d("login: " + i + " name", "" +
			// StaticSessionData.cookies.get(i).getName());
			// Log.d("login: " + i + " path", "" +
			// StaticSessionData.cookies.get(i).getPath());
			// Log.d("login: " + i + " value", "" +
			// StaticSessionData.cookies.get(i).getValue());
			// Log.d("login: " + i + " version", "" +
			// StaticSessionData.cookies.get(i).getVersion());
			// Log.d("login: " + i + " secure", "" +
			// (StaticSessionData.cookies.get(i).isSecure() ? "yes" : "no"));
			// Log.d("login: " + i + " expired", ""
			// + (StaticSessionData.cookies.get(i).isExpired(new Date()) ? "yes"
			// : "no"));
			// Log.d("login: " + i + " persistent", ""
			// + (StaticSessionData.cookies.get(i).isPersistent() ? "yes" :
			// "no"));
			// Log.d("login: " + i + " expire Date", "" +
			// StaticSessionData.cookies.get(i).getExpiryDate());
			//
			// }
			// cookies darf nicht leer sein
		} else {
			throw new HSLoginException(ERROR_MSG_COOKIE_MISSING);
		}

		if (entity != null)
			entity.consumeContent();

		mThreadStatus = STATE_DONE;
		Message oMessage = mParentHandler.obtainMessage();
		oMessage.what = MESSAGE_COMPLETE;
		mParentHandler.sendMessage(oMessage);
	}

	/**
	 * Quick'n'Dirty Test for Login response Lines
	 * 
	 * @param line
	 *            {@link String} with a line from the login response
	 * @param count
	 *            {@link Integer} line count
	 * @throws HSLoginException
	 */
	private void loginStringTest(String line, int count) throws HSLoginException {
		// TODO geht bestimmt schöner, aber funktioniert ;)

		if (count < 10) { // sollte innerhalb der ersten 10 Zeilen stehen..
			if (line.contains("System nicht verf")) {// blöööde Umlaute...
				// getContextClassLoader().get;
				throw new HSLoginException(ERROR_MSG_SITE_MAINTENANCE);
			}
		}
		// A</u>nmelden
		// if (line.contains("Anmeldung fehlgeschlagen")) {
		if (count > 30 && count < 50) {
			// wenn am anfang der link Anmelden steht, hat es wohl mit der
			// anmeldung nicht geklappt
			if (line.contains("A</u>nmelden")) {
				// throw new
				// HSLoginException(this.getString(R.string.error_login_failed));
				throw new HSLoginException(ERROR_MSG_LOGIN_FAILED);
			}
		}
	}
}
