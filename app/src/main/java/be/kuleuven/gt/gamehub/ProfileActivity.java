package be.kuleuven.gt.gamehub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class ProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);

        Toolbar toolbar = findViewById(R.id.toolbar_profile);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("GameHub - Profile");
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));

        Button btnSettings = findViewById(R.id.btnSettings);
        Button btnAchievements = findViewById(R.id.btnAchievements);


        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));
        btnAchievements.setOnClickListener(v -> startActivity(new Intent(this, AchievementsActivity.class)));
    }
    public void openmain(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }
    public void openstatistics(View view) {
        startActivity(new Intent(this, StatisticsActivity.class));
    }
}
