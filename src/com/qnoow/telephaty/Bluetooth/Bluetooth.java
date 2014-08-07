package com.qnoow.telephaty.Bluetooth;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.qnoow.telephaty.MainActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Toast;

public class Bluetooth {

	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	public static final int REQ_CODE = 1001;
	private List<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();

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

	public Intent setEnableDiscoverability() {
		Intent discoverableIntent = new Intent(
				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(
				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0); //  0 means the device is always discoverable
		return discoverableIntent;

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

}
