package id.aziz.laportemu;

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
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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
    private StorageReference storageRef;
    private FirebaseUser currentUser;

    private Uri selectedImageUri = null;

    // Launcher for selecting an image from the gallery
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivEditProfileImage.setVisibility(View.VISIBLE);
                    tvEditInitial.setVisibility(View.GONE);
                    Glide.with(this).load(uri).into(ivEditProfileImage);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference();
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

        rlProfileImage.setOnClickListener(v -> {
            pickImageLauncher.launch("image/*");
        });

        btnSaveProfile.setOnClickListener(v -> saveProfile());

        loadCurrentProfile();
    }

    private void loadCurrentProfile() {
        if (currentUser == null) return;
        
        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String nama = document.getString("nama");
                        String nim = document.getString("nim");
                        String photoUrl = document.getString("photoUrl");

                        if (nama != null) {
                            etEditNama.setText(nama);
                            if (!nama.isEmpty()) {
                                tvEditInitial.setText(nama.substring(0, 1).toUpperCase());
                            }
                        }
                        if (nim != null) {
                            etEditNim.setText(nim);
                        }

                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            ivEditProfileImage.setVisibility(View.VISIBLE);
                            tvEditInitial.setVisibility(View.GONE);
                            Glide.with(this).load(photoUrl).into(ivEditProfileImage);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Gagal memuat profil: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveProfile() {
        String nama = etEditNama.getText().toString().trim();
        String nim = etEditNim.getText().toString().trim();

        if (TextUtils.isEmpty(nama)) {
            etEditNama.setError("Nama wajib diisi");
            return;
        }

        if (TextUtils.isEmpty(nim)) {
            etEditNim.setError("NIM wajib diisi");
            return;
        }

        pbEditProfile.setVisibility(View.VISIBLE);
        btnSaveProfile.setEnabled(false);

        if (selectedImageUri != null) {
            // Upload image first
            StorageReference profileImageRef = storageRef.child("profile_images/" + currentUser.getUid() + ".jpg");
            profileImageRef.putFile(selectedImageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            updateFirestore(nama, nim, uri.toString());
                        });
                    })
                    .addOnFailureListener(e -> {
                        pbEditProfile.setVisibility(View.GONE);
                        btnSaveProfile.setEnabled(true);
                        Toast.makeText(this, "Gagal mengunggah foto: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        } else {
            // Update without new image
            updateFirestore(nama, nim, null);
        }
    }

    private void updateFirestore(String nama, String nim, String newPhotoUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("nama", nama);
        updates.put("nim", nim);
        if (newPhotoUrl != null) {
            updates.put("photoUrl", newPhotoUrl);
        }

        db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    pbEditProfile.setVisibility(View.GONE);
                    btnSaveProfile.setEnabled(true);
                    Toast.makeText(this, "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    finish(); // Go back to profile fragment
                })
                .addOnFailureListener(e -> {
                    // Try to set() if update() fails because document doesn't exist
                    db.collection("users").document(currentUser.getUid()).set(updates)
                            .addOnSuccessListener(aVoid -> {
                                pbEditProfile.setVisibility(View.GONE);
                                btnSaveProfile.setEnabled(true);
                                Toast.makeText(this, "Profil berhasil disimpan", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e2 -> {
                                pbEditProfile.setVisibility(View.GONE);
                                btnSaveProfile.setEnabled(true);
                                Toast.makeText(this, "Gagal memperbarui profil: " + e2.getMessage(), Toast.LENGTH_LONG).show();
                            });
                });
    }
}
