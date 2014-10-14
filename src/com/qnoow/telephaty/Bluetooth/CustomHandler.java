package com.qnoow.telephaty.Bluetooth;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.sax.TextElementListener;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.qnoow.telephaty.Msg;
import com.qnoow.telephaty.MsgArrayAdapter;
import com.qnoow.telephaty.R;
import com.qnoow.telephaty.bbdd.ControllerMensajesCollection;

public class CustomHandler extends Handler {

	private static final String TAG = "Handler";
	private Activity mMainActivity;
	private ControllerMensajesCollection Msgs;

	public CustomHandler(Activity mainActivity) {
		mMainActivity = mainActivity;
		Utilities.AllMsgs = new ArrayList<Msg>();
		Msgs = new ControllerMensajesCollection(mMainActivity);
	}

	public void handleMessage(Message msg) {
		if (msg.what == Utilities.getMessageStateChange()) {
			// For future
		} else if (msg.what == Utilities.getMessageWrite()) {
			byte[] writeBuf = (byte[]) msg.obj;
			String writeMessage = new String(writeBuf);
			final ListView list = (ListView) mMainActivity.findViewById(R.id.listView);
			Msgs.insert(Utilities.lastMsg);
			Utilities.AllMsgs.add(Utilities.lastMsg);
			final MsgArrayAdapter msgs = new MsgArrayAdapter(mMainActivity, Utilities.AllMsgs);
			// fill the list with data
			msgs.notifyDataSetChanged();
			list.setAdapter(msgs);
			list.setSelection(Utilities.AllMsgs.size() -1 );
			if (Utilities.DEBUG)
				Log.i(TAG, "WRITE:" + writeMessage + "!!!");
		} else if (msg.what == Utilities.getMessageSharedKey()) {
			byte[] readBuf = (byte[]) msg.obj; // construct a string from the valid bytes in the buffer
			Utilities.lastmessage = new String(readBuf, 0, msg.arg1);
			if (Utilities.DEBUG)
				Log.i(TAG, "SHARED_KEY READ:" + Utilities.lastmessage + "!!!");
		} else if (msg.what == Utilities.getMessageRead()) {
			byte[] readBuff = (byte[]) msg.obj;
			String readString = new String(readBuff, 0, readBuff.length); // construct a string from the valid bytes in the buffer
			Toast.makeText(Connection.mainContext, readString, Toast.LENGTH_SHORT).show();
			// get a reference to the listview, needed in order
			// to call setItemActionListener on it
			final ListView list = (ListView) mMainActivity.findViewById(R.id.listView);
			Msgs.insert(Utilities.lastMsg);
			Utilities.AllMsgs.add(Utilities.lastMsg);
			final MsgArrayAdapter msgs = new MsgArrayAdapter(mMainActivity, Utilities.AllMsgs);
			// fill the list with data
			msgs.notifyDataSetChanged();
			list.setAdapter(msgs);
			list.setSelection(Utilities.AllMsgs.size() -1 );
			if (Utilities.DEBUG)
				Log.i(TAG, "READ:" + readString + "!!!");
			// build notification
			Utilities.notificationManager.generateNotification(readString);
			Utilities.notificationManager.sendNotification();
			
		} else if (msg.what == Utilities.getMessageDeviceName()) {
			// save the connected device's name
			Toast.makeText(Connection.mainContext, Utilities.mainContext.getString(R.string.connected_to) + msg.getData().getString(Utilities.DEVICE_NAME), Toast.LENGTH_SHORT).show();
		} else if (msg.what == Utilities.getMessageToast()) {
			Toast.makeText(Connection.mainContext, msg.getData().getString(Utilities.TOAST), Toast.LENGTH_SHORT).show();

		}

	}
}