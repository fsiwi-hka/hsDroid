package de.nware.app.hsDroid.ui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BrowserCompatSpec;
import org.apache.http.impl.cookie.CookieSpecBase;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import de.nware.app.hsDroid.R;
import de.nware.app.hsDroid.data.StaticSessionData;
import de.nware.app.hsDroid.provider.onlineService2Data.CertificationsCol;

public class Certifications extends nActivity {
	private static final String TAG = "hsDroid-Certifications";
	private SharedPreferences mPreferences;
	private ListView listView;
	private ProgressDialog mProgressDialog;

	private static final int DIALOG_PROGRESS = 1;

	int contentLength;
	int writtenBytes;
	int contentLengthPercent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.certifications);
		customTitle("Bescheinigungen");

		mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		listView = (ListView) findViewById(R.id.cert_listView);

		final ContentResolver resolver = getContentResolver();
		Cursor cursor = resolver.query(CertificationsCol.CONTENT_URI, null, null, null, null);
		startManagingCursor(cursor);

		final String[] from = new String[] { CertificationsCol.TITLE };
		final int[] to = new int[] { R.id.cert_textView };
		listView.setAdapter(new SimpleCursorAdapter(getApplicationContext(), R.layout.certifications_row_item, cursor,
				from, to));
		findViewById(R.id.certificationsProgress).setVisibility(View.GONE);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.d(TAG, "pos:" + position + " id:" + id + " adapterID:" + listView.getAdapter().getItemId(position));

				String selection = BaseColumns._ID + " LIKE ?";

				final Cursor cur = getContentResolver().query(CertificationsCol.CONTENT_URI, null, selection,
						new String[] { String.valueOf(position) }, null);
				startManagingCursor(cur);
				cur.move(position + 1);
				String idd = cur.getString(cur.getColumnIndexOrThrow(BaseColumns._ID));
				String url = cur.getString(cur.getColumnIndexOrThrow(CertificationsCol.LINK));
				String filename = cur.getString(cur.getColumnIndexOrThrow(CertificationsCol.TITLE));
				Log.d(TAG, "link: " + idd);
				Log.d(TAG, "link: " + url);
				Log.d(TAG, "filename: " + filename);
				doDownload(url, filename);

			}

		});
	}

	private Handler downloadHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				mProgressDialog.setProgress(0);
				// mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				// mProgressDialog.setMessage("Prepare Connection");
				break;
			case 1:
				// mProgressDialog.setMessage("Connect to Server");
				break;
			case 2:
				// mProgressDialog.setMessage("Connection established");
				break;
			case 10:
				// mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

				// mProgressDialog.setMax((int) contentLength);
				contentLengthPercent = contentLength / 100;
				break;
			case 11:
				// mProgressDialog.incrementProgressBy((int) contentLength -
				// writtenBytes);
				// mProgressDialog.setMax((int) contentLength);
				int progress = writtenBytes / contentLengthPercent;
				mProgressDialog.setProgress(progress);
				// Log.d(TAG, "Content written:" + progress + "%");
				break;
			case 12:
				showToast("Download erfolgreich.");
				mProgressDialog.dismiss();
				writtenBytes = 0;
				break;
			case 99:
				mProgressDialog.dismiss();
				showToast("Download fehlgeschlagen");
			default:
				break;
			}

			// tv2.setText("Downloadgröße: " + new
			// Integer(contentLength).toString());
			// tv1.setText("Downloade: " + new
			// Long(numWritten).toString());
		}

	};

	private void doDownload(final String url, String dlFileName) {
		showDialog(DIALOG_PROGRESS);
		mProgressDialog.setTitle("Downloade " + dlFileName);
		final String filename = dlFileName;

		Thread t = new Thread() {
			int fileExistCount = 0;

			public void run() {
				try {
					Looper.prepare();

					// Cursor setzen

					Log.d(TAG, "URI: " + url);
					String downloadPath = mPreferences.getString("downloadPathPref", "/sdcard/download/");
					DownloadFromUrl(url, downloadPath, filename);

				} catch (Exception e) {
					mProgressDialog.dismiss();
					showToast("Ungültige Antwort vom Server");
					// createDialog(GradesList.this.getString(R.string.error),
					// e.getMessage());
					e.printStackTrace();
				}
				Looper.loop();
			}

			public void DownloadFromUrl(String url, String path, String fileName) {

				final File file = checkFilename(path, fileName, "pdf");

				downloadHandler.sendEmptyMessage(0);
				final String USER_AGENT = TAG + "/" + 1;
				final HttpPost httpPost = new HttpPost(url);
				httpPost.addHeader("User-Agent", USER_AGENT);
				CookieSpecBase cookieSpecBase = new BrowserCompatSpec();
				List<Header> cookieHeader = cookieSpecBase.formatCookies(StaticSessionData.cookies);
				httpPost.setHeader(cookieHeader.get(0));

				try {
					downloadHandler.sendEmptyMessage(1);
					HttpClient mHttpClient = new DefaultHttpClient();

					HttpResponse response = mHttpClient.execute(httpPost);

					// Prüfen ob HTTP Antwort ok ist.
					StatusLine status = response.getStatusLine();

					if (status.getStatusCode() != HttpStatus.SC_OK) {
						throw new RuntimeException("Ungültige Antwort vom Server: " + status.toString());
					}
					downloadHandler.sendEmptyMessage(2);

					// Hole Content Stream
					final HttpEntity entity = response.getEntity();

					contentLength = (int) entity.getContentLength();
					Log.d(TAG, "ContentLenght:" + contentLength);
					downloadHandler.sendEmptyMessage(10);
					// FileOutputStream fstream = new
					// FileOutputStream(fileName);

					OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
					InputStream inStream = entity.getContent();

					byte[] buffer = new byte[1024];

					int readBytes;

					while ((readBytes = inStream.read(buffer)) != -1) {

						out.write(buffer, 0, readBytes);

						writtenBytes += readBytes;
						downloadHandler.sendEmptyMessage(11);
					}
					inStream.close();
					out.close();
					entity.consumeContent();
					downloadHandler.sendEmptyMessage(12);
				} catch (MalformedURLException e) {
					Log.d(TAG, "malformedURL");
					downloadHandler.sendEmptyMessage(99);
					e.printStackTrace();
				} catch (IOException e) {
					Log.d(TAG, "IO Exception");
					downloadHandler.sendEmptyMessage(99);
					e.printStackTrace();
				}

			}

			private File checkFilename(String path, String fileName, String extension) {
				File file = new File(path, fileName + "." + extension);
				if (file.exists()) {
					// showToast("Datei existiert bereits.. wird umbenannt");
					return checkFilename(path, filename + "_" + (++fileExistCount), extension);
				} else {
					return file;
				}
			}

		};
		t.start();
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_PROGRESS:
			mProgressDialog = new ProgressDialog(this);
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(false);
			return mProgressDialog;
		default:
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_preferences:
			Intent settingsActivity = new Intent(getBaseContext(), Preferences.class);
			startActivity(settingsActivity);

			return true;
		case R.id.menu_about:
			Log.d("Main menu:", "about");
			new AboutDialog(this);

			return true;
		default:
			Log.d("Main menu:", "default");
			System.out.println("id:" + item.getItemId() + " about: " + R.id.menu_about);
			return super.onOptionsItemSelected(item);
		}

	}
}
