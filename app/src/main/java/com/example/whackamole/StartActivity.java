package com.example.whackamole;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {
    private TextView highScoreTextView, gameDurationTextView;
    private SeekBar durationSeekBar;
    private Button startGameButton;
    private int gameDuration = 60;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        highScoreTextView = findViewById(R.id.highScoreTextView);
        gameDurationTextView = findViewById(R.id.gameDurationTextView);
        durationSeekBar = findViewById(R.id.durationSeekBar);
        startGameButton = findViewById(R.id.startGameButton);

        SharedPreferences prefs = getSharedPreferences("WhackAMole", MODE_PRIVATE);
        int highScore = prefs.getInt("highScore", 0);
        highScoreTextView.setText("High Score: " + highScore);

        durationSeekBar.setMax(110); // 120 sekund - 10 sekund = zakres 110
        durationSeekBar.setProgress(gameDuration - 10);
        gameDurationTextView.setText("Duration: " + gameDuration + " seconds");

        durationSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                gameDuration = progress + 10; // Minimalny czas to 10 sekund
                gameDurationTextView.setText("Duration: " + gameDuration + " seconds");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        startGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(StartActivity.this, MainActivity.class);
            intent.putExtra("gameDuration", gameDuration);
            startActivity(intent);
        });
    }
}
