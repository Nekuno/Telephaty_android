package com.qnoow.telephaty.Bluetooth;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.qnoow.telephaty.MainActivity;
import com.qnoow.telephaty.R;
import com.qnoow.telephaty.security.Support;

/*
 * The Main Class of the Bluetooth connection 
 */
public class Bluetooth {

	public static final int REQ_CODE = 1001;
	private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

	private AcceptThread mSecureAcceptThread;
	private AcceptThread mInsecureAcceptThread;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;

	// Member fields
	private final CustomHandler mHandler;
	private int mState;
	private String TAG = "Bluetooth";

	// The constructor need a Handler to share the information
	public Bluetooth(Context context, CustomHandler customHandler) {
		Connection.mAdapter = BluetoothAdapter.getDefaultAdapter();
		mState = Utilities.STATE_NONE;
		mHandler = customHandler;
	}

	// Function that checks if Bluetooth is supported by a device
	public Boolean isSupported() {
		if (Connection.mAdapter == null) {
			// Device does not support Bluetooth
			return false;
		}
		return true;

	}

	// Function that allow us to enable Bluetooth
	public void setEnable(Context context) {
		if (!Connection.mAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			Activity c = (Activity) context;
			c.startActivityForResult(enableBtIntent, REQ_CODE);
		}
	}

	// Function that shows the paired devices
	public void getPairedDevices(final Context context, final String title) {

		Set<BluetoothDevice> pairedDevices;
		pairedDevices = Connection.mAdapter.getBondedDevices();
		final String[] devices = new String[pairedDevices.size()];
		final String[] MAC = new String[pairedDevices.size()];
		// put it's one to the adapter
		int i = 0;
		for (BluetoothDevice device : pairedDevices) {
			devices[i] = (device.getName() + "\n" + device.getAddress());
			MAC[i] = device.getAddress();
			i++;
		}

		Toast.makeText(context, context.getString(R.string.show_paired_devices), Toast.LENGTH_SHORT).show();
		AlertDialog.Builder builder = new AlertDialog.Builder(context).setCancelable(true);
		builder.setTitle(title);
		builder.setItems(devices, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				// Do anything (connect) when a device is selected
				Toast.makeText(context, devices[item], Toast.LENGTH_SHORT).show();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	// A BroadcastReceiver for ACTION_FOUND, ACTION_DISCOVERY_STARTED and
	// ACTION_DISCOVERY_FINISHED.
	public BroadcastReceiver setBroadcastReceiver(final Context context, final ArrayAdapter mArrayAdapter) {

		BroadcastReceiver mReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();

				if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
					Utilities.rightSends = 0;
					Utilities.MACs.clear();
				} else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					mArrayAdapter.add(device.getName() + "\n" + device.getAddress());
					if (!Utilities.MACs.contains(device.getAddress()))
						Utilities.MACs.add(device.getAddress());
					mArrayAdapter.notifyDataSetChanged();
					// When discovery is finished, change the Activity title
				} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
					// to make calls to connect and send messages 
					if (Connection.difussion == true && Utilities.MACs.size() > 0) {
						if(Utilities.DEBUG)
							Log.d("DEBUGGING", "En receiver enviando msg");
						new sendDifussionAsync().execute(Utilities.message);
					} else {
						Toast.makeText(context, R.string.scan_finished, Toast.LENGTH_SHORT).show();
						try {
							Utilities.progressDialog.dismiss();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

			}
		};

		return mReceiver;
	}

