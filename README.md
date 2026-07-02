# LaporTemu 🔍

<div align="center">
  <img src="app/src/main/res/drawable/ic_laportemu.png" alt="LaporTemu Logo" width="150" />
</div>

**LaporTemu** adalah aplikasi Android modern yang dirancang untuk membantu masyarakat atau mahasiswa melaporkan barang hilang (*Lost*) dan barang yang ditemukan (*Found*). Dengan dukungan sinkronisasi *real-time*, pengguna dapat dengan cepat saling terhubung dan mengembalikan barang ke pemilik aslinya.

## 📥 Unduh & Instal (Coba Langsung!)
Anda tidak perlu repot-repot melakukan *build* dari Android Studio untuk mencoba aplikasi ini. 
Silakan unduh file APK-nya dan pasang langsung di HP Android Anda:

> **[👉 UNDUH LAPORTEMU.APK DI SINI](https://github.com/MAMThanatos/LaporTemu/raw/main/LaporTemu.apk)** 📱

*(Pastikan Anda mengizinkan instalasi dari "Sumber tidak dikenal / Unknown sources" di pengaturan HP Anda).*

## ✨ Fitur Utama
* **Pelaporan Real-time**: Didukung oleh Firebase Cloud Firestore, semua laporan barang hilang maupun ditemukan akan muncul secara instan di layar pengguna lain tanpa perlu memuat ulang (*refresh*) halaman.
* **Keamanan & Kepemilikan**: Setiap laporan dikunci dengan *User ID*. Pengguna hanya dapat mengedit dan menghapus laporan yang mereka buat sendiri.
* **Autentikasi Pengguna**: Sistem Login dan Pendaftaran aman yang ditenagai oleh Firebase Authentication.
* **Manajemen Profil Terintegrasi**: Pengguna dapat mengatur Nama, NIM, serta foto profil yang tersimpan secara *real-time* dan didukung dengan sistem *Cache* pintar agar transisi halaman sangat mulus tanpa *loading/blink*.
* **Input Berbasis Suara (Speech-to-Text)**: Malas mengetik? Isi formulir pelaporan (Nama barang, Lokasi, Deskripsi) dengan mudah menggunakan fitur pengenalan suara Google.
* **Text-to-Speech (TTS) Pintar**: Dukungan aksesibilitas di mana aplikasi bisa membacakan detail informasi laporan barang secara lisan.
* **Integrasi WhatsApp Langsung**: Temukan barang Anda? Hubungi sang penemu secara instan melalui WhatsApp dengan satu kali klik.

## 🛠️ Teknologi & Stack
* **Bahasa Pemrograman**: Java
* **UI/UX**: Material Design Components (MDC) & Custom Drawables
* **Backend as a Service (BaaS)**: 
  * Firebase Authentication
  * Firebase Cloud Firestore
* **Image Processing**: Glide & Base64 Compression/Encoding
* **Hardware APIs**: SpeechRecognizer API, TextToSpeech API, Camera/Gallery Intent

## 🚀 Persiapan & Cara Menjalankan
1. *Clone* repositori ini ke komputer Anda:
   ```bash
   git clone https://github.com/username/LaporTemu.git
   ```
2. Buka *folder* proyek menggunakan **Android Studio**.
3. Hubungkan aplikasi dengan proyek **Firebase** milik Anda:
   - Buat proyek di [Firebase Console](https://console.firebase.google.com/).
   - Daftarkan aplikasi Android dan unduh file `google-services.json`.
   - Letakkan file `google-services.json` ke dalam direktori `app/`.
   - Aktifkan fitur **Firestore Database** (atur *Rules* menjadi *Test Mode*).
   - Aktifkan fitur **Authentication** (pilih penyedia *Email/Password*).
4. Tekan tombol **Run (▶)** (atau `Shift + F10`) di Android Studio untuk memasang aplikasi ke *Emulator* atau *Smartphone* Android Anda.

---
Dibuat dengan ❤️ untuk mengembalikan senyuman mereka yang kehilangan.
