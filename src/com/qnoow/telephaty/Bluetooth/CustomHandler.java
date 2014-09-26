package com.qnoow.telephaty.Bluetooth;

import com.qnoow.telephaty.R;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class CustomHandler extends Handler {

	private static final String TAG = "Handler";
	private Activity mMainActivity;

	public CustomHandler(Activity mainActivity) {
		mMainActivity = mainActivity;
	}

	public void handleMessage(Message msg) {
		if (msg.what == Utilities.getMessageStateChange()) {
			// TODO
		} else if (msg.what == Utilities.getMessageWrite()) {
			byte[] writeBuf = (byte[]) msg.obj;
			String writeMessage = new String(writeBuf);
			Log.i(TAG, "WRITE:" + writeMessage + "!!!");
		} else if (msg.what == Utilities.getMessageSharedKey()) {
			byte[] readBuf = (byte[]) msg.obj; // construct a string from the valid bytes in the buffer
			Utilities.lastmessage = new String(readBuf, 0, msg.arg1);
			Log.i(TAG, "SHARED_KEY READ:" + Utilities.lastmessage + "!!!");
		} else if (msg.what == Utilities.getMessageRead()) {
			byte[] readBuff = (byte[]) msg.obj;
			String readString = new String(readBuff, 0, readBuff.length); // construct a string from the valid bytes in the buffer
			Toast.makeText(Connection.mainContext, readString, Toast.LENGTH_SHORT).show();
			TextView txv = (TextView) mMainActivity.findViewById(R.id.textView1);
			txv.setText(readString);
			Log.i(TAG, "READ:" + readString + "!!!");
			// build notification
			Utilities.notificationManager.generateNotification(readString);
			Utilities.notificationManager.sendNotification();
		} else if (msg.what == Utilities.getMessageDeviceName()) {
			// save the connected device's name
			Toast.makeText(Connection.mainContext, "Conectado con " + msg.getData().getString(Utilities.DEVICE_NAME), Toast.LENGTH_SHORT).show();
		} else if (msg.what == Utilities.getMessageToast()) {
			Toast.makeText(Connection.mainContext, msg.getData().getString(Utilities.TOAST), Toast.LENGTH_SHORT).show();

		}

	}
}