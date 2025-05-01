package be.kuleuven.gt.gamehub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TetrisActivity extends AppCompatActivity {

    private TetrisView tetrisview;
    private LinearLayout gameOverScreen;
    private TextView finalScoreText;
    private Button buttonRotate, buttonDown, buttonLeft, buttonRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tetris);

        tetrisview = findViewById(R.id.tetris_view);
        tetrisview.resume();
        buttonRotate = findViewById(R.id.btnRotate);
        buttonDown = findViewById(R.id.btnDownTetris);
        buttonLeft = findViewById(R.id.btnLeftTetris);
        buttonRight = findViewById(R.id.btnRightTetris);
        TextView textScore = findViewById(R.id.txtScoreTetris);
        TextView textHighScore = findViewById(R.id.txtHighScoreTetris);

        buttonRotate.setOnClickListener(v -> tetrisview.rotateCurrentPiece());
        buttonDown.setOnClickListener(v -> tetrisview.moveDown());
        buttonLeft.setOnClickListener(v -> tetrisview.moveLeft());
        buttonRight.setOnClickListener(v -> tetrisview.moveRight());
        textScore.setText("Score: " + TetrisView.scoreTetris);
        textHighScore.setText("Highscore: " + TetrisView.highscoreTetris);
        tetrisview.setScoreChangeListenerTetris(newScore -> {
            textScore.setText("Score: " + newScore);
            if (newScore > TetrisView.highscoreTetris) {
                TetrisView.highscoreTetris = newScore;
                textHighScore.setText("HighScore: " + newScore);
            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        tetrisview.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tetrisview.resume();
    }

}