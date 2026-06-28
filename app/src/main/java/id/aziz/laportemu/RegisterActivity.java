package id.aziz.laportemu;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etNama, etNim, etEmail, etPassword;
    private Button btnRegister;
    private TextView tvBtnLogin;
    private ProgressBar registerProgress;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etNama = findViewById(R.id.et_register_nama);
        etNim = findViewById(R.id.et_register_nim);
        etEmail = findViewById(R.id.et_register_email);
        etPassword = findViewById(R.id.et_register_password);
        btnRegister = findViewById(R.id.btn_register);
        tvBtnLogin = findViewById(R.id.tv_btn_login);
        registerProgress = findViewById(R.id.register_progress);

        btnRegister.setOnClickListener(v -> registerUser());

        tvBtnLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private void registerUser() {
        String nama = etNama.getText().toString().trim();
        String nim = etNim.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(nama)) {
            etNama.setError("Nama wajib diisi");
            return;
        }

        if (TextUtils.isEmpty(nim)) {
            etNim.setError("NIM wajib diisi");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email wajib diisi");
            return;
        }

        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password minimal 6 karakter");
            return;
        }

        registerProgress.setVisibility(View.VISIBLE);
        btnRegister.setVisibility(View.GONE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        String uid = mAuth.getCurrentUser().getUid();
                        saveUserToFirestore(uid, nama, nim, email);
                    } else {
                        registerProgress.setVisibility(View.GONE);
                        btnRegister.setVisibility(View.VISIBLE);
                        String errMsg = task.getException() != null ? task.getException().getMessage() : "Registrasi gagal";
                        Toast.makeText(RegisterActivity.this, "Error: " + errMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String uid, String nama, String nim, String email) {
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("nama", nama);
        userMap.put("nim", nim);
        userMap.put("email", email);

        db.collection("users").document(uid)
                .set(userMap)
                .addOnSuccessListener(aVoid -> {
                    registerProgress.setVisibility(View.GONE);
                    btnRegister.setVisibility(View.VISIBLE);
                    Toast.makeText(RegisterActivity.this, "Akun Berhasil Dibuat!", Toast.LENGTH_SHORT).show();
                    
                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    registerProgress.setVisibility(View.GONE);
                    btnRegister.setVisibility(View.VISIBLE);
                    Toast.makeText(RegisterActivity.this, "Gagal menyimpan data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
