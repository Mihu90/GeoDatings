/**
 * =====================================
 * Application name 	: GeoDatings
 * Application version	: 1.0.0
 * Author				: Mihai Surdeanu
 * =====================================
 * Copyright (C) 2014-2015 Surdeanu Mihai. All rights reserved.
 */
package ro.mihaisurdeanu.geodatings;

import ro.mihaisurdeanu.geodatings.libraries.UserFunctions;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import ro.mihaisurdeanu.geodatings.R;

public class GCMIntentService extends GCMBaseIntentService {

	private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(UserFunctions.SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: registerId = " + registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered: registerId = " + registrationId);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        String message = intent.getExtras().getString("message");
       
        // Notifica utilizatorul asupra mesajului primit
        generateNotification(context, message);
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        Log.i(TAG, "Received recoverable error: " + errorId);
		return false;
    }

    /**
     * Creaza un mesaj prin care utilizatorul este notificat de faptul ca a
     * aparut un nou eveniment din partea altcuiva.
     */
    @SuppressWarnings("deprecation")
	private static void generateNotification(Context context, String message) {
        int icon = R.drawable.logo;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        
        // Titlul notificarii
        String title = context.getString(R.string.notification_header);
        
        // Continului intregii notificari
        Intent notificationIntent = new Intent(context, HomeActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        
        // Adauga si un sunet
        notification.defaults |= Notification.DEFAULT_SOUND;
        
        // Facem telefonul sa vibreze pentru o perioada scurta de timp
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationManager.notify(0, notification);      

    }

}
