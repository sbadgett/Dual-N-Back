package cs477.fall2020.courseproject_sbadgett;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/*
    This is the broadcast receiver that will receive the pendingIntents set in the settings
    activity. It publishes a simple notification reminding the user to play the game daily.
 */
public class MyNotificationPublisher extends BroadcastReceiver {
    public static String NOTIFICATION_ID = "dualnback-notification-id" ;
    public static String NOTIFICATION = "dualnback-notification";
    @Override

    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //Extracts the notification from the intent
        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        //Make sure the notification channel is set up with the same notification id as is used when
        //Building the notification in the setting activity
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O ) {
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel notificationChannel = new NotificationChannel("10001","NOTIFICATION_CHANNEL_NAME", importance);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        //Extract the notification_ID from the intent
        int id = intent.getIntExtra( NOTIFICATION_ID , 0 );
        //Display the notification
        notificationManager.notify(id, notification);
    }
}
