package be.kuleuven.gt.gamehub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AchievementsActivity extends AppCompatActivity {

    private TextView achievementsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Ativa edge-to-edge corretamente
        setContentView(R.layout.activity_achievements);

        Toolbar toolbar = findViewById(R.id.toolbar_achievements);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("GameHub - Achievements");
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));

        // Aplica padding para evitar sobreposição com status bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.scrollView), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        achievementsText = findViewById(R.id.achievementsText);

        fetchAchievements();
    }
    public void openProfile(View view) {
        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    public void openMain(View view) {
        startActivity(new Intent(this,MainActivity.class));
    }

    private void fetchAchievements() {
        String url = "https://a24pt115.studev.groept.be/get_achievements.php";
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
                            JSONArray array = response.getJSONArray("achievements");
                            StringBuilder builder = new StringBuilder();
                            for (int i = 0; i < array.length(); i++) {
                                builder.append("🏆 ").append(array.getString(i)).append("\n\n");
                            }
                            achievementsText.setText(builder.toString());
                        } else {
                            achievementsText.setText("Failed to load achievements");
                        }
                    } catch (JSONException e) {
                        achievementsText.setText("Parsing error");
                        e.printStackTrace();
                    }
                },
                error -> achievementsText.setText("Network error: " + error.toString())
        );

        queue.add(request);
    }
}
