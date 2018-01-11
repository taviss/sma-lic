package com.sma.smartfinder.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.sma.smartfinder.MainActivity;
import com.sma.smartfinder.ObjectFoundActivity;
import com.sma.smartfinder.SmartFinderApplication;

/**
 * Created by octavian.salcianu on 1/11/2018.
 */

public class ObjectRecognitionReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 42;

    @Override
    public void onReceive(Context context, Intent intent) {
        if(((SmartFinderApplication) context.getApplicationContext()).isInForeground()) {
            if (intent.getAction().equals("com.sma.smartfinder.action.NO_OBJECT_FOUND")) {
                Toast.makeText(context, "Object could not be found using the camera server", Toast.LENGTH_LONG).show();
            } else if (intent.getAction().equals("com.sma.smartfinder.action.OBJECT_FOUND")) {
                //TODO Add the actual image received from the server + location
                Intent objectFoundIntent = new Intent(context, ObjectFoundActivity.class);
                objectFoundIntent.putExtra("imageName", intent.getStringExtra("imageName"));
                context.startActivity(objectFoundIntent);
            }
        } else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            PendingIntent operation = PendingIntent.getActivity(
                    context,
                    -1,
                    new Intent(context, ObjectFoundActivity.class),
                    PendingIntent.FLAG_ONE_SHOT
            );

            Notification notification = new Notification.Builder(context)
                    .setContentTitle("Your object has been processed")
                    .setContentText(intent.getStringExtra("imageName") + " has been found.")
                    .setSmallIcon(android.R.drawable.sym_action_email)
                    .setContentIntent(operation)
                    .setAutoCancel(true)
                    .getNotification();

            notificationManager.notify(NOTIFICATION_ID, notification);
        }
    }
}
