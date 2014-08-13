package com.qnoow.telephaty;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
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

	// Name of the connected device
    private String mConnectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> mConversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
   
 // Layout Views
    private ListView mConversationView;
    private EditText mOutEditText;
    private Button mSendButton;

   

    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// Initialize the BluetoothChatService to perform bluetooth connections
        myBluetooth = new Bluetooth(this, mHandler);
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


	private void setupConnection() {
        Log.d(TAG, "setupConnection()");

        // Initialize the array adapter for the conversation thread
        mConversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        mConversationView = (ListView) findViewById(R.id.in);
        mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText = (EditText) findViewById(R.id.edit_text_out);
        mOutEditText.setOnEditorActionListener(mWriteListener);

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

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

	 /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (myBluetooth.getState() != Utilities.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            myBluetooth.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    // The action listener for the EditText widget, to listen for the return key
    private TextView.OnEditorActionListener mWriteListener =
        new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            Log.i(TAG, "END onEditorAction");
            return true;
        }
    };

    private final void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }

    // The Handler that gets information back from the BluetoothService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case Utilities.MESSAGE_STATE_CHANGE:
                Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case Utilities.STATE_CONNECTED:
                    setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                    mConversationArrayAdapter.clear();
                    break;
                case Utilities.STATE_CONNECTING:
                    setStatus(R.string.title_connecting);
                    break;
                case Utilities.STATE_LISTEN:
                case Utilities.STATE_NONE:
                    setStatus(R.string.title_not_connected);
                    break;
                }
                break;
            case Utilities.MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case Utilities.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                mConversationArrayAdapter.add(mConnectedDeviceName+":  " + readMessage);
                break;
            case Utilities.MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(Utilities.DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case Utilities.MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(Utilities.TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };

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
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                setupConnection();
            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
            .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        
        BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        // Attempt to connect to the device
        myBluetooth.connect(device, secure);
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
