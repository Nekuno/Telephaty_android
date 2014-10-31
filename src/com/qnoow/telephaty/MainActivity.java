package com.qnoow.telephaty;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qnoow.telephaty.Bluetooth.Bluetooth;
import com.qnoow.telephaty.Bluetooth.Connection;
import com.qnoow.telephaty.Bluetooth.CustomHandler;
import com.qnoow.telephaty.Bluetooth.DeviceListActivity;
import com.qnoow.telephaty.Bluetooth.Notifications;
import com.qnoow.telephaty.Bluetooth.Utilities;
import com.qnoow.telephaty.bbdd.ControllerMensajes;
import com.qnoow.telephaty.bbdd.ControllerMensajesCollection;

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
		// Initializing the view and the list of messages
		final ListView list = (ListView) this.findViewById(R.id.listView);
		Utilities.AllMsgs = new ControllerMensajesCollection(
				getApplicationContext()).search();
		list.setAdapter(new MsgArrayAdapter(this, Utilities.AllMsgs));
		list.setSelection(Utilities.AllMsgs.size() -1 );
		// The function resend a message
		list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View v,
					int position, long arg3) {
				clickDialog(MainActivity.this, position, getString(R.string.app_name), getString(R.string.what_do));
			}
		});
		// Initialize the BluetoothChatService to perform bluetooth connections
		init();
	}

	@Override
	public synchronized void onResume() {
		super.onResume();
		if(Connection.myBluetooth.getAdapter().getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
			startActivity(Connection.myBluetooth.enableDiscoverability(0));
		}
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
		Connection.sendDifussion(((TextView) findViewById(R.id.edit_text_out))
				.getText().toString());
		TextView tx = (TextView) findViewById(R.id.edit_text_out);
		tx.setText("");
		Utilities.progressDialog = launchLoadingDialog();
		Utilities.sendCount = true;
	}
	

	public void sendDifussionPrivate(View view) {
		setupService();
		Connection.sendDifussion(((TextView) findViewById(R.id.edit_text_out))
				.getText().toString());
		TextView tx = (TextView) findViewById(R.id.edit_text_out);
		tx.setText("");
		Utilities.progressDialog = launchLoadingDialog();
		Utilities.sendCount = true;
	}

	private void init() {
		if (Connection.myBluetooth == null)
			setupService();
		if (!Connection.myBluetooth.isSupported()) {
			Toast.makeText(this, getString(R.string.bluetooth_no_supported),
					Toast.LENGTH_LONG).show();
			finish();
		} else {
			Utilities.mainContext = this;
			discoverability = false;
			Connection.myBluetooth.setEnable(this);
			mArrayAdapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1);
			Connection.myBluetooth.registerBroadcastReceiver(
					getApplicationContext(), Connection.myBluetooth
							.setBroadcastReceiver(getApplicationContext(),
									mArrayAdapter));
			Connection.mAdapter = BluetoothAdapter.getDefaultAdapter();
			Connection.mainContext = this;
			Utilities.notificationManager = new Notifications(
					(NotificationManager) getSystemService(NOTIFICATION_SERVICE),
					this);
			Connection.BBDDmensajes = new ControllerMensajes(this);
		}
	}

	// Function for scanning devices to an insecure connection
	public void scan_insecure(View view) {
		Toast.makeText(this, getString(R.string.insecure_connection),
				Toast.LENGTH_SHORT).show();
		Intent serverIntent = null;
		mArrayAdapter.clear();
		serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, Utilities.REQUEST_CONNECT_DEVICE);
	}

	// Function for scanning devices to an secure connection
	public void scan_secure(View view) {
		Toast.makeText(this, getString(R.string.secure_connection),
				Toast.LENGTH_SHORT).show();
		Intent serverIntent = null;
		mArrayAdapter.clear();
		serverIntent = new Intent(this, DeviceListActivity.class);
		startActivityForResult(serverIntent, Utilities.REQUEST_CONNECT_DEVICE);
	}

	public void paired(View view) {
		Connection.myBluetooth.getPairedDevices(this,
				getString(R.string.previously_paired));
	}

	public void setEnableDiscoverability(View view) {
		if (discoverability == true) {
			discoverability = false;
			Toast.makeText(this, getString(R.string.disabling_discoverability),
					Toast.LENGTH_SHORT).show();
			startActivity(Connection.enableDiscoverability());
		} else {
			discoverability = true;
			Toast.makeText(this, getString(R.string.enabling_discoverability),
					Toast.LENGTH_SHORT).show();
			startActivity(Connection.disableDiscoverability());
		}
	}

	public void close(View view) {
		Connection.stopBluetooth();
		finish();
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(TAG, "onActivityResult " + resultCode);
		if (!Connection.requestConnection(requestCode, resultCode, data)) {
			// if you have some problem, app will close
			finish();
		}
	}

	private void loadNotification() {
		SharedPreferences prefs = getSharedPreferences("Preferences",
				Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean("notification", false);
		editor.commit();
	}

	// A Listener lo catch on back button pressed to continue listening(service)
	@Override
	public void onBackPressed() {
		Log.d("CDA", "onBackPressed Called");
		Intent setIntent = new Intent(Intent.ACTION_MAIN);
		setIntent.addCategory(Intent.CATEGORY_HOME);
		setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(setIntent);
	}

	// This is a wrapper around the new startForeground method, using the older
	// APIs if it is not available.
	void startForegroundCompat(int id, Notification notification) {
		// If we have the new startForeground API, then use it.
		Log.d("startForegroundCompat", "startForegroundCompat Called");
	}

	
	//dialog to wait for message send
	public ProgressDialog launchLoadingDialog() {

		final ProgressDialog progressDialog = ProgressDialog.show(
				MainActivity.this, getString(R.string.wait), getString(R.string.sending_message),
				true);
		progressDialog.setCancelable(false);
		new Thread(new Runnable() {
			@Override
			public void run() {
				
			}
		}).start();
		return progressDialog;
	}
	
	
	
	public void clickDialog(final Activity activity, final int position, final String title, final String message) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(activity);

	    builder.setTitle(title);

	    builder.setMessage(message);
	    builder.setPositiveButton("Resend", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int id) {
				setupService();
				Connection.sendDifussion(Utilities.AllMsgs.get(position)
						.getMessage());
				Utilities.progressDialog = launchLoadingDialog();
				Utilities.sendCount = true;
	       }
	   });
	    
	    if(!Utilities.AllMsgs.get(position).getMac().equals("me")){
	    	builder.setNegativeButton("Send private message",  new DialogInterface.OnClickListener() {
	    		public void onClick(DialogInterface dialog, int id) {
	    			privateMessageDialog(activity, position, title, "Write your private message");
	    		}
	    	});
	    }
	    builder.setNeutralButton("Delete",  new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int id) {
	    		new ControllerMensajesCollection(getApplicationContext())
	    		.delete(Utilities.AllMsgs.get(position));
	    		Utilities.AllMsgs.remove(position);
	    		final MsgArrayAdapter msgs = new MsgArrayAdapter(
	    				MainActivity.this, Utilities.AllMsgs);
	    		msgs.notifyDataSetChanged();
	    		final ListView list = (ListView) activity.findViewById(R.id.listView);
	    		list.setAdapter(msgs);
	    		list.setSelection(position);
	    	}
	    });
	    builder.show();
	}
	
	public void privateMessageDialog(final Activity activity, final int position, String title, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);

	    builder.setTitle(title);

	    builder.setMessage(message);
	    final EditText input = new EditText(MainActivity.this);  
	    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
	                          LinearLayout.LayoutParams.MATCH_PARENT,
	                          LinearLayout.LayoutParams.MATCH_PARENT);
	    input.setLayoutParams(lp);
	    builder.setView(input);
       

	    builder.setPositiveButton("Send private message", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				setupService();
				Connection.privates = true;
				Connection.sendDifussionPrivate(input.getText().toString(), Utilities.AllMsgs.get(position).getMac());
				Utilities.progressDialog = launchLoadingDialog();
				Utilities.sendCount = true;
				
			}
	       
	       
	   });
	    builder.show();
	}

}
