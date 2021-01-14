package cs477.fall2020.courseproject_sbadgett;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.util.ArrayList;

public class Progress extends AppCompatActivity {
    DatabaseHelper dbHelper;
    SQLiteDatabase myDB;
    Cursor myCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        TextView sessions = (TextView)findViewById(R.id.sessions);
        TextView rounds = (TextView)findViewById(R.id.rounds);
        TextView average = (TextView)findViewById(R.id.avgN);

        //Get session data from the database
        dbHelper = new DatabaseHelper(this);
        myDB = dbHelper.getWritableDatabase();
        String[] columns = new String[]{"_id", "NumRounds", "AverageN"};
        myCursor = myDB.query(dbHelper.TABLE_NAME, columns, null, null, null, null, null);
        DataPoint[] sessionData = new DataPoint[myCursor.getCount()];
        myCursor.moveToFirst();
        double averageN = 0;
        int roundSum = 0;
        int i = 0;
        //Iterate through the cursor and each session's data, updating variables appropriately
        //And adding a new DataPoint to graph
        while (myCursor.isAfterLast() == false) {
            roundSum = roundSum + (myCursor.getInt(1));
            averageN += (myCursor.getInt(1) * myCursor.getDouble(2));
            sessionData[i] = new DataPoint(roundSum, myCursor.getDouble(2));
            i++;
            myCursor.moveToNext();
        }

        int numSessions = myCursor.getCount();

        //Set text based on the values
        rounds.setText("Rounds Completed: " + roundSum);
        sessions.setText("Sessions Completed: " + numSessions);
        if(roundSum > 0) {
            averageN = averageN / roundSum;
        }
        average.setText("Average N: " + String.format("%.2f", averageN));

        //Take the array of Datapoints to create line graph and point graph series
        PointsGraphSeries<DataPoint> series = new PointsGraphSeries<DataPoint>(sessionData);
        LineGraphSeries<DataPoint> series2 = new LineGraphSeries<DataPoint>(sessionData);
        final GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMaxX(roundSum+1);
        //Display the series' on the graph
        graph.addSeries(series);
        graph.addSeries(series2);
    }
}