package com.qnoow.telephaty.Bluetooth;

import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.qnoow.telephaty.R;

public class Bluetooth{

	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	public static final int REQ_CODE = 1001;
    
	
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
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
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
		for(BluetoothDevice device : pairedDevices){
			devices[i] = (device.getName()+ "\n" + device.getAddress());
			MAC[i] = device.getAddress();
			i++;
		}
		Toast.makeText(context,"Show Paired Devices",
				Toast.LENGTH_SHORT).show();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(context).setCancelable(true);
		builder.setTitle(title);
		
		builder.setItems(devices, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				//Do anything (connect) when a device is selected
				Toast.makeText(context, devices[item],
						Toast.LENGTH_SHORT).show();
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	

	// Create a BroadcastReceiver for ACTION_FOUND
	public BroadcastReceiver setBroadcastReceiver(
			BluetoothAdapter mBluetoothAdapter, final ArrayAdapter mArrayAdapter) {

		BroadcastReceiver mReceiver = new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				// When discovery finds a device
				if (BluetoothDevice.ACTION_FOUND.equals(action)) {
					// Get the BluetoothDevice object from the Intent
					BluetoothDevice device = intent
							.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					// Add the name and address to an array adapter to show in a
					// ListView
					mArrayAdapter.add(device.getName() + "\n"
							+ device.getAddress());
				}
			}
		};
		return mReceiver;
	}

	// Register the BroadcastReceiver
	public void registerBroadcastReceiver(Context context, BroadcastReceiver mReceiver) {
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		context.registerReceiver(mReceiver, filter); // Don't forget to unregister
												// during onDestroy

	}



	
	
	
}
