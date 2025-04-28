package be.kuleuven.gt.gamehub;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import be.kuleuven.gt.gamehub.Game2048View;

import androidx.appcompat.app.AppCompatActivity;

public class Game2048Activity extends AppCompatActivity {

    private Game2048View game2048View;
    private LinearLayout gameOverScreen;
    private TextView finalScoreText;
    private Button buttonUp, buttonDown, buttonLeft, buttonRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2048);

        game2048View = findViewById(R.id.game_2048_view);
        buttonUp = findViewById(R.id.btnUp);
        buttonDown = findViewById(R.id.btnDown);
        buttonLeft = findViewById(R.id.btnLeft);
        buttonRight = findViewById(R.id.btnRight);
        TextView textScore = findViewById(R.id.txtScore2048);
        TextView textHighScore = findViewById(R.id.txtHighScore2048);

        buttonUp.setOnClickListener(v -> game2048View.moveUp());
        buttonDown.setOnClickListener(v -> game2048View.moveDown());
        buttonLeft.setOnClickListener(v -> game2048View.moveLeft());
        buttonRight.setOnClickListener(v -> game2048View.moveRight());
        textScore.setText("Score: " + Game2048View.score2048);
        textHighScore.setText("Highscore: " + Game2048View.highscore2048);
        game2048View.setScoreChangeListener(newScore -> {
            textScore.setText("Score: " + newScore);
            if (newScore > Game2048View.highscore2048) {
                Game2048View.highscore2048 = newScore;
                textHighScore.setText("HighScore: " + newScore);
            }
        });


        gameOverScreen = findViewById(R.id.game_over_screen);
        finalScoreText = findViewById(R.id.final_score_text);

        // Set callback for game over
        game2048View.setGameOverListener(score -> runOnUiThread(() -> showGameOver(game2048View.score2048)));

        Button playAgainBtn = findViewById(R.id.btn_play_again);
        playAgainBtn.setOnClickListener(v -> restartGame());

        Button backHomeBtn = findViewById(R.id.btn_back_home);
        backHomeBtn.setOnClickListener(v -> {finish();});
    }

    private void showGameOver(int score) {
        game2048View.pause();
        finalScoreText.setText("Final Score: " + score);
        gameOverScreen.setVisibility(View.VISIBLE);
        buttonDown.setVisibility(View.GONE);
        buttonUp.setVisibility(View.GONE);
        buttonLeft.setVisibility(View.GONE);
        buttonRight.setVisibility(View.GONE);
    }

    private void restartGame() {
        gameOverScreen.setVisibility(View.GONE);
        buttonDown.setVisibility(View.VISIBLE);
        buttonUp.setVisibility(View.VISIBLE);
        buttonLeft.setVisibility(View.VISIBLE);
        buttonRight.setVisibility(View.VISIBLE);
        game2048View.clear();
        game2048View.init();
        game2048View.resume();
    }
}