package cs477.fall2020.courseproject_sbadgett;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.util.Calendar;

public class Settings extends AppCompatActivity {

    RadioGroup notifications;
    Button save;
    EditText nValue, incThresh, decThresh;
    DatabaseHelper dbHelper;
    SQLiteDatabase myDB;
    Cursor myCursor;
    int N;
    int threshold_decrease;
    int threshold_increase;
    RadioButton yes, no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        yes = (RadioButton)findViewById(R.id.yes);
        no = (RadioButton)findViewById(R.id.no);
        nValue = (EditText)findViewById(R.id.nValue);
        incThresh = (EditText)findViewById(R.id.incThresh);
        decThresh = (EditText)findViewById(R.id.decThresh);
        save = (Button)findViewById(R.id.save);
        notifications = (RadioGroup)findViewById(R.id.notifications);

        //Retrieve current settings from database to display as hints
        dbHelper = new DatabaseHelper(this);
        myDB = dbHelper.getWritableDatabase();
        String[] columns = new String[]{"_id", "N", "DecThresh", "IncThresh"};
        myCursor = myDB.query(dbHelper.TABLE_NAME2, columns, null, null, null, null, null);
        myCursor.moveToPosition(0);
        N = myCursor.getInt(1);
        threshold_decrease =  myCursor.getInt(2);
        threshold_increase =  myCursor.getInt(3);
        nValue.setHint(""+N);
        decThresh.setHint(""+threshold_decrease);
        incThresh.setHint(""+threshold_increase);

        //Sets the yes or no radioButton depending on if an alarm is active
        if(alarmActive()){
            yes.setChecked(true);
        }
        else{
            no.setChecked(true);
        }

        //Checks inputted text values and reacts accordingly
        save.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View view) {
                ContentValues contentValues = new ContentValues();
                String toast = "";
                int change = 0;

                if(!nValue.getText().toString().equals("")){
                    //Get new N value
                    int N = Integer.parseInt(nValue.getText().toString());
                    //If it is outside appropriate range, update toast message
                    if(N < 2 || N > 9){
                        toast += "N must be an integer between 2 and 9.\n";
                    }
                    //New value is appropriate, indicate change and put value into contentValues
                    else{
                        change = 1;
                        contentValues.put(DatabaseHelper.Col, N);
                    }
                }

                int incThreshold = threshold_increase;
                if(!incThresh.getText().toString().equals("")){
                    incThreshold = Integer.parseInt(incThresh.getText().toString());
                    //If it is outside appropriate range, update toast message
                    if(incThreshold < 50 || incThreshold > 100){
                        toast += "Increase threshold must be an integer between 50 and 100.\n";
                    }
                    //New value is appropriate, indicate change and put value into contentValues
                    else {
                     change = 1;
                        contentValues.put(DatabaseHelper.Col3, incThreshold);
                    }
                }


                if(!decThresh.getText().toString().equals("")){
                    //Get new decrease threshold value
                    int decThreshold = Integer.parseInt(decThresh.getText().toString());
                    //If it is outside appropriate range, update toast message
                    if(decThreshold < 0 || decThreshold > incThreshold){
                        toast += "Decrease threshold must be an integer between 0 and the increase threshold.\n";
                    }
                    //New value is appropriate, indicate change and put value into contentValues
                    else {
                        change = 1;
                        contentValues.put(DatabaseHelper.Col2, decThreshold);
                    }
                }

                //If a new value is appropriate, update the database
                if(change == 1){
                    myDB.update(DatabaseHelper.TABLE_NAME2, contentValues, "_id = 1", null);
                }
                //If there is a warning to play, show the toast
                if(!toast.equals("")){
                    Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
                }
                //Save is successful and there are no warnings, so return to main menu
                else {
                    onBackPressed();
                }

            }
        });

        //Changing the selected radioButton will automatically turn on or off alarms
        notifications.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(i == R.id.yes){
                    //Create and schedule daily notifications
                    Notification notify = getNotification("Be sure to practice Dual N-Back everyday!");
                    scheduleNotification(notify);
                }
                else{
                    //Cancel notifications
                    Notification notify = getNotification("Be sure to practice Dual N-Back everyday!");
                    cancelNotification(notify);
                }
            }
        });

    }

    /*
        Given a notification, this will create a pendingIntent and use the alarmManager to send the
        intent once a day to the MyNotificationPublisher class
    */
    private void scheduleNotification (Notification notification) {
        Intent notificationIntent = new Intent(this, MyNotificationPublisher.class);
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    //Given a string for the notification to display, build the appropriate notification
    private Notification getNotification (String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder( this, "default" ) ;
        builder.setContentTitle( "Dual N-Back Reminder!");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable. ic_launcher_foreground );
        builder.setAutoCancel( true );
        builder.setChannelId( "10001" );
        return builder.build();
    }

    //Cancels the notification system set above
    private void cancelNotification (Notification notification) {
        Intent notificationIntent = new Intent(this, MyNotificationPublisher.class);
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION, notification);
        PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT).cancel();
    }

    //Determines if the notification pending intent reminders are active, and returns true if so
    private boolean alarmActive(){
        Notification notification = getNotification("Be sure to practice Dual N-Back everyday!");
        Intent notificationIntent = new Intent(this, MyNotificationPublisher.class);
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(MyNotificationPublisher.NOTIFICATION, notification);
        if(PendingIntent.getBroadcast(this, 0, notificationIntent, PendingIntent.FLAG_NO_CREATE) != null){
            return true;
        }
        return false;
    }

}

