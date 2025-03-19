package com.example.whackamole;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;
import android.graphics.RenderEffect;
import android.graphics.Shader;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private FrameLayout[] holes;
    private Set<View> moles;
    private Handler handler;
    private Runnable moleRunnable, timerRunnable;
    private TextView scoreTextView, targetColorTextView, timerTextView;
    private int score = 0;
    private int targetColor;
    private boolean gameRunning = true;
    private int moleSpeed = 1000;
    private int timeLeft;
    private boolean doublePointsActive = false;
    private boolean negativePointsActive = false;
    private boolean screenShakeActive = false;
    private boolean blurEffectActive = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeLeft = getIntent().getIntExtra("gameDuration", 60);
        timerTextView = findViewById(R.id.timerTextView);
        timerTextView.setText(String.format("Time: %02d", timeLeft));

        scoreTextView = findViewById(R.id.scoreTextView);
        targetColorTextView = findViewById(R.id.targetColorTextView);
        timerTextView = findViewById(R.id.timerTextView);

        holes = new FrameLayout[16];
        for (int i = 0; i < 16; i++) {
            String holeId = "hole" + (i + 1);
            int resID = getResources().getIdentifier(holeId, "id", getPackageName());
            holes[i] = findViewById(resID);
        }

        moles = new HashSet<>();
        handler = new Handler();

        setTargetColor();
        startGame();
        startTimer();
    }

    private void setTargetColor() {
        int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};
        targetColor = colors[new Random().nextInt(colors.length)];
        targetColorTextView.setTextColor(targetColor);
        targetColorTextView.setText("Target Color");
    }

    private void startGame() {
        moleRunnable = new Runnable() {
            @Override
            public void run() {
                if (!gameRunning) return;

                maintainFiveMoles();
                handler.postDelayed(this, moleSpeed);
            }
        };
        handler.post(moleRunnable);
    }

    private void startTimer() {
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                if (!gameRunning) return;

                if (timeLeft > 0) {
                    timeLeft--;
                    timerTextView.setText(String.format("Time: %02d", timeLeft));
                    handler.postDelayed(this, 1000);
                } else {
                    endGame();
                }
            }
        };
        handler.post(timerRunnable);
    }

    private void maintainFiveMoles() {
        while (moles.size() < 5) {
            if (new Random().nextFloat() < 0.2) { // 20% szans
                addRandomElixir();
            } else {
                addRandomMole();
            }
        }
    }

    private void addRandomElixir() {
        int randomHole = new Random().nextInt(holes.length);
        FrameLayout selectedHole = holes[randomHole];

        if (selectedHole.getChildCount() > 0) return;

        int elixirType = new Random().nextInt(4);
        int[] elixirColors = {Color.MAGENTA, Color.CYAN, Color.LTGRAY, Color.YELLOW};

        ImageView elixir = new ImageView(this);
        elixir.setImageResource(R.drawable.elixir);
        elixir.setColorFilter(elixirColors[elixirType]);

        elixir.setTag("elixir_" + elixirType);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        elixir.setLayoutParams(params);
        elixir.setOnClickListener(v -> onElixirHit(elixir, elixirType));
        selectedHole.addView(elixir);
        moles.add(elixir);

        int elixirLifetime = 1000 + new Random().nextInt(2000); // Czas życia eliksiru (1-3 sekundy)
        handler.postDelayed(() -> removeMole(elixir), elixirLifetime);
    }

    private void onElixirHit(View elixir, int elixirType) {
        switch (elixirType) {
            case 0:
                activateDoublePoints();
                break;
            case 1:
                activateNegativePoints();
                break;
            case 2:
                activateScreenShake();
                break;
            case 3:
                activateBlurEffect();
                break;
        }
        removeMole(elixir);
    }

    private void activateDoublePoints() {
        if (doublePointsActive) return;
        doublePointsActive = true;
        Toast.makeText(this, "Double Points Active!", Toast.LENGTH_SHORT).show();

        handler.postDelayed(() -> {
            doublePointsActive = false;
            Toast.makeText(this, "Double Points Ended!", Toast.LENGTH_SHORT).show();
        }, 5000);
    }

    private void activateNegativePoints() {
        if (negativePointsActive) return;
        negativePointsActive = true;
        Toast.makeText(this, "Negative Points Active!", Toast.LENGTH_SHORT).show();

        handler.postDelayed(() -> {
            negativePointsActive = false;
            Toast.makeText(this, "Negative Points Ended!", Toast.LENGTH_SHORT).show();
        }, 5000);
    }

    private void activateScreenShake() {
        if (screenShakeActive) return;
        screenShakeActive = true;
        Toast.makeText(this, "Screen Shaking!", Toast.LENGTH_SHORT).show();

        final Runnable shakeRunnable = new Runnable() {
            @Override
            public void run() {
                if (!screenShakeActive) return;
                findViewById(R.id.gameBoard).setTranslationX(new Random().nextInt(10) - 5);
                findViewById(R.id.gameBoard).setTranslationY(new Random().nextInt(10) - 5);
                handler.postDelayed(this, 50);
            }
        };
        handler.post(shakeRunnable);

        handler.postDelayed(() -> {
            screenShakeActive = false;
            findViewById(R.id.gameBoard).setTranslationX(0);
            findViewById(R.id.gameBoard).setTranslationY(0);
            Toast.makeText(this, "Screen Shake Ended!", Toast.LENGTH_SHORT).show();
        }, 5000);
    }

    private void activateBlurEffect() {
        if (blurEffectActive) return;
        blurEffectActive = true;
        Toast.makeText(this, "Blur Effect Active!", Toast.LENGTH_SHORT).show();

        View gameBoard = findViewById(R.id.gameBoard);
        gameBoard.setRenderEffect(RenderEffect.createBlurEffect(100f, 100f, Shader.TileMode.CLAMP));

        handler.postDelayed(() -> {
            blurEffectActive = false;
            gameBoard.setRenderEffect(null);
            Toast.makeText(this, "Blur Effect Ended!", Toast.LENGTH_SHORT).show();
        }, 5000);
    }

    private void addRandomMole() {
        int randomHole = new Random().nextInt(holes.length);
        FrameLayout selectedHole = holes[randomHole];

        if (selectedHole.getChildCount() > 0) return;

        int[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW};
        int moleColor = colors[new Random().nextInt(colors.length)];

        ImageView mole = new ImageView(this);
        mole.setImageResource(R.drawable.mole);
        mole.setColorFilter(moleColor);
        mole.setTag(moleColor);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        mole.setLayoutParams(params);
        mole.setOnClickListener(v -> onMoleHit(mole));
        selectedHole.addView(mole);
        moles.add(mole);

        mole.setTranslationY(selectedHole.getHeight());

        ObjectAnimator animator = ObjectAnimator.ofFloat(mole, "translationY", selectedHole.getHeight(), 0);
        animator.setDuration(600);
        animator.start();

        int moleLifetime = 1000 + new Random().nextInt(2000);
        handler.postDelayed(() -> removeMole(mole), moleLifetime);
    }

    private void removeMole(View mole) {
        if (!moles.contains(mole)) return;

        ObjectAnimator animator = ObjectAnimator.ofFloat(mole, "translationY", 0, mole.getHeight());
        animator.setDuration(600);
        animator.start();

        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                FrameLayout parent = (FrameLayout) mole.getParent();
                if (parent != null) parent.removeView(mole);
                moles.remove(mole);
            }

            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationCancel(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });
    }

    private void onMoleHit(View mole) {
        int moleColor = (int) mole.getTag();
        int pointsChange = moleColor == targetColor ? 1 : -1;

        if (doublePointsActive) pointsChange *= 2;

        if (pointsChange == -1 && negativePointsActive == true) {
            pointsChange *= 2;
        }
        else if (pointsChange == 1 && negativePointsActive == true) {
            pointsChange *= 1;
        }

        score += pointsChange;
        scoreTextView.setText("Score: " + score);

        if (moleColor == targetColor) setTargetColor();
        else Toast.makeText(this, "Wrong Mole!", Toast.LENGTH_SHORT).show();

        removeMole(mole);
    }

    private void endGame() {
        gameRunning = false;
        handler.removeCallbacks(moleRunnable);
        handler.removeCallbacks(timerRunnable);

        SharedPreferences prefs = getSharedPreferences("WhackAMole", MODE_PRIVATE);
        int highScore = prefs.getInt("highScore", 0);
        if (score > highScore) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("highScore", score);
            editor.apply();
            Toast.makeText(this, "New High Score!", Toast.LENGTH_LONG).show();
        }

        Toast.makeText(this, "Final Score: " + score, Toast.LENGTH_LONG).show();
        finish();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gameRunning = false;
        handler.removeCallbacks(moleRunnable);
        handler.removeCallbacks(timerRunnable);
    }
}
