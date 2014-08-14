package com.qnoow.telephaty;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.qnoow.telephaty.Bluetooth.Bluetooth;
import com.qnoow.telephaty.Bluetooth.DeviceListActivity;
import com.qnoow.telephaty.Bluetooth.Utilities;

public class MainActivity extends ActionBarActivity {

	private static final String TAG = "En MAIN";
	private ArrayAdapter mArrayAdapter;
	private Bluetooth myBluetooth = null;
	private ListView listDevicesFound;
	private Boolean discoverability;
	private BluetoothAdapter mAdapter = null;
	private Bluetooth mService = null;
	// Name of the connected device
    private String mConnectedDeviceName = null;

   

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Initialize the BluetoothChatService to perform bluetooth connections
		
		mAdapter  = BluetoothAdapter.getDefaultAdapter();
		
		
		if (mService == null)
			setupService();

	}
	
	
	@Override
	public synchronized void onResume() {
		super.onResume();


		// Performing this check in onResume() covers the case in which
		// Bluetooth was not enabled during onStart(), so we were paused
		// to enable it...
		// onResume() will be called when ACTION_REQUEST_ENABLE activity
		// returns.
		if (mService != null) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (mService.getState() == Utilities.STATE_NONE) {
				// Start the Bluetooth service
				mService.start();
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
		mService = new Bluetooth(this, mHandler);
	}

	@Override
	public synchronized void onPause() {
		super.onPause();

	}

	@Override
	public void onStop() {
		super.onStop();

	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Stop the Bluetooth service
		if (mService != null)
			mService.stop();

	}
	
	

	public void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (mService.getState() != Utilities.STATE_CONNECTED) {
			Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothService to write
			byte[] send = message.getBytes();
			mService.write(send);
		}
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

				Log.e(TAG, "NOSSO DEBUG - WRITE:" + writeMessage + "!!!");
				break;
			case Utilities.MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);

				Log.e(TAG, "NOSSO DEBUG - READ:" + readMessage + "!!!");

			

				break;
			case Utilities.MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(Utilities.DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Conectado em " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case Utilities.MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(Utilities.TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};
	
	

	private void init() {
		discoverability = false;
		myBluetooth.setEnable(this);
		listDevicesFound = (ListView) findViewById(R.id.devicesfound);
		mArrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		listDevicesFound.setAdapter(mArrayAdapter);
		myBluetooth.registerBroadcastReceiver(getApplicationContext(), myBluetooth
				.setBroadcastReceiver(getApplicationContext(), mArrayAdapter));

	}

	// class MyClickListener implements OnClickListener {
	//
	// private Bluetooth bluetooth;
	// private BluetoothAdapter mBluetoothAdapter;
	//
	// public MyClickListener(Bluetooth bluetooth,BluetoothAdapter
	// mBluetoothAdapter) {
	// this.bluetooth = bluetooth;
	// this.mBluetoothAdapter = mBluetoothAdapter;
	// }
	//
	// public void onClick(View view){
	// BroadcastReceiver mReceiver =
	// bluetooth.setBroadcastReceiver(mBluetoothAdapter, mArrayAdapter);
	// bluetooth.registerBroadcastReceiver(getApplicationContext(), mReceiver);
	// }
	//
	// }
	//
	public void scan(View view) {
		Toast.makeText(this, "pulsado scan", Toast.LENGTH_SHORT).show();
		mArrayAdapter.clear();
		BluetoothAdapter.getDefaultAdapter().startDiscovery();

	}

	public void paired(View view) {
		myBluetooth.getPairedDevices(this,
				"Dispositivos emparejados anteriormente");
	}

	public void setEnableDiscoverability(View view) {
		if (discoverability == true) {
			discoverability = false;
			Toast.makeText(this, "Disabling Discoverability",
					Toast.LENGTH_SHORT).show();
			startActivity(myBluetooth.enableDiscoverability(1));
			
		} else {
			discoverability = true;
			Toast.makeText(this, "Enabling Discoverability", Toast.LENGTH_SHORT)
					.show();
			startActivity(myBluetooth.enableDiscoverability(0));
		}
	}


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case Utilities.REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data);
            }
            break;
        case Utilities.REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Here we setup the chat
            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    
    private void connectDevice(Intent data) {
		// Get the device MAC address
		String address = data.getExtras().getString(
				DeviceListActivity.EXTRA_DEVICE_ADDRESS);

		// Get the BluetoothDevice object
		BluetoothDevice device = mAdapter.getRemoteDevice(address);

		// Attempt to connect to the device
		mService.connect(device);
	}

	
   
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, Utilities.REQUEST_CONNECT_DEVICE);
            return true;
      
        }
        return false;
    }
}
