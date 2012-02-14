package de.nware.app.hsDroid.data;

import java.util.List;

import org.apache.http.cookie.Cookie;

import android.util.Log;

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
public class StaticSessionData {
	public static List<Cookie> cookies;
	public static long cookieMillis;

	public static String asiKey;

	public final static int cookieValTimeMin = 29;

	/**
	 * Prüft Cookie auf Gültigkeit
	 * 
	 * @return true wenn Cookie vorhanden/gültig ist
	 */
	public static boolean isCookieValid() {
		if (cookies == null) {
			return false;
		}
		long now = System.currentTimeMillis();
		long nowMin = (now / 60000);
		long cookieMin = (cookieMillis / 60000);
		Log.d("static test:", "millis:" + cookieMillis + " now millis:" + now);
		Log.d("static test:", "min:" + cookieMin + " now min:" + nowMin);
		Log.d("static test:", "diff: " + (nowMin - cookieMin));
		return ((nowMin - cookieMin) < cookieValTimeMin);
	}
}
