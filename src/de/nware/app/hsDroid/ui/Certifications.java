package de.nware.app.hsDroid.ui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
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

	private final int DIALOG_PROGRESS = 1;
	private final int DIALOG_FILE_EXIST = 2;
	private final int DIALOG_OPEN_FILE_NOT_FOUND = 3;
	private final int DIALOG_SEND_FILE_NOT_FOUND = 4;

	private final int FE_RENAME = 1;
	private final int FE_OVERWRITE = 2;

	private int fileExistCount = 0;
	int contentLength;
	int writtenBytes;
	int contentLengthPercent;

	private String currentCertName = null;
	private File currentFile = null;
	private String currentURL = null;

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

				getFileByPos(position, true, false);

			}

		});
		listView.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
				menu.setHeaderTitle("context menu"); // TODO String
				getMenuInflater().inflate(R.menu.cert_menu, menu);
			}
		});

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);

		// Position im ListView Adapter für den das Menü geöffnet wurde
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
		int position = menuInfo.position;

		// Switch über die MenuItem ID, um die Gewünschte Selektion zu erhalten
		switch (item.getItemId()) {
		case R.id.item_cert_menu_Download:
			getFileByPos(position, true, false);
			return true;
		case R.id.item_cert_menu_del:
			getFileByPos(position, false, false);

			final String[] files1 = getFilesWithName(currentCertName);
			if (files1.length > 1) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Datei wählen");
				builder.setItems(files1, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialogInterface, int pos) {
						new File(mPreferences.getString("downloadPathPref", Environment.DIRECTORY_DOWNLOADS),
								files1[pos]).delete();
						showToast("Datei \"" + files1[pos] + "\" gelöscht");
					}
				});
				AlertDialog alert = builder.create();
				alert.show();

			} else if (files1.length == 1) {
				Log.d(TAG, "Filename [" + files1[0] + "]");
				new File(mPreferences.getString("downloadPathPref", Environment.DIRECTORY_DOWNLOADS), files1[0])
						.delete();
				showToast("Datei \"" + files1[0] + "\" gelöscht");
			} else {
				showToast("Datei nicht gefunden");
			}

			return true;
		case R.id.item_cert_menu_open:

			getFileByPos(position, false, false);

			final String[] files = getFilesWithName(currentCertName);
			if (files.length > 1) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Datei wählen");
				builder.setItems(files, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialogInterface, int pos) {
						openPDF(new File(mPreferences.getString("downloadPathPref", Environment.DIRECTORY_DOWNLOADS),
								files[pos]));
					}
				});
				AlertDialog alert = builder.create();
				alert.show();

			} else if (files.length == 1) {
				Log.d(TAG, "Filename [" + files[0] + "]");
				openPDF(new File(mPreferences.getString("downloadPathPref", Environment.DIRECTORY_DOWNLOADS), files[0]));
			} else {
				showDialog(DIALOG_OPEN_FILE_NOT_FOUND);
			}

			return true;
		case R.id.item_cert_menu_send:
			getFileByPos(position, false, false);

			final String[] filesCouldBeSend = getFilesWithName(currentCertName);
			if (filesCouldBeSend.length > 1) {
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Datei wählen");
				builder.setItems(filesCouldBeSend, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialogInterface, int pos) {
						sendEmailWithAttachment(new File(mPreferences.getString("downloadPathPref",
								Environment.DIRECTORY_DOWNLOADS), filesCouldBeSend[pos]));
					}
				});
				AlertDialog alert = builder.create();
				alert.show();

			} else if (filesCouldBeSend.length == 1) {
				Log.d(TAG, "Filename [" + filesCouldBeSend[0] + "]");
				sendEmailWithAttachment(new File(mPreferences.getString("downloadPathPref",
						Environment.DIRECTORY_DOWNLOADS), filesCouldBeSend[0]));
			} else {
				showDialog(DIALOG_SEND_FILE_NOT_FOUND);
			}
			return true;
		}
		return false;
	}

	private void sendEmailWithAttachment(File file) {
		Intent sendIntent = new Intent(Intent.ACTION_SEND);
		sendIntent.setType("application/pdf");
		sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, currentCertName);
		sendIntent.putExtra(Intent.EXTRA_TEXT, "Anhang: " + currentCertName);

		startActivity(Intent.createChooser(sendIntent, "Select Destination"));

	}

	private String[] getFilesWithName(final String name) {
		// ArrayList<File> files = new ArrayList<File>();
		File dlPath = new File(mPreferences.getString("downloadPathPref", Environment.DIRECTORY_DOWNLOADS));

		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String filename) {
				if (filename.startsWith(name)) {
					return true;
				}
				return false;
			}
		};

		return dlPath.list(filter);
	}

	private void openPDF(File file) {
		if (file.exists()) {
			Uri path = Uri.fromFile(file);
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(path, "application/pdf");
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

			try {
				startActivity(intent);
			} catch (ActivityNotFoundException e) {
				showToast("Keine Anwendung vorhanden um PDF Dateien zu Öffnen");
			}
		} else {
			showToast("Datei \"" + file + "\" nicht gefunden.");
		}
	}

	private Handler downloadHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				mProgressDialog.setProgress(0);
				break;
			case 1:
				// mProgressDialog.setMessage("Connect to Server");
				break;
			case 2:
				// mProgressDialog.setMessage("Connection established");
				break;
			case 10:
				contentLengthPercent = contentLength / 100;
				break;
			case 11:
				int progress = writtenBytes / contentLengthPercent;
				mProgressDialog.setProgress(progress);
				// Log.d(TAG, "Content written:" + progress + "%");
				break;
			case 12:
				showToast("Download erfolgreich.");
				mProgressDialog.dismiss();
				writtenBytes = 0;
				break;
			case 13:
				openPDF(currentFile);
				mProgressDialog.dismiss();
				writtenBytes = 0;
				break;
			case 14:
				sendEmailWithAttachment(currentFile);
				mProgressDialog.dismiss();
				writtenBytes = 0;
				break;
			case 99:
				mProgressDialog.dismiss();
				showToast("Download fehlgeschlagen");
			default:
				break;
			}
		}
	};

	private File renameFile(File file, String nameWithoutExtension) {
		if (file.exists() && file.isFile()) {
			String fileName = file.getName();
			String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length());
			file = new File(file.getParent(), nameWithoutExtension + "_" + (++fileExistCount) + "." + fileExtension);
			return renameFile(file, nameWithoutExtension);
		}
		fileExistCount = 0;
		return file;
	}

	private File getFileByPos(int position, boolean downloadFile, boolean openFile) {
		String selection = BaseColumns._ID + " LIKE ?";

		final Cursor cur = getContentResolver().query(CertificationsCol.CONTENT_URI, null, selection,
				new String[] { String.valueOf(position) }, null);
		startManagingCursor(cur);
		cur.move(position + 1);
		String idd = cur.getString(cur.getColumnIndexOrThrow(BaseColumns._ID));
		currentURL = cur.getString(cur.getColumnIndexOrThrow(CertificationsCol.LINK));
		currentCertName = cur.getString(cur.getColumnIndexOrThrow(CertificationsCol.TITLE));
		Log.d(TAG, "id: " + idd);
		Log.d(TAG, "link: " + currentURL);
		Log.d(TAG, "certname: " + currentCertName);

		String downloadPath = mPreferences.getString("downloadPathPref", Environment.DIRECTORY_DOWNLOADS);
		currentFile = new File(downloadPath, currentCertName + ".pdf");

		if (downloadFile) {
			if (currentFile.exists()) {
				showDialog(DIALOG_FILE_EXIST);
			} else {
				doDownload(currentURL, currentFile, openFile, false);
			}
		}

		return currentFile;
	}

	private synchronized void doDownload(final String url, final File fileToSafeAt, final boolean openFile,
			final boolean sendFile) {
		showDialog(DIALOG_PROGRESS);
		mProgressDialog.setMessage("Download von " + fileToSafeAt.getName());
		mProgressDialog.setTitle("test");

		Thread t = new Thread() {

			public void run() {
				try {
					Looper.prepare();

					// Cursor setzen
					DownloadFromUrl(url, fileToSafeAt);

				} catch (Exception e) {
					mProgressDialog.dismiss();
					showToast("Ungültige Antwort vom Server");
					e.printStackTrace();
				}
				Looper.loop();
			}

			private synchronized void DownloadFromUrl(String url, File file) {

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
					notifyAll();
					if (openFile) {
						downloadHandler.sendEmptyMessage(13);
					} else if (sendFile) {
						downloadHandler.sendEmptyMessage(14);
					} else {
						downloadHandler.sendEmptyMessage(12);
					}
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
		case DIALOG_OPEN_FILE_NOT_FOUND:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Datei nicht gefunden");
			builder.setMessage("Soll die Datei Heruntergeladen werden?");
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					doDownload(currentURL, currentFile, true, false);

				}
			});
			builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();

				}
			});
			AlertDialog alert = builder.create();
			return alert;
		case DIALOG_SEND_FILE_NOT_FOUND:
			AlertDialog.Builder builderSendFileNotFoundDia = new AlertDialog.Builder(this);
			builderSendFileNotFoundDia.setTitle("Datei nicht gefunden");
			builderSendFileNotFoundDia.setMessage("Soll die Datei Heruntergeladen werden?");
			builderSendFileNotFoundDia.setPositiveButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					doDownload(currentURL, currentFile, false, true);

				}
			});
			builderSendFileNotFoundDia.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();

				}
			});
			AlertDialog alertSendFileNotFoundDia = builderSendFileNotFoundDia.create();
			return alertSendFileNotFoundDia;
		case DIALOG_FILE_EXIST:
			AlertDialog.Builder builderFileExistDia = new AlertDialog.Builder(this);
			builderFileExistDia.setTitle("Datei existiert bereits");
			final int OVERWRITE = 0;
			final int RENAME = 1;
			builderFileExistDia.setItems(R.array.ifFileExistArray, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, "select: " + which);
					switch (which) {
					case OVERWRITE:
						doDownload(currentURL, currentFile, false, false);
						break;
					case RENAME:
						doDownload(currentURL, renameFile(currentFile, currentCertName), false, false);
						break;
					default:
						break;
					}
				}
			});

			builderFileExistDia.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();

				}
			});
			AlertDialog fileExistDia = builderFileExistDia.create();
			return fileExistDia;
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
			new AboutDialog(this);

			return true;
		default:
			System.out.println("id:" + item.getItemId() + " about: " + R.id.menu_about);
			return super.onOptionsItemSelected(item);
		}

	}

}
