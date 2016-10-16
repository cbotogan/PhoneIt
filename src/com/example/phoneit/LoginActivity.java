package com.example.phoneit;

import org.apache.http.message.BasicNameValuePair;

import com.example.phoneit.R;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import static com.example.phoneit.myHeader.*;

public class LoginActivity extends Activity {

	protected final String PHONE_ID = android.os.Build.SERIAL;
	protected final String PHONE_BRAND = android.os.Build.BRAND;
	protected final String PHONE_MODEL = android.os.Build.MODEL;
	protected EditText et_input_username;
	protected EditText et_input_password;
	protected Button button_login;
	protected TextView tv_login;
	protected String input_username;
	protected String input_password;
	protected GoogleCloudMessaging gcm;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		et_input_username = (EditText) findViewById(R.id.input_username);
		et_input_password = (EditText) findViewById(R.id.input_password);
		button_login = (Button) findViewById(R.id.button_login);
		tv_login = (TextView) findViewById(R.id.textView_login);

		button_login.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View view) {
				input_username = et_input_username.getText().toString();
				input_password = et_input_password.getText().toString();

				if (input_username.isEmpty()) {
					Toast.makeText(view.getContext(), "Enter username",
							Toast.LENGTH_SHORT).show();
					return;
				}

				if (input_password.isEmpty()) {
					Toast.makeText(view.getContext(), "Enter password",
							Toast.LENGTH_SHORT).show();
					return;
				}
				Log.d("serial", PHONE_ID);
				myPostExec post_exec = new myPostExec() {
					@Override
					public void onPostExecute(String response) {
						Log.d("login_response", response);
						if (!response.isEmpty()) {
							if (response.compareTo("ok") == 0) {
								saveSharedPreferences(input_username);
								switchActivity(view, MainActivity.class, true);
							} else if (response.compareTo("confirmation") == 0) {
								Toast.makeText(view.getContext(),
										"Account not confirmed via e-mail",
										Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(view.getContext(),
										"Wrong username/password",
										Toast.LENGTH_SHORT).show();
							}
						}
					}
				};
				BasicNameValuePair nvp_user = new BasicNameValuePair(
						"username", input_username);
				BasicNameValuePair nvp_pass = new BasicNameValuePair(
						"password", input_password);
				BasicNameValuePair nvp_unique_id = new BasicNameValuePair(
						"unique_id", PHONE_ID);
				BasicNameValuePair nvp_brand = new BasicNameValuePair("brand",
						PHONE_BRAND);
				BasicNameValuePair nvp_model = new BasicNameValuePair("model",
						PHONE_MODEL);
				GoogleCloudMessaging gcm = GoogleCloudMessaging
						.getInstance(getApplicationContext());
				new myAsyncTaskPOST(SERVER_URL + "androidLogin.php", post_exec,
						gcm).execute(nvp_user, nvp_pass, nvp_unique_id,
						nvp_brand, nvp_model);

			}
		});

		tv_login.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				switchActivity(view, RegisterActivity.class, false);
			}
		});

	}

	protected void saveSharedPreferences(String username) {
		SharedPreferences shPref = getSharedPreferences(SH_PREFERENCES,
				MODE_PRIVATE);
		Editor editor = shPref.edit();
		editor.putString("logged_user", username);
		editor.commit();
	}

	public void switchActivity(View view, Class<?> destActivity,
			boolean finishCurrent) {
		Intent intent = new Intent(view.getContext(), destActivity);
		startActivity(intent);
		if (finishCurrent) {
			finish();
		}
		return;
	}
}
