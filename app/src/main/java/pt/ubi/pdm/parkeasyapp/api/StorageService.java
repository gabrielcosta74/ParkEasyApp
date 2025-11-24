package pt.ubi.pdm.parkeasyapp.api;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.HEAD;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface StorageService {
    // PUT direto para path fixo (mais simples que multipart POST)
    @PUT("storage/v1/object/{bucket}/{path}")
    Call<ResponseBody> putObject(@Path("bucket") String bucket,
                                 @Path(value = "path", encoded = true) String path,
                                 @Body RequestBody fileBody,
                                 @Query("cacheControl") Integer cacheControl);

    @HEAD("storage/v1/object/{bucket}/{path}")
    Call<Void> headObject(@Path("bucket") String bucket,
                          @Path(value = "path", encoded = true) String path);
}