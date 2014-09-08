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
	// Name of the connected device
	private String mConnectedDeviceName = null;
	private Button mSendButton;
	private Notifications notificationManager;
	String lastmessage;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Initialize the BluetoothChatService to perform bluetooth connections
		init();
		setupCommunication();
		
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		Log.d("CDA", "onResume Called"+lastmessage);
		loadNotification();
		notificationManager.disableNotifications();
		if (Utilities.myBluetooth != null && Utilities.mAdapter.isEnabled()) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (Utilities.myBluetooth.getState() == Utilities.STATE_NONE) {
				// Start the Bluetooth service
				Utilities.myBluetooth.start();
			}
		}
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
		Utilities.myBluetooth = new Bluetooth(this, mHandler);
	}

	@Override
	public synchronized void onPause() {
		notificationManager.activateNotifications();
		super.onPause();
	}

	@Override
	public void onStop() {
		notificationManager.activateNotifications();
		super.onStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		notificationManager.activateNotifications();
	}

	public void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (Utilities.myBluetooth.getState() != Utilities.STATE_CONNECTED_ECDH_FINISH) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothService to write
			byte[] send = message.getBytes();
			Utilities.myBluetooth.write(send, false);
		}
	}
	
	public void sendDifussion(View view){
		setupService();
		Utilities.jump = Utilities.MAXJUMP;
		Utilities.difussion = true;
		Utilities.message = ((TextView) findViewById(R.id.edit_text_out)).getText().toString();
		Utilities.mAdapter.startDiscovery();
	}
	

	// The Handler that gets information back from the BluetoothService
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Utilities.MESSAGE_STATE_CHANGE:
				break;
			case Utilities.MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				
				// construct a string from the buffer 
				
				String writeMessage = new String(writeBuf);
				Log.i(TAG, "WRITE:" + writeMessage + "!!!");
				break;

			case Utilities.SHARED_KEY:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				Toast.makeText(MainActivity.this, readMessage,
						Toast.LENGTH_LONG).show();
				TextView tx = (TextView) findViewById(R.id.textView1);
				tx.setText(readMessage);
				lastmessage = readMessage;
				Log.i(TAG, "SHARED_KEY READ:" + readMessage + "!!!");
				break;

			case Utilities.MESSAGE_READ:
				byte[] readBuff = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readString = new String(readBuff, 0, readBuff.length);
				Toast.makeText(MainActivity.this, readString, Toast.LENGTH_LONG)
						.show();
				TextView txv = (TextView) findViewById(R.id.textView1);
				txv.setText(readString);
				Log.i(TAG, "READ:" + readString + "!!!");
				// build notification
				notificationManager.generateNotification(readString);
				notificationManager.sendNotification();

				break;
			case Utilities.MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(
						Utilities.DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Conectado con " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case Utilities.MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(Utilities.TOAST),
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	private void init() {
		if (Utilities.myBluetooth == null)
			setupService();
		if (!Utilities.myBluetooth.isSupported()) {
			Toast.makeText(this, "Bluetooth no supported", Toast.LENGTH_LONG)
					.show();
			finish();
		} else {
			discoverability = false;
			Utilities.myBluetooth.setEnable(this);
			mArrayAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1);
			Utilities.myBluetooth.registerBroadcastReceiver(getApplicationContext(),
					Utilities.myBluetooth.setBroadcastReceiver(getApplicationContext(),
							mArrayAdapter));
			Utilities.mAdapter = BluetoothAdapter.getDefaultAdapter();
			Utilities.mainContext = this;
			notificationManager = new Notifications((NotificationManager) getSystemService(NOTIFICATION_SERVICE), this);
			Utilities.BBDDmensajes = new ControllerMensajes(this);
		}
		
	}

	public void scan_insecure(View view) {
		Toast.makeText(this, "Insecure connection", Toast.LENGTH_SHORT).show();
		Intent serverIntent = null;
		mArrayAdapter.clear();
		BluetoothAdapter.getDefaultAdapter().startDiscovery();
		serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, Utilities.REQUEST_CONNECT_DEVICE);

	}

	public void scan_secure(View view) {
		Toast.makeText(this, "Secure connection", Toast.LENGTH_SHORT).show();
		Intent serverIntent = null;
		mArrayAdapter.clear();
		BluetoothAdapter.getDefaultAdapter().startDiscovery();
		serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, Utilities.REQUEST_CONNECT_DEVICE);

	}

	public void paired(View view) {
		Utilities.myBluetooth.getPairedDevices(this,
				"Dispositivos emparejados anteriormente");
	}

	public void setEnableDiscoverability(View view) {
		if (discoverability == true) {
			discoverability = false;
			Toast.makeText(this, "Disabling Discoverability",
					Toast.LENGTH_SHORT).show();
			startActivity(Utilities.myBluetooth.enableDiscoverability(1));

		} else {
			discoverability = true;
			Toast.makeText(this, "Enabling Discoverability", Toast.LENGTH_SHORT)
					.show();
			startActivity(Utilities.myBluetooth.enableDiscoverability(0));
		}
	}
	
	public void close(View view) {
		Utilities.myBluetooth.stop();
		finish();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult " + resultCode);
		switch (requestCode) {
		case Utilities.REQUEST_CONNECT_DEVICE:
			// When DeviceListActivity returns with a device to connect
			if (resultCode == Activity.RESULT_OK) {
				connectDevice(data, false);
			}
			break;
		case Utilities.REQUEST_ENABLE_BT:
			// When the request to enable Bluetooth returns
			if (resultCode != Activity.RESULT_OK) {
				// User did not enable Bluetooth or an error occurred
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				finish();
			}
		}
	}

	private void connectDevice(Intent data, boolean difussion) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);
		// Get the BluetoothDevice object
		BluetoothDevice device = Utilities.mAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		Utilities.myBluetooth.connect(device, false, difussion);
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
	
	
	private void loadNotification(){
		SharedPreferences prefs = getSharedPreferences("Preferences",
				Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			
			if (prefs.getBoolean("notification", false) == true){
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
	 * This is a wrapper around the new startForeground method, using the older
	 * APIs if it is not available.
	 */
	void startForegroundCompat(int id, Notification notification) {
	    // If we have the new startForeground API, then use it.
		   Log.d("startForegroundCompat", "startForegroundCompat Called");
	}

}
