package id.aziz.laportemu;

import android.content.Context;
import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.Executors;

public class FCMHelper {
    private static final String TAG = "FCMHelper";

    public static void sendNotificationToAll(Context context, String title, String body) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // 1. Baca service_account.json dari folder assets untuk mendapatkan project_id
                InputStream is = context.getAssets().open("service_account.json");
                int size = is.available();
                byte[] buffer = new byte[size];
                is.read(buffer);
                is.close();
                String jsonStr = new String(buffer, "UTF-8");
                JSONObject accountObj = new JSONObject(jsonStr);
                String projectId = accountObj.getString("project_id");

                // 2. Buat Token OAuth2 menggunakan Google Auth Library
                InputStream credentialsStream = context.getAssets().open("service_account.json");
                GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                        .createScoped(Collections.singletonList("https://www.googleapis.com/auth/firebase.messaging"));
                credentials.refreshIfExpired();
                String accessToken = credentials.getAccessToken().getTokenValue();

                // 3. Susun data JSON untuk server Firebase Cloud Messaging
                JSONObject messageObj = new JSONObject();
                JSONObject messageContent = new JSONObject();
                messageContent.put("topic", "all_reports");

                JSONObject notificationObj = new JSONObject();
                notificationObj.put("title", title);
                notificationObj.put("body", body);

                messageContent.put("notification", notificationObj);
                messageObj.put("message", messageContent);

                // 4. Tembakkan HTTP POST Request ke Google API
                URL url = new URL("https://fcm.googleapis.com/v1/projects/" + projectId + "/messages:send");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Authorization", "Bearer " + accessToken);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                os.write(messageObj.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "FCM Broadcast Response Code: " + responseCode);
                
                if (responseCode != 200) {
                    InputStream errorStream = conn.getErrorStream();
                    if(errorStream != null) {
                        int errSize = errorStream.available();
                        byte[] errBuffer = new byte[errSize];
                        errorStream.read(errBuffer);
                        Log.e(TAG, "FCM Error: " + new String(errBuffer, "UTF-8"));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Gagal mengirim Push Notification", e);
            }
        });
    }
}
