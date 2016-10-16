package com.example.phoneit;

import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import static com.example.phoneit.myHeader.*;

public class RegisterActivity extends Activity {
	protected EditText et_input_username;
	protected EditText et_input_password;
	protected EditText et_input_email;
	protected Button button_register;
	protected TextView tv_register;
	protected String input_username;
	protected String input_password;
	protected String input_email;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		et_input_username = (EditText) findViewById(R.id.input_username_register);
		et_input_password = (EditText) findViewById(R.id.input_password_register);
		et_input_email = (EditText) findViewById(R.id.input_email_register);
		button_register = (Button) findViewById(R.id.button_register);
		tv_register = (TextView) findViewById(R.id.textView_register);

		button_register.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View view) {
				input_username = et_input_username.getText().toString();
				input_password = et_input_password.getText().toString();
				input_email = et_input_email.getText().toString();

				if (input_username.isEmpty() || input_username.length() < 4
						|| input_username.length() > 20) {
					Toast.makeText(view.getContext(),
							"Username must be 4-20 chars long",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (input_password.isEmpty() || input_password.length() < 5
						|| input_password.length() > 20) {
					Toast.makeText(view.getContext(),
							"Password must be 5-20 chars long",
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (input_email.isEmpty() || input_email.length() < 5
						|| input_email.length() > 40) {
					Toast.makeText(view.getContext(),
							"E-mail must be 5-40 chars long",
							Toast.LENGTH_SHORT).show();
					return;
				}

				myPostExec post_exec = new myPostExec() {

					@Override
					public void onPostExecute(String response) {
						Log.d("register_response", response);
						if (!response.isEmpty()) {
							if (response.compareTo("ok") == 0) {
								switchActivity(view, LoginActivity.class, false);
								Toast.makeText(view.getContext(),
										"Validate your account via e-mail",
										Toast.LENGTH_SHORT).show();
							} else {
								Toast.makeText(view.getContext(),
										"Invalid input", Toast.LENGTH_SHORT)
										.show();
							}
						}
					}
				};

				BasicNameValuePair nvp_user = new BasicNameValuePair(
						"username", input_username);
				BasicNameValuePair nvp_pass = new BasicNameValuePair(
						"password", input_password);
				BasicNameValuePair nvp_email = new BasicNameValuePair("email",
						input_email);
				new myAsyncTaskPOST(SERVER_URL + "androidRegisterUser.php",
						post_exec).execute(nvp_user, nvp_pass, nvp_email);

			}
		});

		tv_register.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				switchActivity(view, LoginActivity.class, false);
			}
		});

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
