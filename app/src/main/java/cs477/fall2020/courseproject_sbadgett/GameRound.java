package cs477.fall2020.courseproject_sbadgett;

import android.content.Context;


import java.util.concurrent.TimeUnit;
import android.os.Handler;
import android.os.Message;

/*
    The GameRound class represents a single round of the Dual N-Back game. It is a thread that is
    called to run when the start button is pressed in the Game activity.
 */
public class GameRound extends Thread {
    final int ROUNDEND = 4;                   //Used for message for handler
    private int N;
    private int matchNumber = 4;
    private GameFrame[] frames;               //Array used to store GameFrames in this round
    Handler handler;

    /*
        GameRound constructor takes an N value and a handler

        It creates a GameFrame array consisting of 20 + N frames and creates each
        GameFrame, passing it the handler
     */
    public GameRound(int N, Handler handler){
        this.N = N;
        frames = new GameFrame[20 + N];
        for(int i = 0; i < 20 + N; i++){
            frames[i] = new GameFrame(handler);
        }
        this.handler = handler;
    }

    /*
        Initialize round fills the GameFrame[] with appropriate values for the game
     */
    public void initializeRound(){
        insertVisualMatches();
        insertAudioMatches();
        fillFrames();
    }

    /*
        insertVisualMatches chooses four random locations to insert matches. Each location will have
        a matching tile value stored at the location N higher than them. Accidental matches with
        other frames are looked for and avoided. Letter values are chosen randomly from 1 to 8.
     */
    public void insertVisualMatches(){
        for(int i = 0; i < matchNumber; i++){
            int loc = (int)Math.floor(Math.random() * 20);
            while(frames[loc].getTile() != null || frames[loc+N].getTile() != null){
                loc = (int)Math.floor(Math.random() * 20);
            }
            while(frames[loc + N].getTile() == null){
                int tile = addRandomTile(loc);
                if(loc + N >= 20 ? true : (frames[loc + N + N].getTile() == null ? true : frames[loc+N+N].getTile() != tile)){
                    frames[loc + N].setTile(tile);
                }
            }
            frames[loc + N].setVisualMatch(true);
        }
    }

    /*
        insertAudioMatches chooses four random locations to insert matches. Each location will have
        a matching tile value stored at the location N higher than them. Accidental matches with
        other frames are looked for and avoided. Audio values are chosen randomly from 1 to 8.
     */
    private void insertAudioMatches(){
        for(int i = 0; i < matchNumber; i++) {
            int loc = (int) Math.floor(Math.random() * 20);
            while (frames[loc].getLetter() != null || frames[loc + N].getLetter() != null) {
                loc = (int)Math.floor(Math.random() * 20);
            }
            while(frames[loc + N].getLetter() == null){
                int letter = addRandomLetter(loc);
                if(loc + N >= 20 ? true : (frames[loc + N + N].getLetter() == null ? true : frames[loc+N+N].getLetter() != letter)){
                    frames[loc + N].setLetter(letter);
                }
            }
            frames[loc + N].setAudioMatch(true);
        }
    }

    //After the matches are inserted, the remaining audio and tile values that are still null are
    //Assigned random values, still avoiding accidental unplanned matches
    private void fillFrames(){
        for(int i = 0; i < 20 + N; i++){
            if(frames[i].getTile() == null){
                addRandomTile(i);
            }
            if(frames[i].getLetter() == null){
                addRandomLetter(i);
            }
        }
    }

    //Assigns the frame at index f a random tile value from 1 to 8, avoiding accidental matches
    //With frames within N of them
    private int addRandomTile(int f){
        int tile = (int) Math.floor(Math.random() * 8) + 1;
        while((f < N ? false : (frames[f-N].getTile() == null ? false : frames[f-N].getTile() == tile))
                || (f >= 20 ? false : (frames[f+N].getTile() == null ? false : frames[f+N].getTile() == tile))){
            tile = (int) Math.floor(Math.random() * 8) + 1;
        }
        frames[f].setTile(tile);
        return tile;
    }

    //Assigns the frame at index f a random letter value from 1 to 8, avoiding accidental matches
    //With frames within N of them
    private int addRandomLetter(int f){
        int letter = (int) Math.floor(Math.random() * 8) + 1;
        while((f < N ? false : (frames[f-N].getLetter() == null ? false : frames[f-N].getLetter() == letter))
                || (f >= 20 ? false : (frames[f+N].getLetter() == null ? false : frames[f+N].getLetter() == letter))){
            letter = (int) Math.floor(Math.random() * 8) + 1;
        }
        frames[f].setLetter(letter);
        return letter;
    }

    //Method used for debugging
    public void printRound(){
        System.out.print("\nTiles: ");
        for(int i = 0; i < frames.length; i++){
            System.out.print(frames[i].getTile());
        }
        System.out.println("");
        System.out.print("Tiles: ");
        for(int i = 0; i < frames.length; i++){
            if(frames[i].getVisualMatch()){
                System.out.print('t');
            }
            else {
                System.out.print('f');
            }
        }
        System.out.println("");
        System.out.print("Letters: ");
        for(int i = 0; i < frames.length; i++){
            System.out.print(frames[i].getLetter());
        }
        System.out.println("");
        System.out.print("Letters: ");
        for(int i = 0; i < frames.length; i++){
            if(frames[i].getAudioMatch()){
                System.out.print('t');
            }
            else {
                System.out.print('f');
            }
        }
        System.out.println("");
    }

    /*
        Running the GameRound thread causes it to play each GameFrame, and then sleep for 3 seconds.
        Interrupts will cause the thread to exit.

        After the GameFrames have all played, the handler is sent a ROUNDEND message indicating
        that the round is now over.
     */
    @Override
    public void run() {
        for(int i = 0; i < frames.length; i++){
            try {
                if (!Thread.currentThread().isInterrupted()) {
                    frames[i].playFrame();
                    TimeUnit.SECONDS.sleep(3);
                }
            } catch (Exception e){
                return;
            }
        }
        Message msg = handler.obtainMessage(ROUNDEND, 0);
        handler.sendMessage(msg);
    }

    /*
        Cancel is used to stop the thread from continuing if the user leaves the game.
     */
    public void cancel() { interrupt(); }

}
