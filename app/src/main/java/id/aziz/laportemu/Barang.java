package id.aziz.laportemu;

public class Barang {
    private String nama;
    private String lokasi;
    private String status;
    private String waktu;
    private String deskripsi;
    private String phone;
    private String imageUriString;
    private int imageResId = 0;

    public Barang(String nama, String lokasi, String status, String waktu, String deskripsi, String phone, String imageUriString) {
        this.nama = nama;
        this.lokasi = lokasi;
        this.status = status;
        this.waktu = waktu;
        this.deskripsi = deskripsi;
        this.phone = phone;
        this.imageUriString = imageUriString;
    }

    public Barang(String nama, String lokasi, String status, String waktu, String deskripsi, String phone, int imageResId) {
        this.nama = nama;
        this.lokasi = lokasi;
        this.status = status;
        this.waktu = waktu;
        this.deskripsi = deskripsi;
        this.phone = phone;
        this.imageResId = imageResId;
    }

    public String getNama() { return nama; }
    public String getLokasi() { return lokasi; }
    public String getStatus() { return status; }
    public String getWaktu() { return waktu; }
    public String getDeskripsi() { return deskripsi; }
    public String getPhone() { return phone; }
    public String getImageUriString() { return imageUriString; }
    public int getImageResId() { return imageResId; }
}