package com.example.phoneit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.phoneit.myHeader.*;

public class TransferListAdapter extends ArrayAdapter<TransferItem> {

	private final Activity context;
	private ArrayList<TransferItem> TransferItems;
	private Bitmap picture;

	public TransferListAdapter(Activity context,
			ArrayList<TransferItem> TransferItems) {
		super(context, R.layout.transfer_list_item, TransferItems);
		this.context = context;
		this.TransferItems = TransferItems;
	}

	public int getCount() {
		return TransferItems.size();
	}

	public TransferItem getItem(int position) {
		return TransferItems.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();

		View rowView = inflater
				.inflate(R.layout.transfer_list_item, null, true);

		TransferItem transfer = TransferItems.get(position);

		TextView device_title = (TextView) rowView
				.findViewById(R.id.device_textview);
		TextView device_date = (TextView) rowView
				.findViewById(R.id.date_textview);
		TextView note_title = (TextView) rowView
				.findViewById(R.id.title_textview);
		TextView note_description = (TextView) rowView
				.findViewById(R.id.note_textview);
		ImageView type_image = (ImageView) rowView
				.findViewById(R.id.type_image);
		ImageView shareView = (ImageView) rowView
				.findViewById(R.id.share_image);
		ImageView note_picture = (ImageView) rowView
				.findViewById(R.id.preview_image);

		if (transfer.getType().equals("file")) {
			String[] imageExtensions = new String[] { "jpg", "png", "gif",
					"jpeg" };
			//
			boolean is_image = false, file_exists = false;
			String local_file_path = Environment.getExternalStorageDirectory()
					.toString() + "/PhoneIt/" + transfer.getFilename();
			File file = new File(local_file_path);
			if (file.exists()) {
				file_exists = true;

			}
			if (file_exists) {
				shareView.setOnClickListener(new ShareOnClickListener(
						local_file_path, "file"));
			} else {
				shareView.setOnClickListener(new ShareOnClickListener("",
						"false"));
			}

			// check if image
			for (String extension : imageExtensions) {
				if (transfer.getFilename().toLowerCase().endsWith(extension)) {
					is_image = true;

					if (transfer.getFileBitmap() != null) {

						note_picture.setImageBitmap(transfer.getFileBitmap());
						note_picture.setVisibility(View.VISIBLE);
						note_picture
								.setOnClickListener(new PhotoOnClickListener(
										local_file_path, "photo"));
					}
					break;
				}
			}

			if (!file_exists) {
				TextView filename_view = (TextView) rowView
						.findViewById(R.id.filename_textview);
				filename_view.setText(transfer.getFilename());
				filename_view.setVisibility(View.VISIBLE);
				filename_view
						.setOnClickListener(new FilenameViewOnClickListener(
								transfer.getFilename(), "download"));
			} else if (!is_image) {
				TextView filename_view = (TextView) rowView
						.findViewById(R.id.filename_textview);
				filename_view.setText(transfer.getFilename());
				filename_view.setVisibility(View.VISIBLE);
				filename_view
						.setOnClickListener(new FilenameViewOnClickListener(
								local_file_path, "open"));
			}
		} else if (transfer.getType().equals("address")) {
			
			note_picture.setImageBitmap(transfer.getMapBitmap());
			note_picture.setVisibility(View.VISIBLE);
			note_picture.setOnClickListener(new PhotoOnClickListener(transfer.getNote(), "map"));
			shareView.setOnClickListener(new ShareOnClickListener(transfer
					.getNote(), "address"));
		} else {

			shareView.setOnClickListener(new ShareOnClickListener(transfer
					.getTitle() + " | " + transfer.getNote(), "note"));
		}

		if (transfer.getDirection().equals("From")) {
			device_title.setText(transfer.getDirection() + " "
					+ transfer.getUser() + " to " + transfer.getDeviceName());
		} else {
			device_title.setText("To " + transfer.getUser() + " on "
					+ transfer.getDeviceName());
		}
		device_date.setText(transfer.getDate());
		note_title.setText(transfer.getTitle());
		note_description.setText(transfer.getNote());
		if (transfer.getType().equals("file")
				|| transfer.getType().equals("image")) {
			type_image.setImageDrawable(rowView.getResources().getDrawable(
					R.drawable.file_40));
		}
		if (transfer.getType().equals("note")) {
			type_image.setImageDrawable(rowView.getResources().getDrawable(
					R.drawable.note_40));
		}
		if (transfer.getType().equals("address")) {
			type_image.setImageDrawable(rowView.getResources().getDrawable(
					R.drawable.address_40));
		}

		return rowView;
	}
}