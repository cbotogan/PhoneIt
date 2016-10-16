package com.example.phoneit;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.os.AsyncTask;
import static com.example.phoneit.myHeader.PROJECT_NUMBER;

public class myAsyncTaskPOST extends
		AsyncTask<BasicNameValuePair, Void, String> {
	myPostExec PostExec;
	String SERVER_URL;
	GoogleCloudMessaging gcm;
	boolean do_gcm_request = false;

	public myAsyncTaskPOST(String URL, myPostExec PostExec) {
		this.SERVER_URL = URL;
		this.PostExec = PostExec;
	}

	public myAsyncTaskPOST(String URL, myPostExec PostExec,
			GoogleCloudMessaging gcm) {
		this.SERVER_URL = URL;
		this.PostExec = PostExec;
		this.gcm = gcm;
		this.do_gcm_request = true;
	}

	@Override
	protected String doInBackground(BasicNameValuePair... params) {
		// TODO Auto-generated method stub
		ArrayList<NameValuePair> toSend = new ArrayList<NameValuePair>();
		String response = null;
		String regid = null;

		for (BasicNameValuePair pair : params) {
			toSend.add(pair);
		}

		if (do_gcm_request) {
			try {
				regid = gcm.register(PROJECT_NUMBER);
			} catch (IOException e) {
				e.printStackTrace();
			}
			BasicNameValuePair nvp_gcm = new BasicNameValuePair("gcm_reg_id",
					regid);
			toSend.add(nvp_gcm);
		}

		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(SERVER_URL);

		try {
			httppost.setEntity(new UrlEncodedFormEntity(toSend));
			response = httpclient.execute(httppost, new BasicResponseHandler());
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return response;
	}

	@Override
	protected void onPostExecute(String response) {
		PostExec.onPostExecute(response);
	}

}

/*
 * new AsyncTask<String, Void, String>() {
 * 
 * @Override protected String doInBackground(String... params) { String username
 * = params[0]; String password = params[1]; String response = null;
 * 
 * ArrayList<NameValuePair> toSend = new ArrayList<NameValuePair>();
 * toSend.add(new BasicNameValuePair("username", username)); toSend.add(new
 * BasicNameValuePair("password", password));
 * 
 * HttpClient httpclient = new DefaultHttpClient(); HttpPost httppost = new
 * HttpPost(SERVER_URI_PUBLIC);
 * 
 * try { httppost.setEntity(new UrlEncodedFormEntity(toSend)); response =
 * httpclient.execute(httppost, new BasicResponseHandler()); } catch
 * (UnsupportedEncodingException e1) { e1.printStackTrace(); } catch
 * (ClientProtocolException e) { // TODO Auto-generated catch block
 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated catch
 * block e.printStackTrace(); } return response; }
 * 
 * @Override protected void onPostExecute(String response) { if
 * (!response.isEmpty()) { if (response.compareTo("ok") == 0) {
 * saveSharedPreferences(input_username); switchActivity(MainActivity.class,
 * true); } else { Toast.makeText(view.getContext(), "Wrong username/password",
 * Toast.LENGTH_SHORT).show(); } } } }.execute(input_username, input_password);
 */
