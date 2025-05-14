package be.kuleuven.gt.gamehub;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class TetrisActivity extends AppCompatActivity {

    private TetrisView tetrisview;
    private TetrisPreviewView tetrispreviewview;
    private LinearLayout gameOverScreen;
    private TextView finalScoreText;
    private ImageButton buttonRotate, buttonDown, buttonLeft, buttonRight, buttonReturn;
    private Button buttonHold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tetris);

        Toolbar toolbar = findViewById(R.id.toolbar_tetris);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("GameHub - Tetris");
        }

        tetrisview = findViewById(R.id.tetris_view);
        tetrisview.resume();
        buttonRotate = findViewById(R.id.btnRotate);
        buttonDown = findViewById(R.id.btnDownTetris);
        buttonLeft = findViewById(R.id.btnLeftTetris);
        buttonRight = findViewById(R.id.btnRightTetris);
        buttonHold = findViewById(R.id.btnHold);
        buttonReturn = findViewById(R.id.btnReturnTetris);
        TextView textScore = findViewById(R.id.txtScoreTetris);
        TextView textHighScore = findViewById(R.id.txtHighScoreTetris);
        TetrisPreviewView holdPreview = findViewById(R.id.hold_preview);
        TetrisPreviewView nextPreview = findViewById(R.id.next_preview);

        buttonRotate.setOnClickListener(v -> tetrisview.rotateCurrentPiece());
        buttonDown.setOnClickListener(v -> tetrisview.moveDown());
        buttonLeft.setOnClickListener(v -> tetrisview.moveLeft());
        buttonRight.setOnClickListener(v -> tetrisview.moveRight());
        buttonHold.setOnClickListener(v -> tetrisview.holdPiece());
        buttonReturn.setOnClickListener(v -> {finish();});

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

        gameOverScreen = findViewById(R.id.game_over_screen);
        finalScoreText = findViewById(R.id.final_score_text);

        // Set callback for game over
        tetrisview.setGameOverListener(score -> runOnUiThread(() -> showGameOver(tetrisview.scoreTetris)));

        Button playAgainBtn = findViewById(R.id.btn_play_again);
        playAgainBtn.setOnClickListener(v -> restartGame());

        Button backHomeBtn = findViewById(R.id.btn_back_home);
        backHomeBtn.setOnClickListener(v -> {
            finish();
        });
    }


        private void showGameOver(int score) {
            tetrisview.pause();
            finalScoreText.setText("Final Score: " + score);
            gameOverScreen.setVisibility(View.VISIBLE);
            tetrisview.setVisibility(View.GONE);
            buttonDown.setVisibility(View.GONE);
            buttonRotate.setVisibility(View.GONE);
            buttonLeft.setVisibility(View.GONE);
            buttonRight.setVisibility(View.GONE);
            buttonHold.setVisibility(View.GONE);

            sendScoreToServer(score, 3); // 1 = ID do jogo Tetris
        }

        private void restartGame() {
            gameOverScreen.setVisibility(View.GONE);
            tetrisview.setVisibility(View.VISIBLE);
            buttonDown.setVisibility(View.VISIBLE);
            buttonRotate.setVisibility(View.VISIBLE);
            buttonLeft.setVisibility(View.VISIBLE);
            buttonRight.setVisibility(View.VISIBLE);
            buttonHold.setVisibility(View.VISIBLE);
            tetrisview.resetGame();
            tetrisview.resume();
        }

        private void sendScoreToServer(int score, int gameId) {
            String url = "https://a24pt115.studev.groept.be/save_score.php";
            RequestQueue queue = Volley.newRequestQueue(this);

            JSONObject jsonBody = new JSONObject();
            try {
                int userId = SessionManager.getInstance().getUserId();
                jsonBody.put("userId", userId);
                jsonBody.put("gameId", gameId);
                jsonBody.put("score", score);

                // Adicione um Toast para debug
                Toast.makeText(this, "Enviando: userId=" + userId + ", score=" + score, Toast.LENGTH_SHORT).show();

            } catch (JSONException e) {
                e.printStackTrace();
            }

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> Toast.makeText(this, "Score salvo com sucesso!", Toast.LENGTH_SHORT).show(),
                    error -> Toast.makeText(this, "Erro ao salvar score: " + error.toString(), Toast.LENGTH_LONG).show()
            );

            queue.add(request);
        }


    }