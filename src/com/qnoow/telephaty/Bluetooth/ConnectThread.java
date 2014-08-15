package com.qnoow.telephaty.Bluetooth;

import java.io.IOException;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;


public class ConnectThread extends Thread {
	
	// Member fields
	private final Bluetooth mService;
    private final BluetoothSocket mSocket;
    private final BluetoothDevice mDevice;

    public ConnectThread(Bluetooth service, BluetoothDevice device, boolean secure) {
    	mService = service;
    	mDevice = device;
    	BluetoothSocket tmp = null;

    	// Get a BluetoothSocket for a connection with the
    	// given BluetoothDevice

    	if (secure){
    		try {
    			tmp = device.createRfcommSocketToServiceRecord(
    					Utilities.MY_UUID);
    		} catch (IOException e) {
    			Log.e(Utilities.TAG, "create() failed", e);
    		}
    		mSocket = tmp;
    	}
    	else{
    		try {
    			tmp = device.createInsecureRfcommSocketToServiceRecord(
    					Utilities.MY_UUID);
    		} catch (IOException e) {
    			Log.e(Utilities.TAG, "create() failed", e);
    		}
    		mSocket = tmp;
    	}
    	
    }

    public void run() {
        Log.i(Utilities.TAG, "BEGIN mConnectThread.");
        setName("ConnectThread");

        // Always cancel discovery because it will slow down a connection
        mService.getAdapter().cancelDiscovery();

        // Make a connection to the BluetoothSocket
        try {
            // This is a blocking call and will only return on a
            // successful connection or an exception
            mSocket.connect();
        } catch (IOException e) {
            // Close the socket
        	Log.e(Utilities.TAG, "NOSSO DEBUG:" + e.getMessage() + "!!!");
            try {
                mSocket.close();
            } catch (IOException e2) {
                Log.e(Utilities.TAG, "unable to close() socket during connection failure", e2);
            }
            mService.connectionFailed();
            return;
        }

        // Reset the ConnectThread because we're done
        synchronized (mService) {
            mService.setConnectThread(null);
        }

        // Start the connected thread
        mService.connected(mSocket, mDevice);
    }

    public void cancel() {
        try {
            mSocket.close();
        } catch (IOException e) {
            Log.e(Utilities.TAG, "close() of connect socket failed", e);
        }
    }
}
