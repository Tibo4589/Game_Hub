package be.kuleuven.gt.gamehub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class PongActivity extends AppCompatActivity {

    private PongView pongView;
    private LinearLayout gameOverScreen;
    private TextView finalScoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pong);

        pongView = findViewById(R.id.pong_view);
        gameOverScreen = findViewById(R.id.game_over_screen);
        finalScoreText = findViewById(R.id.final_score_text);

        // Set callback for game over
        pongView.setGameOverListener(score -> runOnUiThread(() -> showGameOver(score)));

        Button playAgainBtn = findViewById(R.id.btn_play_again);
        playAgainBtn.setOnClickListener(v -> restartGame());

        Button backHomeBtn = findViewById(R.id.btn_back_home);
        backHomeBtn.setOnClickListener(v -> {
            finish(); // go back to MainActivity
        });
    }

    private void showGameOver(int score) {
        pongView.pause();
        finalScoreText.setText("Final Score: " + score);
        gameOverScreen.setVisibility(View.VISIBLE);
    }

    private void restartGame() {
        gameOverScreen.setVisibility(View.GONE);
        pongView.resetGame();
        pongView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pongView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameOverScreen.getVisibility() == View.GONE) {
        }
    }
}