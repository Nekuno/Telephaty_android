package com.qnoow.telephaty.Bluetooth;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
	
	String title = "Qnoow Notification";
	String ticker = "Qnoow ticker";
	
	public Notifications(NotificationManager basicNotificationManager, Context externalContext) {
		notificationManager = basicNotificationManager;
		context = externalContext;
		Intent intent = new Intent(context, MainActivity.class);
		pIntent = PendingIntent.getActivity(context, 0, intent, 0);
	}

	public void generateNotification(String msg) {
		if (Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
			NotificationCompat.Builder builder = new NotificationCompat.Builder(
					context);
			builder.setContentIntent(pIntent)
					.setSmallIcon(R.drawable.ic_launcher)
					.setTicker("holaa!! soy una notificacion")
					.setWhen(System.currentTimeMillis()).setAutoCancel(true)
					.setDefaults(Notification.DEFAULT_SOUND)
					.setContentTitle(title).setContentText(msg);
			mNotification = builder.getNotification();
		} else {
			Builder builder = new Builder(context);
			mNotification = builder.setContentTitle(title)
					.setContentText(msg).setTicker("Notification!")
					.setWhen(System.currentTimeMillis())
					.setContentIntent(pIntent)
					.setDefaults(Notification.DEFAULT_SOUND)
					.setAutoCancel(true).setSmallIcon(R.drawable.ic_launcher)
					.build();

		}
	}

	public void sendNotification() {
		notificationManager.notify(0, mNotification);

	}
}
