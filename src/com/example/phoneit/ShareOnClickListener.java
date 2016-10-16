package com.example.phoneit;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

public class ShareOnClickListener implements OnClickListener {
	String to_share, type;

	public ShareOnClickListener(String to_share, String type) {
		this.to_share = to_share;
		this.type = type;
	}

	@Override
	public void onClick(View v) {
		if (type.equals("false")) {
			Toast.makeText(v.getContext(), "File not found!",
					Toast.LENGTH_SHORT);
			return;
		}
		if (type.equals("file")) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.putExtra(Intent.EXTRA_STREAM,
					Uri.parse("file://" + to_share));
			intent.setType("*/*");
			v.getContext().startActivity(
					Intent.createChooser(intent, "Share via"));
		} else {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, to_share);
			v.getContext().startActivity(intent);
		}
		 
	}
}