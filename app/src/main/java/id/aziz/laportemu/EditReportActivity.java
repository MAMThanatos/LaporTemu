package id.aziz.laportemu;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;


public class EditReportActivity extends AppCompatActivity {

    private int itemIndex = -1;
    private Barang original;

    private AutoCompleteTextView spinnerKategori;
    private TextInputEditText etNama, etLokasi, etDeskripsi, etWhatsapp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_report);

        itemIndex = getIntent().getIntExtra("ITEM_INDEX", -1);
        if (itemIndex == -1 || itemIndex >= DataStore.barangList.size()) {
            Toast.makeText(this, "Data tidak ditemukan", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        original = DataStore.barangList.get(itemIndex);

        // Views
        ImageView btnBack = findViewById(R.id.btn_back_edit);
        spinnerKategori   = findViewById(R.id.spinner_edit_kategori);
        etNama            = findViewById(R.id.et_edit_nama_barang);
        etLokasi          = findViewById(R.id.et_edit_lokasi);
        etDeskripsi       = findViewById(R.id.et_edit_deskripsi);
        etWhatsapp        = findViewById(R.id.et_edit_whatsapp);
        Button btnSave    = findViewById(R.id.btn_save_edit);

        // Dropdown
        String[] options = {"Hilang", "Ditemukan"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, options);
        spinnerKategori.setAdapter(adapter);

        // Pre-fill existing data
        spinnerKategori.setText(original.getStatus(), false);
        etNama.setText(original.getNama());
        etLokasi.setText(original.getLokasi());
        etDeskripsi.setText(original.getDeskripsi());
        etWhatsapp.setText(original.getPhone());

        // Back
        btnBack.setOnClickListener(v -> finish());

        // Save
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        String status    = spinnerKategori.getText().toString().trim();
        String nama      = etNama.getText() != null ? etNama.getText().toString().trim() : "";
        String lokasi    = etLokasi.getText() != null ? etLokasi.getText().toString().trim() : "";
        String deskripsi = etDeskripsi.getText() != null ? etDeskripsi.getText().toString().trim() : "";
        String phone     = etWhatsapp.getText() != null ? etWhatsapp.getText().toString().trim() : "";

        if (nama.isEmpty() || lokasi.isEmpty() || status.isEmpty()) {
            Toast.makeText(this, "Nama, Lokasi, dan Jenis Laporan wajib diisi!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Build updated Barang (preserve existing image + timestamp)
        Barang updated = new Barang(nama, lokasi, status, original.getWaktu(), deskripsi, phone, original.getImageBase64());
        updated.setId(original.getId());
        // Preserve original timestamp so time display stays accurate
        updated.setTimestamp(original.getTimestamp());

        if (original.getId() != null) {
            FirebaseFirestore.getInstance().collection("reports").document(original.getId())
                .set(updated)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "✅ Laporan berhasil diperbarui di Cloud!", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                });
        } else {
            DataStore.barangList.set(itemIndex, updated);
            Toast.makeText(this, "✅ Laporan lokal diperbarui!", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        }
    }
}
