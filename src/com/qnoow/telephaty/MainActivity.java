package com.qnoow.telephaty;

import java.io.IOException;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qnoow.telephaty.Bluetooth.Bluetooth;
import com.qnoow.telephaty.Bluetooth.Connection;
import com.qnoow.telephaty.Bluetooth.CustomHandler;
import com.qnoow.telephaty.Bluetooth.DeviceListActivity;
import com.qnoow.telephaty.Bluetooth.Notifications;
import com.qnoow.telephaty.Bluetooth.Utilities;
import com.qnoow.telephaty.bbdd.BBDDMensajes;
import com.qnoow.telephaty.bbdd.ControllerMensajes;
import com.qnoow.telephaty.security.Support;

public class MainActivity extends ActionBarActivity {

	private static final String TAG = "En MAIN";
	private ArrayAdapter mArrayAdapter;
	private Boolean discoverability;
	private Button mSendButton;
	CustomHandler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// The Handler that gets information back from the BluetoothService
		mHandler = new CustomHandler(this);
		// Initialize the BluetoothChatService to perform bluetooth connections
		init();
		setupCommunication();

	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		loadNotification();
		Utilities.notificationManager.disableNotifications();
		Connection.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	private void setupService() {
		Log.d(TAG, "setupService()");
		// Initialize the BluetoothService to perform bluetooth connections
		Connection.myBluetooth = new Bluetooth(this, mHandler);
	}

	@Override
	public synchronized void onPause() {
		Utilities.notificationManager.activateNotifications();
		super.onPause();
	}

	@Override
	public void onStop() {
		Utilities.notificationManager.activateNotifications();
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Utilities.notificationManager.activateNotifications();
	}

	public void sendMessage(String message) {
		Connection.sendMessage(message);
	}

	public void sendDifussion(View view) {
		setupService();
		Connection.sendDifussion(((TextView) findViewById(R.id.edit_text_out)).getText().toString());

	}

	private void init() {
		if (Connection.myBluetooth == null)
			setupService();
		if (!Connection.myBluetooth.isSupported()) {
			Toast.makeText(this, "Bluetooth no supported", Toast.LENGTH_LONG).show();
			finish();
		} else {
			Utilities.mainContext = this;
			discoverability = false;
			Connection.myBluetooth.setEnable(this);
			mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
			Connection.myBluetooth.registerBroadcastReceiver(getApplicationContext(), Connection.myBluetooth.setBroadcastReceiver(getApplicationContext(), mArrayAdapter));
			Connection.mAdapter = BluetoothAdapter.getDefaultAdapter();
			Connection.mainContext = this;
			Utilities.notificationManager = new Notifications((NotificationManager) getSystemService(NOTIFICATION_SERVICE), this);
			Connection.BBDDmensajes = new ControllerMensajes(this);
		}

	}

	public void scan_insecure(View view) {
		Toast.makeText(this, "Insecure connection", Toast.LENGTH_SHORT).show();
		Intent serverIntent = null;
		mArrayAdapter.clear();
		serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, Utilities.REQUEST_CONNECT_DEVICE);

	}

	public void scan_secure(View view) {
		Toast.makeText(this, "Secure connection", Toast.LENGTH_SHORT).show();
		Intent serverIntent = null;
		mArrayAdapter.clear();
		serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, Utilities.REQUEST_CONNECT_DEVICE);

	}

	public void paired(View view) {
		Connection.myBluetooth.getPairedDevices(this, "Dispositivos emparejados anteriormente");
	}

	public void setEnableDiscoverability(View view) {
		if (discoverability == true) {
			discoverability = false;
			Toast.makeText(this, "Disabling Discoverability", Toast.LENGTH_SHORT).show();
			startActivity(Connection.enableDiscoverability());

		} else {
			discoverability = true;
			Toast.makeText(this, "Enabling Discoverability", Toast.LENGTH_SHORT).show();
			startActivity(Connection.disableDiscoverability());
		}
	}

	public void close(View view) {
		Connection.stopBluetooth(); 
		finish();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult " + resultCode);
		if (!Connection.requestConnection(requestCode, resultCode, data)){
			// if you have some problem, app will close
			finish();
		}
	}


	private void setupCommunication() {
		Log.d(TAG, "setupCommunication");
		// Initialize the send button with a listener that for click events
		mSendButton = (Button) findViewById(R.id.button_send);
		mSendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Send a message using content of the edit text widget
				TextView view = (TextView) findViewById(R.id.edit_text_out);
				String message = view.getText().toString();
				sendMessage(message);
			}
		});

	}

	private void loadNotification() {
		SharedPreferences prefs = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();

		if (prefs.getBoolean("notification", false) == true) {
			TextView tv = (TextView) findViewById(R.id.textView1);
			tv.setText(prefs.getString("msg", ""));
		}
		editor.putBoolean("notification", false);
		editor.commit();
	}

	@Override
	public void onBackPressed() {
		Log.d("CDA", "onBackPressed Called");
		Intent setIntent = new Intent(Intent.ACTION_MAIN);
		setIntent.addCategory(Intent.CATEGORY_HOME);
		setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(setIntent);
	}

	/**
	 * This is a wrapper around the new startForeground method, using the older APIs if it is not available.
	 */
	void startForegroundCompat(int id, Notification notification) {
		// If we have the new startForeground API, then use it.
		Log.d("startForegroundCompat", "startForegroundCompat Called");
	}

}
