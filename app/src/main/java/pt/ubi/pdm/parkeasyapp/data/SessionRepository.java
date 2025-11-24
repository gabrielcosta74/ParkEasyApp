package pt.ubi.pdm.parkeasyapp.data;

import android.content.Context;
import pt.ubi.pdm.parkeasyapp.api.RestService;
import pt.ubi.pdm.parkeasyapp.api.StorageService;
import pt.ubi.pdm.parkeasyapp.api.models.ParkingSession;
import java.io.File;
import java.io.IOException;
import java.util.List;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class SessionRepository {
    private final RestService rest;
    private final StorageService storage;

    public SessionRepository(RestService rest, StorageService storage){
        this.rest = rest; this.storage = storage;
    }

    public String uploadPhoto(String bucket, String path, File file) throws IOException {
        RequestBody body = RequestBody.create(file, MediaType.parse("image/jpeg"));
        Call<ResponseBody> call = storage.putObject(bucket, path, body, 3600);
        Response<ResponseBody> resp = call.execute();
        if(!resp.isSuccessful()) throw new IOException("Upload falhou: "+resp.code());
        return path;
    }

    public ParkingSession insertSession(ParkingSession s) throws IOException {
        Call<List<ParkingSession>> call = rest.insert(s);
        Response<List<ParkingSession>> resp = call.execute();
        if(!resp.isSuccessful() || resp.body()==null || resp.body().isEmpty())
            throw new IOException("Insert falhou: "+resp.code());
        return resp.body().get(0);
    }

    public List<ParkingSession> last3() throws IOException {
        Call<List<ParkingSession>> call = rest.listLast3("*", "created_at.desc", 3);
        Response<List<ParkingSession>> resp = call.execute();
        if(!resp.isSuccessful() || resp.body()==null) throw new IOException("Erro listagem");
        return resp.body();
    }
}