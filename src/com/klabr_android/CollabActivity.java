package com.klabr_android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.klabr_android.R;

public class CollabActivity extends Activity {
	private BootstrapButton logInButton, createAccountButton;//, forgotPasswordButton;
	private BootstrapEditText userName, password;
	private String SERVER_IP = "http://mickey.cs.vt.edu:3000";//"http://ec2-54-186-249-114.us-west-2.compute.amazonaws.com:3000";//"http://10.0.0.14:3000";

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_collab);
		setTitle("Klabr");
		
		instantiateItems();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.collab, menu);
		return true;
	}
	
	private void instantiateItems() 
	{
		logInButton = (BootstrapButton) findViewById(R.id.loginButton);
		createAccountButton = (BootstrapButton) findViewById(R.id.createAccountButton);
//		forgotPasswordButton = (BootstrapButton) findViewById(R.id.forgotPasswordButton);
		userName = (BootstrapEditText) findViewById(R.id.userName);
		password = (BootstrapEditText) findViewById(R.id.password);
		
		logInButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				String userNameString = userName.getText().toString();
				String passwordString = password.getText().toString();
					try {
						if (userNameString != null && passwordString != null &&
								userNameString.length() > 0 && passwordString.length() > 0)
						{
							String result = new LoginAsyncTask().execute().get();
							if (result != null) {
					            JSONObject jObject = new JSONObject(result);
					            if (!jObject.has("Error")) 
					            {
						            Intent i = new Intent(getApplicationContext(), SearchActivity.class);
						            i.putExtra("Username", jObject.getString("Username"));
//						            i.putExtra("Email", jObject.getString("Email"));
//						            i.putExtra("First_Name", jObject.getString("First_Name"));
//						            i.putExtra("Last_Name", jObject.getString("Last_Name"));
						            i.putExtra("id", jObject.getString("id"));
						            startActivity(i);
						            finish();
					            } else {
					            	showToast("Information provided is not correct!");
					            }
							}
							else {
						    	showToast("Problem with server. Try again later");
							}
						} 
						else 
						{
							showToast("Please fill in the Username and Password fields!");
							return;
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}	
		});
		
		createAccountButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent i = new Intent(getApplicationContext(), CreateAccount.class);
				startActivity(i);
			}
			
		});
		
//		forgotPasswordButton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				Intent i = new Intent(getApplicationContext(), ForgotPassword.class);
//				startActivity(i);
//			}
//			
//		});
	}
	
	private void showToast(String message) 
	{
		Context context = getApplicationContext();
    	int duration = Toast.LENGTH_SHORT;

    	Toast toast = Toast.makeText(context, message, duration);
    	toast.show();
	}
	
	private class LoginAsyncTask extends AsyncTask<Void, Void, String>
	{

		@Override
		protected String doInBackground(Void... arg0) {
			// Create a new HttpClient and Post Header
		    HttpClient httpclient = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost(SERVER_IP + "/user/login");

		    try {
		    	String userNameString = userName.getText().toString();
				String passwordString = password.getText().toString();
				
				// Add your data
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair("Username", userNameString));
		        nameValuePairs.add(new BasicNameValuePair("Password", passwordString));
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

		        // Execute HTTP Post Request
		        HttpResponse response = httpclient.execute(httppost);
		        HttpEntity entity = response.getEntity();
		        
		        if (entity != null)
		        {
		        	String result = "";
		            InputStream inputStream = entity.getContent();
		            // json is UTF-8 by default
		            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
		            StringBuilder sb = new StringBuilder();

		            String line = null;
		            while ((line = reader.readLine()) != null)
		            {
		                sb.append(line + "\n");
		            }
		            result = sb.toString();
					return result;
		        }
		    } catch (ClientProtocolException e) {
		        // TODO Auto-generated catch block
		    } catch (IOException e) {
		        // TODO Auto-generated catch block
		    }
			return null;
		}
		
	}

}