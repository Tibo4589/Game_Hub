package be.kuleuven.gt.gamehub;

import android.graphics.drawable.ColorDrawable;
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


import org.json.JSONException;
import org.json.JSONObject;

import be.kuleuven.gt.gamehub.Game2048View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class Game2048Activity extends AppCompatActivity {

    private Game2048View game2048View;
    private LinearLayout gameOverScreen;
    private TextView finalScoreText;
    private ImageButton buttonUp, buttonDown, buttonLeft, buttonRight, buttonReturn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game2048);

        Game2048View.score2048 = 0;

        Toolbar toolbar = findViewById(R.id.toolbar_2048);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle("GameHub - 2048");
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.yellow_2048));

        game2048View = findViewById(R.id.game_2048_view);
        buttonUp = findViewById(R.id.btnUp2048);
        buttonDown = findViewById(R.id.btnDown2048);
        buttonLeft = findViewById(R.id.btnLeft2048);
        buttonRight = findViewById(R.id.btnRight2048);
        buttonReturn = findViewById(R.id.btnReturn2048);
        TextView textScore = findViewById(R.id.txtScore2048);
        TextView textHighScore = findViewById(R.id.txtHighScore2048);

        buttonUp.setOnClickListener(v -> game2048View.moveUp());
        buttonDown.setOnClickListener(v -> game2048View.moveDown());
        buttonLeft.setOnClickListener(v -> game2048View.moveLeft());
        buttonRight.setOnClickListener(v -> game2048View.moveRight());
        buttonReturn.setOnClickListener(v -> {finish();});
        textScore.setText("Score: " + game2048View.score2048);
        textHighScore.setText("Highscore: " + game2048View.highscore2048);
        game2048View.setScoreChangeListener2048(newScore -> {
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

        sendScoreToServer(score, 1); // 1 = ID do jogo 2048
    }

    private void restartGame() {
        gameOverScreen.setVisibility(View.GONE);
        buttonDown.setVisibility(View.VISIBLE);
        buttonUp.setVisibility(View.VISIBLE);
        buttonLeft.setVisibility(View.VISIBLE);
        buttonRight.setVisibility(View.VISIBLE);
        game2048View.clear();
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


}