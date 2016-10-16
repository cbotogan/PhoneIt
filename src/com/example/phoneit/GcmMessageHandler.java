package com.example.phoneit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigPictureStyle;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.widget.Toast;
import static com.example.phoneit.myHeader.*;

public class GcmMessageHandler extends IntentService {

	String title, note, type, file_url, file_type, file_name, local_file_path,
			user, date, device_name;
	int not_id;

	String icon_url = SERVER_URL + "icon.png";
	private Handler handler;
	private static NotificationManager notification_manager;

	public GcmMessageHandler() {
		super("GcmMessageHandler");
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		handler = new Handler();
		notification_manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();

		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		title = extras.getString("title");
		note = extras.getString("note");
		type = extras.getString("type");
		file_name = extras.getString("file_name");
		file_url = extras.getString("file_url");
		file_type = extras.getString("file_type");
		user = extras.getString("user");
		date = extras.getString("date");
		device_name = extras.getString("device_name");
		not_id = Integer.parseInt(extras.getString("notif_id"));

		showNotification();

		Log.i("GCM",
				"Received : (" + messageType + ")  "
						+ extras.getString("title"));
		Log.i("GCM",
				"Received : (" + messageType + ")  " + extras.getString("note"));
		Log.i("GCM",
				"Received : (" + messageType + ")  " + extras.getString("type"));
		Log.i("GCM",
				"Received : (" + messageType + ")  "
						+ extras.getString("file_name"));
		Log.i("GCM",
				"Received : (" + messageType + ")  "
						+ extras.getString("file_url"));
		Log.i("GCM", "Received : (" + messageType + ")  " + not_id);

		GcmBroadcastReceiver.completeWakefulIntent(intent);

	}

	public void showNotification() {
		handler.post(new Runnable() {
			public void run() {
				CreateNotification cn = new CreateNotification();
				cn.execute();
			}
		});

	}

	class CreateNotification extends AsyncTask<Void, String, Void> {

		Builder notif_builder;

		public CreateNotification() {
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			if (type.compareTo("file") == 0) {
				notif_builder = setNormNotification("start");
				Notification noti;
				noti = notif_builder.build();
				noti.defaults |= Notification.DEFAULT_LIGHTS;
				noti.defaults |= Notification.DEFAULT_VIBRATE;
				noti.defaults |= Notification.DEFAULT_SOUND;
				notification_manager.notify(not_id, noti);
			}

		}

