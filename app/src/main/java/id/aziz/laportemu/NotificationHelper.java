package id.aziz.laportemu;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID   = "lapor_temu_channel";
    private static final String CHANNEL_NAME = "Laporan Barang";
    private static final String CHANNEL_DESC = "Notifikasi laporan barang hilang & temuan";
    private static int notifId = 1000;

    /** Call once at app startup (e.g. MainActivity.onCreate) */
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);
            channel.setShowBadge(true);
            NotificationManager nm = context.getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    /**
     * Show a push notification for a new report.
     * @param namaBarang Name of the reported item
     * @param status     "Hilang" or "Ditemukan"
     */
    public static void sendLaporanNotification(Context context, String namaBarang, String status) {
        // Intent to open MainActivity when tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String emoji  = "Hilang".equalsIgnoreCase(status) ? "🔍" : "✅";
        String title  = emoji + " Laporan " + status + " Dikirim!";
        String body   = "\"" + namaBarang + "\" berhasil dilaporkan. "
                      + "Semoga segera " + ("Hilang".equalsIgnoreCase(status) ? "ditemukan!" : "diklaim pemiliknya!");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_report)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 300, 100, 300});

        NotificationManagerCompat nm = NotificationManagerCompat.from(context);

        // On Android 13+ check POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context,
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return; // Permission not granted yet
            }
        }

        nm.notify(notifId++, builder.build());
    }
}
