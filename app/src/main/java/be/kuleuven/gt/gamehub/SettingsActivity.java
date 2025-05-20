package be.kuleuven.gt.gamehub;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

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

import org.json.JSONException;
import org.json.JSONObject;


public class SettingsActivity extends AppCompatActivity {

    private boolean isDarkTheme = false; // Simples toggle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Corrige problemas de sobreposição com o header
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar_settings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("GameHub - Settings");
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white));

        // Corrige o layout para não ficar por trás do topo da tela
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnChangeTheme = findViewById(R.id.btnChangeTheme);
        Button btnLogout = findViewById(R.id.btnLogout);
        Button btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        btnChangeTheme.setOnClickListener(v -> {
            isDarkTheme = !isDarkTheme;
            String theme = isDarkTheme ? "Dark" : "Light";
            Toast.makeText(this, "Theme changed to " + theme, Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> {
            SessionManager.getInstance().logout();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        btnDeleteAccount.setOnClickListener(v -> {
            int userId = SessionManager.getInstance().getUserId();
            String url = "https://a24pt115.studev.groept.be/delete_account.php";

            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("userId", userId);
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, "Erro ao preparar requisição", Toast.LENGTH_SHORT).show();
                return;
            }

            RequestQueue queue = Volley.newRequestQueue(this);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                    response -> {
                        try {
                            if (response.getString("status").equals("success")) {
                                Toast.makeText(this, "Conta deletada com sucesso!", Toast.LENGTH_LONG).show();
                                SessionManager.getInstance().logout();
                                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                Toast.makeText(this, "Erro ao deletar conta", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(this, "Erro ao processar resposta", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                    },
                    error -> {
                        Toast.makeText(this, "Erro de rede: " + error.toString(), Toast.LENGTH_LONG).show();
                    });

            queue.add(request);
        });
    }
}
