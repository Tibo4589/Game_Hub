package be.kuleuven.gt.gamehub;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

public class PongActivity extends AppCompatActivity {

    private PongView pongView;
    private LinearLayout gameOverScreen;
    private TextView finalScoreText, textScorePong, textHighScorePong;
    private ImageButton buttonReturn, buttonPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pong);
        pongView = findViewById(R.id.pong_view);

        fetchHighScore();

        Toolbar toolbar = findViewById(R.id.toolbar_pong);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("GameHub - Pong");
        toolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.black));
        pongView.post(() -> {
                    String savedStateJson = getIntent().getStringExtra("saved_game_state");
                    if (savedStateJson != null) {
                        try {
                            JSONObject state = new JSONObject(savedStateJson);
                            pongView.loadStatePong(state);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        String savedPrefsState = getSharedPreferences("Pong", MODE_PRIVATE)
                                .getString("game_state_pong", null);
                        if (savedPrefsState != null) {
                            try {
                                JSONObject state = new JSONObject(savedPrefsState);
                                pongView.loadStatePong(state);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            pongView.resetGame();
                        }
                    }
                });

        pongView.resume();

        gameOverScreen = findViewById(R.id.game_over_screen);
        finalScoreText = findViewById(R.id.final_score_text);
        buttonReturn = findViewById(R.id.btnReturnPong);
        buttonPause = findViewById(R.id.btnPausePong);
        textScorePong = findViewById(R.id.txtScorePong);
        textHighScorePong = findViewById(R.id.txtHighScorePong);
        textScorePong.setText("Score: " + pongView.score);
        pongView.setScoreChangeListenerPong(newScore -> {
            textScorePong.setText("Score: " + newScore);
            if (newScore > PongView.highscore) {
                PongView.highscore = newScore;
                textHighScorePong.setText("HighScore: " + newScore);
            }
        });

        LinearLayout pauseScreen = findViewById(R.id.pause_screen);
        Button buttonResume = findViewById(R.id.btnResumePong);
        Button buttonRestart = findViewById(R.id.btnRestartPong);

        buttonPause.setOnClickListener(v -> {
            pongView.pause();
            pauseScreen.setVisibility(View.VISIBLE);
        });

        buttonResume.setOnClickListener(v -> {
            pongView.resume();
            pauseScreen.setVisibility(View.GONE);
        });
        buttonRestart.setOnClickListener(v -> {
            restartGame();
            pauseScreen.setVisibility(View.GONE);
        });

        buttonReturn.setOnClickListener(v -> {finish();});

        pongView.setGameOverListener(score -> runOnUiThread(() -> showGameOver(score)));

        Button playAgainBtn = findViewById(R.id.btn_play_again);
        playAgainBtn.setOnClickListener(v -> restartGame());

        Button backHomeBtn = findViewById(R.id.btn_back_home);
        backHomeBtn.setOnClickListener(v -> {
            finish();
        });
    }

    private void showGameOver(int score) {
        pongView.pause();
        finalScoreText.setText("Final Score: " + score);
        gameOverScreen.setVisibility(View.VISIBLE);

        sendScoreToServer(score, 2); // 2 = ID do jogo Pong
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
        JSONObject state = pongView.saveStatePong();
        getSharedPreferences("Pong", MODE_PRIVATE)
                .edit()
                .putString("game_state_pong", state.toString())
                .apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        pongView.resume();
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
            Toast.makeText(this, "Saving score: " + score, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> Toast.makeText(this, "Score saved successfully!", Toast.LENGTH_SHORT).show(),
                error -> {
                    error.printStackTrace(); // Adiciona esse log para ver no Logcat
                    Toast.makeText(this, "Error saving score: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }

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

                                if (name.equals("Pong")) {
                                    int serverHighscore = game.getInt("allTime");
                                    PongView.highscore = serverHighscore;
                                    TextView textHighScore = findViewById(R.id.txtHighScorePong);
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
