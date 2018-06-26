package com.sma.smartfinder.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.sma.smartfinder.ObjectFoundActivity;
import com.sma.smartfinder.ObjectFoundDetailsActivity;
import com.sma.smartfinder.SmartFinderApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by octavian.salcianu on 3/22/2018.
 *
 * BroadcastReceiver for object detection messages
 */

public class ObjectFindReceiver extends BroadcastReceiver {
    private static final int NOTIFICATION_ID = 42;

    @Override
    public void onReceive(Context context, Intent intent) {
        // Sends a message and possibly starts the activity for object finding if in foreground
        // If the app is in the background, creates a notification
        if(((SmartFinderApplication) context.getApplicationContext()).isInForeground()) {
            if (intent.getAction().equals("com.sma.smartfinder.action.NO_OBJECT_FOUND")) {
                Toast.makeText(context, "Object could not be found using the camera server", Toast.LENGTH_LONG).show();
            } else if (intent.getAction().equals("com.sma.smartfinder.action.OBJECT_FOUND")) {
                if(intent.getByteArrayExtra("image") != null) {
                    Intent objectRecognizedIntent = new Intent(context, ObjectFoundDetailsActivity.class);
                    objectRecognizedIntent.putExtra("image", intent.getByteArrayExtra("image")).putExtra("name", intent.getStringExtra("name") + " (Last seen)");
                    context.startActivity(objectRecognizedIntent);
                } else {
                    Intent objectRecognizedIntent = new Intent(context, ObjectFoundActivity.class);
                    ArrayList<String> recogs = intent.getStringArrayListExtra("recognitions");
                    objectRecognizedIntent.putStringArrayListExtra("recognitions", recogs);
                    context.startActivity(objectRecognizedIntent);
                }
            }
        } else {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            PendingIntent operation;
            if(intent.getByteArrayExtra("image") != null) {
                operation = PendingIntent.getActivity(
                        context,
                        -1,
                        new Intent(context, ObjectFoundActivity.class).putExtra("image", intent.getByteArrayExtra("image")).putExtra("name", intent.getStringExtra("name")),
                        PendingIntent.FLAG_ONE_SHOT
                );
            } else {
                operation = PendingIntent.getActivity(
                        context,
                        -1,
                        new Intent(context, ObjectFoundActivity.class).putExtra("recognitions", intent.getStringExtra("recognitions")),
                        PendingIntent.FLAG_ONE_SHOT
                );
            }

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
