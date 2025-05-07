package be.kuleuven.gt.gamehub;

import android.content.Intent;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StatisticsActivity extends AppCompatActivity {

    private TextView txt2048Today, txt2048Month, txt2048All, txt2048Global;
    private TextView txtPongToday, txtPongMonth, txtPongAll, txtPongGlobal;
    private TextView txtTetrisToday, txtTetrisMonth, txtTetrisAll, txtTetrisGlobal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        // Vincula os TextViews
        txt2048Today = findViewById(R.id.txt2048Today);
        txt2048Month = findViewById(R.id.txt2048Month);
        txt2048All = findViewById(R.id.txt2048All);
        txt2048Global = findViewById(R.id.txt2048Global);

        txtPongToday = findViewById(R.id.txtPongToday);
        txtPongMonth = findViewById(R.id.txtPongMonth);
        txtPongAll = findViewById(R.id.txtPongAll);
        txtPongGlobal = findViewById(R.id.txtPongGlobal);

        txtTetrisToday = findViewById(R.id.txtTetrisToday);
        txtTetrisMonth = findViewById(R.id.txtTetrisMonth);
        txtTetrisAll = findViewById(R.id.txtTetrisAll);
        txtTetrisGlobal = findViewById(R.id.txtTetrisGlobal);


        // Move para baixo 100px
        LinearLayout layout = findViewById(R.id.statistics_layout);
        layout.setPadding(layout.getPaddingLeft(), layout.getPaddingTop() + 100, layout.getPaddingRight(), layout.getPaddingBottom());

        // Botão de voltar para o MainActivity
        Button btnBack = findViewById(R.id.btnBackHome);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(StatisticsActivity.this, ProfileActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();

        });

//        layout.addView(btnBack);

        // Chamada à API
        fetchStatistics();
    }

    private void fetchStatistics() {
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
                                    txt2048Today.setText("Best of today: " + game.getInt("today"));
                                    txt2048Month.setText("Best of the month: " + game.getInt("month"));
                                    txt2048All.setText("Best of all times: " + game.getInt("allTime"));
                                    txt2048Global.setText("Global Best: " + game.getInt("global"));
                                } else if (name.equals("Pong")) {
                                    txtPongToday.setText("Best of today: " + game.getInt("today"));
                                    txtPongMonth.setText("Best of the month: " + game.getInt("month"));
                                    txtPongAll.setText("Best of all times: " + game.getInt("allTime"));
                                    txtPongGlobal.setText("Global Best: " + game.getInt("global"));
                                } else if (name.equals("Tetris")) {
                                    txtTetrisToday.setText("Best of today: " + game.getInt("today"));
                                    txtTetrisMonth.setText("Best of the month: " + game.getInt("month"));
                                    txtTetrisAll.setText("Best of all times: " + game.getInt("allTime"));
                                    txtTetrisGlobal.setText("Global Best: " + game.getInt("global"));
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
