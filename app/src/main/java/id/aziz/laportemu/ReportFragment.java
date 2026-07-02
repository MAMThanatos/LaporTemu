package id.aziz.laportemu;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ReportFragment extends Fragment {

    // Field targets for speech recognition
    private static final int MIC_NAMA      = 1;
    private static final int MIC_LOKASI    = 2;
    private static final int MIC_DESKRIPSI = 3;
    private static final int MIC_WA        = 4;

    private int currentMicTarget = MIC_NAMA;

    private ImageView imgPreview;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> galleryLauncher;
    private Uri imageUri;

    private TextInputEditText etNamaBarang, etLokasiBarang, etDeskripsi, etWhatsapp;
    private AutoCompleteTextView spinnerKategori;
    private View rootView;

    // ─── Speech Recognition Launcher ──────────────────────────────────────────
    private final ActivityResultLauncher<Intent> speechLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() == null) return;
                ArrayList<String> results = result.getData()
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (results != null && !results.isEmpty()) {
                    String text = results.get(0);
                    switch (currentMicTarget) {
                        case MIC_NAMA:      etNamaBarang.setText(text);    break;
                        case MIC_LOKASI:    etLokasiBarang.setText(text);  break;
                        case MIC_DESKRIPSI: etDeskripsi.setText(text);     break;
                        case MIC_WA:        etWhatsapp.setText(text);      break;
                    }
                }
            });

    // ─── Permission Launcher for RECORD_AUDIO ─────────────────────────────────
    private final ActivityResultLauncher<String> audioPerm =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) startSpeechRecognition();
                else Toast.makeText(getContext(), "Izin mikrofon diperlukan.", Toast.LENGTH_SHORT).show();
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_report, container, false);

        imgPreview     = rootView.findViewById(R.id.img_preview);
        imgPreview.setColorFilter(requireContext().getColor(R.color.primary));
        View btnSelectPhoto = rootView.findViewById(R.id.btn_select_photo);
        Button btnSubmit    = rootView.findViewById(R.id.btn_submit);

        etNamaBarang   = rootView.findViewById(R.id.et_nama_barang);
        spinnerKategori = rootView.findViewById(R.id.spinner_kategori);
        etLokasiBarang = rootView.findViewById(R.id.et_lokasi_barang);
        etDeskripsi    = rootView.findViewById(R.id.et_deskripsi);
        etWhatsapp     = rootView.findViewById(R.id.et_whatsapp);

        // ── Spinner setup ────────────────────────────────────────────────────
        String[] categories = {"Kehilangan", "Ditemukan"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, categories);
        spinnerKategori.setAdapter(adapter);

        // ── Camera / Gallery launchers ────────────────────────────────────────
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), result -> {
            if (result) {
                imgPreview.setImageURI(imageUri);
                imgPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imgPreview.clearColorFilter();
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                imageUri = uri;
                imgPreview.setImageURI(uri);
                imgPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imgPreview.clearColorFilter();
                try {
                    requireContext().getContentResolver()
                            .takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } catch (SecurityException ignored) {}
            }
        });

        btnSelectPhoto.setOnClickListener(v -> showImagePickerDialog());
        btnSubmit.setOnClickListener(v -> submitReport());

        // ── Mic buttons ──────────────────────────────────────────────────────
        rootView.findViewById(R.id.btn_mic_nama).setOnClickListener(v -> {
            currentMicTarget = MIC_NAMA;
            checkMicAndSpeak();
        });
        rootView.findViewById(R.id.btn_mic_lokasi).setOnClickListener(v -> {
            currentMicTarget = MIC_LOKASI;
            checkMicAndSpeak();
        });
        rootView.findViewById(R.id.btn_mic_deskripsi).setOnClickListener(v -> {
            currentMicTarget = MIC_DESKRIPSI;
            checkMicAndSpeak();
        });
        rootView.findViewById(R.id.btn_mic_wa).setOnClickListener(v -> {
            currentMicTarget = MIC_WA;
            checkMicAndSpeak();
        });

        return rootView;
    }

    // ─── Speech Recognition ───────────────────────────────────────────────────
    private void checkMicAndSpeak() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) {
            startSpeechRecognition();
        } else {
            audioPerm.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void startSpeechRecognition() {
        String hint;
        switch (currentMicTarget) {
            case MIC_NAMA:      hint = "Sebutkan nama barang...";     break;
            case MIC_LOKASI:    hint = "Sebutkan lokasi kejadian..."; break;
            case MIC_WA:        hint = "Sebutkan nomor WhatsApp...";  break;
            default:            hint = "Jelaskan ciri-ciri barang..."; break;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id-ID");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, hint);

        try {
            speechLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Speech recognition tidak tersedia di perangkat ini.",
                    Toast.LENGTH_SHORT).show();
        }
    }

    // ─── Submit Report ────────────────────────────────────────────────────────
    private void submitReport() {
        String nama     = etNamaBarang.getText().toString().trim();
        String kategori = spinnerKategori.getText().toString().trim();
        String lokasi   = etLokasiBarang.getText().toString().trim();
        String deskripsi = etDeskripsi.getText().toString().trim();
        String wa       = etWhatsapp.getText().toString().trim();

        if (nama.isEmpty() || kategori.isEmpty() || lokasi.isEmpty()
                || deskripsi.isEmpty() || wa.isEmpty()) {
            Toast.makeText(getContext(), "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show();
            return;
        }

        String status = kategori.equals("Kehilangan") ? "Hilang" : "Ditemukan";
        String imageBase64 = null;
        if (imageUri != null) {
            imageBase64 = ImageUtils.compressAndEncodeBase64(requireContext(), imageUri);
        }
        
        String currentUserId = "";
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }
        
        Barang newBarang   = new Barang(nama, lokasi, status, "Baru saja", deskripsi, wa, imageBase64, currentUserId);

        // 🚀 Upload ke Firestore
        FirebaseFirestore.getInstance().collection("reports").add(newBarang)
            .addOnSuccessListener(documentReference -> {
                // 🔔 Push notification (Local)
                NotificationHelper.sendLaporanNotification(requireContext(), nama, status);
                
                // 📡 Broadcast Notifikasi ke Semua HP (Lintas Perangkat)
                FCMHelper.sendNotificationToAll(requireContext(), "Laporan Baru: " + nama, "Seseorang melaporkan barang " + status + " di " + lokasi);

                Toast.makeText(requireContext(), "Laporan Berhasil Disimpan!", Toast.LENGTH_SHORT).show();
                if (rootView != null) {
                    Snackbar.make(rootView, "Laporan \"" + nama + "\" berhasil dikirim!", Snackbar.LENGTH_LONG)
                            .setAction("Lihat", v -> requireActivity().findViewById(R.id.navigation_home).performClick())
                            .setActionTextColor(requireContext().getColor(R.color.secondary))
                            .show();
                }

                // Clear form
                etNamaBarang.setText("");
                spinnerKategori.setText("");
                etLokasiBarang.setText("");
                etDeskripsi.setText("");
                etWhatsapp.setText("");
                imgPreview.setImageResource(android.R.drawable.ic_menu_camera);
                imgPreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                imgPreview.setColorFilter(requireContext().getColor(R.color.primary));
                imageUri = null;

                // Navigate to Home
                requireActivity().findViewById(R.id.navigation_home).performClick();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Gagal mengirim: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    // ─── Image picker ─────────────────────────────────────────────────────────
    private void showImagePickerDialog() {
        String[] options = {getString(R.string.source_camera), getString(R.string.source_gallery)};
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.choose_source)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        try {
                            imageUri = createTempUri();
                            cameraLauncher.launch(imageUri);
                        } catch (IOException e) {
                            Toast.makeText(getContext(), "Gagal menyiapkan kamera", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        galleryLauncher.launch(new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build());
                    }
                }).show();
    }

    private Uri createTempUri() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile("JPEG_" + timeStamp + "_", ".jpg", storageDir);
        return FileProvider.getUriForFile(requireContext(), "id.aziz.laportemu.fileprovider", image);
    }
}