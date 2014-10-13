package com.qnoow.telephaty.Bluetooth;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import android.app.ProgressDialog;
import android.content.Context;

import com.qnoow.telephaty.Msg;



public class Utilities {

	// Debugging
	private static final String ID_PROJECT = "QnoowBluetoothConnection";

	// Message types sent from the Bluetooth Handler
	private static final int MESSAGE_STATE_CHANGE = 1;
	private static final int MESSAGE_READ = 2;
	private static final int MESSAGE_WRITE = 3;
	private static final int MESSAGE_DEVICE_NAME = 4;
	private static final int MESSAGE_TOAST = 5;
	private static final int MESSAGE_SHARED_KEY = 6;

	// Key names received from the Bluetooth Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	// Name for the SDP record when creating server socket
	public static final String NAME_SECURE = ID_PROJECT;
	public static final String NAME_INSECURE = ID_PROJECT;

	// Unique UUID for this application
	public static final UUID MY_UUID_SECURE = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
	public static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_LISTEN = 1; // now listening for incoming connections
	public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
	public static final int STATE_CONNECTED = 3; // now connected to a remote device
	public static final int STATE_CONNECTED_ECDH_FINISH = 4; // now connected to a remote device

	// Intent request codes
	public static final int REQUEST_CONNECT_DEVICE = 1;
	public static final int REQUEST_ENABLE_BT = 3;
	public static final boolean DEBUG = true;

	public static List<String> MACs = new ArrayList<String>();

	public static Context mainContext;
	
	// Messages
	public static String message = "";
	public static String jump = "";
	public static String difusion = "1";
	public static String identifier = "";
	public static String lastmessage = "";

	// Notifications
	public static Notifications notificationManager;

	public static int getMessageStateChange() {
		return MESSAGE_STATE_CHANGE;
	}

	public static int getMessageRead() {
		return MESSAGE_READ;
	}

	public static int getMessageWrite() {
		return MESSAGE_WRITE;
	}

	public static int getMessageDeviceName() {
		return MESSAGE_DEVICE_NAME;
	}

	public static int getMessageToast() {
		return MESSAGE_TOAST;
	}

	public static int getMessageSharedKey() {
		return MESSAGE_SHARED_KEY;
	}

	public static String generateIdentifier(){
		// currentDateTimeString is the id of the message
		String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
		return currentDateTimeString.replaceAll("/", "").replaceAll(":", "").replaceAll(" ", "");
	}
	
	public static Msg lastMsg;
	public static List<Msg> AllMsgs;
	public static ProgressDialog progressDialog;
}
