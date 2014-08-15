package com.qnoow.telephaty.Bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.qnoow.telephaty.MainActivity;

public class Bluetooth {

	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	public static final int REQ_CODE = 1001;
	private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
	
	private AcceptThread mSecureAcceptThread;
	private AcceptThread mInsecureAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;

	// Member fields
	private final BluetoothAdapter mAdapter;
	private final Handler mHandler;
	private int mState;
	
	
	
	public Bluetooth(Context context, Handler handler) {
		mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = Utilities.STATE_NONE;
		mHandler = handler;
	}

	// Function that checks if Bluetooth is supported by a device
	public Boolean isSupported() {
		if (mBluetoothAdapter == null) {
			// Device does not support Bluetooth
			return false;
		}
		return true;

	}

	// function that allow us to enable Bluetooth
	public void setEnable(Context context) {
		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			Activity c = (Activity) context;
			c.startActivityForResult(enableBtIntent, REQ_CODE);
		}
	}

	// Function that shows the paired devices
	public void getPairedDevices(final Context context, final String title) {

		Set<BluetoothDevice> pairedDevices;
		pairedDevices = mBluetoothAdapter.getBondedDevices();
		final String[] devices = new String[pairedDevices.size()];
		final String[] MAC = new String[pairedDevices.size()];
		// put it's one to the adapter
		int i = 0;
		for (BluetoothDevice device : pairedDevices) {
			devices[i] = (device.getName() + "\n" + device.getAddress());
			MAC[i] = device.getAddress();
			i++;
		}
		Toast.makeText(context, "Show Paired Devices", Toast.LENGTH_SHORT)
				.show();

		AlertDialog.Builder builder = new AlertDialog.Builder(context)
				.setCancelable(true);
		builder.setTitle(title);

		builder.setItems(devices, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				// Do anything (connect) when a device is selected
				Toast.makeText(context, devices[item], Toast.LENGTH_SHORT)
						.show();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	// Create a BroadcastReceiver for ACTION_FOUND, ACTION_DISCOVERY_STARTED and
	// ACTION_DISCOVERY_FINISHED
	public BroadcastReceiver setBroadcastReceiver(final Context context,
			final ArrayAdapter mArrayAdapter) {

		BroadcastReceiver mReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				String action = intent.getAction();
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					mArrayAdapter.add(device.getName() + "\n"
							+ device.getAddress());
					mArrayAdapter.notifyDataSetChanged();
				}

			}
		};

		return mReceiver;
	}

	// Register the BroadcastReceiver
	public void registerBroadcastReceiver(Context context,
			BroadcastReceiver mReceiver) {
		IntentFilter filter = new IntentFilter();

		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		context.registerReceiver(mReceiver, filter);

	}

	// Function that enables/disables the discoverability depending on the
	// parameter time
	// time == 0 -> Enable visibility for ever.
	// time == 1 -> Disable visibility in 1 sec, this is the only method to
	// disable it
	public Intent enableDiscoverability(int time) {
		Intent discoverableIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(
				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, time); // 0 means
																		// the
																		// device
																		// is
																		// always
																		// discoverable
		return discoverableIntent;
	}

	public void disableDiscoverability() {
		mBluetoothAdapter.cancelDiscovery();
	}

	// public void showBlueDialog(Context context) {
	//
	// AlertDialog.Builder builder = new AlertDialog.Builder(context);
	// builder.setTitle("BlueTooth List");
	//
	// builder.setItems(bluetoothNames, new DialogInterface.OnClickListener() {
	//
	// public void onClick(DialogInterface dialog, int position) {
	// BluetoothDevice device = mBluetoothAdapter
	// .getRemoteDevice(devices.get(position).getAddress());
	// //service.connect(device);
	// }
	// });
	//
	// builder.create().show();
	// }
	

	
	
	
	/**
	 * Set the current state of the chat connection
	 * @param state
	 *  An integer defining the current connection state
	 */
	private synchronized void setState(int state) {
		if (Utilities.D)
			Log.d(Utilities.TAG, "setState() " + mState + " -> " + state);
		mState = state;

		// Give the new state to the Handler so the UI Activity can update
		mHandler.obtainMessage(Utilities.MESSAGE_STATE_CHANGE, state, -1)
				.sendToTarget();
	}

	
	 // Return the current connection state.
	 
	public synchronized int getState() {
		return mState;
	}

	/**
	 * Start the chat service. Specifically start AcceptThread to begin a
	 * session in listening (server) mode. Called by the Activity onResume()
	 */
	public synchronized void start() {
		if (Utilities.D)
			Log.d(Utilities.TAG, "start");

		// Cancel any thread attempting to make a connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		setState(Utilities.STATE_LISTEN);

		// Start the thread to listen on a BluetoothServerSocket
		if (mSecureAcceptThread == null) {
			mSecureAcceptThread = new AcceptThread(this, true);
			mSecureAcceptThread.start();
		}
		if (mInsecureAcceptThread == null) {
			mInsecureAcceptThread = new AcceptThread(this, false);
			mInsecureAcceptThread.start();
		}
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * 
	 * @param device
	 *            The BluetoothDevice to connect
	 */
	public synchronized void connect(BluetoothDevice device, Boolean secure) {
		if (Utilities.D)
			Log.d(Utilities.TAG, "connect to: " + device);

		// Cancel any thread attempting to make a connection
		if (mState == Utilities.STATE_CONNECTING) {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
			}
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(this, device, secure);
		mConnectThread.start();
		setState(Utilities.STATE_CONNECTING);
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param socket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {
		if (Utilities.D)
			Log.d(Utilities.TAG, "connected.");

		// Cancel the thread that completed the connection
		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Cancel the accept thread because we only want to connect to one
		// device
		if (mSecureAcceptThread != null) {
			mSecureAcceptThread.cancel();
			mSecureAcceptThread = null;
		}
		if (mInsecureAcceptThread != null) {
			mInsecureAcceptThread.cancel();
			mInsecureAcceptThread = null;
		}

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(this, socket);
		mConnectedThread.start();

		// Send the name of the connected device back to the UI Activity
		Message msg = mHandler.obtainMessage(Utilities.MESSAGE_DEVICE_NAME);
		Bundle bundle = new Bundle();
		bundle.putString(Utilities.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		setState(Utilities.STATE_CONNECTED);
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		if (Utilities.D)
			Log.d(Utilities.TAG, "stop");

		if (mConnectThread != null) {
			mConnectThread.cancel();
			mConnectThread = null;
		}

		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (mSecureAcceptThread != null) {
			mSecureAcceptThread.cancel();
			mSecureAcceptThread = null;
		}

		if (mInsecureAcceptThread != null) {
			mInsecureAcceptThread.cancel();
			mInsecureAcceptThread = null;
		}
		setState(Utilities.STATE_NONE);
	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(byte[] out) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (mState != Utilities.STATE_CONNECTED)
				return;
			r = mConnectedThread;
		}
		// Perform the write unsynchronized
		r.write(out);
	}

	/**
	 * Indicate that the connection attempt failed and notify the UI Activity.
	 */
	void connectionFailed() {
		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage(Utilities.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(Utilities.TOAST, "No se puede conectar con dispositivo.");
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		// Start the service over to restart listening mode
		Bluetooth.this.start();
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	void connectionLost() {
		// Send a failure message back to the Activity
		Message msg = mHandler.obtainMessage(Utilities.MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(Utilities.TOAST, "Conexion perdida.");
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		// Start the service over to restart listening mode
		Bluetooth.this.start();
	}

	public BluetoothDevice getRemoteDevice() {
		return mConnectedThread.getRemoteDevice();
	}

	public BluetoothAdapter getAdapter() {
		return mAdapter;
	}

	public void setConnectThread(ConnectThread connectThread) {
		mConnectThread = connectThread;
	}

	public Handler getHandler() {
		return mHandler;
	}
}
