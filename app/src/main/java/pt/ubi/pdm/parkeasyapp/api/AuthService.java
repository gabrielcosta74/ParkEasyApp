package pt.ubi.pdm.parkeasyapp.api;

import pt.ubi.pdm.parkeasyapp.api.models.AuthModels;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface AuthService {
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("auth/v1/token")
    Call<AuthModels.SessionResponse> login(@Query("grant_type") String grant,
                                           @Body AuthModels.LoginRequest body);

    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("auth/v1/signup")
    Call<AuthModels.SessionResponse> signup(@Body AuthModels.SignupRequest body);
}