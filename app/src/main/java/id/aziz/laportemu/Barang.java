package id.aziz.laportemu;

public class Barang {
    private String nama;
    private String lokasi;
    private String status;
    private String waktu;        // kept for backward-compat with saved data
    private long timestamp;      // Unix millis — used for relative time display
    private String deskripsi;
    private String phone;
    private String imageUriString;
    private int imageResId = 0;

    // ─── Primary constructor (new reports) ─────────────────────────────────────
    public Barang(String nama, String lokasi, String status, String waktu,
                  String deskripsi, String phone, String imageUriString) {
        this.nama = nama;
        this.lokasi = lokasi;
        this.status = status;
        this.waktu = waktu;
        this.timestamp = System.currentTimeMillis(); // save real time
        this.deskripsi = deskripsi;
        this.phone = phone;
        this.imageUriString = imageUriString;
    }

    // ─── Constructor with drawable resource ────────────────────────────────────
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
        // Fallback for old data (timestamp == 0)
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

    // ─── Getters ────────────────────────────────────────────────────────────────
    public String getNama()          { return nama; }
    public String getLokasi()        { return lokasi; }
    public String getStatus()        { return status; }
    public String getWaktu()         { return waktu; }
    public long   getTimestamp()     { return timestamp; }
    public void   setTimestamp(long ts) { this.timestamp = ts; }
    public String getDeskripsi()     { return deskripsi; }
    public String getPhone()         { return phone; }
    public String getImageUriString(){ return imageUriString; }
    public int    getImageResId()    { return imageResId; }
}