package id.aziz.laportemu;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView btnBack, ivEditProfileImage;
    private TextView tvEditInitial;
    private RelativeLayout rlProfileImage;
    private TextInputEditText etEditNama, etEditNim;
    private Button btnSaveProfile;
    private ProgressBar pbEditProfile;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private Uri selectedImageUri = null;

    // ✅ PickVisualMedia — tidak butuh permission & tidak butuh Firebase Storage
    private final ActivityResultLauncher<PickVisualMediaRequest> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivEditProfileImage.setVisibility(View.VISIBLE);
                    tvEditInitial.setVisibility(View.GONE);
                    Glide.with(this).load(uri).circleCrop().into(ivEditProfileImage);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        btnBack = findViewById(R.id.btn_back);
        ivEditProfileImage = findViewById(R.id.iv_edit_profile_image);
        tvEditInitial = findViewById(R.id.tv_edit_initial);
        rlProfileImage = findViewById(R.id.rl_profile_image);
        etEditNama = findViewById(R.id.et_edit_nama);
        etEditNim = findViewById(R.id.et_edit_nim);
        btnSaveProfile = findViewById(R.id.btn_save_profile);
        pbEditProfile = findViewById(R.id.pb_edit_profile);

        btnBack.setOnClickListener(v -> finish());

        rlProfileImage.setOnClickListener(v ->
                pickImageLauncher.launch(
                        new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                .build()
                )
        );

        btnSaveProfile.setOnClickListener(v -> saveProfile());
        loadCurrentProfile();
    }

    private void loadCurrentProfile() {
        if (currentUser == null) return;

        // Load nama, NIM, and photo from Firestore
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String nama = document.getString("nama");
                        String nim = document.getString("nim");
                        String photoBase64 = document.getString("photoBase64");
                        
                        if (nama != null && !nama.isEmpty()) {
                            etEditNama.setText(nama);
                            tvEditInitial.setText(nama.substring(0, 1).toUpperCase());
                        }
                        if (nim != null) etEditNim.setText(nim);
                        
                        if (photoBase64 != null && !photoBase64.isEmpty()) {
                            Bitmap bitmap = ImageUtils.decodeBase64(photoBase64);
                            if (bitmap != null) {
                                ivEditProfileImage.setVisibility(View.VISIBLE);
                                tvEditInitial.setVisibility(View.GONE);
                                Glide.with(this).load(bitmap).circleCrop().into(ivEditProfileImage);
                            }
                        }
                    }
                });
    }

    private void saveProfile() {
        String nama = etEditNama.getText().toString().trim();
        String nim = etEditNim.getText().toString().trim();

        if (TextUtils.isEmpty(nama)) { etEditNama.setError("Nama wajib diisi"); return; }
        if (TextUtils.isEmpty(nim))  { etEditNim.setError("NIM wajib diisi"); return; }

        pbEditProfile.setVisibility(View.VISIBLE);
        btnSaveProfile.setEnabled(false);

        Map<String, Object> updates = new HashMap<>();
        updates.put("nama", nama);
        updates.put("nim", nim);

        if (selectedImageUri != null) {
            String base64Image = ImageUtils.compressAndEncodeBase64(this, selectedImageUri);
            if (base64Image != null) {
                updates.put("photoBase64", base64Image);
            }
        }

        db.collection("users").document(currentUser.getUid())
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    pbEditProfile.setVisibility(View.GONE);
                    btnSaveProfile.setEnabled(true);
                    Toast.makeText(this, "Profil berhasil disimpan!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    pbEditProfile.setVisibility(View.GONE);
                    btnSaveProfile.setEnabled(true);
                    Toast.makeText(this, "Gagal menyimpan: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
