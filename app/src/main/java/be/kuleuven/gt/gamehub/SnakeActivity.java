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

public class SnakeActivity extends AppCompatActivity {
    private SnakeView snakeview;
    private LinearLayout gameOverScreen;
    private TextView finalScoreText;
    private ImageButton buttonDown, buttonLeft, buttonRight, buttonUp, buttonReturn, buttonPause;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_snake);

        snakeview = findViewById(R.id.snakeView);
        fetchHighScore();

        Toolbar toolbar = findViewById(R.id.toolbar_snake);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("GameHub - Snake");
        toolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.white));

        String savedJson = getSharedPreferences("Snake", MODE_PRIVATE).getString(snakeview.saveKey, null);
        if (savedJson != null) {
            try {
                JSONObject state = new JSONObject(savedJson);
                snakeview.loadStateSnake(state);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        buttonDown = findViewById(R.id.btnDownSnake);
        buttonLeft = findViewById(R.id.btnLeftSnake);
        buttonRight = findViewById(R.id.btnRightSnake);
        buttonUp = findViewById(R.id.btnUpSnake);
        buttonReturn = findViewById(R.id.btnReturnSnake);
        buttonPause = findViewById(R.id.btnPauseSnake);

        buttonRight.setOnClickListener(v -> snakeview.moveright());
        buttonDown.setOnClickListener(v -> snakeview.movedown());
        buttonLeft.setOnClickListener(v -> snakeview.moveleft());
        buttonUp.setOnClickListener(v -> snakeview.moveup());
        buttonReturn.setOnClickListener(v -> {finish();});
        TextView textScore = findViewById(R.id.txtScoreSnake);
        TextView textHighScore = findViewById(R.id.txtHighScoreSnake);
        snakeview.setScoreChangeListenerSnake(newScore -> {
            textScore.setText("Score: " + newScore);
            if (newScore > snakeview.highscoresnake) {
                snakeview.highscoresnake = newScore;
                textHighScore.setText("HighScore: " + newScore);
            }
        });

        LinearLayout pauseScreen = findViewById(R.id.pause_screen);
        Button resumeButton = findViewById(R.id.btnResumeSnake);
        Button buttonRestart = findViewById(R.id.btnRestartSnake);

        buttonPause.setOnClickListener(v -> {
            snakeview.pause();
            pauseScreen.setVisibility(View.VISIBLE);
        });

        resumeButton.setOnClickListener(v -> {
            snakeview.resume();
            pauseScreen.setVisibility(View.GONE);
        });

        buttonRestart.setOnClickListener(v -> {
            restartGame();
            pauseScreen.setVisibility(View.GONE);
        });

        gameOverScreen = findViewById(R.id.game_over_screen);
        finalScoreText = findViewById(R.id.final_score_text);

        // Set callback for game over
        snakeview.setGameOverListener(score -> runOnUiThread(() -> showGameOver(snakeview.scoresnake)));

        Button playAgainBtn = findViewById(R.id.btn_play_again);
        playAgainBtn.setOnClickListener(v -> restartGame());

        Button backHomeBtn = findViewById(R.id.btn_back_home);
        backHomeBtn.setOnClickListener(v -> {
            finish();
        });
    }
    private void showGameOver(int score) {
        snakeview.pause();
        finalScoreText.setText("Final Score: " + score);
        gameOverScreen.setVisibility(View.VISIBLE);
        buttonDown.setVisibility(View.GONE);
        buttonUp.setVisibility(View.GONE);
        buttonLeft.setVisibility(View.GONE);
        buttonRight.setVisibility(View.GONE);

        sendScoreToServer(score, 4); // 4 = ID do jogo Snake
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (snakeview != null) {
            JSONObject state = snakeview.saveStateSnake();
            getSharedPreferences("Snake", MODE_PRIVATE).edit()
                    .putString(snakeview.saveKey, state.toString())
                    .apply();
        }
    }
    private void restartGame() {
        gameOverScreen.setVisibility(View.GONE);
        buttonDown.setVisibility(View.VISIBLE);
        buttonUp.setVisibility(View.VISIBLE);
        buttonLeft.setVisibility(View.VISIBLE);
        buttonRight.setVisibility(View.VISIBLE);
        snakeview.restart();
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

                                if (name.equals("Snake")) {
                                    int serverHighscore = game.getInt("allTime");
                                    SnakeView.highscoresnake = serverHighscore;
                                    TextView textHighScore = findViewById(R.id.txtHighScoreSnake);
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

