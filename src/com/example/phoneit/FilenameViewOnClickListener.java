package com.example.phoneit;

import java.io.File;
import java.io.FileNotFoundException;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class FilenameViewOnClickListener implements OnClickListener {
	String filename;
	String operation;

	public FilenameViewOnClickListener(String filename, String operation) {
		this.filename = filename;
		this.operation = operation;
	}

	@Override
	public void onClick(View v) {
		if (operation.equals("download")) {
			File folder_path = new File(
					Environment.getExternalStorageDirectory() + "/PhoneIt");

			if (!folder_path.exists()) {
				folder_path.mkdirs();
			}
			DownloadManager mgr = (DownloadManager) v.getContext()
					.getSystemService(Context.DOWNLOAD_SERVICE);
			Uri downloadUri = Uri.parse(myHeader.SERVER_URL + "uploads/"
					+ filename);
			DownloadManager.Request request = new DownloadManager.Request(
					downloadUri);
			request.setDescription("Downloading " + filename)
					.setDestinationInExternalPublicDir("/PhoneIt", filename);

		} else {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("file://" + filename));
			v.getContext().startActivity(intent);	
		}
		
	}
}