package id.aziz.laportemu;

import com.google.firebase.firestore.DocumentId;

public class Barang {
    @DocumentId
    private String id;
    
    private String nama;
    private String lokasi;
    private String status;
    private String waktu;        // kept for backward-compat
    private long timestamp;
    private String deskripsi;
    private String phone;
    private String imageBase64;
    private int imageResId = 0;

    // Default constructor required for calls to DataSnapshot.getValue(Barang.class) / Firestore
    public Barang() {
    }

    // ─── Primary constructor (new reports) ─────────────────────────────────────
    public Barang(String nama, String lokasi, String status, String waktu,
                  String deskripsi, String phone, String imageBase64) {
        this.nama = nama;
        this.lokasi = lokasi;
        this.status = status;
        this.waktu = waktu;
        this.timestamp = System.currentTimeMillis();
        this.deskripsi = deskripsi;
        this.phone = phone;
        this.imageBase64 = imageBase64;
    }

    // ─── Constructor with drawable resource (for Dummy Data) ────────────────────
    public Barang(String nama, String lokasi, String status, String waktu,
                  String deskripsi, String phone, int imageResId) {
        this.nama = nama;
        this.lokasi = lokasi;
        this.status = status;
        this.waktu = waktu;
        this.timestamp = System.currentTimeMillis();
        this.deskripsi = deskripsi;
        this.phone = phone;
        this.imageResId = imageResId;
    }

    // ─── Relative time helper ──────────────────────────────────────────────────
    public String getRelativeTime() {
        if (timestamp == 0) return waktu != null ? waktu : "Baru saja";
        long diff = System.currentTimeMillis() - timestamp;
        long minutes = diff / 60_000;
        long hours   = diff / 3_600_000;
        long days    = diff / 86_400_000;
        if (minutes < 1)   return "Baru saja";
        if (minutes < 60)  return minutes + " menit lalu";
        if (hours   < 24)  return hours   + " jam lalu";
        if (days    < 30)  return days    + " hari lalu";
        long months = days / 30;
        if (months  < 12)  return months  + " bulan lalu";
        return (months / 12) + " tahun lalu";
    }

    // ─── Getters and Setters (Required by Firestore) ────────────────────────────
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getLokasi() { return lokasi; }
    public void setLokasi(String lokasi) { this.lokasi = lokasi; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getWaktu() { return waktu; }
    public void setWaktu(String waktu) { this.waktu = waktu; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }

    public int getImageResId() { return imageResId; }
    public void setImageResId(int imageResId) { this.imageResId = imageResId; }
}