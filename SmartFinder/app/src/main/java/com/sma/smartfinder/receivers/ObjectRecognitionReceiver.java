package com.sma.smartfinder.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.sma.smartfinder.ObjectRecognizedActivity;
import com.sma.smartfinder.SmartFinderApplication;

/**
 * Created by octavian.salcianu on 1/11/2018.
 *
 * Broadcast receiver for object recognition
 */

public class ObjectRecognitionReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 42;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Sends a message and possibly starts the activity for object recognition if in foreground
        // If the app is in the background, creates a notification
        if(((SmartFinderApplication) context.getApplicationContext()).isInForeground()) {
            if (intent.getAction().equals("com.sma.smartfinder.action.OBJECT_NOT_RECOGNIZED")) {
                Toast.makeText(context, "Object could not be found using the camera server", Toast.LENGTH_LONG).show();
                Intent objectRecognizedIntent = new Intent(context, ObjectRecognizedActivity.class).putExtra("image", intent.getStringExtra("image"));
                context.startActivity(objectRecognizedIntent);
            } else if (intent.getAction().equals("com.sma.smartfinder.action.OBJECT_RECOGNIZED")) {
                Intent objectRecognizedIntent = new Intent(context, ObjectRecognizedActivity.class);
                objectRecognizedIntent.putExtra("image", intent.getStringExtra("image")).putExtra("recognitions", intent.getStringExtra("recognitions"));
                context.startActivity(objectRecognizedIntent);
            }
        } else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            PendingIntent operation = PendingIntent.getActivity(
                    context,
                    -1,
                    new Intent(context, ObjectRecognizedActivity.class).putExtra("recognitions", intent.getStringExtra("recognitions")).putExtra("image", intent.getStringExtra("image")),
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
