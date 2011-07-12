package de.nware.app.hsDroid.data;

import java.util.List;

import org.apache.http.cookie.Cookie;

import android.util.Log;

public class StaticSessionData {
	public static List<Cookie> cookies;
	public static long cookieMillis;

	public static String asiKey;

	public static boolean isCookieValid() {
		long now = System.currentTimeMillis();
		long nowMin = now / 60000;
		long cookieMin = cookieMillis / 60000;
		Log.d("static test:", "millis:" + cookieMillis + " now millis:" + now);
		Log.d("static test:", "min:" + cookieMin + " now min:" + nowMin);
		Log.d("static test:", "diff: " + (nowMin - cookieMin));
		return ((nowMin - cookieMin) < 30);
	}
}
