// SupabaseClient.java
package pt.ubi.pdm.parkeasyapp.api;

import android.content.Context;
import pt.ubi.pdm.parkeasyapp.BuildConfig; // <-- importa isto
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {
    public final Retrofit retrofit;
    public final AuthInterceptor authInterceptor;

    public SupabaseClient(Context ctx){
        String baseUrl = BuildConfig.SUPABASE_URL;           // ✅ vem do build.gradle.kts
        if(!baseUrl.endsWith("/")) baseUrl += "/";
        String anon = BuildConfig.SUPABASE_ANON_KEY;         // ✅ idem

        authInterceptor = new AuthInterceptor(anon);

        HttpLoggingInterceptor log = new HttpLoggingInterceptor();
        log.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient http = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .addInterceptor(log)
                // (opcional) timeouts mais curtos para não “pendurar”
                .connectTimeout(java.time.Duration.ofSeconds(10))
                .readTimeout(java.time.Duration.ofSeconds(15))
                .writeTimeout(java.time.Duration.ofSeconds(15))
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(http)
                .build();
    }
}
