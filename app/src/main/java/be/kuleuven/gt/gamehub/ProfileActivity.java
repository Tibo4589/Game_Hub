package be.kuleuven.gt.gamehub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);


        //Button btnStatistics = findViewById(R.id.btnStatistics);
        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnAchievements = findViewById(R.id.btnAchievements);
        //Button btnBackHome = findViewById(R.id.btnBackHome);

        //btnStatistics.setOnClickListener(v -> startActivity(new Intent(this, StatisticsActivity.class)));
        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        btnAchievements.setOnClickListener(v -> startActivity(new Intent(this, AchievementsActivity.class)));

        //btnBackHome.setOnClickListener(v -> finish());
    }
    public void openmain(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }
    public void openstatistics(View view) {
        startActivity(new Intent(this, StatisticsActivity.class));
    }
}
