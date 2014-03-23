package com.collabhawk_android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class SearchActivity extends Activity {
	private EditText searchField;
	private ListView searchView;
	private Button searchButton;
	private final String KEY="AIzaSyDJhytcECfUZ64UX-PqFPifGJc5gvrhppk";
	private User loggedInUser;
	private double lat, lon;
	private LocationManager locationManager;
	private ArrayList<String> placeNames;
	private ArrayList<String> placeLocation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		initializeComponents();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}
	
	private void initializeComponents() 
	{
		searchField = (EditText) findViewById(R.id.searchText);
		searchView = (ListView) findViewById(R.id.searchView);
		searchButton = (Button) findViewById(R.id.searchButton);
		
		loggedInUser = new User(getIntent().getStringExtra("Username"),
				getIntent().getStringExtra("First_Name"),
				getIntent().getStringExtra("Last_Name"),
				getIntent().getStringExtra("Email"));
	    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    
	    placeNames = new ArrayList<String>();
		placeLocation  = new ArrayList<String>();
		
		searchView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent i = new Intent(getApplicationContext(), ChatActivity.class);
	            i.putExtra("Username", getIntent().getStringExtra("Username"));
	            i.putExtra("Email", getIntent().getStringExtra("Email"));
	            i.putExtra("First_Name", getIntent().getStringExtra("First_Name"));
	            i.putExtra("Last_Name", getIntent().getStringExtra("Last_Name"));
	            i.putExtra("id", getIntent().getStringExtra("id"));
	            i.putExtra("room", arg0.getItemAtPosition(arg2).toString());
	            startActivity(i);
			}
			
		});
		
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			    Criteria criteria = new Criteria();
			    Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
			    lat = location.getLatitude();
			    lon = location.getLongitude();
			    
			    placeNames.removeAll(placeNames);
			    placeLocation.removeAll(placeLocation);
			    
			    try {
			    	if(getCurrentFocus()!=null && getCurrentFocus() instanceof EditText){
			            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			            imm.hideSoftInputFromWindow(searchField.getWindowToken(), 0);
			        }
			    	
					String totalJson = new SearchAsyncTask().execute().get();
					JSONObject jObject = new JSONObject(totalJson);
					JSONArray results = jObject.getJSONArray("results");
					
					if (results.length() > 0)
					{
						searchView.setEnabled(true);
						for (int i = 0; i < results.length(); i++)
						{
							placeNames.add(((JSONObject)results.get(i)).getString("name"));
							placeLocation.add(((JSONObject)results.get(i)).getString("vicinity"));
						}
					} else {
						placeNames.add("No results found");
						searchView.setEnabled(false);

					}
					
					
					ArrayAdapter<String> adp=new ArrayAdapter<String> (getBaseContext(),
							android.R.layout.simple_dropdown_item_1line,placeNames);
					adp.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
					
					searchView.setAdapter(adp);
				   
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
	    
	}
	
	private void showToast(String message) 
	{
		Context context = getApplicationContext();
    	int duration = Toast.LENGTH_SHORT;

    	Toast toast = Toast.makeText(context, message, duration);
    	toast.show();
	}
	
	private class SearchAsyncTask extends AsyncTask<Void, Void, String>
	{

		@Override
		protected String doInBackground(Void... arg0) {
		    try {
		    	HttpClient httpclient = new DefaultHttpClient();
		        HttpGet request = new HttpGet();
		        URI website = new URI(getParameters());
		        request.setURI(website);
		        String result = "";
		        HttpResponse response = httpclient.execute(request);
		        HttpEntity entity = response.getEntity();
		        if (entity != null) 
		        {
		            // json is UTF-8 by default
		            InputStream inputStream = entity.getContent();
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
		    } catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}

		private String getParameters() {
			
//			String[] textArray = searchField.getText().toString().replace(" ", "%20");
//			StringBuilder textArrayToSearch = new StringBuilder();
//			for (int i = 0; i < textArray.length; i++)
//			{
//				textArrayToSearch.append(textArray[i]);
//			}
			String replaceString = searchField.getText().toString().replace(" ", "%20");
			StringBuilder builder = new StringBuilder();
			builder.append("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
			builder.append("location="+ lat + "," + lon + "&");
			builder.append("radius=50000&");
			builder.append("rankBy=distance&");
			builder.append("name=" + replaceString + "&");
			builder.append("sensor=true&");
			builder.append("key=" + KEY);
			return builder.toString();
		}
		
	}

}
