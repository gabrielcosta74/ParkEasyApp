package pt.ubi.pdm.parkeasyapp.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Task;

public class LocationHelper {
    private final FusedLocationProviderClient client;
    public LocationHelper(Context ctx){ client = LocationServices.getFusedLocationProviderClient(ctx); }

    @SuppressLint("MissingPermission")
    public interface Callback { void onLocation(Location l); void onError(Exception e); }

    @SuppressLint("MissingPermission")
    public void getLastLocation(Callback cb){
        Task<Location> t = client.getLastLocation();
        t.addOnSuccessListener(location -> {
            if(location!=null) cb.onLocation(location);
            else cb.onError(new Exception("Sem localização"));
        }).addOnFailureListener(cb::onError);
    }
}