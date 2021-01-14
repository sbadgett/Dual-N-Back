package cs477.fall2020.courseproject_sbadgett;

import android.content.Context;
import android.os.Message;
import android.view.View;
import android.os.Handler;

/*
    The GameFrame class is used to represent one state during a round. Each of these frame states
    consists of a value for the tile highlighted from 1 to 8, a value for the letter spoken from
    1 to 8, and booleans indicating if this round is either an auditory or visual match to the
    frame that preceeded it by N in the GameRound.
 */
public class GameFrame {
    final int TILE = 1;
    final int LETTER = 2;
    final int MATCH = 3;
    boolean visualMatch;
    boolean audioMatch;
    private Integer tile;
    private Integer letter;
    private Handler handler;

    View v;

    /*
        The constructor takes the handler value and sets other values to null and false.
     */
    public GameFrame(Handler handler){
        this.handler = handler;
        this.tile = null;
        this.letter = null;
        this.visualMatch = false;
        this.audioMatch = false;
    }

    /*
        When a GameFrame is played, it sends three messages to the handler in the Game Activity,
        which takes appropriate actions depending on the values contained within.
     */
    public void playFrame(){
        //Send message with tile to highlight
        Message msg = handler.obtainMessage(TILE, tile);
        handler.sendMessage(msg);
        //Send message with letter to play
        msg = handler.obtainMessage(LETTER, letter);
        handler.sendMessage(msg);
        //Send message containing info about matches for this frame
        msg = handler.obtainMessage(MATCH, convertMatchesToInt());
        handler.sendMessage(msg);
    }

    /*
        Combines audioMatch and visualMatch into a single int to send in the MATCH message

        This is decoded again by the handler in Main Activity
     */
    private int convertMatchesToInt(){
        if(audioMatch && visualMatch){
            return 3;
        }
        if(audioMatch){
            return 2;
        }
        if(visualMatch){
            return 1;
        }
        return 0;
    }

    public void setVisualMatch(boolean visualMatch){
        this.visualMatch = visualMatch;
    }

    public void setAudioMatch(boolean audioMatch){
        this.audioMatch = audioMatch;
    }

    public void setTile(Integer tile){
        this.tile = tile;
    }

    public void setLetter(Integer letter){
        this.letter = letter;
    }

    public boolean getVisualMatch(){
        return this.visualMatch;
    }

    public boolean getAudioMatch(){
        return this.audioMatch;
    }

    public Integer getTile(){
        return this.tile;
    }

    public Integer getLetter(){
        return this.letter;
    }


}
