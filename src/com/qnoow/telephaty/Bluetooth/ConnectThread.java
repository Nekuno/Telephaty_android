package com.qnoow.telephaty.Bluetooth;

import java.io.IOException;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * This thread tries to establishing connection . It behaves like a server-side client.
 *  It runs until a connection is accepted (or until cancelled).
 */
public class ConnectThread extends Thread {
	
	// Member fields
	private final Bluetooth mService;
    private final BluetoothSocket mSocket;
    private final BluetoothDevice mDevice;
    private boolean mDifussion;
    String TAG = "ConnectThread";
    
    public ConnectThread(Bluetooth service, BluetoothDevice device, boolean secure, boolean diffusion) {
    	mService = service;
    	mDevice = device;
    	BluetoothSocket tmp = null;
    	mDifussion = diffusion;
    	// Get a BluetoothSocket for a connection with the given BluetoothDevice
    	if(Utilities.DEBUG)
    		Log.d("DEBUGGING", "Entrando en Connectthread");
    	if (secure){
    		try {
    			tmp = device.createRfcommSocketToServiceRecord(
    					Utilities.MY_UUID_SECURE);
    		} catch (IOException e) {
    			Log.e(TAG, "create() failed", e);
    		}
    		mSocket = tmp;
    	}
    	else{
    		try {
    			tmp = device.createInsecureRfcommSocketToServiceRecord(
    					Utilities.MY_UUID_INSECURE);
    		} catch (IOException e) {
    			Log.e(TAG, "create() failed", e);
    		}
    		mSocket = tmp;
    	}
    	
    }

    public void run() {
    	if(Utilities.DEBUG)
    		Log.i(TAG, "BEGIN mConnectThread.");
        setName("ConnectThread");
        // Always cancel discovery because it will slow down a connection
        mService.getAdapter().cancelDiscovery();
        Connection.myBluetooth.getAdapter().cancelDiscovery();
        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            mSocket.connect();
        } catch (IOException e) {
            // Close the socket
        	if(Utilities.DEBUG)
        		Log.e(TAG, "Connection DEBUG:" + e.getMessage() + "!!!");
            try {
                mSocket.close();
            } catch (IOException e2) {
                Log.e(TAG, "unable to close() socket during connection failure", e2);
            }
            mService.connectionFailed();
            return;
        }

        // Reset the ConnectThread because we're done
        synchronized (mService) {
            mService.setConnectThread(null);
        }

        // Start the connected thread
        if (mDifussion == true){
        	mService.connected(mSocket, mDevice, false);
        }
        else{
        	mService.connected(mSocket, mDevice, true);
        }
    	if(Utilities.DEBUG)
    		Log.d("DEBUGGING", "Saliendo de Connectthread");

    }

    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }
}
