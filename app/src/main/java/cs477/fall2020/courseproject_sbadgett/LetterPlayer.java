package cs477.fall2020.courseproject_sbadgett;

import android.content.Context;
import android.media.MediaPlayer;

/*
    The LetterPlayer is a class used to play the letters for a round of Dual N-Back
 */
public class LetterPlayer {
    private Context context;
    private MediaPlayer mediaPlayer;

    /*
        It is constructed using the context of the calling activity
     */
    public LetterPlayer(Context context){
        mediaPlayer = null;
        this.context = context;
    }

    //When playLetter is called, a mediaPlayer for the appropriate letter file is created and played
    public void playLetter(int i){
        switch(i) {
            case 1: mediaPlayer = MediaPlayer.create(context, R.raw.letter1k);
            break;
            case 2: mediaPlayer = MediaPlayer.create(context, R.raw.letter2m);
            break;
            case 3: mediaPlayer = MediaPlayer.create(context, R.raw.letter3s);
            break;
            case 4: mediaPlayer = MediaPlayer.create(context, R.raw.letter4q);
            break;
            case 5: mediaPlayer = MediaPlayer.create(context, R.raw.letter5r);
            break;
            case 6: mediaPlayer = MediaPlayer.create(context, R.raw.letter6l);
            break;
            case 7: mediaPlayer = MediaPlayer.create(context, R.raw.letter7f);
            break;
            default: mediaPlayer = MediaPlayer.create(context, R.raw.letter8g);
            break;
        }
        //Set the OnCompletionoListener so that the created mediaPlayer releases when it is done
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.release();
            }
        });
        //Start the mediaPlayer and play the letter
        mediaPlayer.start();
    }
}
