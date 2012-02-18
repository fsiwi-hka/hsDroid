package de.nware.app.hsDroid.provider;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class HttpClientFactory {

	private static DefaultHttpClient httpClient;

	public synchronized static DefaultHttpClient getHttpClient(int connectionTimeoutMillis) {
		if (httpClient == null) {
			httpClient = new DefaultHttpClient();
			ClientConnectionManager connectionManager = httpClient.getConnectionManager();
			HttpParams httpParams = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParams, connectionTimeoutMillis);

			httpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams,
					connectionManager.getSchemeRegistry()), httpParams);
		}
		return httpClient;
	}
}