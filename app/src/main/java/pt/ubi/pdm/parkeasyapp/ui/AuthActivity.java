package pt.ubi.pdm.parkeasyapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import pt.ubi.pdm.parkeasyapp.R;
import pt.ubi.pdm.parkeasyapp.api.AuthService;
import pt.ubi.pdm.parkeasyapp.api.SupabaseClient;
import pt.ubi.pdm.parkeasyapp.api.models.AuthModels;
import pt.ubi.pdm.parkeasyapp.data.LocalCache;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthActivity extends AppCompatActivity {

    private SupabaseClient client;
    private AuthService auth;

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progress;

    @Override protected void onCreate(Bundle savedInstanceState) {
        // redirect se já estiver logado
        LocalCache cache = new LocalCache(this);
        String access = cache.getAccess();
        if (access != null && !access.isEmpty()) {
            startActivity(new Intent(this, MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            finish();
            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_login);

        client = new SupabaseClient(this);
        auth = client.retrofit.create(AuthService.class);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progress = findViewById(R.id.progress);
        TextView tvGoRegister = findViewById(R.id.tvGoRegister);

        btnLogin.setOnClickListener(v -> doLogin());
        tvGoRegister.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
    }

    private void doLogin() {
        tilEmail.setError(null);
        tilPassword.setError(null);

        String email = etEmail.getText() == null ? "" : etEmail.getText().toString().trim();
        String pass  = etPassword.getText() == null ? "" : etPassword.getText().toString();

        boolean ok = true;
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Email inválido");
            ok = false;
        }
        if (pass.length() < 6) {
            tilPassword.setError("Mínimo 6 caracteres");
            ok = false;
        }
        if (!ok) return;

        setLoading(true);
        auth.login("password", new AuthModels.LoginRequest(email, pass))
                .enqueue(new Callback<AuthModels.SessionResponse>() {
                    @Override public void onResponse(Call<AuthModels.SessionResponse> call, Response<AuthModels.SessionResponse> resp) {
                        setLoading(false);
                        if (resp.isSuccessful() && resp.body()!=null) {
                            LocalCache cache = new LocalCache(AuthActivity.this);
                            cache.setTokens(resp.body().access_token, resp.body().refresh_token);
                            if (resp.body().user != null && resp.body().user.id != null) {
                                cache.setUserId(resp.body().user.id);
                            }
                            Intent i = new Intent(AuthActivity.this, MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(i);
                            finish();
                        } else {
                            Toast.makeText(AuthActivity.this, "Credenciais inválidas", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<AuthModels.SessionResponse> call, Throwable t) {
                        setLoading(false);
                        String msg = (t instanceof java.net.UnknownHostException)
                                ? "Sem internet/DNS no dispositivo"
                                : t.getMessage();
                        Toast.makeText(AuthActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean loading){
        btnLogin.setEnabled(!loading);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
