package pt.ubi.pdm.parkeasyapp.ui;

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
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private SupabaseClient client;
    private AuthService auth;

    private TextInputLayout tilEmail, tilPassword, tilPassword2;
    private TextInputEditText etEmail, etPassword, etPassword2;
    private MaterialButton btnRegister;
    private ProgressBar progress;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth_register);

        client = new SupabaseClient(this);
        auth = client.retrofit.create(AuthService.class);

        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilPassword2 = findViewById(R.id.tilPassword2);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPassword2 = findViewById(R.id.etPassword2);
        btnRegister = findViewById(R.id.btnRegister);
        progress = findViewById(R.id.progress);
        TextView tvGoLogin = findViewById(R.id.tvGoLogin);

        btnRegister.setOnClickListener(v -> doRegister());
        tvGoLogin.setOnClickListener(v -> finish());
    }

    private void doRegister(){
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilPassword2.setError(null);

        String email = etEmail.getText()==null ? "" : etEmail.getText().toString().trim();
        String p1 = etPassword.getText()==null ? "" : etPassword.getText().toString();
        String p2 = etPassword2.getText()==null ? "" : etPassword2.getText().toString();

        boolean ok = true;
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){ tilEmail.setError("Email inválido"); ok=false; }
        if (p1.length()<6){ tilPassword.setError("Mínimo 6 caracteres"); ok=false; }
        if (!p1.equals(p2)){ tilPassword2.setError("Passwords não coincidem"); ok=false; }
        if(!ok) return;

        setLoading(true);
        auth.signup(new AuthModels.SignupRequest(email, p1))
                .enqueue(new Callback<AuthModels.SessionResponse>() {
                    @Override public void onResponse(Call<AuthModels.SessionResponse> call, Response<AuthModels.SessionResponse> resp) {
                        setLoading(false);
                        if (resp.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Conta criada! Faz login.", Toast.LENGTH_LONG).show();
                            finish(); // volta ao login
                        } else {
                            Toast.makeText(RegisterActivity.this, "Não foi possível criar a conta", Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override public void onFailure(Call<AuthModels.SessionResponse> call, Throwable t) {
                        setLoading(false);
                        String msg = (t instanceof java.net.UnknownHostException)
                                ? "Sem internet/DNS no dispositivo"
                                : t.getMessage();
                        Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setLoading(boolean loading){
        btnRegister.setEnabled(!loading);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }
}
