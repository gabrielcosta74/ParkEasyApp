package pt.ubi.pdm.parkeasyapp.notify;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import pt.ubi.pdm.parkeasyapp.ui.MainActivity;

public class AlarmScheduler {

    private static PendingIntent contentIntent(Context ctx) {
        Intent open = new Intent(ctx, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(
                ctx, 9999, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    public static void scheduleExact(Context ctx, long triggerAtMillis){
        AlarmManager am = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(ctx, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(
                ctx,
                (int) (triggerAtMillis % Integer.MAX_VALUE),
                i,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        try {
            if (Build.VERSION.SDK_INT >= 31) {
                // Se não pode agendar exatos, faz fallback inexact + dica ao utilizador
                if (am != null && !am.canScheduleExactAlarms()) {
                    if (am != null) am.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
                    // (Opcional) abrir settings para o user autorizar:
                    // ctx.startActivity(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    return;
                }
                // Tenta alarme exato visível (não deve exigir permissão na maioria dos devices)
                AlarmManager.AlarmClockInfo info =
                        new AlarmManager.AlarmClockInfo(triggerAtMillis, contentIntent(ctx));
                if (am != null) am.setAlarmClock(info, pi);
            } else if (Build.VERSION.SDK_INT >= 23) {
                if (am != null) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
            } else {
                if (am != null) am.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
            }
        } catch (SecurityException se) {
            // Último fallback para não crashar
            if (am != null) am.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi);
        }
    }
}