		@Override
		protected Void doInBackground(Void... params) {

			// if note
			if (type.compareTo("note") == 0) {
				notif_builder = setBigNotification();
			}
			// if address
			else if (type.compareTo("address") == 0) {
				try {
					notif_builder = setBigPictureNotification();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				// if file
			} else {

				boolean download_succes = false;
				try {
					download_succes = DownloadFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
				if (download_succes == false) {
					notif_builder.setContentText("Download failed");
				} else {
					if (file_type.startsWith("image")) {
						try {
							notif_builder = setBigPictureNotification();
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					} else {
						Log.d("test norm", "before");
						notif_builder = setNormNotification("complete");
						Log.d("test norm", "after");
					}
				}
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
			int progress = Integer.parseInt(values[0]);
			notif_builder.setProgress(100, progress, false);
			notif_builder.setContentText("Downloading (" + values[0] + "%)");
			notification_manager.notify(not_id, notif_builder.build());
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			Log.d("postexec builder", notif_builder.toString());
			Notification noti = notif_builder.setLights(0xFF0000FF, 500, 1000).setPriority(Notification.PRIORITY_DEFAULT).build();
			Log.d("postexec noti", noti.toString());
			if (!type.equals("file")) {				
				noti.defaults |= Notification.DEFAULT_VIBRATE;
				noti.defaults |= Notification.DEFAULT_SOUND;
			}
			noti.flags |= Notification.FLAG_SHOW_LIGHTS;
			noti.flags |= Notification.FLAG_AUTO_CANCEL;
			
			notification_manager.notify(not_id, noti);

		}

		boolean DownloadFile() throws IOException {

			if (!isExternalStorageWritable()) {
				return false;
			}
			Log.d("download", "oke");
			URL url = new URL("http://" + file_url);
			URLConnection connection = url.openConnection();
			connection.connect();

			// this will be useful for 0-100% progress bar
			int lenghtOfFile = connection.getContentLength();

			InputStream input = new BufferedInputStream(url.openStream(), 8192);

			File storage_root = Environment.getExternalStorageDirectory();
			File app_folder = new File(storage_root, "PhoneIt");
			if (!app_folder.isDirectory() || !app_folder.exists()) {
				app_folder.mkdir();
			}
			File received_file = new File(app_folder, file_name);
			OutputStream output = new FileOutputStream(received_file);

			byte data[] = new byte[204800];
			long total = 0;
			int count;
			int no = 0;
			while ((count = input.read(data)) != -1) {
				total += count;
				++no;
				Log.d("ciclu", Integer.toString(no));
				// publishing the progress....
				// After this onProgressUpdate will be called
				if (no % 64 == 0) {

					Log.d("publish ciclu", Integer.toString(no));
					publishProgress("" + (int) ((total * 100) / lenghtOfFile));
				}
				// writing data to file
				output.write(data, 0, count);
			}

			Log.d("download file", "complete");

			// flushing output
			output.flush();

			// closing streams
			output.close();
			input.close();

			local_file_path = Environment.getExternalStorageDirectory()
					.toString() + "/PhoneIt/" + file_name;

			return true;

		}

	}

	private NotificationCompat.Builder setNormNotification(String status) {

		Bitmap app_icon = null;
		app_icon = BitmapFactory.decodeResource(getResources(),
				R.drawable.logo_blue);

		/*
		// clicked on notification
		Intent open_item_Intent = new Intent(this, ItemDetailsActivity.class);
		PendingIntent open_item_pendingIntent = PendingIntent.getActivity(this,
				0, open_item_Intent, 0);
		*/
		if (status.compareTo("start") == 0) {
			return new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.logo_white).setLargeIcon(app_icon)
					.setContentTitle(title + "(" + file_name + ")")
					.setContentText("Preparing download");
		}

		Intent open_Intent = new Intent(Intent.ACTION_VIEW);
		open_Intent.setData(Uri.fromFile(new File(local_file_path)));

		PendingIntent open_pendingIntent = PendingIntent.getActivity(this, 0,
				open_Intent, PendingIntent.FLAG_UPDATE_CURRENT);
		Log.d("norm not", "done");
		return new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.logo_white).setLargeIcon(app_icon)
				.setContentTitle(title).setContentIntent(open_pendingIntent)
				.setContentText("Download complete").setProgress(0, 0, false);

	}

	private NotificationCompat.Builder setBigNotification() {

		// Create the style object with BigTextStyle subclass.
		NotificationCompat.BigTextStyle notiStyle = new NotificationCompat.BigTextStyle();
		notiStyle.setBigContentTitle(title);
		notiStyle.setSummaryText("Note received");

		// Creates share action when clicked on Share button for received
		// notes
		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(Intent.EXTRA_TEXT, note);
		PendingIntent share_pendingIntent = PendingIntent.getActivity(this, 0,
				shareIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Add the big text to the style.
		notiStyle.bigText(note);
		// Set notification icon
		Bitmap app_icon = null;
		app_icon = BitmapFactory.decodeResource(getResources(),
				R.drawable.logo_blue);

		// Creates an explicit intent for an ItemActivity to receive when
		// clicked on notification
		Intent open_item_Intent = new Intent(this, MainActivity.class);
		open_item_Intent.putExtra("transfer_id", not_id);
		open_item_Intent.putExtra("date", date);
		open_item_Intent.putExtra("type", type);
		open_item_Intent.putExtra("title", title);
		open_item_Intent.putExtra("note", note);
		open_item_Intent.putExtra("device_name", device_name);
		open_item_Intent.putExtra("user", user);
		open_item_Intent.putExtra("direction", "From");
		open_item_Intent.putExtra("filename", file_name);
		PendingIntent open_item_pendingIntent = PendingIntent.getActivity(this,
				0, open_item_Intent, PendingIntent.FLAG_UPDATE_CURRENT);

		return new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.logo_white)
				.setLargeIcon(app_icon)
				.setContentIntent(open_item_pendingIntent)
				.addAction(R.drawable.ic_action_share, "Share",
						share_pendingIntent).setContentTitle(title)
				.setContentText(note).setStyle(notiStyle);
	}

	private NotificationCompat.Builder setBigPictureNotification()
			throws MalformedURLException, IOException {
		Bitmap app_icon = null, picture = null;
		String address_escaped, address_maps_url;
		// Create the style object with BigTextStyle subclass.
		NotificationCompat.BigPictureStyle notiStyle = new NotificationCompat.BigPictureStyle();
		notiStyle.setBigContentTitle(title);

		// On share intent
		Intent shareIntent = new Intent();
		// On item click intent
		Intent open_item_Intent = new Intent(Intent.ACTION_VIEW);

		if (type.compareTo("file") == 0 && file_type.startsWith("image")) {
			picture = BitmapFactory.decodeFile(local_file_path);
			notiStyle.setSummaryText(file_name);
			open_item_Intent.setDataAndType(
					Uri.parse("file://" + local_file_path), "image/*");

			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.putExtra(Intent.EXTRA_STREAM,
					Uri.parse("file://" + local_file_path));
			shareIntent.setType("image/*");

		}
		if (type.compareTo("address") == 0) {
			notiStyle.setSummaryText(note);
			address_escaped = URLEncoder.encode(note, "utf-8");
			address_maps_url = "http://maps.googleapis.com/maps/api/staticmap?center="
					+ address_escaped
					+ "&zoom=16&size=600x300&markers=color:red|label:none|"
					+ address_escaped
					+ "&key=AIzaSyCZ-rY4gjxmyf5RoUzO0cY75yhyLfvzvJk";
			picture = BitmapFactory.decodeStream((InputStream) new URL(
					address_maps_url).getContent());

			shareIntent.setAction(Intent.ACTION_SEND);
			shareIntent.setType("text/plain");
			shareIntent.putExtra(Intent.EXTRA_TEXT, note);

			Uri geolocation = Uri.parse("geo:0,0?q=" + address_escaped);
			open_item_Intent.setData(geolocation);

		}

		// get image or map picture
		app_icon = BitmapFactory.decodeResource(getResources(),
				R.drawable.logo_blue);

		// set notification icon
		notiStyle.bigPicture(picture);

		PendingIntent share_pendingIntent = PendingIntent.getActivity(this, 0,
				shareIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent open_item_pendingIntent = PendingIntent.getActivity(this,
				0, open_item_Intent, PendingIntent.FLAG_UPDATE_CURRENT);

		return new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.logo_white)
				.setLargeIcon(app_icon)
				.setContentIntent(open_item_pendingIntent)
				.addAction(R.drawable.ic_action_share, "Share",
						share_pendingIntent).setContentTitle(title)
				.setContentText(note).setStyle(notiStyle);

	}

	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		Log.d("external storage state", state);
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}
}