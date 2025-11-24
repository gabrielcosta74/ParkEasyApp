package pt.ubi.pdm.parkeasyapp.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import pt.ubi.pdm.parkeasyapp.R;
import pt.ubi.pdm.parkeasyapp.api.RestService;
import pt.ubi.pdm.parkeasyapp.api.StorageService;
import pt.ubi.pdm.parkeasyapp.api.SupabaseClient;
import pt.ubi.pdm.parkeasyapp.api.models.ParkingSession;
import pt.ubi.pdm.parkeasyapp.data.LocalCache;
import pt.ubi.pdm.parkeasyapp.data.SessionRepository;
import pt.ubi.pdm.parkeasyapp.location.LocationHelper;
import pt.ubi.pdm.parkeasyapp.notify.AlarmScheduler;
import pt.ubi.pdm.parkeasyapp.notify.NotificationUtils;
import pt.ubi.pdm.parkeasyapp.util.PhotoHelper;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private EditText etLat, etLng, etCustom, etOffset, etNotes;
    private ImageView img;
    private RecyclerView rv;

    private Uri photoUri = null;
    private File photoFile = null;

    private LocationHelper loc;
    private SessionRepository repo;
    private SupabaseClient client;

    // ---- Launchers / permissões ----
    private final ActivityResultLauncher<String> permNotif =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {});

    private final ActivityResultLauncher<String[]> permLoc =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), r -> {});

    // Permissão da câmara (pede e, se concedido, volta a abrir a câmara)
    private final ActivityResultLauncher<String> cameraPermLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) openCamera();
                else Toast.makeText(this, "Permissão da câmara negada", Toast.LENGTH_LONG).show();
            });

    // Captura direta de foto para um URI do FileProvider
    private final ActivityResultLauncher<Uri> takePictureLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
                if (success && photoFile != null) {
                    Glide.with(this).load(photoFile).into(img);
                } else {
                    Toast.makeText(this, "Foto cancelada", Toast.LENGTH_SHORT).show();
                }
            });

    // Escolher imagem da galeria
    private final ActivityResultLauncher<String> pickImage =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    photoUri = uri;
                    photoFile = null;
                    Glide.with(this).load(uri).into(img);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ Gate de autenticação
        String access = new LocalCache(this).getAccess();
        if (access == null || access.isEmpty()) {
            Intent i = new Intent(this, AuthActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // Canal de notificações
        NotificationUtils.ensureChannel(this);
        if (Build.VERSION.SDK_INT >= 33) {
            permNotif.launch(Manifest.permission.POST_NOTIFICATIONS);
        }

        // Supabase + repo
        client = new SupabaseClient(this);
        client.authInterceptor.setAccessToken(access);
        Retrofit retrofit = client.retrofit;
        repo = new SessionRepository(retrofit.create(RestService.class), retrofit.create(StorageService.class));

        // Views
        img = findViewById(R.id.imgPhoto);
        etLat = findViewById(R.id.etLat);
        etLng = findViewById(R.id.etLng);
        etCustom = findViewById(R.id.etCustomMinutes);
        etOffset = findViewById(R.id.etOffset);
        etNotes = findViewById(R.id.etNotes);
        rv = findViewById(R.id.rvHistory);
        rv.setLayoutManager(new LinearLayoutManager(this));

        // Localização
        loc = new LocationHelper(this);

        // Clicks
        findViewById(R.id.btnTakePhoto).setOnClickListener(v -> openCamera());
        findViewById(R.id.btnPickPhoto).setOnClickListener(v -> pickImage.launch("image/*"));
        findViewById(R.id.btnUseCurrent).setOnClickListener(v -> useCurrentLocation());
        findViewById(R.id.btn30).setOnClickListener(v -> etCustom.setText("30"));
        findViewById(R.id.btn60).setOnClickListener(v -> etCustom.setText("60"));
        findViewById(R.id.btn120).setOnClickListener(v -> etCustom.setText("120"));
        findViewById(R.id.btnSave).setOnClickListener(v -> saveSession());

        // Logout
        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            new LocalCache(this).clear(); // apaga tokens / drafts / userId
            Intent i = new Intent(this, AuthActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });

        requestLocationPerms();
        loadHistory();
    }

    private void requestLocationPerms() {
        permLoc.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }

    private void openCamera() {
        // 1) pedir permissão da câmara se necessário
        if (Build.VERSION.SDK_INT >= 23 &&
                checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            cameraPermLauncher.launch(Manifest.permission.CAMERA);
            return;
        }

        // 2) criar ficheiro temporário em cache + content:// Uri via FileProvider
        photoFile = PhotoHelper.createTempImageFile(this);
        photoUri = PhotoHelper.getUriForFile(this, photoFile);

        // 3) lançar captura direta (o sistema grava no URI)
        takePictureLauncher.launch(photoUri);
    }

    private void useCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestLocationPerms();
            return;
        }
        loc.getLastLocation(new LocationHelper.Callback() {
            @Override
            public void onLocation(Location l) {
                etLat.setText(String.valueOf(l.getLatitude()));
                etLng.setText(String.valueOf(l.getLongitude()));
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(MainActivity.this, "Sem GPS", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveSession() {
        String sLat = etLat.getText().toString().trim();
        String sLng = etLng.getText().toString().trim();
        String sMin = etCustom.getText().toString().trim();
        String sOff = etOffset.getText().toString().trim();

        if (sLat.isEmpty() || sLng.isEmpty() || sMin.isEmpty()) {
            Toast.makeText(this, "Preencha localização e minutos", Toast.LENGTH_SHORT).show();
            return;
        }

        double lat = Double.parseDouble(sLat);
        double lng = Double.parseDouble(sLng);
        int minutes = Integer.parseInt(sMin);
        int offset = sOff.isEmpty() ? 5 : Integer.parseInt(sOff);

        long now = System.currentTimeMillis();
        long expires = now + minutes * 60_000L;
        long remindAt = expires - offset * 60_000L;

        // 1) Upload foto (se houver)
        String photoPath = null;
        try {
            String userId = new LocalCache(this).getUserId();
            if (userId == null) {
                Toast.makeText(this, "Sessão inválida. Faça login novamente.", Toast.LENGTH_LONG).show();
                return;
            }

            if (photoFile != null) {
                String path = userId + "/" + UUID.randomUUID() + ".jpg";  // path começa por user_id (RLS)
                photoPath = repo.uploadPhoto("parking-photos", path, photoFile);
            } else if (photoUri != null) {
                // Copiar o conteúdo do URI para ficheiro temporário antes de enviar
                File tmp = PhotoHelper.createTempImageFile(this);
                try (java.io.InputStream in = getContentResolver().openInputStream(photoUri);
                     java.io.FileOutputStream out = new java.io.FileOutputStream(tmp)) {
                    byte[] buf = new byte[8192]; int n;
                    while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
                }
                String path = userId + "/" + UUID.randomUUID() + ".jpg";
                photoPath = repo.uploadPhoto("parking-photos", path, tmp);
            }
        } catch (Exception e) {
            Toast.makeText(this, "Falha upload foto (seguimos sem foto)", Toast.LENGTH_SHORT).show();
        }

        // 2) Inserir sessão no Supabase
        ParkingSession ps = new ParkingSession();
        ps.lat = lat;
        ps.lng = lng;
        ps.reminder_offset_minutes = offset;
        ps.photo_path = photoPath;
        ps.notes = etNotes.getText().toString();
        ps.expires_at = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US)
                .format(new Date(expires));

        final ParkingSession psFinal = ps;
        final String photoPathFinal = photoPath;
        final long expiresFinal = expires;
        final long remindAtFinal = remindAt;

        new Thread(() -> {
            boolean ok = true;

            try {
                repo.insertSession(psFinal);
            } catch (Exception e) {
                ok = false;
            }

            // 3) Agendar notificação local
            AlarmScheduler.scheduleExact(MainActivity.this, remindAtFinal);

            // 4) Guardar draft offline se falhar
            if (!ok) {
                try {
                    org.json.JSONObject obj = new org.json.JSONObject();
                    obj.put("lat", lat);
                    obj.put("lng", lng);
                    obj.put("expires", expiresFinal);
                    obj.put("offset", offset);
                    obj.put("notes", psFinal.notes);
                    obj.put("remindAt", remindAtFinal);
                    if (photoPathFinal != null) obj.put("photo_path", photoPathFinal);
                    new LocalCache(MainActivity.this).saveSessionDraft(obj);
                } catch (Exception ignored) {}
            }

            final boolean okFinal = ok;
            runOnUiThread(() -> {
                if (okFinal)
                    Toast.makeText(MainActivity.this, "Sessão guardada", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(MainActivity.this, "Guardado offline; sincroniza depois", Toast.LENGTH_LONG).show();
                loadHistory();
            });
        }).start();
    }

    // --- HISTÓRICO: busca as últimas 3 sessões e atualiza a RecyclerView ---
    private void loadHistory() {
        new Thread(() -> {
            try {
                final List<ParkingSession> list = repo.last3();
                runOnUiThread(() -> rv.setAdapter(new SessionAdapter(list)));
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Sem histórico (offline?)", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}
