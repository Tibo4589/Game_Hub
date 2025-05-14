package be.kuleuven.gt.gamehub;

import android.content.Intent;
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
        startActivity(new Intent(this, PongActivity.class));
    }

    public void open2048(View view) {
        startActivity(new Intent(this, Game2048Activity.class));
    }

    public void openTetris(View view) {
        startActivity(new Intent(this, TetrisActivity.class));
    }

    public void openProfile(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void openStatistics(View view) {
        startActivity(new Intent(this,StatisticsActivity.class));
    }



}