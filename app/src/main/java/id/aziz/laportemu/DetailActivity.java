package id.aziz.laportemu;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        
        // Hide ActionBar if present since we have our own back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Get Index
        int index = getIntent().getIntExtra("ITEM_INDEX", -1);
        if (index == -1 || index >= DataStore.barangList.size()) {
            Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Barang barang = DataStore.barangList.get(index);

        // Views
        ImageView imgPhoto = findViewById(R.id.img_detail_photo);
        ImageButton btnBack = findViewById(R.id.btn_back);
        ImageButton btnShare = findViewById(R.id.btn_share);
        
        CardView cvBadge = findViewById(R.id.cv_detail_badge);
        TextView tvStatus = findViewById(R.id.tv_detail_status);
        TextView tvNama = findViewById(R.id.tv_detail_nama);
        TextView tvWaktu = findViewById(R.id.tv_detail_waktu);
        TextView tvLokasi = findViewById(R.id.tv_detail_lokasi);
        TextView tvDeskripsi = findViewById(R.id.tv_detail_deskripsi);
        Button btnHubungi = findViewById(R.id.btn_hubungi);
        Button btnHapus = findViewById(R.id.btn_hapus);

        // Bind Data
        if (barang.getImageUriString() != null) {
            try {
                imgPhoto.setImageURI(Uri.parse(barang.getImageUriString()));
            } catch (Exception e) {
                e.printStackTrace();
                imgPhoto.setImageResource(R.mipmap.ic_launcher);
            }
        } else if (barang.getImageResId() != 0) {
            imgPhoto.setImageResource(barang.getImageResId());
        } else {
            imgPhoto.setImageResource(R.mipmap.ic_launcher);
        }

        tvNama.setText(barang.getNama());
        tvWaktu.setText(barang.getWaktu());
        tvLokasi.setText(barang.getLokasi());
        
        // Deskripsi (if empty, show a placeholder)
        if (barang.getDeskripsi() != null && !barang.getDeskripsi().isEmpty()) {
            tvDeskripsi.setText(barang.getDeskripsi());
        } else {
            tvDeskripsi.setText("Tidak ada deskripsi tambahan.");
        }

        tvStatus.setText(barang.getStatus());
        if ("Hilang".equalsIgnoreCase(barang.getStatus())) {
            cvBadge.setCardBackgroundColor(ContextCompat.getColor(this, R.color.lost_bg));
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.lost_text));
        } else {
            cvBadge.setCardBackgroundColor(ContextCompat.getColor(this, R.color.found_bg));
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.found_text));
        }

        // Actions
        btnBack.setOnClickListener(v -> finish());
        
        btnShare.setOnClickListener(v -> {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Info " + barang.getStatus() + ": " + barang.getNama() + " di " + barang.getLokasi() + ". Hubungi: " + barang.getPhone());
            startActivity(Intent.createChooser(shareIntent, "Bagikan Informasi"));
        });

        btnHubungi.setOnClickListener(v -> {
            String message = "Halo, saya melihat info di aplikasi LaporTemu mengenai barang *" + barang.getNama() + "* " + barang.getStatus() + " di lokasi *" + barang.getLokasi() + "*.\n\n"
                    + "Apakah saya bisa minta info lebih lanjut terkait hal ini?";
            
            String phone = barang.getPhone();
            if (phone != null) {
                if (phone.startsWith("0")) {
                    phone = "62" + phone.substring(1);
                }
                phone = phone.replace("+", "").replace(" ", "").replace("-", "");
            } else {
                phone = "";
            }

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.setPackage("com.whatsapp");
            intent.putExtra(Intent.EXTRA_TEXT, message);
            intent.putExtra("jid", phone + "@s.whatsapp.net");
            
            if (barang.getImageUriString() != null) {
                try {
                    Uri imageUri = Uri.parse(barang.getImageUriString());
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (barang.getImageResId() != 0) {
                try {
                    Uri imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + barang.getImageResId());
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            try {
                startActivity(intent);
            } catch (Exception e) {
                String url = "https://api.whatsapp.com/send?phone=" + phone + "&text=" + Uri.encode(message);
                Intent fallbackIntent = new Intent(Intent.ACTION_VIEW);
                fallbackIntent.setData(Uri.parse(url));
                try {
                    startActivity(fallbackIntent);
                } catch (Exception ex) {
                    Toast.makeText(this, "WhatsApp tidak terinstal", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnHapus.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle("Hapus Laporan")
                .setMessage("Apakah laporan ini sudah selesai dan ingin dihapus dari daftar?")
                .setPositiveButton("Ya, Hapus", (dialog, which) -> {
                    DataStore.barangList.remove(index);
                    DataStore.saveData(this);
                    Toast.makeText(this, "Laporan berhasil dihapus", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Batal", null)
                .show();
        });
    }
}