	// Register the BroadcastReceiver
	public void registerBroadcastReceiver(Context context, BroadcastReceiver mReceiver) {
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
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, time);
		// 0 means the device is always discoverable
		return discoverableIntent;
	}

	public void disableDiscoverability() {
		Connection.mAdapter.cancelDiscovery();
	}

	// Function that allows user to send difussion messages, create a new
	// connection with all near devices and send a message and later close the connection
	public void sendDifussion(String msg) {
		for (int i = 0; i < Utilities.MACs.size(); i++) {

			BluetoothDevice device = Connection.mAdapter.getRemoteDevice(Utilities.MACs.get(i));
			if (!Connection.BBDDmensajes.search(Utilities.identifier, Utilities.MACs.get(i))) {
				// Attempt to connect to the device
				connect(device, false, true);
				long time = System.currentTimeMillis();
				while (getState() != Utilities.STATE_CONNECTED_ECDH_FINISH && System.currentTimeMillis() - time < 5000) {
				}
				if (getState() == Utilities.STATE_CONNECTED_ECDH_FINISH) {
					if(Utilities.DEBUG)
						Log.w("Antes del write", "Conectado con mac = " + Utilities.MACs.get(i));
					write(msg.getBytes(), true);
					Utilities.rightSends ++;
					
				} else {
					if(Utilities.DEBUG)
						Log.w("disconnected", "Esta petando el otro movil!");
					connectionFailed();
				}
				Connection.BBDDmensajes.insert(Utilities.identifier, device.getAddress());
			}
		}
		Connection.difussion = false;
		Connection.privates = false;
	}

	private class sendDifussionAsync extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			sendDifussion(params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (Utilities.sendCount){
				Toast.makeText(Utilities.mainContext, "Mensaje enviado correctamente a " + Utilities.rightSends + "/" + Utilities.MACs.size() + " dispositivos" , Toast.LENGTH_SHORT).show();
				Utilities.sendCount = false;
			}
		}
	}

	//Set the current state of the chat connection. State is an integer defining the current connection state
	public synchronized void setState(int state) {
		if (Utilities.DEBUG)
			Log.d(TAG, "setState() " + mState + " -> " + state);
		mState = state;
		// Give the new state to the Handler so the UI Activity can update
		mHandler.obtainMessage(Utilities.getMessageStateChange(), state, -1).sendToTarget();
	}

	// Return the current connection state.
	public synchronized int getState() {
		return mState;
	}

	// Start the chat service. Specifically start AcceptThread to begin a session in listening (server) mode. Called by the Activity onResume()
	public synchronized void start() {
		if (Utilities.DEBUG)
			Log.d(TAG, "start");

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

	// Start the ConnectThread to initiate a connection to a remote device.
	// Device is the bluetooth device to connect
	public synchronized void connect(BluetoothDevice device, Boolean secure, boolean difussion) {
		if (Utilities.DEBUG)
			Log.d(TAG, "connect to: " + device);
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
		mConnectThread = new ConnectThread(this, device, secure, difussion);
		mConnectThread.start();
		setState(Utilities.STATE_CONNECTING);
	}

	// Start the ConnectedThread to begin managing a Bluetooth connection
	// Socket is the bluetooth socket on which the connection was made
	// Device is the bluetooth device that has been connected
	public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, boolean diffusion) {
		if (Utilities.DEBUG)
			Log.d(TAG, "connected.");

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
		mConnectedThread = new ConnectedThread(this, socket, diffusion);
		mConnectedThread.start();

		// Send the name of the connected device back to the UI Activity
		Message msg = mHandler.obtainMessage(Utilities.getMessageDeviceName());
		Bundle bundle = new Bundle();
		bundle.putString(Utilities.DEVICE_NAME, device.getName());
		msg.setData(bundle);
		mHandler.sendMessage(msg);

		setState(Utilities.STATE_CONNECTED);
	}

	// To Stop all threads
	public synchronized void stop() {
		if (Utilities.DEBUG)
			Log.d(TAG, "stop");

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

	// Write to the ConnectedThread in an unsynchronized manner
	// Out has the bytes to write
	public void write(byte[] out, boolean diffusion) {
		// Create temporary object
		ConnectedThread r;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) {
			if (mState != Utilities.STATE_CONNECTED_ECDH_FINISH)
				return;
			r = mConnectedThread;
		}

		// Perform the write unsynchronized
		r.write(out, diffusion);
	}

	// Indicate that the connection attempt failed and notify the UI Activity.
	void connectionFailed() {
		// Send a failure message back to the Activity
//		Message msg = mHandler.obtainMessage(Utilities.getMessageToast());
//		Bundle bundle = new Bundle();
//		bundle.putString(Utilities.TOAST, Utilities.mainContext.getString(R.string.can_not_connect));
//		msg.setData(bundle);
//		mHandler.sendMessage(msg);
		// Start the service over to restart listening mode
		Bluetooth.this.start();
		try {
			Utilities.progressDialog.dismiss();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Indicate that the connection was lost and notify the UI Activity.
	void connectionLost() {
		// Send a failure message back to the Activity
//		Message msg = mHandler.obtainMessage(Utilities.getMessageToast());
//		Bundle bundle = new Bundle();
//		bundle.putString(Utilities.TOAST, Utilities.mainContext.getString(R.string.lost_connection));
//		msg.setData(bundle);
//		mHandler.sendMessage(msg);
		// Start the service over to restart listening mode
		Bluetooth.this.start();
		try {
			Utilities.progressDialog.dismiss();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public BluetoothDevice getRemoteDevice() {
		return mConnectedThread.getRemoteDevice();
	}

	public BluetoothAdapter getAdapter() {
		return Connection.mAdapter;
	}

	public void setConnectThread(ConnectThread connectThread) {
		mConnectThread = connectThread;
	}

	public Handler getHandler() {
		return mHandler;
	}

}
