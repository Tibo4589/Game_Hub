package be.kuleuven.gt.gamehub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class TetrisActivity extends AppCompatActivity {

    private TetrisView tetrisview;
    private TetrisPreviewView tetrispreviewview;
    private LinearLayout gameOverScreen;
    private TextView finalScoreText;
    private Button buttonRotate, buttonDown, buttonLeft, buttonRight, buttonHold;

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
        buttonHold = findViewById(R.id.btnHold);
        TextView textScore = findViewById(R.id.txtScoreTetris);
        TextView textHighScore = findViewById(R.id.txtHighScoreTetris);
        TetrisPreviewView holdPreview = findViewById(R.id.hold_preview);
        TetrisPreviewView nextPreview = findViewById(R.id.next_preview);

        buttonRotate.setOnClickListener(v -> tetrisview.rotateCurrentPiece());
        buttonDown.setOnClickListener(v -> tetrisview.moveDown());
        buttonLeft.setOnClickListener(v -> tetrisview.moveLeft());
        buttonRight.setOnClickListener(v -> tetrisview.moveRight());
        buttonHold.setOnClickListener(v -> tetrisview.holdPiece());

        textScore.setText("Score: " + TetrisView.scoreTetris);
        textHighScore.setText("Highscore: " + TetrisView.highscoreTetris);
        holdPreview.setPiece(tetrisview.getHeldPiece());
        nextPreview.setPiece(tetrisview.getNextPiece());
        tetrisview.setNextPieceChangeListener(nextPreview::setPiece);
        tetrisview.setHeldPieceChangeListener(holdPreview::setPiece);
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