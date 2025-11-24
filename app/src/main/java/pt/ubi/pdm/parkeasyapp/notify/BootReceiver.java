package pt.ubi.pdm.parkeasyapp.notify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.json.JSONArray;
import org.json.JSONObject;
import pt.ubi.pdm.parkeasyapp.data.LocalCache;

import java.util.concurrent.TimeUnit;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        // Re-agendar rascunhos pendentes após reboot
        LocalCache cache = new LocalCache(context);
        JSONArray drafts = cache.getDrafts();
        long now = System.currentTimeMillis();
        for(int i=0;i<drafts.length();i++){
            JSONObject o = drafts.optJSONObject(i);
            if(o==null) continue;
            long remindAt = o.optLong("remindAt", -1);
            if(remindAt>now){
                AlarmScheduler.scheduleExact(context, remindAt);
            }
        }
    }
}