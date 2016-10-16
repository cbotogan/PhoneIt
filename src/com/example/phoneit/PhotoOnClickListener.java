package com.example.phoneit;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class PhotoOnClickListener implements OnClickListener {
	String address;
	String type;

	public PhotoOnClickListener(String address, String type) {
		this.address = address;
		this.type = type;
	}

	@Override
	public void onClick(View v) {
		if (type.equals("photo")) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + address), "image/*");
			v.getContext().startActivity(intent);
		} else {
			Intent intent = new Intent();
			String address_escaped = null;
			try {
				address_escaped = URLEncoder.encode(address, "utf-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			Uri geolocation = Uri.parse("geo:0,0?q=" + address_escaped);
			intent.setAction(Intent.ACTION_VIEW);
			intent.setData(geolocation);
			v.getContext().startActivity(intent);
		}
	}
	
}