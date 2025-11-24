package pt.ubi.pdm.parkeasyapp.notify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        NotificationUtils.show(context, "Estacionamento a terminar",
                "O tempo pago está a terminar. Verifique o carro.");
    }
}