package be.kuleuven.gt.gamehub;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("GameHub - Home");
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));
    }

    public void openPong(View view) {
        int userId = SessionManager.getInstance().getUserId();
        String saveKey = "pong_state_" + userId;
        SharedPreferences prefs = getSharedPreferences("Pong", MODE_PRIVATE);
        String savedState = prefs.getString(saveKey, null);

        Intent intent = new Intent(this, PongActivity.class);
        if (savedState != null) {
            intent.putExtra("saved_game_state", savedState);
        }
        startActivity(intent);
    }

    public void open2048(View view) {
        SharedPreferences prefs = getSharedPreferences("Game2048", MODE_PRIVATE);
        String savedState = prefs.getString("game_state_2048", null);

        Intent intent = new Intent(this, Game2048Activity.class);
        if (savedState != null) {
            intent.putExtra("saved_game_state", savedState);
        }
        startActivity(intent);
    }

    public void openTetris(View view) {
        SharedPreferences prefs = getSharedPreferences("Tetris", MODE_PRIVATE);
        String savedState = prefs.getString("game_state_tetris", null);

        Intent intent = new Intent(this, TetrisActivity.class);
        if (savedState != null) {
            intent.putExtra("saved_game_state", savedState);
        }
        startActivity(intent);
    }
    public void openSnake(View view) {
        SharedPreferences prefs = getSharedPreferences("Snake", MODE_PRIVATE);
        String savedState = prefs.getString("game_state_snake", null);

        Intent intent = new Intent(this, SnakeActivity.class);
        if (savedState != null) {
            intent.putExtra("saved_game_state", savedState);
        }
        startActivity(intent);
    }

    public void openProfile(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void openStatistics(View view) {
        startActivity(new Intent(this,StatisticsActivity.class));
    }



}