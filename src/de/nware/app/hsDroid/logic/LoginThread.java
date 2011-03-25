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
import de.nware.app.hsDroid.data.StaticSessionData;

/**
 * Login Thread
 * 
 * @author Oliver Eichner
 * 
 */
public class LoginThread extends Thread {

	private final static String UPDATE_URL = "https://qis2.hs-karlsruhe.de/qisserver/rds?state=user&type=1&category=auth.login&startpage=portal.vm&breadCrumbSource=portal";

	public final static byte STATE_NOT_STARTED = 0;
	public final static byte STATE_RUNNING = 1;
	public final static byte STATE_DONE = 2;
	public final static byte STATE_ERROR = 3;

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

	public Handler HandlerOfCaller;

	/**
	 * Login into qis2 server and save cookie and asiKey
	 * 
	 * @param nHandler
	 * @param nUsername
	 *            {@link String} Username
	 * @param nPassword
	 *            {@link String} Password
	 */
	public LoginThread(Handler nHandler, String nUsername, String nPassword) {
		this.HandlerOfCaller = nHandler;
		this.username = nUsername;
		this.password = nPassword;
	}

	@Override
	public void run() {
		// flasg setzen
		mStoppingThread = false;
		mThreadStatus = STATE_RUNNING;

		DefaultHttpClient client = new DefaultHttpClient();

		HttpResponse response;
		HttpEntity entity;
		try {
			// progressHandle.sendMessage(progressHandle.obtainMessage(1));
			Looper.prepare();
			Message connectMessage = HandlerOfCaller.obtainMessage();
			connectMessage.what = MESSAGE_PROGRESS_CONNECT;
			HandlerOfCaller.sendMessage(connectMessage);
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

			Message parseMessage = HandlerOfCaller.obtainMessage();
			parseMessage.what = MESSAGE_PROGRESS_PARSE;
			HandlerOfCaller.sendMessage(parseMessage);
			entity = response.getEntity();
			InputStream is = entity.getContent();
			BufferedReader rd = new BufferedReader(new InputStreamReader(is), 4096);
			String line;
			int count = 0;
			// progressHandle.sendMessage(progressHandle.obtainMessage(2));
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
			Message cookieMessage = HandlerOfCaller.obtainMessage();
			cookieMessage.what = MESSAGE_PROGRESS_COOKIE;
			HandlerOfCaller.sendMessage(cookieMessage);
			if (client.getCookieStore().getCookies().size() != 0) {
				StaticSessionData.cookies = client.getCookieStore().getCookies();
				// cookies darf nicht leer sein
			} else {
				throw new HSLoginException(ERROR_MSG_COOKIE_MISSING);
			}

			if (entity != null)
				entity.consumeContent();

			mThreadStatus = STATE_DONE;
			Message oMessage = HandlerOfCaller.obtainMessage();
			oMessage.what = MESSAGE_COMPLETE;
			HandlerOfCaller.sendMessage(oMessage);

		} catch (Exception e) {
			mThreadStatus = STATE_ERROR;
			Message oMessage = HandlerOfCaller.obtainMessage();
			Bundle oBundle = new Bundle();
			String strMessage = e.getMessage();
			oBundle.putString("Message", strMessage);
			oMessage.setData(oBundle);
			oMessage.what = MESSAGE_ERROR;
			HandlerOfCaller.sendMessage(oMessage);
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Looper.loop();
	}

	public void stopThread() {
		this.mStoppingThread = true;
	}

	public byte getStatus() {
		return this.mThreadStatus;
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
