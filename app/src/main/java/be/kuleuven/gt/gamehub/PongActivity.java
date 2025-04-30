package be.kuleuven.gt.gamehub;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameOverScreen.getVisibility() == View.GONE) {
            // jogo continua
        }
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
}
