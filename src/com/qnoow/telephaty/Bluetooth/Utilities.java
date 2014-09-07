package com.qnoow.telephaty.Bluetooth;

import java.util.UUID;

import com.qnoow.telephaty.bbdd.ControllerMensajes;

import android.content.Context;

public class Utilities {

	// Debugging
    private static final String ID_PROJECT = "QnoowBluetoothConnection";
    public static final String TAG = "BluetoothConnection Telephaty";
    
    // Message types sent from the Bluetooth Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int SHARED_KEY = 6;

    // Key names received from the Bluetooth Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    

    // Name for the SDP record when creating server socket
    public static final String NAME_SECURE = ID_PROJECT;
    public static final String NAME_INSECURE = ID_PROJECT;

    // Unique UUID for this application
    public static final UUID MY_UUID_SECURE =  UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final UUID MY_UUID_INSECURE =  UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a67");
    
    
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    public static final int STATE_CONNECTED_ECDH_FINISH = 4;  // now connected to a remote device
    
    // Intent request codes
    public static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_ENABLE_BT = 3;
	public static final boolean D = true;
   
	//SharedKey
	public static byte[] sharedKey;
	public static Context mainContext;
	public static boolean difussion = false;
	public static String message = "";
	
	public static ControllerMensajes BBDDmensajes;
	
}
