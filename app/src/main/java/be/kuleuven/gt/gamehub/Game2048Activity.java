package be.kuleuven.gt.gamehub;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class Game2048Activity extends AppCompatActivity {

    private Game2048View game2048View;
    private Game2048Logic game2048Logic;
    private LinearLayout gameOverScreen;
    private TextView finalScoreText;
    private ImageButton buttonUp, buttonDown, buttonLeft, buttonRight, buttonReturn, buttonPause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2048);
        game2048View = findViewById(R.id.game2048_view);

        fetchHighScore();

        SharedPreferences prefs = getSharedPreferences("Game2048", MODE_PRIVATE);
        String savedState = prefs.getString("game_state_2048", null);
        game2048Logic = new Game2048Logic(Game2048View.GRID_SIZE);
        game2048View.setLogic(game2048Logic);
        game2048Logic.setOnGridChangedListener(() -> game2048View.invalidate());

        if (savedState != null) {
            try {
                JSONObject state = new JSONObject(savedState);
                game2048Logic.loadState2048(state);
            } catch (JSONException e) {
                e.printStackTrace();
                game2048Logic.startGame();
            }
        } else {
            game2048Logic.startGame();
            Game2048Logic.score2048 = 0;
        }

        Toolbar toolbar = findViewById(R.id.toolbar_2048);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("GameHub - 2048");
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow_2048));
        toolbar.setTitleTextColor(ContextCompat.getColor(this,R.color.white));

        buttonUp = findViewById(R.id.btnUp2048);
        buttonDown = findViewById(R.id.btnDown2048);
        buttonLeft = findViewById(R.id.btnLeft2048);
        buttonRight = findViewById(R.id.btnRight2048);
        buttonReturn = findViewById(R.id.btnReturn2048);
        buttonPause = findViewById(R.id.btnPause2048);
        TextView textScore = findViewById(R.id.txtScore2048);
        TextView textHighScore = findViewById(R.id.txtHighScore2048);

        buttonUp.setOnClickListener(v -> game2048Logic.move(Game2048Logic.Direction.UP));
        buttonDown.setOnClickListener(v -> game2048Logic.move(Game2048Logic.Direction.DOWN));
        buttonLeft.setOnClickListener(v -> game2048Logic.move(Game2048Logic.Direction.LEFT));
        buttonRight.setOnClickListener(v -> game2048Logic.move(Game2048Logic.Direction.RIGHT));
        buttonReturn.setOnClickListener(v -> {finish();});
        textScore.setText("Score: " + game2048Logic.score2048);
        game2048Logic.setScoreChangeListener2048(newScore -> {
            textScore.setText("Score: " + newScore);
            if (newScore > Game2048Logic.highscore2048) {
                Game2048Logic.highscore2048 = newScore;
                textHighScore.setText("HighScore: " + newScore);
            }
        });

        LinearLayout pauseScreen = findViewById(R.id.pause_screen);
        Button resumeButton = findViewById(R.id.btnResume2048);
        Button buttonRestart = findViewById(R.id.btnRestart2048);

        buttonPause.setOnClickListener(v -> {
            game2048View.pause();
            pauseScreen.setVisibility(View.VISIBLE);
        });

        resumeButton.setOnClickListener(v -> {
            game2048View.resume();
            pauseScreen.setVisibility(View.GONE);
        });

        buttonRestart.setOnClickListener(v -> {
            restartGame();
            pauseScreen.setVisibility(View.GONE);
        });


        gameOverScreen = findViewById(R.id.game_over_screen);
        finalScoreText = findViewById(R.id.final_score_text);

        // Set callback for game over
        game2048Logic.setGameOverListener(score -> runOnUiThread(() -> showGameOver(game2048Logic.score2048)));

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

        sendScoreToServer(score, 1); // 1 = ID do jogo 2048
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            JSONObject state = game2048Logic.saveState2048();
            getSharedPreferences("Game2048", MODE_PRIVATE)
                    .edit()
                    .putString("game_state_2048", state.toString())
                    .apply();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void restartGame() {
        gameOverScreen.setVisibility(View.GONE);
        buttonDown.setVisibility(View.VISIBLE);
        buttonUp.setVisibility(View.VISIBLE);
        buttonLeft.setVisibility(View.VISIBLE);
        buttonRight.setVisibility(View.VISIBLE);

        game2048Logic.startGame();
        game2048View.resume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

                                if (name.equals("2048")) {
                                    int serverHighscore = game.getInt("allTime");
                                    Game2048Logic.highscore2048 = serverHighscore;
                                    TextView textHighScore = findViewById(R.id.txtHighScore2048);
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