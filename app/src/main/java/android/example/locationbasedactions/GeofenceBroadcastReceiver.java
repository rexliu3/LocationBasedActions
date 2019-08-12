package android.example.locationbasedactions;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.example.locationbasedactions.Fragments.ActionListFragment;
import android.graphics.Color;
import android.os.Build;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import androidx.core.app.NotificationCompat;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            Utils.sendErrorMessageBox(context);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            String textTitle = "Within Area";
            String textContent = "You are currently within the preset geofence";

            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel insideChannel = new NotificationChannel("INSIDE_CHANNEL", "Within Area", NotificationManager.IMPORTANCE_DEFAULT);
                insideChannel.setLightColor(Color.BLUE);
                nm.createNotificationChannel(insideChannel);
            }

            Intent notifIntent = new Intent(context, ActionListFragment.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notifIntent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "INSIDE_CHANNEL")
                    .setSmallIcon(R.drawable.ic_notifications_black_24dp)
                    .setContentTitle(textTitle)
                    .setContentText(textContent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            nm.notify(544, builder.build());

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            //List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
//            String geofenceTransitionDetails = getGeofenceTransitionDetails(
//                    this,
//                    geofenceTransition,
//                    triggeringGeofences
//            );

            // Send notification and log the transition details.
            //sendNotification(geofenceTransitionDetails);

        } else {
            // Log the error.
        }
    }
}