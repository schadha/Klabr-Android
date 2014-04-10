package com.klabr_android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.BootstrapEditText;
import com.klabr_android.R;

public class SearchActivity extends Activity {
	private BootstrapEditText searchField;
	private ListView searchView;
	private BootstrapButton searchButton;
	private final String KEY="AIzaSyDJhytcECfUZ64UX-PqFPifGJc5gvrhppk";
	private User loggedInUser;
	private double lat, lon;
	private LocationManager locationManager;
	private ArrayList<String> placeNames;
	private ArrayList<String> placeLocation;
	private String SERVER_IP = "http://mickey.cs.vt.edu:3000";//"http://ec2-54-186-249-114.us-west-2.compute.amazonaws.com:3000";//"http://10.0.0.14:3000";
	
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
		setTitle("Search Places");
		searchField = (BootstrapEditText) findViewById(R.id.searchText);
		searchView = (ListView) findViewById(R.id.searchView);
		searchButton = (BootstrapButton) findViewById(R.id.searchButton);
		
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
	            i.putExtra("room", placeNames.get(arg2));
	            startActivity(i);
			}
			
		});
		
		searchButton.setOnClickListener(new OnClickListener() {
			@SuppressWarnings("unchecked")
			@SuppressLint("NewApi")
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
							String name = ((JSONObject)results.get(i)).getString("name");
							String address = null;
							try {
								address = ((JSONObject)results.get(i)).getString("vicinity").split(",")[0];
							} catch (JSONException e) {
								address = null;
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (address == null)
							{
								address = "No Address Available";
							}
							
							String key = name + " (" + address + ")";
							key = key.replace("&", "and");
							placeNames.add(key);
							placeLocation.add(address);
						}
					} else {
						placeNames.add("No results found");
						placeLocation.add("");
						searchView.setEnabled(false);
					}
					String numClientJson = new NumClientsAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, placeNames).get();
					if (numClientJson == null)
					{
						showToast("Server is down. Please try again later!");
						return;
					}
				    JSONObject numClientObject = new JSONObject(numClientJson);
					List<Map<String, String>> data = new ArrayList<Map<String, String>>();
					for (int i = 0; i < placeNames.size(); i++) {
					    Map<String, String> datum = new HashMap<String, String>(2);
					    datum.put("name", placeNames.get(i));
					    datum.put("numClients", "Number of Users: " + numClientObject.getJSONArray("num_clients").get(i));
					    data.add(datum);
					}
					SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), data,
					                                          android.R.layout.simple_list_item_2,
					                                          new String[] {"name", "numClients"},
					                                          new int[] {android.R.id.text1,
					                                                     android.R.id.text2});
					searchView.setAdapter(adapter);
				   
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
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
	
	private class NumClientsAsyncTask extends AsyncTask<ArrayList<String>, Void, String>
	{

		@Override
		protected String doInBackground(ArrayList<String>... arg0) {
			// Create a new HttpClient and Post Header
		    HttpClient httpclient = new DefaultHttpClient();
		    HttpPost httppost = new HttpPost(SERVER_IP + "/chat/clients");

		    try {
		    	// Add your data
		        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        System.out.println(convertArray(arg0[0]));
		        nameValuePairs.add(new BasicNameValuePair("rooms", convertArray(arg0[0])));
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
	
	private String convertArray(ArrayList<String> rooms)
	{
		StringBuilder result = new StringBuilder("");
		for (int i = 0; i < rooms.size(); i++)
		{
			if (i != rooms.size() - 1)
				result.append(rooms.get(i).toString() + "\t");
			else
				result.append(rooms.get(i).toString());
		}
		return result.toString();
	}

}