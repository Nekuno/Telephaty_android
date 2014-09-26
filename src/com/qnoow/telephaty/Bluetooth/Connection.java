package com.qnoow.telephaty.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.qnoow.telephaty.R;
import com.qnoow.telephaty.bbdd.ControllerMensajes;

public class Connection {

	// SharedKey
	public static byte[] sharedKey;
	public static Context mainContext;
	public static boolean difussion = false;
	public static final String MAXJUMP = "5";

	public static Bluetooth myBluetooth = null;
	public static BluetoothAdapter mAdapter = null;
	public static ControllerMensajes BBDDmensajes;

	private static final String TAG = "Connection";

	public static void start() {

		if (myBluetooth != null && mAdapter.isEnabled()) {
			// Only if the state is STATE_NONE, do we know that we haven't
			// started already
			if (myBluetooth.getState() == Utilities.STATE_NONE) {
				// Start the Bluetooth service
				myBluetooth.start();
			}
		}

	}

	public static boolean isECDHFinish() {
		return myBluetooth.getState() != Utilities.STATE_CONNECTED_ECDH_FINISH;

	}

	public static void sendMessage(String message) {
		// Check that we're actually connected before trying anything
		if (Connection.isECDHFinish()) {
			Toast.makeText(Utilities.mainContext, R.string.not_connected, Toast.LENGTH_SHORT).show();
			return;
		}
		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothService to write
			byte[] send = message.getBytes();
			Connection.myBluetooth.write(send, false);
		}

	}

	public static void sendDifussion(String message) {
		Utilities.jump = Connection.MAXJUMP;
		difussion = true;
		Utilities.identifier = Utilities.generateIdentifier();
		Utilities.message = message;
		mAdapter.startDiscovery();

	}

	public static void connectDevice(Intent data) {
		// Get the device MAC address
		String address = data.getExtras().getString(Utilities.DEVICE_NAME);
		// Get the BluetoothDevice object
		BluetoothDevice device = Connection.mAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
		myBluetooth.connect(device, false, difussion);

	}

	public static boolean requestConnection(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case Utilities.REQUEST_CONNECT_DEVICE :
				// When DeviceListActivity returns with a device to connect
				if (resultCode == Activity.RESULT_OK) {
					Connection.connectDevice(data);
					// connectDevice(data, false);
				}
				break;
			case Utilities.REQUEST_ENABLE_BT :
				// When the request to enable Bluetooth returns
				if (resultCode != Activity.RESULT_OK) {
					// User did not enable Bluetooth or an error occurred
					Log.d(TAG, "BT not enabled");
					Toast.makeText(Utilities.mainContext, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
					return false;

				}
		}
		return true;

	}

	public static Intent enableDiscoverability() {
		return myBluetooth.enableDiscoverability(1);
	}

	public static Intent disableDiscoverability() {
		return myBluetooth.enableDiscoverability(0);
	}

	public static void stopBluetooth() {
		myBluetooth.stop();

	}
}
