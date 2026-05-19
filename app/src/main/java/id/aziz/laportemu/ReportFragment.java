package id.aziz.laportemu;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;

import android.content.Intent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ReportFragment extends Fragment {

    private ImageView imgPreview;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> galleryLauncher;
    private Uri imageUri;

    private TextInputEditText etNamaBarang;
    private AutoCompleteTextView spinnerKategori;
    private TextInputEditText etLokasiBarang;
    private TextInputEditText etDeskripsi;
    private TextInputEditText etWhatsapp;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report, container, false);

        imgPreview = view.findViewById(R.id.img_preview);
        imgPreview.setColorFilter(getResources().getColor(R.color.primary));
        View btnSelectPhoto = view.findViewById(R.id.btn_select_photo);
        Button btnSubmit = view.findViewById(R.id.btn_submit);

        etNamaBarang = view.findViewById(R.id.et_nama_barang);
        spinnerKategori = view.findViewById(R.id.spinner_kategori);
        etLokasiBarang = view.findViewById(R.id.et_lokasi_barang);
        etDeskripsi = view.findViewById(R.id.et_deskripsi);
        etWhatsapp = view.findViewById(R.id.et_whatsapp);

        // Setup Spinner
        String[] categories = new String[]{"Kehilangan", "Ditemukan"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, categories);
        spinnerKategori.setAdapter(adapter);

        // Initialize Launchers
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

                // Take persistable URI permission
                int takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                try {
                    requireContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
            }
        });

        btnSelectPhoto.setOnClickListener(v -> showImagePickerDialog());

        btnSubmit.setOnClickListener(v -> submitReport());

        return view;
    }

    private void submitReport() {
        String nama = etNamaBarang.getText().toString().trim();
        String kategori = spinnerKategori.getText().toString().trim();
        String lokasi = etLokasiBarang.getText().toString().trim();
        String deskripsi = etDeskripsi.getText().toString().trim();
        String wa = etWhatsapp.getText().toString().trim();

        if (nama.isEmpty() || kategori.isEmpty() || lokasi.isEmpty() || deskripsi.isEmpty() || wa.isEmpty()) {
            Toast.makeText(getContext(), "Mohon lengkapi semua data", Toast.LENGTH_SHORT).show();
            return;
        }

        String status = kategori.equals("Kehilangan") ? "Hilang" : "Ditemukan";
        String waktu = "Baru saja";

        String imageUriStr = imageUri != null ? imageUri.toString() : null;
        Barang newBarang = new Barang(nama, lokasi, status, waktu, deskripsi, wa, imageUriStr);
        
        // Add to top of the list
        DataStore.barangList.add(0, newBarang);
        DataStore.saveData(getContext());

        Toast.makeText(getContext(), R.string.report_success, Toast.LENGTH_SHORT).show();

        // Clear Form
        etNamaBarang.setText("");
        spinnerKategori.setText("");
        etLokasiBarang.setText("");
        etDeskripsi.setText("");
        etWhatsapp.setText("");
        imgPreview.setImageResource(android.R.drawable.ic_menu_camera);
        imgPreview.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        imgPreview.setColorFilter(getResources().getColor(R.color.primary));
        imageUri = null;

        // Navigate to Home
        requireActivity().findViewById(R.id.navigation_home).performClick();
    }

    private void showImagePickerDialog() {
        String[] options = {getString(R.string.source_camera), getString(R.string.source_gallery)};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(R.string.choose_source);
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Camera
                try {
                    imageUri = createTempUri();
                    cameraLauncher.launch(imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "Gagal menyiapkan kamera", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Gallery
                galleryLauncher.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });
        builder.show();
    }

    private Uri createTempUri() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        
        return FileProvider.getUriForFile(getContext(), 
                "id.aziz.laportemu.fileprovider", 
                image);
    }
}