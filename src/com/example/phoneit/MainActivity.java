package com.example.phoneit;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.phoneit.R;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import static com.example.phoneit.myHeader.*;

public class MainActivity extends Activity {
	protected String username = "";
	protected final String PHONE_ID = android.os.Build.SERIAL;

	private DrawerLayout drawer_layout;
	private ArrayList<DrawerItem> drawer_items;
	private DrawerListAdapter drawer_adapter;
	private ListView drawer_list;
	private ActionBarDrawerToggle drawer_toggle;
	private DrawerItem selected_device = null;
	private boolean launched_by_notification = false;
	private TransferItem notification_transfer;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SharedPreferences shPref = getSharedPreferences(SH_PREFERENCES,
				MODE_PRIVATE);
		if (shPref.contains("logged_user")) {
			username = shPref.getString("logged_user", "");
			setContentView(R.layout.activity_main);
		} else {
			switchActivity(LoginActivity.class, true);
			return;
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			SystemBarTintManager tintManager;
			tintManager = new SystemBarTintManager(this);
			tintManager.setStatusBarTintEnabled(true);
			int actionBarColor = Color.parseColor("#3366CC");
			tintManager.setStatusBarTintColor(actionBarColor);
		}
		
		Intent intent = getIntent();
		int transfer_id_notif = intent.getIntExtra("transfer_id", -1);
		if (transfer_id_notif != -1) {
			if (savedInstanceState != null){
				savedInstanceState = null;
			}
			launched_by_notification = true;
			notification_transfer = 
					new TransferItem(
							transfer_id_notif, 
							intent.getStringExtra("type"), 
							intent.getStringExtra("title"), 
							intent.getStringExtra("note"), 
							intent.getStringExtra("device_name"), 
							intent.getStringExtra("user"), 
							intent.getStringExtra("direction"), 
							intent.getStringExtra("date"), 
							intent.getStringExtra("filename"));
		}
		if (savedInstanceState == null) {

			createDrawer();
		}
	}

	public void createDrawer() {
		drawer_layout = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer_list = (ListView) findViewById(R.id.list_slidermenu);
		drawer_list.setClipToPadding(false);
		drawer_list.setFitsSystemWindows(true);
		setInsets(this, drawer_list);
		drawer_list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				DrawerItem device = drawer_items.get(position);
				drawer_layout.closeDrawer(drawer_list);
				createFragment(device);
				selected_device = device;
			}

		});

		drawer_items = new ArrayList<DrawerItem>();

		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		drawer_toggle = new ActionBarDrawerToggle(this, drawer_layout,
				R.drawable.ic_navigation_drawer, R.string.app_name,
				R.string.app_name);
		drawer_layout.setDrawerListener(drawer_toggle);

		myPostExec post_exec = new myPostExec() {

			@Override
			public void onPostExecute(String response) {
				JSONObject json_obj;
				JSONArray json_array = null;
				String device_name, device_owner;
				int device_id;
				try {
					json_array = new JSONArray(response);

				} catch (JSONException e) {
					e.printStackTrace();
				}
				for (int i = 0; i < json_array.length(); i++) {
					try {
						json_obj = json_array.getJSONObject(i);
						device_name = json_obj.getString("brand") + " "
								+ json_obj.getString("model");
						device_owner = json_obj.getString("owner");
						device_id = json_obj.getInt("device_id");
						drawer_items.add(new DrawerItem(device_name,
								device_owner, device_id));
					} catch (JSONException e) {
						e.printStackTrace();
					}

				}
				drawer_adapter = new DrawerListAdapter(getApplicationContext(),
						drawer_items);
				drawer_list.setAdapter(drawer_adapter);

				selected_device = drawer_items.get(0);
				// display transfers fragment
				if (launched_by_notification == false) {
					createFragment(selected_device);
				} else {
					getFragmentManager()
					.beginTransaction()
					.add(R.id.container,
							new PlaceholderFragment("item",
									notification_transfer)).commit();
					launched_by_notification = false;
				}
			}
		};
		BasicNameValuePair nvp_username = new BasicNameValuePair("username",
				username);
		new myAsyncTaskPOST(SERVER_URL + "androidListDevices.php", post_exec)
				.execute(nvp_username);

	}

	public void refreshTransfers() {
		if (selected_device != null) {
			createFragment(selected_device);
		}
	}

	public void createFragment(DrawerItem device_info) {
		myPostExec post_exec = new myPostExec() {

			@SuppressWarnings("unchecked")
			@Override
			public void onPostExecute(String response) {
				JSONArray json_array = null;
				JSONObject json_obj;
				String user = null, direction = null, type = null, title = null, note = null, filename = null, date = null, device_name = null;
				int transfer_id = 0;
				boolean show_images = false;

				ArrayList<TransferItem> transfer_items = new ArrayList<TransferItem>();

				try {
					json_array = new JSONArray(response);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				for (int i = 0; i < json_array.length(); i++) {
					try {
						json_obj = json_array.getJSONObject(i);
						transfer_id = json_obj.getInt("transfer_id");
						direction = json_obj.getString("direction");
						type = json_obj.getString("type");
						title = json_obj.getString("title");
						note = json_obj.getString("note");
						user = json_obj.getString("user");
						filename = json_obj.getString("filename");
						date = json_obj.getString("date");
						device_name = json_obj.getString("device_name");
						transfer_items.add(new TransferItem(transfer_id, type,
								title, note, device_name, user, direction,
								date, filename));

					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				new AsyncTask<ArrayList<TransferItem>, Void, ArrayList<TransferItem>>() {
					@Override
					protected ArrayList<TransferItem> doInBackground(
							ArrayList<TransferItem>... params) {
						String address_escaped = null;
						Bitmap picture = null;
						String address_maps_url = null;
						for (TransferItem item : params[0]) {
							if (item.getType().equals("address")) {
								try {
									address_escaped = URLEncoder.encode(
											item.getNote(), "utf-8");
								} catch (UnsupportedEncodingException e) {
									e.printStackTrace();
								}
								address_maps_url = "http://maps.googleapis.com/maps/api/staticmap?center="
										+ address_escaped
										+ "&zoom=16&size=600x300&markers=color:red|label:none|"
										+ address_escaped
										+ "&key=AIzaSyCZ-rY4gjxmyf5RoUzO0cY75yhyLfvzvJk";
								picture = null;
								try {
									picture = BitmapFactory
											.decodeStream((InputStream) new URL(
													address_maps_url)
													.getContent());
								} catch (MalformedURLException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
								item.addMapBitmap(picture);
							}

							if (item.getType().equals("file")) {
								String[] imageExtensions = new String[] {
										"jpg", "png", "gif", "jpeg" };
								// check if image
								boolean is_image = false, file_exists = false;
								for (String extension : imageExtensions) {
									if (item.getFilename().toLowerCase()
											.endsWith(extension)) {
										is_image = true;
										String local_file_path = Environment
												.getExternalStorageDirectory()
												.toString()
												+ "/PhoneIt/"
												+ item.getFilename();
										File file = new File(local_file_path);
										if (file.exists()) {
											file_exists = true;
											picture = BitmapFactory
													.decodeFile(local_file_path);
											item.addFileBitmap(picture);
										}
										break;
									}
								}

							}

						}
						return params[0];
					}

					@Override
					protected void onPostExecute(
							ArrayList<TransferItem> transfers) {
						super.onPostExecute(transfers);
						getFragmentManager()
								.beginTransaction()
								.add(R.id.container,
										new PlaceholderFragment("list",
												transfers)).commit();

					}
				}.execute(transfer_items);

			}
		};
		BasicNameValuePair sender, receiver;
		if (device_info.getDeviceOwner().equals(username)) {
			sender = new BasicNameValuePair("sender", "*");
			receiver = new BasicNameValuePair("receiver",
					device_info.getDeviceOwner());
		} else {
			sender = new BasicNameValuePair("sender", username);
			receiver = new BasicNameValuePair("receiver",
					device_info.getDeviceOwner());
		}
		BasicNameValuePair device_id = new BasicNameValuePair("device_id",
				Integer.toString(device_info.getDeviceId()));
		new myAsyncTaskPOST(SERVER_URL + "androidListTransfers.php", post_exec)
				.execute(sender, receiver, device_id);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onPostCreate(savedInstanceState);
		drawer_toggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// TODO Auto-generated method stub
		super.onConfigurationChanged(newConfig);
		drawer_toggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		if (drawer_toggle.onOptionsItemSelected(item)) {
			return true;
		}

		int id = item.getItemId();
		if (id == R.id.action_refresh) {
			refreshTransfers();
			return true;
		}
		if (id == R.id.action_signout) {
			signOut();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		String fragment_type;
		ArrayList<TransferItem> transfer_items;
		TransferListAdapter transfer_adapter;
		TransferItem current_item;
		DrawerItem current_device;

		public PlaceholderFragment(String fragment_type,
				ArrayList<TransferItem> transfer_items) {
			this.fragment_type = fragment_type;
			this.transfer_items = transfer_items;
		}

		public PlaceholderFragment(String fragment_type,
				TransferItem current_item) {
			this.fragment_type = "item";
			this.current_item = current_item;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView;
			if (fragment_type.compareTo("list") == 0) {
				rootView = inflater.inflate(R.layout.fragment_main, container,
						false);
			} else {
				rootView = inflater.inflate(R.layout.fragment_item, container,
						false);
			}
			return rootView;

		}

		@Override
		public void onViewCreated(View view, Bundle savedInstanceState) {
			super.onViewCreated(view, savedInstanceState);

			if (fragment_type.compareTo("list") == 0) {

				ListView list = (ListView) view
						.findViewById(R.id.transfer_listview);
				list.setClipToPadding(false);
				list.setFitsSystemWindows(true);
				setInsets(getActivity(), list);

				transfer_adapter = new TransferListAdapter(getActivity(),
						transfer_items);
				list.setAdapter(transfer_adapter);

				list.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						TransferItem transfer = transfer_items.get(position);
						PlaceholderFragment item_fragment = new PlaceholderFragment(
								"item", transfer);
						FragmentTransaction transaction = getFragmentManager()
								.beginTransaction();
						transaction.replace(R.id.container, item_fragment);
						transaction.addToBackStack(null);
						transaction.commit();

					}
				});
			} else {
				LinearLayout ly = (LinearLayout) view
						.findViewById(R.id.item_layout);
				ly.setClipToPadding(false);
				ly.setFitsSystemWindows(true);
				setInsets(getActivity(), ly);

				TextView device_title = (TextView) view
						.findViewById(R.id.device_textview_details);
				TextView device_date = (TextView) view
						.findViewById(R.id.date_textview_details);
				TextView note_title = (TextView) view
						.findViewById(R.id.title_textview_details);
				TextView note_description = (TextView) view
						.findViewById(R.id.note_textview_details);
				ImageView type_image = (ImageView) view
						.findViewById(R.id.type_image_details);
				ImageView shareView = (ImageView) view
						.findViewById(R.id.share_image_details);
				ImageView note_picture = (ImageView) view
						.findViewById(R.id.preview_image_details);

				if (current_item.getType().equals("file")) {
					String[] imageExtensions = new String[] { "jpg", "png",
							"gif", "jpeg" };
					boolean is_image = false, file_exists = false;
					String local_file_path = Environment
							.getExternalStorageDirectory().toString()
							+ "/PhoneIt/" + current_item.getFilename();
					File file = new File(local_file_path);
					if (file.exists()) {
						file_exists = true;
					}

					if (file_exists) {
						shareView.setOnClickListener(new ShareOnClickListener(
								local_file_path, "file"));
					} else {
						shareView.setOnClickListener(new ShareOnClickListener(
								"", "false"));
					}

					// check if image
					for (String extension : imageExtensions) {
						if (current_item.getFilename().toLowerCase()
								.endsWith(extension)) {
							is_image = true;

							if (current_item.getFileBitmap() != null) {

								note_picture.setImageBitmap(current_item
										.getFileBitmap());
								note_picture.setVisibility(View.VISIBLE);
								note_picture
										.setOnClickListener(new PhotoOnClickListener(
												local_file_path, "photo"));
							}
							break;
						}
					}
					if (!file_exists) {
						TextView filename_view = (TextView) view
								.findViewById(R.id.filename_textview_details);
						filename_view.setText(current_item.getFilename());
						filename_view.setVisibility(View.VISIBLE);
						filename_view
								.setOnClickListener(new FilenameViewOnClickListener(
										current_item.getFilename(), "download"));
					} else if (!is_image) {
						TextView filename_view = (TextView) view
								.findViewById(R.id.filename_textview_details);
						filename_view.setText(current_item.getFilename());
						filename_view.setVisibility(View.VISIBLE);
						filename_view
								.setOnClickListener(new FilenameViewOnClickListener(
										local_file_path, "open"));
					}
				} else if (current_item.getType().equals("address")) {

					note_picture.setImageBitmap(current_item.getMapBitmap());
					note_picture.setVisibility(View.VISIBLE);
					note_picture.setOnClickListener(new PhotoOnClickListener(
							current_item.getNote(), "map"));
					shareView.setOnClickListener(new ShareOnClickListener(
							current_item.getNote(), "address"));
				} else {

					shareView.setOnClickListener(new ShareOnClickListener(
							current_item.getTitle() + " | "
									+ current_item.getNote(), "note"));
				}

				if (current_item.getDirection().equals("From")) {
					device_title.setText(current_item.getDirection() + " "
							+ current_item.getUser() + " to "
							+ current_item.getDeviceName());
				} else {
					device_title.setText("To " + current_item.getUser()
							+ " on " + current_item.getDeviceName());
				}

				device_title.setText(current_item.getDirection() + " "
						+ current_item.getUser() + " to "
						+ current_item.getDeviceName());
				device_date.setText(current_item.getDate());
				note_title.setText(current_item.getTitle());
				note_description.setText(current_item.getNote());

				if (current_item.getType().equals("file")
						|| current_item.getType().equals("image")) {
					type_image.setImageDrawable(view.getResources()
							.getDrawable(R.drawable.file_40));
				}
				if (current_item.getType().equals("note")) {
					type_image.setImageDrawable(view.getResources()
							.getDrawable(R.drawable.note_40));
				}
				if (current_item.getType().equals("address")) {
					type_image.setImageDrawable(view.getResources()
							.getDrawable(R.drawable.address_40));
				}
			}
		}
	}

	public static void setInsets(Activity context, View view) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT)
			return;
		SystemBarTintManager tintManager = new SystemBarTintManager(context);
		SystemBarTintManager.SystemBarConfig config = tintManager.getConfig();
		view.setPadding(0, config.getPixelInsetTop(true),
				config.getPixelInsetRight(), config.getPixelInsetBottom());
	}

	public void switchActivity(Class<?> destActivity, boolean finishCurrent) {
		Intent intent = new Intent(this, destActivity);
		startActivity(intent);
		if (finishCurrent) {
			finish();
		}
		return;
	}

	public void signOut() {
		SharedPreferences shPref = getSharedPreferences(SH_PREFERENCES,
				MODE_PRIVATE);
		Editor editor = shPref.edit();
		editor.remove("logged_user");
		editor.commit();

		myPostExec post_exec = new myPostExec() {

			@Override
			public void onPostExecute(String response) {
				Log.d("signout_response", response);
			}
		};
		BasicNameValuePair nvp_user = new BasicNameValuePair("username",
				username);
		BasicNameValuePair nvp_device = new BasicNameValuePair("device_id",
				PHONE_ID);
		new myAsyncTaskPOST(SERVER_URL + "androidSignOut.php", post_exec)
				.execute(nvp_user, nvp_device);

		switchActivity(LoginActivity.class, true);
	}
}
