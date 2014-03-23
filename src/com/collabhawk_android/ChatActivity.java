package com.collabhawk_android;


import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


public class ChatActivity extends Activity{
	private EditText input_text;
	private Button btn_Send;
	private ListView listView;
	private SocketListener socketListener;
	SocketIO socket;
	ArrayList<String> messages;
	ArrayAdapter<String> adp;
	private String SERVER_IP = "http://10.0.0.14:3000";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat);
		
		initializeComponents();
	}
	
	private void initializeComponents()
	{
		setTitle(getIntent().getStringExtra("room"));
		input_text = (EditText) findViewById(R.id.input_text);
		btn_Send = (Button) findViewById(R.id.btn_Send);
		listView = (ListView) findViewById(R.id.listView);
		socketListener = new SocketListener();
		messages = new ArrayList<String>();
		btn_Send.setOnClickListener(socketListener);
		adp = new ArrayAdapter<String> (getBaseContext(),
				R.layout.custom_list,messages);
//		adp.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		listView.setAdapter(adp);
		connectSockets();
	}
	
	private void connectSockets()
	{
				try {
					socket = new SocketIO(SERVER_IP);
					socket.connect(new IOCallback() {
					    @Override
					    public void on(String event, IOAcknowledge ack, Object... args) {
					        if ("new_message".equals(event) && args.length > 0) {
					            JSONObject jObject = (JSONObject) args[0];
					            try {
									messages.add(jObject.getString("Username") + " said: " + jObject.getString("Message"));
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											adp.notifyDataSetChanged();
								            listView.setSelection(adp.getCount() - 1);
										}
									});
									} catch (JSONException e) {
									e.printStackTrace();
								}
					        } else if ("num_clients".equals(event) && args.length > 0) {
					        } else if ("joined_room".equals(event) && args.length > 0) {
					        	try {
						        	JSONObject jObject = (JSONObject) args[0];
									JSONArray jsonArgs = jObject.getJSONArray("messages");
									for (int i = 0; i < jsonArgs.length(); i++)
									{
										JSONObject jMessage = jsonArgs.getJSONObject(i);
										String userName = jMessage.getString("Username");
										String message = jMessage.getString("said");
										messages.add(userName + " said: " + message);
										runOnUiThread(new Runnable() {
											@Override
											public void run() {
												adp.notifyDataSetChanged();
									            listView.setSelection(adp.getCount() - 1);
											}
										});
									}
						        	}catch (Exception e) {
						        		e.printStackTrace();
						        	}
					        }
					    }

					    @Override
					    public void onMessage(JSONObject json, IOAcknowledge ack) {}
					    @Override
					    public void onMessage(String data, IOAcknowledge ack) {}
					    @Override
					    public void onError(SocketIOException socketIOException) {}
					    @Override
					    public void onDisconnect() {}
					    @Override
					    public void onConnect() {}
					});
					
					IOAcknowledge ack = new IOAcknowledge() {
					    @Override
					    public void ack(Object... args) {}
					};
					socket.emit("join_room", ack, getIntent().getExtras().getString("room"));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}
	
	private class SocketListener implements OnClickListener
	{

		@Override
		public void onClick(View v) {
			String message = input_text.getText().toString();
			if (message != null && message.length() > 0) 
			{
				IOAcknowledge ack = new IOAcknowledge() {
				    @Override
				    public void ack(Object... args) {}
				};
				JSONObject jObject = new JSONObject();
				try {
					jObject.put("Username", getIntent().getExtras().getString("Username"));
					jObject.put("Message", message);
					jObject.put("userKey", getIntent().getExtras().getString("id"));
					jObject.put("room", getIntent().getExtras().getString("room"));
					socket.emit("add_message", ack, jObject);
					messages.add(getIntent().getExtras().getString("Username") + " said: " + message);
					adp = new ArrayAdapter<String> (getBaseContext(),
							R.layout.custom_list, messages);
					listView.setAdapter(adp);
		            listView.setSelection(adp.getCount() - 1);
		            input_text.setText("");
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	}
}
