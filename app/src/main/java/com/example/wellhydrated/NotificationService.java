package com.example.wellhydrated;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationService extends IntentService {
    private static final String NAME = "WellHydrated Notification Service";
    private static final String CHANNEL_ID = "4521";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public NotificationService() {
        super(NAME);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent dummy) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_name)
                .setContentTitle("It's Time for a Glass of Water")
                .setContentText("The cooldown is over.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(4521, builder.build());
    }
}
