package pt.ubi.pdm.parkeasyapp.api;

import pt.ubi.pdm.parkeasyapp.api.models.ParkingSession;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface RestService {
    @Headers({"Accept: application/json", "Content-Type: application/json"})
    @POST("rest/v1/parking_sessions")
    Call<List<ParkingSession>> insert(@Body ParkingSession body);

    @GET("rest/v1/parking_sessions")
    Call<List<ParkingSession>> listLast3(
            @Query("select") String select,
            @Query("order") String order,
            @Query("limit") int limit
    );
}