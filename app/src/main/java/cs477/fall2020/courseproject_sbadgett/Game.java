package cs477.fall2020.courseproject_sbadgett;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Handler;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class Game extends AppCompatActivity {
    double threshold_increase, threshold_decrease;
    int belowThresholdCounter;
    final int TILE = 1;
    final int LETTER = 2;
    final int MATCH = 3;
    final int ROUNDEND = 4;
    int N, frameValue, correct, total, roundsCompleted;
    double averageSessionN;
    Button audio, visual, both, start;
    boolean visMatch = false;
    boolean audMatch = false;
    LetterPlayer letterPlayer;
    SQLiteDatabase myDB;
    DatabaseHelper dbHelper;
    Cursor myCursor;
    TextView score, nvalue, frameNum;
    GameRound round;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        //Creates the letterplayer to handle audio
        letterPlayer = new LetterPlayer(this);

        belowThresholdCounter = 0;
        roundsCompleted = 0;
        averageSessionN = N;

        audio = (Button)findViewById(R.id.button2);
        visual = (Button)findViewById(R.id.VisualMatch);
        both = (Button)findViewById(R.id.both);
        start = (Button)findViewById(R.id.start);

        //Audio match checks if there is a current audio match, increasing correct score if so
        audio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(audMatch){
                    correct++;
                    audMatch = false; //Each match can be counted only once, so its changed to false here
                }
                total++;
                score.setText("Score: " + correct + "/" + total);
            }
        });
        //Visual match checks if there is a current visualmatch, increasing correct score if so
        visual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(visMatch){
                    correct++;
                    visMatch = false; //Each match can be counted only once, so its changed to false here
                }
                total++;
                score.setText("Score: " + correct + "/" + total);
            }
        });
        //Both button checks if there is both an audio and visual match
        both.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(audMatch){
                    correct++;
                    audMatch = false;
                }
                if(visMatch){
                    correct++;
                    visMatch = false;
                }
                total = total + 2;
                score.setText("Score: " + correct + "/" + total);
            }
        });
        //Start button will create a new GameRound thread, initialize it, and run it
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                round= new GameRound(N, handler);
                frameValue = 0;
                frameNum.setText(frameValue + " of " + (N + 20));
                nvalue.setText("N = " + N);
                correct = 0;
                total = 0;
                round.initializeRound();
                //round.printRound();
                round.start();
                score.setText("Score: " + correct + "/" + total);
                start.setVisibility(View.GONE);
            }
        });

        score = (TextView)findViewById(R.id.score);
        frameNum = (TextView)findViewById(R.id.frameNum);
        nvalue = (TextView)findViewById(R.id.nNum);

        //Extract N from the database at the start
        dbHelper = new DatabaseHelper(this);
        myDB = dbHelper.getWritableDatabase();
        String[] columns = new String[]{"_id", "N", "DecThresh", "IncThresh"};
        myCursor = myDB.query(dbHelper.TABLE_NAME2, columns, null, null, null, null, null);
        myCursor.moveToPosition(0);
        N = myCursor.getInt(1);
        threshold_decrease =  myCursor.getInt(2) / 100.0;
        threshold_increase =  myCursor.getInt(3) / 100.0;


        //Set N value and frame to 0
        nvalue.setText("N = " + N);
        frameNum.setText("" + 0 + " of " + (N + 20));

    }


    /*
        When the user goes back to the main menu or otherwise closes the app, the session will
        end and the data associated with the session will be saved. The number of rounds completed
        and the average N across those rounds is saved in the sessions database. The last value of N
        is stored in the settings to use at the start of the next session.
     */
    @Override
    public void onBackPressed()
    {
        if(roundsCompleted > 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.Col_1, roundsCompleted);
            contentValues.put(DatabaseHelper.Col_2, averageSessionN);
            myDB.insert(DatabaseHelper.TABLE_NAME, null, contentValues);
            contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.Col, N);
            myDB.update(DatabaseHelper.TABLE_NAME2, contentValues, "_id = 1", null);
        }
        if(round != null && round.isAlive()) {round.cancel();}
        super.onBackPressed();
    }

    //The session will save and exit if onPause is called
    @Override
    protected void onPause() {
        onBackPressed();
        super.onPause();
    }

    //The handler is responsible for updating the UI and playing the audio associated with each frame
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case TILE:
                    setTiles((Integer)msg.obj);
                    frameNum.setText(++frameValue + " of " + (N + 20));
                    break;
                case LETTER:
                    letterPlayer.playLetter((Integer)msg.obj);
                    break;
                case MATCH:
                    setMatchBooleans((Integer)msg.obj);
                    break;
                case ROUNDEND:
                    setMatchBooleans(0);
                    analyzeRound();
            }
        }

        /*
            Analyze round is called when the handler recieves a ROUNDEND message, which is only sent
            by the GameRound when all the GameFrames have been played. AnalyzeRound will update
            roundsCompleted and averageSessionN, reset the board, calculate the score, and compare the
            score with increase threshold and decrease threshold to determine what changes to the N
            value should be made.
         */
        private void analyzeRound(){
            resetBoard();

            if(roundsCompleted == 0){
                averageSessionN = N;
            }
            else {
                averageSessionN = averageSessionN * (((double) roundsCompleted) / (roundsCompleted + 1)) + N * ((double) 1) / (roundsCompleted + 1);
            }

            double score = ((double)correct)/((double)total);
            Toast toast;

            //If the score is greater than the increase threshold, then N is increased
            if(score > threshold_increase){
                N++;
                belowThresholdCounter = 0;
                toast = Toast.makeText(getApplicationContext(), "Score: " + String.format("%.2f", score) + "%\nN has increased to " + N, Toast.LENGTH_LONG);
            }
            //If the score is lower than the decrease threshold, than the belowThresholdCounter is incremented
            //If this counter hits 3, then N will be decreased as long as N is greater than 2, the minimum N value.
            else if(score < threshold_decrease){
                belowThresholdCounter++;
                if(belowThresholdCounter == 3 && N>2){
                    N--;
                    toast = Toast.makeText(getApplicationContext(), "Score: " + String.format("%.2f", score) + "%\nN has decreased to " + N, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    belowThresholdCounter = 0;
                }
                else if(N > 2) {
                    toast = Toast.makeText(getApplicationContext(), "Score: " + String.format("%.2f", score) + "%\nN remains the same.\nImprove your score to avoid a decrease in N", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                }
                else {
                    toast = Toast.makeText(getApplicationContext(), "Score: " + String.format("%.2f", score) + "%\nN remains the same. Keep practicing!", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                }
            }
            //If N is between the two thresholds, N remains the same.
            else {
                toast = Toast.makeText(getApplicationContext(), "Score: " + String.format("%.2f", score) + "%\nN remains the same. Keep practicing!", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                belowThresholdCounter = 0;
            }
            //A Toast is formatted and displayed, reporting the results of this analysis to the user, and informing
            //The user of any changes to N
            LinearLayout toastLayout = (LinearLayout) toast.getView();
            TextView toastTV = (TextView) toastLayout.getChildAt(0);
            toastTV.setTextSize(18);
            toastTV.setGravity(Gravity.CENTER_HORIZONTAL);
            toast.show();

            //The start button is made visible again so another round can be played
            start.setVisibility(View.VISIBLE);
            roundsCompleted++;
        }

        /*
            When the handler receives a MATCH message from a GameFrame, it updates these booleans
            so that the correct points can be awarded when the user presses the buttons.
         */
        private void setMatchBooleans(int match){
            if(audMatch){
                total++;  //If either audMatch or visMatch are still true, it indicates that
                          //the user did not correctly guess this one, so the total count is
                          //increased but the correct int is not.
            }
            if(visMatch){
                total++;
            }

            switch (match) {
                case 0:                   //No match
                    audMatch = false;
                    visMatch = false;
                    break;
                case 1:                   //Only visMatch
                    visMatch = true;
                    audMatch = false;
                    break;
                case 2:                   //Only audMatch
                    audMatch = true;
                    visMatch = false;
                    break;
                case 3:                   //Both match
                    audMatch = true;
                    visMatch = true;
            }
            score.setText("Score: " + correct + "/" + total);
        }

        /*
            setTiles is called when the handler receives a TILE message from a GameFrame. The TILE
            message will contain an int corresponding to the tile that is to be highlighted. The
            switch statement below checks the tile number and selects the appropriate view in the UI
         */
        private void setTiles(int tile) {
            switch (tile) {
                case 1:
                    findViewById(R.id.view1).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.selected));
                    break;
                case 2:
                    findViewById(R.id.view2).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.selected));
                    break;
                case 3:
                    findViewById(R.id.view3).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.selected));
                    break;
                case 4:
                    findViewById(R.id.view4).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.selected));
                    break;
                case 5:
                    findViewById(R.id.view6).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.selected));
                    break;
                case 6:
                    findViewById(R.id.view7).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.selected));
                    break;
                case 7:
                    findViewById(R.id.view8).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.selected));
                    break;
                case 8:
                    findViewById(R.id.view9).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.selected));
                    break;
            }

            //These if statements below make sure every tile that is not selected is displayed just a bordered white square, unselected

            if (tile != 1) {
                findViewById(R.id.view1).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_border));
            }
            if (tile != 2) {
                findViewById(R.id.view2).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_border));
            }
            if (tile != 3) {
                findViewById(R.id.view3).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_border));
            }
            if (tile != 4) {
                findViewById(R.id.view4).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_border));
            }
            if (tile != 5) {
                findViewById(R.id.view6).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_border));
            }
            if (tile != 6) {
                findViewById(R.id.view7).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_border));
            }
            if (tile != 7) {
                findViewById(R.id.view8).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_border));
            }
            if (tile != 8) {
                findViewById(R.id.view9).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_border));
            }
        }
    };

    //Reset board makes sure every tile has the unselected background
    private void resetBoard(){
        findViewById(R.id.view1).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_border));
        findViewById(R.id.view2).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_border));
        findViewById(R.id.view3).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_border));
        findViewById(R.id.view4).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_border));
        findViewById(R.id.view6).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_border));
        findViewById(R.id.view7).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_border));
        findViewById(R.id.view8).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_border));
        findViewById(R.id.view9).setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.my_border));

    }
}