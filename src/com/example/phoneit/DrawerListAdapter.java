package com.example.phoneit;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class DrawerListAdapter extends ArrayAdapter<DrawerItem> {

	private Context context;
	private ArrayList<DrawerItem> DrawerItems;

	public DrawerListAdapter(Context context,
			ArrayList<DrawerItem> navDrawerItems) {
		super(context, R.layout.drawer_list_item, navDrawerItems);
		this.context = context;
		this.DrawerItems = navDrawerItems;
	}

	public int getCount() {
		return DrawerItems.size();
	}

	public DrawerItem getItem(int position) {
		return DrawerItems.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater mInflater = (LayoutInflater) context
					.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			convertView = mInflater.inflate(R.layout.drawer_list_item, null);
		}

		TextView device_name = (TextView) convertView
				.findViewById(R.id.device_title_drawer);
		TextView device_owner = (TextView) convertView
				.findViewById(R.id.device_owner_drawer);

		device_name.setText(DrawerItems.get(position).getDeviceName());
		device_owner.setText(DrawerItems.get(position).getDeviceOwner());

		return convertView;
	}

}