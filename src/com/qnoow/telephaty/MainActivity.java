package com.qnoow.telephaty;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
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



   

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Initialize the BluetoothChatService to perform bluetooth connections
		if (!myBluetooth.isSupported())
			Toast.makeText(this, "Bluetooth no soportado", Toast.LENGTH_SHORT)
					.show();
		init();

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	

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
                //connectDevice(data);
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
