package pt.ubi.pdm.parkeasyapp.api;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    private volatile String accessToken = null;
    private final String anonKey;

    public AuthInterceptor(String anonKey){ this.anonKey = anonKey; }

    public void setAccessToken(String token){ this.accessToken = token; }

    @Override public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder b = original.newBuilder()
                .header("apikey", anonKey)
                .header("X-Client-Info", "parkeasy-android-java");
        if(accessToken != null){ b.header("Authorization", "Bearer "+accessToken); }
        return chain.proceed(b.build());
    }
}