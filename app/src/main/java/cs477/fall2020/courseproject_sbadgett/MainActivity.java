package cs477.fall2020.courseproject_sbadgett;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    Button play, progress, settings, how;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        play = (Button)findViewById(R.id.button3);
        progress = (Button)findViewById(R.id.button4);
        settings = (Button)findViewById(R.id.button5);
        how = (Button)findViewById(R.id.how);

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), Game.class);
                startActivityForResult(intent, 1);
            }
        });
        progress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), Progress.class);
                startActivityForResult(intent, 1);
            }
        });
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), Settings.class);
                startActivityForResult(intent, 1);
            }
        });
        how.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), HowToPlay.class);
                startActivityForResult(intent, 1);
            }
        });

    }
}