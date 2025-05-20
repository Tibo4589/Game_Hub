package be.kuleuven.gt.gamehub;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TetrisActivity extends AppCompatActivity {

    private TetrisView tetrisview;
    private TetrisGameLogic logic;
    private TetrisPreviewView tetrispreviewview;
    private LinearLayout gameOverScreen;
    private TextView finalScoreText;
    private ImageButton buttonRotate, buttonDown, buttonLeft, buttonRight, buttonReturn, buttonPause;
    private Button buttonHold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tetris);

        fetchHighScore();

        Toolbar toolbar = findViewById(R.id.toolbar_tetris);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("GameHub - Tetris");
        toolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.white));


        tetrisview = findViewById(R.id.tetris_view);
        logic = new TetrisGameLogic();
        tetrisview.setLogic(logic);

        SharedPreferences prefs = getSharedPreferences("Tetris", MODE_PRIVATE);
        String savedState = prefs.getString("game_state_tetris", null);

        if (savedState != null) {
            try {
                JSONObject state = new JSONObject(savedState);
                tetrisview.loadStateTetris(state);
            } catch (JSONException e) {
                e.printStackTrace();
                logic.reset();
            }
        } else {
            logic.reset();
        }
        tetrisview.resume();
        buttonRotate = findViewById(R.id.btnRotate);
        buttonDown = findViewById(R.id.btnDownTetris);
        buttonLeft = findViewById(R.id.btnLeftTetris);
        buttonRight = findViewById(R.id.btnRightTetris);
        buttonHold = findViewById(R.id.btnHold);
        buttonReturn = findViewById(R.id.btnReturnTetris);
        buttonPause = findViewById(R.id.btnPauseTetris);
        TextView textScore = findViewById(R.id.txtScoreTetris);
        TextView textHighScore = findViewById(R.id.txtHighScoreTetris);
        TetrisPreviewView holdPreview = findViewById(R.id.hold_preview);
        TetrisPreviewView nextPreview = findViewById(R.id.next_preview);

        buttonRotate.setOnClickListener(v -> logic.rotate());
        buttonDown.setOnClickListener(v -> logic.move(1,0));
        buttonLeft.setOnClickListener(v -> logic.move(0,-1));
        buttonRight.setOnClickListener(v -> logic.move(0,1));
        buttonHold.setOnClickListener(v -> logic.hold());
        buttonReturn.setOnClickListener(v -> {finish();});

        textScore.setText("Score: " + TetrisView.scoreTetris);
        textHighScore.setText("Highscore: " + TetrisView.highscoreTetris);
        int [][] heldPiece = logic.heldPiece;
        holdPreview.setPiece(heldPiece);
        nextPreview.setPiece(logic.nextPiece);
        logic.onNextPieceChange = nextPreview::setPiece;
        logic.onHoldPieceChange = holdPreview::setPiece;
        tetrisview.setScoreChangeListenerTetris(newScore -> {
            textScore.setText("Score: " + newScore);
            if (newScore > TetrisView.highscoreTetris) {
                TetrisView.highscoreTetris = newScore;
                textHighScore.setText("HighScore: " + newScore);
            }
        });

        LinearLayout pauseScreen = findViewById(R.id.pause_screen);
        Button resumeButton = findViewById(R.id.btnResumeTetris);
        Button buttonRestart = findViewById(R.id.btnRestartTetris);

        buttonPause.setOnClickListener(v -> {
            tetrisview.pause();
            pauseScreen.setVisibility(View.VISIBLE);
            tetrisview.setVisibility(View.GONE);
            buttonDown.setVisibility(View.GONE);
            buttonRotate.setVisibility(View.GONE);
            buttonLeft.setVisibility(View.GONE);
            buttonRight.setVisibility(View.GONE);
            buttonHold.setVisibility(View.GONE);
        });

        resumeButton.setOnClickListener(v -> {
            tetrisview.resume();
            pauseScreen.setVisibility(View.GONE);
            tetrisview.setVisibility(View.VISIBLE);
            buttonDown.setVisibility(View.VISIBLE);
            buttonRotate.setVisibility(View.VISIBLE);
            buttonLeft.setVisibility(View.VISIBLE);
            buttonRight.setVisibility(View.VISIBLE);
            buttonHold.setVisibility(View.VISIBLE);
        });

        buttonRestart.setOnClickListener(v -> restartGame());

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

    @Override
    protected void onPause() {
        super.onPause();
        JSONObject state = tetrisview.saveStateTetris();
        getSharedPreferences("Tetris", MODE_PRIVATE)
                .edit()
                .putString("game_state_tetris", state.toString())
                .apply();
    }

        private void restartGame() {
            gameOverScreen.setVisibility(View.GONE);
            tetrisview.setVisibility(View.VISIBLE);
            buttonDown.setVisibility(View.VISIBLE);
            buttonRotate.setVisibility(View.VISIBLE);
            buttonLeft.setVisibility(View.VISIBLE);
            buttonRight.setVisibility(View.VISIBLE);
            buttonHold.setVisibility(View.VISIBLE);
            logic.reset();
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

    private void fetchHighScore() {
        String url = "https://a24pt115.studev.groept.be/get_statistics.php";
        RequestQueue queue = Volley.newRequestQueue(this);

        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("userId", SessionManager.getInstance().getUserId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, requestBody,
                response -> {
                    try {
                        if (response.getString("status").equals("success")) {
                            JSONArray data = response.getJSONArray("data");

                            for (int i = 0; i < data.length(); i++) {
                                JSONObject game = data.getJSONObject(i);
                                String name = game.getString("name");

                                if (name.equals("Tetris")) {
                                    int serverHighscore = game.getInt("allTime");
                                    TetrisView.highscoreTetris = serverHighscore;
                                    TextView textHighScore = findViewById(R.id.txtHighScoreTetris);
                                    textHighScore.setText("Highscore: " + serverHighscore);
                                }
                            }
                        } else {
                            Toast.makeText(this, "Error loading statistics", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "JSON parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Error: " + error.toString(), Toast.LENGTH_SHORT).show());

        queue.add(request);
    }
}