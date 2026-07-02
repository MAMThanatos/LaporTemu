package id.aziz.laportemu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ImageUtils {

    /**
     * Mengambil gambar dari URI, mengecilkannya (max 800px), mengompres (JPEG 60%),
     * lalu mengubahnya menjadi format string Base64.
     */
    public static String compressAndEncodeBase64(Context context, Uri imageUri) {
        if (imageUri == null) return null;
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);
            if (originalBitmap == null) return null;

            // Resize gambar agar tidak membebani Firestore
            int maxDim = 800;
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();
            float ratio = (float) width / (float) height;
            
            if (width > maxDim || height > maxDim) {
                if (ratio > 1) {
                    width = maxDim;
                    height = (int) (maxDim / ratio);
                } else {
                    height = maxDim;
                    width = (int) (maxDim * ratio);
                }
            }
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, width, height, true);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // Kompresi JPEG ke kualitas 60%
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream);
            byte[] imageBytes = outputStream.toByteArray();
            
            return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Mengubah format string Base64 kembali menjadi gambar Bitmap untuk ditampilkan.
     */
    public static Bitmap decodeBase64(String base64Str) {
        if (base64Str == null || base64Str.isEmpty()) return null;
        try {
            byte[] decodedBytes = Base64.decode(base64Str, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
