package pt.ubi.pdm.parkeasyapp.notify;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import pt.ubi.pdm.parkeasyapp.R;

public class NotificationUtils {
    public static void ensureChannel(Context ctx){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String id = ctx.getString(R.string.notification_channel_id);
            NotificationChannel ch = new NotificationChannel(id,
                    ctx.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            nm.createNotificationChannel(ch);
        }
    }
    public static void show(Context ctx, String title, String text){
        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, ctx.getString(R.string.notification_channel_id))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);
        NotificationManagerCompat.from(ctx).notify((int)System.currentTimeMillis(), b.build());
    }
}