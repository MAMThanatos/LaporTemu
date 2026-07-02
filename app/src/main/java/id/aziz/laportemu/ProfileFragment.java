package id.aziz.laportemu;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import android.widget.ImageView;

public class ProfileFragment extends Fragment {

    private TextView tvInitial, tvName, tvNim, tvEmail;
    private ImageView ivProfileImage;
    private Button btnLogout, btnEditProfile;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    // Cache to prevent blinking when switching tabs
    private static String cachedNama = null;
    private static String cachedNim = null;
    private static String cachedPhotoBase64 = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvInitial = view.findViewById(R.id.tv_profile_initial);
        ivProfileImage = view.findViewById(R.id.iv_profile_image);
        tvName = view.findViewById(R.id.tv_profile_name);
        tvNim = view.findViewById(R.id.tv_profile_nim);
        tvEmail = view.findViewById(R.id.tv_profile_email);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnEditProfile = view.findViewById(R.id.btn_edit_profile);

        btnLogout.setOnClickListener(v -> logoutUser());
        
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfileActivity.class);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            redirectToLogin();
            return;
        }

        String uid = currentUser.getUid();
        String email = currentUser.getEmail();
        tvEmail.setText("Surel: " + email);

        // Apply cached data immediately to prevent blinking
        if (cachedNama != null) {
            tvName.setText(cachedNama.isEmpty() ? "Pengguna" : cachedNama);
        }
        if (cachedNim != null) {
            tvNim.setText(cachedNim.isEmpty() ? "NIM: Tidak diatur" : "NIM: " + cachedNim);
        }
        if (cachedPhotoBase64 != null && !cachedPhotoBase64.isEmpty()) {
            android.graphics.Bitmap bitmap = ImageUtils.decodeBase64(cachedPhotoBase64);
            if (bitmap != null) {
                ivProfileImage.setVisibility(View.VISIBLE);
                tvInitial.setVisibility(View.GONE);
                Glide.with(getActivity()).load(bitmap).circleCrop().into(ivProfileImage);
            } else {
                showInitial(cachedNama);
            }
        } else if (cachedNama != null) {
            showInitial(cachedNama);
        }

        // Fetch nama, NIM, and photo from Firestore (updates cache silently)
        db.collection("users").document(uid).get()
                .addOnCompleteListener(task -> {
                    if (isAdded()) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                String nama = document.getString("nama");
                                String nim = document.getString("nim");
                                String photoBase64 = document.getString("photoBase64");

                                // Update cache
                                cachedNama = nama;
                                cachedNim = nim;
                                cachedPhotoBase64 = photoBase64;

                                tvName.setText(nama != null && !nama.isEmpty() ? nama : "Pengguna");
                                tvNim.setText(nim != null && !nim.isEmpty() ? "NIM: " + nim : "NIM: Tidak diatur");

                                if (photoBase64 != null && !photoBase64.isEmpty()) {
                                    android.graphics.Bitmap bitmap = ImageUtils.decodeBase64(photoBase64);
                                    if (bitmap != null) {
                                        ivProfileImage.setVisibility(View.VISIBLE);
                                        tvInitial.setVisibility(View.GONE);
                                        Glide.with(getActivity()).load(bitmap).circleCrop().into(ivProfileImage);
                                    } else {
                                        showInitial(nama);
                                    }
                                } else {
                                    showInitial(nama);
                                }
                            } else {
                                tvName.setText("Pengguna");
                                tvNim.setText("NIM: Tidak diatur");
                                tvInitial.setText("P");
                                ivProfileImage.setVisibility(View.GONE);
                                tvInitial.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getContext(), "Gagal memuat profil: " +
                                    (task.getException() != null ? task.getException().getMessage() : "Unknown"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showInitial(String nama) {
        ivProfileImage.setVisibility(View.GONE);
        tvInitial.setVisibility(View.VISIBLE);
        tvInitial.setText(nama != null && !nama.isEmpty() ? nama.substring(0, 1).toUpperCase() : "P");
    }


    private void logoutUser() {
        // Clear cache on logout
        cachedNama = null;
        cachedNim = null;
        cachedPhotoBase64 = null;
        
        mAuth.signOut();
        Toast.makeText(getContext(), "Berhasil keluar akun", Toast.LENGTH_SHORT).show();
        redirectToLogin();
    }

    private void redirectToLogin() {
        if (getActivity() != null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            getActivity().finish();
        }
    }
}
