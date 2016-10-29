package com.example.dillonwastrack.libusy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import static android.content.Context.NOTIFICATION_SERVICE;

public class OnCheckInAlarmReceive extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("Alarm_received", "Alarm received");

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                        .setContentTitle("LiBusy")
                        .setContentText("Click here to check in!")
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true);;

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, MainActivity.class);

        resultIntent.putExtra("showCheckIn", true);
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        // Sets an ID for the notification
        int mNotificationId = 001;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr =
                (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);

        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
        MainActivity.hasReceivedNotification = true;

        // post user location to heatmap
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        Double userLat = Double.longBitsToDouble(sharedPref.getLong("userLat", 0));
        Double userLng = Double.longBitsToDouble(sharedPref.getLong("userLng", 0));
        NetworkManager.getInstance().postUserLocation(userLat.toString(), userLng.toString());
        MainActivity.addedToHeatmap = true;

    }
}
