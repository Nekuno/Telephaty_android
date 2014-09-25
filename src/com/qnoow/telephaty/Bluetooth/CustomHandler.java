package com.qnoow.telephaty.Bluetooth;

import com.qnoow.telephaty.R;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class CustomHandler extends Handler{

	private static final String TAG = "Handler";
	private Activity mMainActivity;

	public CustomHandler(Activity mainActivity) {
		mMainActivity = mainActivity;
	}

	public void handleMessage(Message msg) {
		switch (msg.what) {
			case Utilities.MESSAGE_STATE_CHANGE :
				break;
			case Utilities.MESSAGE_WRITE :
				byte[] writeBuf = (byte[]) msg.obj;
				String writeMessage = new String(writeBuf);
				Log.i(TAG, "WRITE:" + writeMessage + "!!!");
				break;

			case Utilities.SHARED_KEY :
				byte[] readBuf = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readMessage = new String(readBuf, 0, msg.arg1);
				// Toast.makeText(MainActivity.this, readMessage,
				// Toast.LENGTH_LONG).show();
//				TextView tx = (TextView) mMainActivity.findViewById(R.id.textView1);
//				tx.setText(readMessage);
				Utilities.lastmessage = readMessage;
				Log.i(TAG, "SHARED_KEY READ:" + readMessage + "!!!");
				break;

			case Utilities.MESSAGE_READ :
				byte[] readBuff = (byte[]) msg.obj;
				// construct a string from the valid bytes in the buffer
				String readString = new String(readBuff, 0, readBuff.length);
				Toast.makeText(Utilities.mainContext, readString, Toast.LENGTH_SHORT).show();
				TextView txv = (TextView) mMainActivity.findViewById(R.id.textView1);
				txv.setText(readString);
				Log.i(TAG, "READ:" + readString + "!!!");
				// build notification
				Utilities.notificationManager.generateNotification(readString);
				Utilities.notificationManager.sendNotification();

				break;
			case Utilities.MESSAGE_DEVICE_NAME :
				// save the connected device's name
				String mConnectedDeviceName = msg.getData().getString(Utilities.DEVICE_NAME);
				Toast.makeText(Utilities.mainContext, "Conectado con " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
				break;
			case Utilities.MESSAGE_TOAST :
				Toast.makeText(Utilities.mainContext, msg.getData().getString(Utilities.TOAST), Toast.LENGTH_SHORT).show();
				break;
		}
	}
}