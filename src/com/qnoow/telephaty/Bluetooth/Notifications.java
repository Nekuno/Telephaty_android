package com.qnoow.telephaty.Bluetooth;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;

import com.qnoow.telephaty.MainActivity;
import com.qnoow.telephaty.R;

public class Notifications {
	NotificationManager notificationManager = null;
	Context context;
	PendingIntent pIntent;
	Notification mNotification;

	String title = "Nuevo mensaje";
	String ticker = "Qnoow Notification";
	// to show notifications or not, it depends of your current state
	Boolean activated;

	public Notifications(NotificationManager basicNotificationManager,
			Context externalContext) {
		notificationManager = basicNotificationManager;
		context = externalContext;
		Intent intent = new Intent(context, MainActivity.class);
		pIntent = PendingIntent.getActivity(context, 0, intent, intent.FLAG_ACTIVITY_CLEAR_TOP);
		activated = false;
	}

	public void generateNotification(String msg) {
		
		SharedPreferences prefs = context.getSharedPreferences("Preferences",
				Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
	
			editor.putBoolean("notification", true);
			editor.putString("msg", msg);
			editor.commit();
		
		
		if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					context);
			builder.setContentIntent(pIntent)
					.setSmallIcon(R.drawable.ic_launcher).setTicker(ticker)
					.setWhen(System.currentTimeMillis()).setAutoCancel(true)
					.setDefaults(Notification.DEFAULT_SOUND)
					.setContentTitle(title).setContentText(msg);
			mNotification = builder.getNotification();
		} else {
			Builder builder = new Builder(context);
			mNotification = builder.setContentTitle(title).setContentText(msg)
					.setTicker(ticker).setWhen(System.currentTimeMillis())
					.setContentIntent(pIntent)
					.setDefaults(Notification.DEFAULT_SOUND)
					.setAutoCancel(true).setSmallIcon(R.drawable.ic_launcher)
					.build();
		}
	}

	public void sendNotification() {
		if (activated) {			
			notificationManager.notify(0, mNotification);
		}
	}
	
	public void activateNotifications() {
		activated = true;
	}
	
	public void disableNotifications() {
		activated = false;
	}
}
