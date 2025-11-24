package pt.ubi.pdm.parkeasyapp.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import pt.ubi.pdm.parkeasyapp.R;
import pt.ubi.pdm.parkeasyapp.api.models.ParkingSession;
import java.text.SimpleDateFormat;
import java.util.*;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.VH> {
    private final List<ParkingSession> items;
    public SessionAdapter(List<ParkingSession> items){ this.items = items; }

    static class VH extends RecyclerView.ViewHolder{
        ImageView img; TextView date, coords; Button maps;
        VH(@NonNull View v){ super(v); img=v.findViewById(R.id.imgThumb); date=v.findViewById(R.id.tvDate); coords=v.findViewById(R.id.tvCoords); maps=v.findViewById(R.id.btnOpenMaps);} }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int pos) {
        ParkingSession s = items.get(pos);
        Context ctx = h.itemView.getContext();
        if(s.photo_path!=null){
            // Necessita URL assinada; aqui apenas placeholder cinzento
            Glide.with(ctx).load(R.drawable.ic_launcher_background).into(h.img);
        }
        h.date.setText(s.created_at!=null? s.created_at : "");
        h.coords.setText(String.format(Locale.US, "%.5f, %.5f", s.lat, s.lng));
        h.maps.setOnClickListener(v -> {
            String uri = String.format(Locale.US, "geo:%f,%f?q=%f,%f(ParkEasy)", s.lat, s.lng, s.lat, s.lng);
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            ctx.startActivity(i);
        });
    }

    @Override public int getItemCount() { return items==null?0:items.size(); }
}