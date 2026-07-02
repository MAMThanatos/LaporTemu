package id.aziz.laportemu;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private int itemIndex = -1;

    // Launcher untuk menerima hasil dari EditReportActivity
    private final ActivityResultLauncher<Intent> editLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    // Data berubah — reload layar ini
                    recreate();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );

        setContentView(R.layout.activity_detail);

        itemIndex = getIntent().getIntExtra("ITEM_INDEX", -1);
        if (itemIndex == -1 || itemIndex >= DataStore.barangList.size()) {
            Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Barang barang = DataStore.barangList.get(itemIndex);

        // ─── Views ────────────────────────────────────────────────────────────────
        ImageView imgPhoto    = findViewById(R.id.img_detail_photo);
        ImageButton btnBack   = findViewById(R.id.btn_back);
        ImageButton btnShare  = findViewById(R.id.btn_share);
        CardView cvBadge      = findViewById(R.id.cv_detail_badge);
        TextView tvStatus     = findViewById(R.id.tv_detail_status);
        TextView tvNama       = findViewById(R.id.tv_detail_nama);
        TextView tvWaktu      = findViewById(R.id.tv_detail_waktu);
        TextView tvLokasi     = findViewById(R.id.tv_detail_lokasi);
        TextView tvDeskripsi  = findViewById(R.id.tv_detail_deskripsi);
        CardView btnSpeak     = findViewById(R.id.btn_speak);
        Button btnHubungi     = findViewById(R.id.btn_hubungi);
        Button btnEdit        = findViewById(R.id.btn_edit);
        Button btnHapus       = findViewById(R.id.btn_hapus);

        // ─── Bind Data ────────────────────────────────────────────────────────────
        if (barang.getImageBase64() != null && !barang.getImageBase64().isEmpty()) {
            try { 
                android.graphics.Bitmap bmp = ImageUtils.decodeBase64(barang.getImageBase64());
                if (bmp != null) imgPhoto.setImageBitmap(bmp);
                else imgPhoto.setImageResource(R.mipmap.ic_launcher);
            } catch (Exception e) { imgPhoto.setImageResource(R.mipmap.ic_launcher); }
        } else if (barang.getImageResId() != 0) {
            imgPhoto.setImageResource(barang.getImageResId());
        } else {
            imgPhoto.setImageResource(R.mipmap.ic_launcher);
        }

        tvNama.setText(barang.getNama());
        tvWaktu.setText(barang.getRelativeTime());
        tvLokasi.setText(barang.getLokasi());
        tvDeskripsi.setText((barang.getDeskripsi() != null && !barang.getDeskripsi().isEmpty())
                ? barang.getDeskripsi() : "Tidak ada deskripsi tambahan.");

        tvStatus.setText(barang.getStatus());
        if ("Hilang".equalsIgnoreCase(barang.getStatus())) {
            cvBadge.setCardBackgroundColor(ContextCompat.getColor(this, R.color.lost_bg));
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.lost_text));
        } else {
            cvBadge.setCardBackgroundColor(ContextCompat.getColor(this, R.color.found_bg));
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.found_text));
        }

        // ─── TTS Init ─────────────────────────────────────────────────────────────
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(new Locale("id", "ID"));
            }
        });

        // ─── Listeners ────────────────────────────────────────────────────────────
        btnBack.setOnClickListener(v -> finish());

        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT,
                    "Info " + barang.getStatus() + ": " + barang.getNama()
                    + " di " + barang.getLokasi()
                    + ". Hubungi: " + barang.getPhone());
            startActivity(Intent.createChooser(shareIntent, "Bagikan Informasi"));
        });

        // TTS — Bacakan deskripsi barang
        btnSpeak.setOnClickListener(v -> {
            String teks = "Barang " + barang.getStatus() + ". "
                    + "Nama barang: " + barang.getNama() + ". "
                    + "Lokasi: " + barang.getLokasi() + ". "
                    + "Deskripsi: " + tvDeskripsi.getText().toString();
            if (tts != null) {
                tts.speak(teks, TextToSpeech.QUEUE_FLUSH, null, "detail_tts");
                Toast.makeText(this, "🔊 Membacakan informasi...", Toast.LENGTH_SHORT).show();
            }
        });

        // Edit
        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, EditReportActivity.class);
            intent.putExtra("ITEM_INDEX", itemIndex);
            editLauncher.launch(intent);
        });

        // Hapus
        btnHapus.setOnClickListener(v ->
            new AlertDialog.Builder(this)
                .setTitle("Hapus Laporan")
                .setMessage("Laporan ini akan dihapus dari daftar. Lanjutkan?")
                .setPositiveButton("Ya, Hapus", (dialog, which) -> {
                    if (barang.getId() != null) {
                        FirebaseFirestore.getInstance().collection("reports").document(barang.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Laporan berhasil dihapus", Toast.LENGTH_SHORT).show();
                                finish();
                            });
                    } else {
                        // Fallback for local dummy data
                        DataStore.barangList.remove(itemIndex);
                        Toast.makeText(this, "Laporan lokal berhasil dihapus", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Batal", null)
                .show()
        );

        // Hubungi WA
        btnHubungi.setOnClickListener(v -> {
            String message = "Halo, saya melihat info di aplikasi LaporTemu mengenai barang *"
                    + barang.getNama() + "* " + barang.getStatus()
                    + " di lokasi *" + barang.getLokasi() + "*.\n\n"
                    + "Apakah saya bisa minta info lebih lanjut terkait hal ini?";
            String phone = barang.getPhone() != null ? barang.getPhone() : "";
            if (phone.startsWith("0")) phone = "62" + phone.substring(1);
            phone = phone.replace("+", "").replace(" ", "").replace("-", "");

            String url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + Uri.encode(message);
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            } catch (Exception e) {
                Toast.makeText(this, "WhatsApp tidak terinstal", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
